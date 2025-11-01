package crud;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password; // In a real app, this should be hashed!

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    // This method checks the password
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}