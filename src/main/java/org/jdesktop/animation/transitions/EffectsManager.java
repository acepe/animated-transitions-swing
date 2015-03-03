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

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

/**
 * This class manages the cache of effects for the application. Users wishing to have specific effects occur on
 * particular components should cache those effects here. These effects will be retrieved at transition time, based on
 * which components are transitioning and which {@link TransitionType} is needed.
 *
 * @author Chet Haase
 */
public final class EffectsManager {

    /**
     * The type of transition that this effect should be used for.
     */
    public static enum TransitionType {
        /**
         * Applies to components that exist in both the start and end states of the transition.
         */
        CHANGING,
        /**
         * Applies to components that do not exist in the start state, but exist in the end state.
         */
        APPEARING,
        /**
         * Applies to components that do not exist in the end state, but exist in the start state.
         */
        DISAPPEARING
    }

    private final Map<JComponent, Effect> cachedChangingEffects = new HashMap<>();
    private final Map<JComponent, Effect> cachedAppearingEffects = new HashMap<>();
    private final Map<JComponent, Effect> cachedDisappearingEffects = new HashMap<>();

    public EffectsManager() {
    }

    /**
     * This method is used to cache a custom effect on a per-component basis for the application. Note that these custom
     * effects are application wide for the duration of the process, or until a new or null effect is set for this
     * component. Note also that custom effects are registered according to the {@link TransitionType}. So a custom
     * <code>TransitionType.CHANGING</code> effect for a given component will have no bearing on the effect used in a
     * transition where the component either appears or disappers between the transition states.
     * 
     * @param component
     *            The JComponent that this effect should be applied to
     * @param effect
     *            The custom effect desired. A null argument effectively cancels any prior custom value for this
     *            component and this TransitionType; it is equivalent to calling
     *            {@link #removeEffect(JComponent, TransitionType) removeComponent()}.
     * @param transitionType
     *            The type of transition to apply this effect on
     * @see TransitionType
     */
    public void setEffect(JComponent component, Effect effect, TransitionType transitionType) {
        if (effect == null) {
            removeEffect(component, transitionType);
            return;
        }
        switch (transitionType) {
            case CHANGING:
                cachedChangingEffects.put(component, effect);
                break;
            case APPEARING:
                cachedAppearingEffects.put(component, effect);
                break;
            case DISAPPEARING:
                cachedDisappearingEffects.put(component, effect);
                break;
            default:
                throw new InternalError("unknown TransitionType");
        }
    }

    /**
     * This method is called during the setup phase for any transition. It queries the cache for a custom effect
     * associated with a given component and <code>TransitionType</code>
     * 
     * @param component
     *            The component we are querying on behalf of
     * @param transitionType
     *            The type of transition that the effect would be used on for this component
     * @return Effect The custom effect associated with this component and <code>transitionType</code>. A null return
     *         value indicates that there is no custom effect associated with this component and transition type
     */
    public Effect getEffect(JComponent component, TransitionType transitionType) {
        switch (transitionType) {
            case CHANGING:
                return cachedChangingEffects.get(component);
            case APPEARING:
                return cachedAppearingEffects.get(component);
            case DISAPPEARING:
                return cachedDisappearingEffects.get(component);
            default:
                throw new InternalError("unknown TransitionType");
        }
    }

    /**
     * This method removes an effect for the specified component and <code>transitionType</code>, if an effect exists.
     * 
     * @param component
     *            The component associated with the effect that should be removed
     * @param transitionType
     *            The type of transition associated with the component and effect
     */
    public void removeEffect(JComponent component, TransitionType transitionType) {
        switch (transitionType) {
            case CHANGING:
                cachedChangingEffects.remove(component);
                break;
            case APPEARING:
                cachedAppearingEffects.remove(component);
                break;
            case DISAPPEARING:
                cachedDisappearingEffects.remove(component);
                break;
            default:
                throw new InternalError("unknown TransitionType");
        }
    }

    /**
     * This method clears all effects for the specified <code>transitionType</code>.
     * 
     * @param transitionType
     *            The type of transition for which all custom effects should be cleared
     */
    public void clearEffects(TransitionType transitionType) {
        switch (transitionType) {
            case CHANGING:
                cachedChangingEffects.clear();
                break;
            case APPEARING:
                cachedAppearingEffects.clear();
                break;
            case DISAPPEARING:
                cachedDisappearingEffects.clear();
                break;
            default:
                throw new InternalError("unknown TransitionType");
        }
    }

    /**
     * This method clears the manager of all custom effects currently set.
     */
    public void clearAllEffects() {
        cachedChangingEffects.clear();
        cachedAppearingEffects.clear();
        cachedDisappearingEffects.clear();
    }

}
