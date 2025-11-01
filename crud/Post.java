package crud;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class Post implements Serializable {
    
    private static int nextId = 1;
    private static final DateTimeFormatter formatter = 
            DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a");

    private int id;
    private String author;
    private String authorAvatarUrl;
    private String content;
    private String imageUrl;
    private LocalDateTime timestamp;
    private ArrayList<Comment> comments;
    
    private HashSet<String> usersWhoLiked;
    private HashSet<String> usersWhoDisliked;
    
    private int repostCount = 0;
    private String originalPostAuthor = null;

    public Post(String author, String authorAvatarUrl, String content, String imageUrl) {
        this.id = nextId++;
        this.author = author;
        this.authorAvatarUrl = (authorAvatarUrl != null) ? authorAvatarUrl.trim() : "";
        this.content = content;
        this.imageUrl = (imageUrl != null) ? imageUrl.trim() : "";
        this.timestamp = LocalDateTime.now();
        this.comments = new ArrayList<>();
        
        this.usersWhoLiked = new HashSet<>();
        this.usersWhoDisliked = new HashSet<>();
    }
    
    // --- Like/Dislike Toggle Logic ---
    
    public void toggleLike(String username) {
        usersWhoDisliked.remove(username);
        if (!usersWhoLiked.add(username)) {
            usersWhoLiked.remove(username);
        }
    }
    
    public void toggleDislike(String username) {
        usersWhoLiked.remove(username);
        if (!usersWhoDisliked.add(username)) {
            usersWhoDisliked.remove(username);
        }
    }
    
    // --- Getters for Like/Dislike ---
    public int getLikes() { 
        if (usersWhoLiked == null) usersWhoLiked = new HashSet<>();
        return usersWhoLiked.size(); 
    }
    public int getDislikes() { 
        if (usersWhoDisliked == null) usersWhoDisliked = new HashSet<>();
        return usersWhoDisliked.size(); 
    }
    public boolean didUserLike(String username) {
        if (usersWhoLiked == null) usersWhoLiked = new HashSet<>();
        return usersWhoLiked.contains(username);
    }
    public boolean didUserDislike(String username) {
        if (usersWhoDisliked == null) usersWhoDisliked = new HashSet<>();
        return usersWhoDisliked.contains(username);
    }

    // --- Comment Methods ---
    public void addComment(Comment comment) {
        if (comments == null) comments = new ArrayList<>();
        this.comments.add(comment);
    }
    
    public void deleteComment(Comment comment) {
        if (comments == null) return;
        this.comments.remove(comment);
    }
    
    public void editComment(Comment comment, String newContent) {
        if (comments == null) return;
        for (Comment c : comments) {
            if (c.getId() == comment.getId()) {
                c.setContent(newContent);
                break;
            }
        }
    }
    
    // --- THIS IS THE NEW METHOD YOU NEED ---
    public Set<String> getHashtags() {
        Set<String> hashtags = new HashSet<>();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            hashtags.add(matcher.group());
        }
        return hashtags;
    }
    
    // --- Repost Methods ---
    public void addRepost() {
        this.repostCount++;
    }
    
    public void setAsRepost(String originalAuthor) {
        this.originalPostAuthor = originalAuthor;
    }
    
    public boolean isRepost() {
        return this.originalPostAuthor != null;
    }
    
    // --- Other Getters/Setters ---
    public int getId() { return id; }
    public String getAuthor() { return author; }
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public int getRepostCount() { return repostCount; }
    public String getOriginalPostAuthor() { return originalPostAuthor; }
    public String getTimestampString() {
        return this.timestamp.format(formatter);
    }
    public ArrayList<Comment> getComments() {
        if (comments == null) comments = new ArrayList<>();
        return comments;
    }
    
    public void setContent(String content) { this.content = content; }
    public void setImageUrl(String imageUrl) { this.imageUrl = (imageUrl != null) ? imageUrl.trim() : ""; }
    
    // --- Static method ---
    public static void updateNextId(ArrayList<Post> posts) {
        if (posts == null) return;
        
        int maxId = 0;
        for (Post post : posts) {
            if (post.getId() > maxId) {
                maxId = post.getId();
            }
        }
        nextId = maxId + 1;
        
        Comment.updateNextId(posts);
    }
}