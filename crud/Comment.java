package crud;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial") // Fix for yellow warning
public class Comment implements Serializable {
    // ... rest of your existing Comment.java code ...
    // (No other changes needed here)
// ...
    private static long nextId = 1;
    private long id;
    
    private String author;
    private String content;

    public Comment(String author, String content) {
        this.id = nextId++; // Assign a unique ID
        
        if (author == null || author.trim().isEmpty()) {
            this.author = "Anonymous";
        } else {
            this.author = author;
        }
        this.content = content;
    }

    // --- Getters ---
    public long getId() { return id; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    
    // --- NEW: Setter for editing ---
    public void setContent(String newContent) {
        this.content = newContent;
    }

    // --- NEW: Static method for Save/Load ---
    public static void updateNextId(ArrayList<Post> posts) {
        long maxId = 0;
        if (posts != null) {
            for (Post post : posts) {
                for (Comment comment : post.getComments()) {
                    if (comment.getId() > maxId) {
                        maxId = comment.getId();
                    }
                }
            }
        }
        nextId = maxId + 1;
    }
}