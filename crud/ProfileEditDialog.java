package crud;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class ProfileEditDialog extends JDialog {

    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField avatarUrlField;
    
    // --- NEW: This will tell the main GUI what action to take ---
    public enum DialogResult { CANCEL, SAVE, SWITCH, LOGOUT }
    private DialogResult dialogResult;

    public ProfileEditDialog(Dialog parent, User currentUser, UserManager userManager) {
        super(parent, "Edit Your Profile", true);
        setSize(450, 200); // Made slightly wider
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // --- 1. Fields Panel (Unchanged) ---
        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        fieldsPanel.add(new JLabel("New Password:"));
        passwordField = new JPasswordField();
        fieldsPanel.add(passwordField);
        
        fieldsPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        fieldsPanel.add(confirmPasswordField);
        
        fieldsPanel.add(new JLabel("New Avatar URL:"));
        avatarUrlField = new JTextField(currentUser.getAvatarUrl());
        fieldsPanel.add(avatarUrlField);
        
        add(fieldsPanel, BorderLayout.CENTER);

        // --- 2. Buttons Panel (UPDATED) ---
        // This panel now uses a BorderLayout to separate left/right buttons
        JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));

        // --- NEW: Left side buttons (Logout, Switch) ---
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton logoutButton = new JButton("Logout");
        JButton switchAccountButton = new JButton("Switch Account");
        
        // Style "dangerous" buttons to be red
        logoutButton.setForeground(Color.RED);
        
        leftButtons.add(logoutButton);
        leftButtons.add(switchAccountButton);
        buttonsPanel.add(leftButtons, BorderLayout.WEST);

        // --- Right side buttons (Save, Cancel) ---
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        rightButtons.add(saveButton);
        rightButtons.add(cancelButton);
        buttonsPanel.add(rightButtons, BorderLayout.EAST);
        
        add(buttonsPanel, BorderLayout.SOUTH);

        // --- 3. Actions ---
        
        // Set the default result to CANCEL
        this.dialogResult = DialogResult.CANCEL; 
        
        cancelButton.addActionListener(e -> {
            this.dialogResult = DialogResult.CANCEL;
            dispose(); // Close dialog
        });
        
        logoutButton.addActionListener(e -> {
            this.dialogResult = DialogResult.LOGOUT;
            dispose();
        });
        
        switchAccountButton.addActionListener(e -> {
            this.dialogResult = DialogResult.SWITCH;
            dispose();
        });
        
        saveButton.addActionListener(e -> {
            String newPassword = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String newAvatarUrl = avatarUrlField.getText();

            if (!newPassword.isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                    return; 
                }
                currentUser.setPassword(newPassword);
            }
            
            currentUser.setAvatarUrl(newAvatarUrl);
            userManager.saveUsers();
            
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            
            this.dialogResult = DialogResult.SAVE;
            dispose();
        });
    }
    
    // --- NEW: This method is called by SocialFeedGUI ---
    public DialogResult showDialog() {
        setVisible(true); // This blocks until the dialog is closed
        return this.dialogResult; // Return the user's choice
    }
}