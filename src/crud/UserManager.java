package crud;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class UserManager implements Serializable {
    
    private ArrayList<User> users;
    private transient DatabaseManager dbManager; // 'transient' means don't save this object

    public UserManager(ArrayList<User> users, DatabaseManager dbManager) {
        this.users = users;
        this.dbManager = dbManager;
    }

    public User login(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username) && user.checkPassword(password)) {
                return user;
            }
        }
        return null;
    }

    public User register(String username, String password, String avatarUrl) {
        if (username.isEmpty() || password.isEmpty()) return null;
        
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) return null;
        }
        
        User newUser = new User(username, password, avatarUrl);
        users.add(newUser);
        dbManager.saveUser(newUser); // Save to DB immediately
        return newUser;
    }
    
    public User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) return user;
        }
        return null;
    }

    // Called when users change (follow/unfollow/edit profile)
    public void saveUsers() {
        for (User user : users) {
            dbManager.saveUser(user);
        }
    }
}