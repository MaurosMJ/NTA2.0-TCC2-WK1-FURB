/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.awt.*;
import javax.swing.border.AbstractBorder;

/**
 *
 * @author Mauros
 */
public class RoundedBorder extends AbstractBorder {

    private final Color color;
    private final int thickness;
    private final int arc;

    public RoundedBorder(Color color, int thickness, int arc) {
        this.color = color;
        this.thickness = thickness;
        this.arc = arc;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawRoundRect(x + thickness / 2, y + thickness / 2,
                width - thickness, height - thickness, arc, arc);
        g2d.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(thickness, thickness, thickness, thickness);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = insets.top = insets.bottom = thickness;
        return insets;
    }
}
