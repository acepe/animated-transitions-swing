//@formatter:off
/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
//@formatter:on

package org.jdesktop.animation.transitions;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;

/**
 * This class is used to run animated transitions in an application.
 * <p>
 * <code>ScreenTransition</code> is given a container in a Swing application. When the application wishes to transition
 * from one state of the application to another, the {@link #start()} method is called, which calls back into the
 * application to set up the next state of the GUI. Then <code>ScreenTransition</code> runs an animation from the
 * previous state of the application to the new state.
 *
 * @author Chet Haase
 */
public class ScreenTransition {
    private static final Integer DEFAULT_LAYER_ID = 301;

    /**
     * Sets the passed effects manager as the default used for the construction of transitions. If no effects manager is
     * explicitly set, a default is used.
     * 
     * Passing {@code null} to this method resets the effect manager to the default.
     */
    public static void setDefaultEffectsManager(EffectsManager effectsManager) {
        Builder.setDefaultEffectsManager(effectsManager);
    }

    public static EffectsManager getDefaultEffectsManager() {
        return Builder.getDefaultEffectsManager();
    }

    /*
     * Implementation detail: The key to making ScreenTransition work correctly is having two different views or layers
     * of ScreenTransition under the covers. One layer is the "containerLayer", which is where the actual child
     * components of ScreenTransition are placed. The other (more hidden) layer is the "animationLayer", which exists
     * solely for displaying the animations which take place during any transition. The reason we cannot animate the
     * transitions in the same container where the actual components live (at least not trivially) is that we need to
     * layout the container for both states of a transition prior to actually running the animation. Moving the
     * components around during these layout phases cannot happen onscreen (the net effect would be that the user would
     * see the end result before the transition), so we do the layout on an invisible container instead. Then the
     * animation happens to transition between the two states, in his separate animationLayer. Further detail: the
     * "animationLayer" is added to be the layered pane of the application frame. The layered pane already has the
     * functionality we need (specifically, it overlays the area that we need to render during the transition); the only
     * trick is that we must position the rendering correctly since the layered pane typically covers the entire
     * application frame, but in this case the transition container may only occupy a portion of the frame.
     */

    /**
     * Handles the structure and rendering of the actual animation during the transitions.
     */
    private AnimationManager animationManager;

    /**
     * The component where the transition animation occurs. This component (which is set to be the layered pane) is
     * visible during the transition, but is otherwise invisible.
     */
    private AnimationLayer animationLayer;

    /**
     * The component supplied at contruction time that holds the actual components added to ScreenTransition by the
     * application. Keeping this container separate from ScreenTransition allows us to render the AnimationLayer during
     * the transitions and separate the animation sequence from the actual container of the components.
     */
    private JComponent containerLayer;

    /**
     * Image used to store the current state of the transition animation. This image will be rendered to during
     * timingEvent() and then copied into the layered pane during the repaint cycle.
     */
    private BufferedImage transitionImage;

    /**
     * The user-defined code which ScreenTransition will call to setup the next state of the GUI when a transition is
     * started.
     */
    private TransitionTarget transitionTarget;

    /**
     * Animation engine for the transition.
     */
    private Animator animator = null;

    public static class Builder {
        private static AtomicReference<EffectsManager> globalEffectsManager = new AtomicReference<>(new EffectsManager());

        /**
         * Sets the passed effects manager as the default used for the construction of transitions. If no effects
         * manager is explicitly set, a default is used.
         *
         * Passing {@code null} to this method resets the effect manager to the default.
         */
        static void setDefaultEffectsManager(EffectsManager effectsManager) {
            if (effectsManager == null) {
                globalEffectsManager.set(new EffectsManager());
            } else {
                globalEffectsManager.set(effectsManager);
            }
        }

        static EffectsManager getDefaultEffectsManager() {
            return globalEffectsManager.get();
        }

        private final JComponent transitionComponent;
        private final TransitionTarget transitionTarget;

        private Animator animator = null;
        private EffectsManager effectsManager;

        public Builder(JComponent transitionComponent, TransitionTarget transitionTarget) {
            this.transitionComponent = transitionComponent;
            this.transitionTarget = transitionTarget;
        }

