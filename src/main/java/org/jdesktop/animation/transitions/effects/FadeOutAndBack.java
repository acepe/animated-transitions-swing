package org.jdesktop.animation.transitions.effects;

import org.jdesktop.animation.transitions.ComponentState;
import org.jdesktop.animation.transitions.Effect;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.PropertySetter;
import org.jdesktop.core.animation.timing.TimingTarget;

/**
 * Simple subclass of Fade effect that will fade a component from opaque to the user supplied value and back to opaque.
 *
 * @author Andre Ackermann
 */
public class FadeOutAndBack extends Fade {

    private final Float targetOpacity;

    // animation target used to fade our during the transition
    private TimingTarget ps;

    /**
     * Creates a new instance of FadeOut with the given start state.
     *
     * @param targetOpacity
     *            the opacity we are fading to in the first half of the transition and then from in the second half.
     */
    public FadeOutAndBack(Float targetOpacity) {
        this.targetOpacity = targetOpacity;
    }

    /**
     * Creates a new instance of FadeOut with the given start state.
     *
     * @param start
     *            The <code>ComponentState</code> at the beginning of the transition; this is what we are fading from.
     * @param targetOpacity
     *            the opacity we are fading to in the first half of the transition and then from in the second half.
     */
    public FadeOutAndBack(ComponentState start, Float targetOpacity) {
        this.targetOpacity = targetOpacity;
        setStart(start);
    }

    /**
     * Initializes the effect, adding an animation target that will fade the component of the effect our from opaque to
     * target opacity and back during the course of the transition.
     */
    @Override
    public void init(Animator animator, Effect parentEffect) {
        ps = PropertySetter.getTarget(this, "opacity", 1f, targetOpacity, targetOpacity, 1f);
        animator.addTarget(ps);
        setOpacity(1f);
        super.init(animator, null);
    }

    /**
     * Removes the fading target from the animation to avoid leaking resources
     */
    @Override
    public void cleanup(Animator animator) {
        animator.removeTarget(ps);
    }

}
