package crud;

import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserManager userManager;
    private User loggedInUser = null; // This will store the user if login is successful

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginScreen(Frame parent, UserManager userManager) {
        super(parent, "Login or Register", true); // 'true' makes it a modal dialog
        this.userManager = userManager;

        setLayout(new BorderLayout(10, 10));
        setSize(300, 150);
        setLocationRelativeTo(parent); // Center on the parent window
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Just close this dialog

        // --- Components ---
        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        fieldsPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        fieldsPanel.add(usernameField);
        
        fieldsPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        fieldsPanel.add(passwordField);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);

        // --- Layout ---
        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        // --- Actions ---
        loginButton.addActionListener(e -> onLogin());
        registerButton.addActionListener(e -> onRegister());
        
        // Also allow login with Enter key
        passwordField.addActionListener(e -> onLogin());
    }

    private void onLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        User user = userManager.login(username, password);
        
        if (user != null) {
            this.loggedInUser = user;
            dispose(); // Close the dialog
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userManager.register(username, password);
        
        if (user != null) {
            this.loggedInUser = user;
            JOptionPane.showMessageDialog(this, "Registration successful! You are now logged in.");
            dispose(); // Close the dialog
        } else {
            JOptionPane.showMessageDialog(this, "Username is already taken.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // This is the public method the main app will call
    public User showLoginDialog() {
        setVisible(true); // This call will "block" until the dialog is closed
        return this.loggedInUser; // Return the user (or null)
    }
}