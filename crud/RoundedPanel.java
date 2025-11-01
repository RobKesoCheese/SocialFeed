package crud;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial") // Fix for yellow warning
public class RoundedPanel extends JPanel {
    // ... rest of your existing RoundedPanel.java code ...
    // (No other changes needed here)
// ...
    private Color backgroundColor;
    private int cornerRadius = 15;

    public RoundedPanel(LayoutManager layout, int radius, Color bgColor) {
        super(layout);
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(backgroundColor);
        graphics.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.drawRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
    }
}