        /**
         * The Animator used to drive this ScreenTransition will be created internally using the duration.
         *
         * @param durationInMillis
         *            the length of time in milliseconds that the transition will last
         */
        public Builder setDuration(int durationInMillis) {
            return setAnimator(new Animator.Builder().setDuration(durationInMillis, TimeUnit.MILLISECONDS).build());
        }

        /**
         * Sets the Animator that will be used to drive the ScreenTransition. Transition will start if either
         * {@link #start} is called or {@link Animator#start} is called.
         *
         * @throws IllegalStateException
         *             if animator is already running
         * @throws IllegalArgumentException
         *             animator must be non-null
         * @param animator
         *            the animator that defines the characteristics of the transition animation, such as its duration
         * @see Animator#isRunning()
         * @see Animator#start()
         */
        public Builder setAnimator(Animator animator) {
            if (animator == null) {
                throw new IllegalArgumentException("Animator must be non-null");
            }
            if (animator.isRunning()) {
                throw new IllegalStateException("Cannot perform this operation " + "while animator is running");
            }
            this.animator = animator;
            return this;
        }

        /**
         * Sets the effect manager to be used by this transition. The default effect manager is the global default.
         * 
         * @param effectsManager
         *            the effect manager used for this transition
         */
        public Builder setEffectsManager(EffectsManager effectsManager) {
            this.effectsManager = effectsManager;
            return this;
        }

        /**
         * Constructs a screen transition with the settings defined by this builder.
         *
         * @throws IllegalArgumentException
         *             if no animator or duration was provided
         * 
         * @return a screen transition.
         */
        public ScreenTransition build() {
            if (animator == null)
                throw new IllegalArgumentException("Either an animator or a duration must be provided.");

            EffectsManager customEffectManager = effectsManager == null ? globalEffectsManager.get() : effectsManager;
            return new ScreenTransition(transitionComponent, transitionTarget, customEffectManager, animator);
        }
    }

    private ScreenTransition(JComponent containerLayer,
            TransitionTarget transitionTarget,
            EffectsManager effectsManager,
            Animator animator) {
        this.containerLayer = containerLayer;
        this.transitionTarget = transitionTarget;

        animationManager = new AnimationManager(effectsManager, containerLayer);
        animationLayer = new AnimationLayer(this);
        animationLayer.setVisible(false);
        containerLayer.addComponentListener(new ContainerSizeListener());
        createTransitionImages();
        setAnimator(animator);
    }

    /**
     * Create the transition images here and in AnimationManager if necessary.
     */
    private void createTransitionImages() {
        int cw = containerLayer.getWidth();
        int ch = containerLayer.getHeight();
        if ((cw > 0 && ch > 0)
            && (transitionImage == null || transitionImage.getWidth() != cw || transitionImage.getHeight() != ch)) {
            // Recreate transition image and background for new dimensions
            transitionImage = (BufferedImage) containerLayer.createImage(cw, ch);
            animationManager.recreateImage();
        }
    }

    /**
     * Listen for changes to the transition container size and recreate transition images as necessary. Doing this on
     * component size change events prevents having to do it as needed at the start of the next transition, which can
     * cause a unwanted delay in that animation.
     */
    private class ContainerSizeListener extends ComponentAdapter {
        public void componentResized(ComponentEvent ce) {
            createTransitionImages();
        }
    }

    /**
     * Returns <code>Animator</code> object that drives this ScreenTransition.
     * 
     * @return the Animator that drives this ScreenTransition
     */
    public Animator getAnimator() {
        return animator;
    }

    /**
     * Sets animator that drives this ScreenTransition. Animator cannot be null. Animator also cannot be running when
     * this method is called (because important setup information for ScreenTransition happens at Animator start time).
     * Transition will start if either {@link #start} is called or {@link Animator#start} is called.
     * 
     * @param animator
     *            non-null Animator object that will drive this ScreenTransition. Animator cannot be running when this
     *            is called.
     * @throws IllegalStateException
     *             if animator is already running
     * @throws IllegalArgumentException
     *             animator must be non-null
     * @see Animator#isRunning()
     */
    private void setAnimator(Animator animator) {
        this.animator = animator;
        animator.addTarget(transitionTimingTarget);
    }

