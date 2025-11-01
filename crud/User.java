package crud;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial") // Fix for yellow warning
public class User implements Serializable {
    private String username;
    private String password;
    private String avatarUrl;
    private ArrayList<String> following; 

    public User(String username, String password, String avatarUrl) {
        this.username = username;
        this.password = password;
        this.avatarUrl = (avatarUrl != null) ? avatarUrl.trim() : "";
        this.following = new ArrayList<>();
    }

    // --- Getters ---
    public String getUsername() {
        return username;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
    
    // --- NEW: Setters for Edit Profile ---
    public void setPassword(String newPassword) {
        this.password = newPassword;
    }
    
    public void setAvatarUrl(String newUrl) {
        this.avatarUrl = (newUrl != null) ? newUrl.trim() : "";
    }

    // --- Following Methods (safe getters) ---
    public ArrayList<String> getFollowing() {
        if (this.following == null) {
            this.following = new ArrayList<>();
        }
        return this.following;
    }
    
    public boolean isFollowing(String username) {
        return getFollowing().contains(username);
    }
    
    public void followUser(String username) {
        if (!isFollowing(username) && !this.username.equals(username)) {
            getFollowing().add(username);
        }
    }
    
    public void unfollowUser(String username) {
        getFollowing().remove(username);
    }
}