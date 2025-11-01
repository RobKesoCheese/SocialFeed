package crud;

import javax.swing.*;
import java.awt.*;

/**
 * A custom JPanel that paints itself with rounded corners.
 */
public class RoundedPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private Color backgroundColor;
    private int cornerRadius = 15;

    public RoundedPanel(LayoutManager layout, int radius, Color bgColor) {
        super(layout);
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        setOpaque(false); // We will paint our own background
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draws the rounded panel with borders.
        graphics.setColor(backgroundColor);
        graphics.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
        graphics.setColor(Color.LIGHT_GRAY); // Border color
        graphics.drawRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
    }
}