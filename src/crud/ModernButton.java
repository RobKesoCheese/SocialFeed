package crud;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class ModernButton extends JButton {

    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;

    public ModernButton(String text, Color normal, Color hover, Color textCol) {
        super(text);
        this.normalColor = normal;
        this.hoverColor = hover;
        this.pressedColor = hover.darker();
        this.textColor = textCol;

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(textColor);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add padding
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { setBackground(normalColor); }
            @Override
            public void mousePressed(MouseEvent e) { setBackground(pressedColor); }
            @Override
            public void mouseReleased(MouseEvent e) { setBackground(hoverColor); }
        });
        
        setBackground(normalColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(getBackground());
        // Draw rounded rectangle (20px radius)
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        
        super.paintComponent(g2);
        g2.dispose();
    }
}