package crud;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class SocialFeed {

    private static ArrayList<Post> posts;
    private static final String SAVE_FILE = "posts.dat";
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadPosts();
        showMenu();
        savePosts();
        System.out.println("All posts saved. Goodbye!");
    }

    // Displays the main menu
    public static void showMenu() {
        while (true) {
            System.out.println("\n--- Simple Social Feed (Console) ---");
            System.out.println("1. Create Post");
            System.out.println("2. View All Posts");
            System.out.println("3. Search Posts");
            System.out.println("4. Edit a Post");
            System.out.println("5. Delete a Post");
            System.out.println("6. Comment on a Post");
            System.out.println("7. Like a Post");
            System.out.println("8. Dislike a Post");
            System.out.println("9. Exit & Save");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": addPost(); break;
                case "2": viewPosts(posts); break;
                case "3": searchPosts(); break;
                case "4": editPost(); break;
                case "5": deletePost(); break;
                case "6": addComment(); break;
                case "7": likePost(); break;
                case "8": dislikePost(); break;
                case "9": return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // --- Feature Methods ---

    public static void addPost() {
        System.out.print("Enter your username: ");
        String author = scanner.nextLine();
        System.out.print("Enter your message: ");
        String content = scanner.nextLine();
        
        if (author.isEmpty() || content.isEmpty()) {
            System.out.println("Author and message cannot be empty.");
            return;
        }
        
        Post newPost = new Post(author, content, ""); // Add "" for the empty image URL
        posts.add(newPost);
        System.out.println("Post created successfully!");
    }

    public static void viewPosts(ArrayList<Post> postsToView) {
        System.out.println("\n--- Your Feed ---");
        if (postsToView.isEmpty()) {
            System.out.println("No posts to show.");
        } else {
            // Loop from newest to oldest
            for (int i = postsToView.size() - 1; i >= 0; i--) {
                // This uses the Post.java toString() which is now HTML.
                // It will look messy in the console, but it's functional.
                // We'll create a simple text output here instead.
                printPostAsText(postsToView.get(i));
            }
        }
    }
    
    // Helper to print a post as plain text in the console
    private static void printPostAsText(Post post) {
        System.out.println("----------------------------------------");
        System.out.printf("Post #%d by %s (%s) [%d Likes, %d Dislikes]\n",
            post.getId(), post.getAuthor(), post.getTimestampString(),
            post.getLikes(), post.getDislikes());
        System.out.println("\n    \"" + post.getContent() + "\"\n");
        
        if (!post.getComments().isEmpty()) {
            System.out.println("    Comments:");
            for (Comment comment : post.getComments()) {
                System.out.printf("        > %s: %s\n",
                    comment.getAuthor(), comment.getContent());
            }
        }
        System.out.println("----------------------------------------");
    }

    public static void searchPosts() {
        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine().toLowerCase();
        
        ArrayList<Post> searchResults = new ArrayList<>();
        for (Post post : posts) {
            if (post.getAuthor().toLowerCase().contains(searchTerm) ||
                post.getContent().toLowerCase().contains(searchTerm)) {
                searchResults.add(post);
            }
        }
        System.out.println("--- Search Results ---");
        viewPosts(searchResults);
    }

    public static void editPost() {
        Post post = findPostById();
        if (post != null) {
            System.out.println("Current content: " + post.getContent());
            System.out.print("Enter new message: ");
            String newContent = scanner.nextLine();
            post.setContent(newContent);
            System.out.println("Post #" + post.getId() + " updated!");
        }
    }

    public static void deletePost() {
        Post post = findPostById();
        if (post != null) {
            posts.remove(post);
            System.out.println("Post #" + post.getId() + " deleted.");
        }
    }

    // --- UPDATED addComment ---
    public static void addComment() {
        Post post = findPostById();
        if (post != null) {
            System.out.print("Enter your username (leave blank for Anonymous): ");
            String author = scanner.nextLine();
            
            System.out.print("Enter your comment: ");
            String content = scanner.nextLine();
            
            if (!content.isEmpty()) {
                // Create a new Comment object
                post.addComment(new Comment(author, content));
                System.out.println("Comment added to Post #" + post.getId());
            }
        }
    }

    public static void likePost() {
        Post post = findPostById();
        if (post != null) {
            post.addLike();
            System.out.println("You liked Post #" + post.getId() + "!");
        }
    }
    
    // --- NEW dislikePost ---
    public static void dislikePost() {
        Post post = findPostById();
        if (post != null) {
            post.addDislike();
            System.out.println("You disliked Post #" + post.getId() + ".");
        }
    }

    // --- Helper Method ---
    private static Post findPostById() {
        System.out.print("Enter the ID of the post: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            for (Post post : posts) {
                if (post.getId() == id) {
                    return post;
                }
            }
            System.out.println("Post with ID #" + id + " not found.");
            return null;
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID. Please enter a number.");
            return null;
        }
    }

    // --- Save & Load Methods (Unchanged) ---
    @SuppressWarnings("unchecked")
    private static void loadPosts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            posts = (ArrayList<Post>) ois.readObject();
            Post.updateNextId(posts);
        } catch (java.io.FileNotFoundException e) {
            posts = new ArrayList<>();
        } catch (Exception e) {
            posts = new ArrayList<>();
        }
    }

    private static void savePosts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(posts);
        } catch (Exception e) {
            System.err.println("Error saving posts: " + e.getMessage());
        }
    }
}