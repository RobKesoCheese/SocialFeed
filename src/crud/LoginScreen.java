package crud;

import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.util.Properties;

@SuppressWarnings("serial")
public class LoginScreen extends JDialog {

    private UserManager userManager;
    private User loggedInUser = null;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField avatarUrlField;
    
    // ---  Remember Me Checkbox ---
    private JCheckBox rememberMeCheck;

    public LoginScreen(Frame parent, UserManager userManager) {
        super(parent, "Login or Register", true);
        this.userManager = userManager;

        setLayout(new BorderLayout(10, 10));
        setSize(350, 240); // Made slightly taller
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // --- Components ---
        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        fieldsPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        fieldsPanel.add(usernameField);
        
        fieldsPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        fieldsPanel.add(passwordField);
        
        fieldsPanel.add(new JLabel("Avatar URL (optional):"));
        avatarUrlField = new JTextField();
        fieldsPanel.add(avatarUrlField);
        
        // ---  Center Panel for Fields + Checkbox ---
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(fieldsPanel, BorderLayout.NORTH);
        
        rememberMeCheck = new JCheckBox("Remember Me");
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkPanel.add(rememberMeCheck);
        centerContainer.add(checkPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);

        // --- Layout ---
        add(centerContainer, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        // --- Actions ---
        loginButton.addActionListener(e -> onLogin());
        registerButton.addActionListener(e -> onRegister());
        passwordField.addActionListener(e -> onLogin());
    }

    private void onLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        User user = userManager.login(username, password);
        
        if (user != null) {
            this.loggedInUser = user;
            // ---  Handle Remember Me ---
            if (rememberMeCheck.isSelected()) {
                saveCredentials(username, password);
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String avatarUrl = avatarUrlField.getText();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userManager.register(username, password, avatarUrl);
        
        if (user != null) {
            this.loggedInUser = user;
            // ---  Handle Remember Me ---
            if (rememberMeCheck.isSelected()) {
                saveCredentials(username, password);
            }
            JOptionPane.showMessageDialog(this, "Registration successful! You are now logged in.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Username is already taken.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ---  Helper to save credentials file ---
    private void saveCredentials(String user, String pass) {
        try (FileOutputStream out = new FileOutputStream("login.conf")) {
            Properties props = new Properties();
            props.setProperty("username", user);
            props.setProperty("password", pass);
            props.store(out, "Auto Login Config");
        } catch (Exception e) {
            System.err.println("Could not save remember me file.");
        }
    }

    public User showLoginDialog() {
        setVisible(true);
        return this.loggedInUser;
    }
}