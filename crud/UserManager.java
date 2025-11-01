package crud;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial") // Fix for yellow warning
public class UserManager implements Serializable {
    // ... rest of your existing UserManager.java code ...
    // (No other changes needed here)
// ...
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

    public User register(String username, String password, String avatarUrl) {
        if (username.isEmpty() || password.isEmpty()) {
            return null; // Don't allow empty fields
        }
        
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return null; // Username taken
            }
        }
        
        User newUser = new User(username, password, avatarUrl);
        users.add(newUser);
        saveUsers();
        return newUser;
    }
    
    public User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
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

    public void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
            System.out.println("Users saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}