package crud;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Post implements Serializable {
    
    private static final long serialVersionUID = 1L; 
    private static int nextId = 1;
    private static final DateTimeFormatter formatter = 
            DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a");

    private int id;
    private String author;
    private String content;
    private String imageUrl;
    private LocalDateTime timestamp;
    private int likes;
    private int dislikes;
    private ArrayList<Comment> comments;

    public Post(String author, String content, String imageUrl) {
        this.id = nextId++;
        this.author = author;
        this.content = content;
        this.imageUrl = (imageUrl != null) ? imageUrl.trim() : "";
        this.timestamp = LocalDateTime.now();
        this.likes = 0;
        this.dislikes = 0;
        this.comments = new ArrayList<>();
    }
    
    // --- Methods for features ---
    public void addLike() { this.likes++; }
    public void addDislike() { this.dislikes++; }
    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
    
    public void deleteComment(Comment comment) {
        this.comments.remove(comment);
    }
    
    public void editComment(Comment comment, String newContent) {
        for (Comment c : comments) {
            if (c.getId() == comment.getId()) {
                c.setContent(newContent);
                break;
            }
        }
    }
    
    // --- NEW: Method to extract hashtags ---
    public Set<String> getHashtags() {
        Set<String> hashtags = new HashSet<>();
        // This regex finds words starting with #
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            hashtags.add(matcher.group()); // Adds the full hashtag (e.g., "#java")
        }
        return hashtags;
    }
    
    // --- Getters ---
    public int getId() { return id; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public int getLikes() { return likes; }
    public int getDislikes() { return dislikes; }
    public String getTimestampString() {
        return this.timestamp.format(formatter);
    }
    public ArrayList<Comment> getComments() {
        return comments;
    }
    
    // --- Setters ---
    public void setContent(String content) { this.content = content; }
    public void setImageUrl(String imageUrl) { this.imageUrl = (imageUrl != null) ? imageUrl.trim() : ""; }
    
    // --- Static method for Save/Load (unchanged) ---
    public static void updateNextId(ArrayList<Post> posts) {
        if (!posts.isEmpty()) {
            int maxId = 0;
            for (Post post : posts) {
                if (post.getId() > maxId) {
                    maxId = post.getId();
                }
            }
            nextId = maxId + 1;
        }
        Comment.updateNextId(posts);
    }
}