    /**
     * Returns image used during timingEvent rendering. This is called by AnimationLayer to get the contents for the
     * layered pane
     */
    Image getTransitionImage() {
        return transitionImage;
    }

    /**
     * Begin the transition from the current application state to the next one. This method will start the transition's
     * {@link Animator} which will cause the transition to begin. This will result in a call into the
     * {@link TransitionTarget} specified in the <code>ScreenTransition</code> constructor:
     * <code>setupNextScreen()</code> will be called to allow the application to set up the state of the next screen.
     * Then the transition animation will begin.
     */
    public void start() {
        if (animator.isRunning()) {
            animator.restart();
            return;
        }
        animator.start();
    }

    /**
     * This class receives the timing events from the animator and performs the appropriate operations on the
     * ScreenTransition object. This could be done by having ScreenTransition implement TimingTarget methods directly,
     * but there is no need to expose those methods as public API (which would be necessary since TimingTarget needs the
     * methods to be public). Having this as an internal private class hides the methods from the ScreenTransition API
     * while allowing the same functionality.
     */
    private TimingTarget transitionTimingTarget = new TimingTargetAdapter() {

        /**
         * This method is called as a result of a call to {@link ScreenTransition#start()}. The method sets up
         * appropriate state for the transition, creating any necessary background images, capturing the current state
         * of the components in the transition container, calling the application's setupNextScreen() method, and
         * capturing the new state of the components. It then determines the effects to use during the transition, based
         * on the changes taking place in the components between the two screens and initializes those effects
         * appropriately.
         */
        @Override
        public void begin(Animator source) {
            // Make sure that our background images exist and is the right size
            createTransitionImages();

            // Capture the current state of the application into the
            // AnimationManager; this sets up the state we are transitioning from
            animationManager.setupStart();

            // This records data in animationLayer used to copy the transition
            // contents correctly into the layered pane
            animationLayer.setupBackground(containerLayer);

            // Make the animationLayer visible and the contentPane invisible. This
            // frees us to validate the application state for the next screen while
            // keeping that new state invisible from the user; the animationLayer
            // will only display contents appropriate to the transition (the previous
            // state before the transition begins, the transitioning state during
            // the transition).

            containerLayer.getRootPane().getLayeredPane().add(animationLayer, DEFAULT_LAYER_ID);
            animationLayer.setVisible(true);

            // Now that the contentPane is invisible to the user, have the
            // application setup the next screen. This will define the end state
            // of the application for this transition.
            transitionTarget.setupNextScreen();

            // Validating the container layer component ensures correct layout
            // for the next screen of the application
            containerLayer.validate();

            // Iterate through the visible components in the next application
            // screen and add those end states to the AnimationManager
            animationManager.setupEnd();

            // Init the AnimationManager; this sets up default or custom effects
            // for each of the components involved in the transition
            animationManager.init(animator);

            // Now that we have recorded (and rendered) the initial state, make
            // the container invisible for the duration of the transition
            containerLayer.setVisible(false);

            // workaround: need layered pane to reflect initial contents when we
            // exit this function to avoid flash of blank container
            timingEvent(source, 0);
        }

        /**
         * Implementation of the <code>TimingTarget</code> interface. This method is called repeatedly during the
         * transition animation. We force a repaint, which causes the current transition state to be rendered.
         */
        @Override
        public void timingEvent(Animator source, double elapsedFraction) {
            Graphics2D gImg = (Graphics2D) transitionImage.getGraphics();

            // Render this frame of the animation
            animationManager.paint(gImg);

            gImg.dispose();

            // Force transitionImage to be copied to the layered pane
            animationLayer.repaint();
        }

        /**
         * Override of <code>TimingTarget.end()</code>; switch the visibility of the containerLayer and animationLayer
         * and force repaint.
         */
        @Override
        public void end(Animator source) {
            containerLayer.getRootPane().getLayeredPane().remove(animationLayer);
            containerLayer.setVisible(true);
            containerLayer.repaint();
            // Reset the AnimationManager (this releases all previous transition
            // data structures)
            animationManager.reset(animator);
        }
    };
}
