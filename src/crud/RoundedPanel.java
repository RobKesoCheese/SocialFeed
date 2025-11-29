package crud;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class RoundedPanel extends JPanel {
    private Color backgroundColor;
    private int cornerRadius;

    public RoundedPanel(LayoutManager layout, int radius, Color bgColor) {
        super(layout);
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, cornerRadius, cornerRadius);
        
        // Optional: Very subtle border
        g2.setColor(new Color(0, 0, 0, 20)); // 20 alpha = very transparent black
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, cornerRadius, cornerRadius);
    }
    
    // Allow changing color dynamically (for dark mode)
    public void setPanelBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }
}