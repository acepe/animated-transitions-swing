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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

/**
 * This is the component where the transition animation is displayed. During a transition, this layer becomes visible as
 * the GlassPane of the application window that contains the transition container. Repaints of this component happen
 * during the transition, which become paintComponent() events here. The paintComponent method simply copies the current
 * transition image (into which the current frame of the transition animation was rendered) to its component Graphics
 * and Swing copies it onto window.
 *
 * @author Chet Haase
 */
class AnimationLayer extends JComponent {

    // We call into ScreenTransition to get the current transition image
    // which holds each frame's rendering during the transition
    private final ScreenTransition screenTransition;

    private Point componentLocationInLayeredPane;
    private Rectangle visibleRectInLayeredPane;

    /**
     * Construct the AnimationLayer with a reference to the ScreenTransition object, which will be used later at
     * paintComponent() time
     */
    public AnimationLayer(ScreenTransition screenTransition) {
        this.screenTransition = screenTransition;
        setOpaque(false);
    }

    /**
     * Called from ScreenTransition to set up the correct location to copy the animation to in the layered pane
     */
    public void setupBackground(JComponent targetComponent) {
        JLayeredPane layeredPane = targetComponent.getRootPane().getLayeredPane();
        setBounds(layeredPane.getBounds());

        Rectangle visibleRect = targetComponent.getVisibleRect();
        visibleRectInLayeredPane = SwingUtilities.convertRectangle(targetComponent, visibleRect, layeredPane);
        componentLocationInLayeredPane = convertLocation(targetComponent, layeredPane);
    }

    private Point convertLocation(JComponent component, JComponent target) {
        // Walk the parent hierarchy up to target
        // Calculate the relative XY location of the original component as we go
        int x = 0, y = 0;
        JComponent topmost = component;
        JComponent prevTopmost = component;
        while (topmost != target && topmost.getParent() != null && topmost.getParent() instanceof JComponent) {
            topmost = (JComponent) topmost.getParent();
            x += prevTopmost.getX();
            y += prevTopmost.getY();
            prevTopmost = topmost;
        }
        return new Point(x, y);
    }

    /**
     * Called during the Swing repaint process for this component. This simply copies the transitionImage from
     * ScreenTransition into the appropriate location in the layered pane.
     */
    @Override
    public void paintComponent(Graphics g) {
        g.clipRect(visibleRectInLayeredPane.x,
                   visibleRectInLayeredPane.y,
                   visibleRectInLayeredPane.width,
                   visibleRectInLayeredPane.height);

        Image transitionImage = screenTransition.getTransitionImage();
        g.translate(componentLocationInLayeredPane.x, componentLocationInLayeredPane.y);
        g.drawImage(transitionImage, 0, 0, null);
    }
}
