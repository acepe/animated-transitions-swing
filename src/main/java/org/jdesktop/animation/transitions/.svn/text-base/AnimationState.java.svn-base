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

package org.jdesktop.animation.transitions;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.transitions.effects.CompositeEffect;
import org.jdesktop.animation.transitions.effects.FadeIn;
import org.jdesktop.animation.transitions.effects.FadeOut;
import org.jdesktop.animation.transitions.effects.Move;
import org.jdesktop.animation.transitions.effects.Scale;
import org.jdesktop.animation.transitions.effects.Unchanging;

/**
 * This class holds the start and/or end states for a <code>JComponent</code>.  
 * It also determines (at <code>init()</code> time) the <code>Effect</code> 
 * to use during the transition and calls the appropriate Effect during the
 * {@link #paint(Graphics2D) paint()} method to cause the appropriate rendering
 * of the component during the transition.
 *
 * @author Chet Haase
 */
class AnimationState {

    /**
     * The component for this AnimationState. There is one component per
     * state, with either a start, an end, or both states.
     */
    private JComponent component;
    /**
     * Start/end states for this AnimationState. These may be set to a non-null
     * value or not, depending on whether the component exists in the
     * respective start/end screen(s) of the transition.
     */
    private ComponentState start;
    private ComponentState end;
    /**
     * Effect used to transition between the start and end states for this
     * AnimationState.  This effect is set during the init() method just
     * prior to running the transition.
     */
    private Effect effect;

    /**
     * Default constructor. This object is typically constructed with a
     * JComponent or a ComponentState object, not through the default
     * constructor. This constructor is currently used
     * simply to force some eager initialization of the system when
     * ScreenTransition is first instantiated.
     */
    AnimationState() {
    }

    /**
     * Creates the AnimationState with the given start/end ComponentState
     */
    AnimationState(ComponentState state, boolean isStart) {
        this.component = state.getComponent();
        if (isStart) {
            start = state;
        } else {
            end = state;
        }
    }

    /**
     * Constructs a new AnimationState with either the start
     * or end state for the component.
     */
    AnimationState(JComponent component, boolean isStart) {
        this.component = component;
        ComponentState compState = new ComponentState(component);
        if (isStart) {
            start = compState;
        } else {
            end = compState;
        }
    }

    void setStart(ComponentState compState) {
        start = compState;
    }

    void setEnd(ComponentState compState) {
        end = compState;
    }

    ComponentState getStart() {
        return start;
    }

    ComponentState getEnd() {
        return end;
    }

    Component getComponent() {
        return component;
    }

    /**
     * Called just prior to running the transition.  This method examines the
     * start and end states as well as the Effect repository to
     * determine the appropriate Effect to use during the transition for
     * this AnimationState.  If there is an existing custom effect defined
     * for the component for this type of transition, that effect will be used,
     * Otherwise, the system will use the appropriate default effect (fading
     * in, fading out, or moving/resizing).
     */
    void init(Animator animator) {
        if (start == null) {
            // component is appearing during transition; search for existing
            // custom effects for this transition type
            effect = EffectsManager.getEffect(component, EffectsManager.TransitionType.APPEARING);
            if (effect == null) {
                effect = new FadeIn(end);
            } else {
                effect.setEnd(end);
            }
        } else if (end == null) {
            // component is disappearing during transition; search for existing
            // custom effects for this transition type
            effect = EffectsManager.getEffect(component, EffectsManager.TransitionType.DISAPPEARING);
            if (effect == null) {
                effect = new FadeOut(start);
            } else {
                effect.setStart(start);
            }
        } else {
            // component is in both screens; search for existing
            // custom effects for this transition type
            effect = EffectsManager.getEffect(component, 
                    EffectsManager.TransitionType.CHANGING);
            if (effect == null) {
                // No custom effect exists; use move/scale combinations
                // as appropriate
                boolean move = false;
                boolean scale = false;
                if (start.getX() != end.getX() || start.getY() != end.getY()) {
                    // position changes; use Move effect
                    move = true;
                }
                if ((start.getWidth() != end.getWidth()) || 
                        (start.getHeight() != end.getHeight())) {
                    // size changes; use Scale effect
                    scale = true;
                }
                if (move) {
                    if (scale) {
                        // move/scale composite effect needed
                        effect = new Move(start, end);
                        Effect scaleEffect = new Scale(start, end);
                        effect = new CompositeEffect(effect);
                        ((CompositeEffect) effect).addEffect(scaleEffect);
                    } else {
                        // just move
                        effect = new Move(start, end);
                    }
                } else {
                    if (scale) {
                        // just scale
                        effect = new Scale(start, end);
                    } else {
                        // Noop
                        effect = new Unchanging(start, end);
                    }
                }
            } else {
                // Custom effect; set it up for this transition
                effect.setStart(start);
                effect.setEnd(end);
            }
        }
        // initialize the effect that we are about to run in the transition
        effect.init(animator, null);
    }

    /**
     * Clean up any artifacts created during the transition. This could
     * include, for example, PropertySetter objects (or other TimingTargets)
     * added to the animator during the init() phase.
     */
    void cleanup(Animator animator) {
        effect.cleanup(animator);
    }

    /**
     * Render this AnimationState into the given Graphics object, by
     * asking the Effect to render itself.
     */
    void paint(Graphics g) {
        if (effect != null) {
            // Create/use temporary Graphics object to avoid leaking 
            // state between one AnimationState and the next during
            // the transition
            Graphics2D g2d = (Graphics2D) g.create();
            effect.render(g2d);
            g2d.dispose();
        }
    }
}