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

package org.jdesktop.animation.transitions.effects;

import org.jdesktop.animation.transitions.ComponentState;
import org.jdesktop.animation.transitions.Effect;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.PropertySetter;
import org.jdesktop.core.animation.timing.TimingTarget;

/**
 * Simple subclass of Fade effect that will fade a component from opaque to transparent.
 *
 * @author Chet Haase
 */
public class FadeOut extends Fade {

    // animation target used to fade our during the transition
    private TimingTarget ps;

    /**
     * Initializes the effect, adding an animation target that will fade the component of the effect our from opaque to
     * transparent during the course of the transition.
     */
    @Override
    public void init(Animator animator, Effect parentEffect) {
        ps = PropertySetter.getTarget(this, "opacity", 1f, 0f);
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

    public FadeOut() {
    }

    /**
     * Creates a new instance of FadeOut with the given start state.
     *
     * @param start
     *            The <code>ComponentState</code> at the beginning of the transition; this is what we are fading from.
     */
    public FadeOut(ComponentState start) {
        setStart(start);
    }
}
