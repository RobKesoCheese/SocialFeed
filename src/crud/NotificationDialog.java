package crud;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class NotificationDialog extends JDialog {

    public NotificationDialog(JDialog parent, ArrayList<Notification> notifs) {
        super(parent, "Notifications", true);
        setSize(400, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        
        if (notifs.isEmpty()) {
            JLabel empty = new JLabel("No notifications yet.");
            empty.setBorder(new EmptyBorder(20, 20, 20, 20));
            listPanel.add(empty);
        } else {
            for (Notification n : notifs) {
                JPanel item = new JPanel(new BorderLayout());
                item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    new EmptyBorder(10, 10, 10, 10)
                ));
                item.setBackground(Color.WHITE);
                
                JLabel label = new JLabel(n.toString());
                label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                
                item.add(label, BorderLayout.CENTER);
                listPanel.add(item);
            }
        }
        
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        add(closeBtn, BorderLayout.SOUTH);
    }
}