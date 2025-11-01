package crud;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class UserManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<User> users;
    private final String USER_FILE = "users.dat";

    public UserManager() {
        this.users = new ArrayList<>();
        loadUsers();
    }

    // --- Public Methods ---

    public User login(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username) && user.checkPassword(password)) {
                return user; // Login success!
            }
        }
        return null; // Login failed
    }

    public User register(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return null; // Don't allow empty fields
        }
        
        // Check if username is already taken
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return null; // Username taken
            }
        }
        
        // Create new user, add to list, save, and return it
        User newUser = new User(username, password);
        users.add(newUser);
        saveUsers();
        return newUser;
    }

    // --- Save & Load ---
    
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_FILE))) {
            users = (ArrayList<User>) ois.readObject();
            System.out.println("Users loaded successfully!");
        } catch (java.io.FileNotFoundException e) {
            System.out.println("No user file found. Starting with new user list.");
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
            System.out.println("Users saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}