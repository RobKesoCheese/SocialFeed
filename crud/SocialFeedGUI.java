package crud;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

// --- CHANGED: This is now a JDialog, not a JFrame ---
@SuppressWarnings("serial")
public class SocialFeedGUI extends JDialog {

    private ArrayList<Post> posts;
    private final String SAVE_FILE = "posts.dat"; 
    
    private UserManager userManager;
    private User currentUser;

    private JPanel feedPanel;
    private JTextField searchField;
    private JTextField messageField;
    private JTextField imageUrlField;
    private JButton addButton;
    private JToggleButton feedToggle;
    
    // --- NEW: This flag tells the main() loop whether to exit or switch accounts ---
    private boolean shouldLogout = false;
    
    private static final Color APP_BACKGROUND = new Color(240, 242, 245);

    // --- CHANGED: The constructor now takes a 'Frame' as a parent ---
    public SocialFeedGUI(Frame parent, User user, UserManager manager) {
        // --- CHANGED: Call the JDialog constructor to make it modal ---
        super(parent, "My Social Feed - Logged in as: " + user.getUsername(), true);
        
        this.currentUser = user;
        this.userManager = manager;
        
        loadPosts();
        
        // setTitle(...) is no longer needed (it's in the 'super' call)
        setSize(700, 700);
        
        // --- CHANGED: Just dispose the dialog, don't exit the app ---
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // This is triggered by the 'X' button
                saveAllData();
                shouldLogout = true; // --- NEW: 'X' button means logout/exit
            }
        });
        
        setLayout(new BorderLayout(5, 5));
        getContentPane().setBackground(APP_BACKGROUND);

        // --- TOP: Search, Toggle, and Profile Button (Unchanged) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(APP_BACKGROUND);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(APP_BACKGROUND);
        searchField = new JTextField(30);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.WEST);
        
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlsPanel.setBackground(APP_BACKGROUND);
        
        feedToggle = new JToggleButton("Following");
        feedToggle.setToolTipText("Toggle between 'Following' and 'Global' feeds");
        feedToggle.addActionListener(e -> refreshPosts());
        controlsPanel.add(feedToggle);
        
        JButton profileButton = new JButton("My Profile");
        profileButton.addActionListener(e -> openProfileEditor());
        controlsPanel.add(profileButton);
        
        topPanel.add(controlsPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: Scrollable Feed Panel (Unchanged) ---
        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(APP_BACKGROUND);
        
        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM: "Add Post" Panel (Unchanged) ---
        JPanel addPostPanel = new JPanel();
        addPostPanel.setLayout(new BoxLayout(addPostPanel, BoxLayout.Y_AXIS));
        addPostPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        addPostPanel.setBackground(new Color(230, 230, 230));

        JPanel messageRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messageRow.setOpaque(false);
        messageField = new JTextField(35);
        messageRow.add(new JLabel("Message:"));
        messageRow.add(messageField);
        addPostPanel.add(messageRow);

        JPanel imageUrlRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imageUrlRow.setOpaque(false);
        imageUrlField = new JTextField(35);
        imageUrlRow.add(new JLabel("Image URL:"));
        imageUrlRow.add(imageUrlField);
        addPostPanel.add(imageUrlRow);
        
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.setOpaque(false);
        addButton = new JButton("Add Post");
        buttonRow.add(addButton);
        addPostPanel.add(buttonRow);

        add(addPostPanel, BorderLayout.SOUTH);

        // --- Action Listeners (Unchanged) ---
        addButton.addActionListener(e -> addPost());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshPosts(); }
            public void removeUpdate(DocumentEvent e) { refreshPosts(); }
            public void changedUpdate(DocumentEvent e) { refreshPosts(); }
        });
        
        refreshPosts();
    }
    
    // --- NEW: Public getter for the main() method ---
    public boolean shouldLogout() {
        return this.shouldLogout;
    }

    // --- Action Methods (Unchanged) ---
    private void addPost() {
        String content = messageField.getText();
        String imageUrl = imageUrlField.getText();
        
        if (content.isEmpty() && imageUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Post must have content or an image URL.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        posts.add(new Post(
            currentUser.getUsername(), 
            currentUser.getAvatarUrl(), 
            content, 
            imageUrl
        ));
        
        messageField.setText("");
        imageUrlField.setText("");
        refreshPosts();
        savePosts();
    }
    
    public void repost(Post originalPost) {
        String repostContent = "Shared from @" + originalPost.getAuthor() + ":\n\n\"" + originalPost.getContent() + "\"";
        Post repost = new Post(
            currentUser.getUsername(), 
            currentUser.getAvatarUrl(), 
            repostContent, 
            originalPost.getImageUrl()
        );
        repost.setAsRepost(originalPost.getAuthor());
        posts.add(repost);
        originalPost.addRepost();
        refreshPosts();
        savePosts();
    }
    
    public void deletePost(Post post) {
        posts.remove(post);
        refreshPosts();
        savePosts();
    }
    
    // --- UPDATED: This method now handles the logic for switching accounts ---
    private void openProfileEditor() {
        ProfileEditDialog dialog = new ProfileEditDialog(this, currentUser, userManager);
        
        ProfileEditDialog.DialogResult result = dialog.showDialog();
        
        switch (result) {
            case SAVE:
                refreshPosts();
                // We use getOwner() because 'this' is a JDialog
                ((JFrame)getOwner()).setTitle("My Social Feed - Logged in as: " + currentUser.getUsername());
                break;
                
            case LOGOUT:
                saveAllData();
                this.shouldLogout = true; // --- NEW: Set flag
                dispose(); // Close main GUI
                break;
                
            case SWITCH:
                saveAllData();
                this.shouldLogout = false; // --- NEW: Set flag (default)
                dispose(); // Close main GUI
                break;
                
            case CANCEL:
            default:
                break;
        }
    }

    // --- refreshPosts() (Unchanged) ---
    public void refreshPosts() {
        feedPanel.removeAll();
        
        String searchText = searchField.getText().toLowerCase();
        ArrayList<String> following = (feedToggle.isSelected()) ? currentUser.getFollowing() : null;
        
        for (int i = posts.size() - 1; i >= 0; i--) {
            Post post = posts.get(i);
            
            if (following != null) {
                if (!post.getAuthor().equals(currentUser.getUsername()) && !following.contains(post.getAuthor())) {
                    continue;
                }
            }
            if (post == null) continue;

            boolean matchesSearch = (post.getContent() != null && post.getContent().toLowerCase().contains(searchText)) ||
                                    (post.getAuthor() != null && post.getAuthor().toLowerCase().contains(searchText));
            
            if (matchesSearch) {
                PostPanel panel = new PostPanel(post, this, currentUser);
                feedPanel.add(panel);
                feedPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        
        feedPanel.revalidate();
        feedPanel.repaint();
        feedPanel.add(Box.createVerticalGlue());
    }
    
    // --- Profile/Hashtag Methods (Unchanged) ---
    public void showProfileFor(String username) {
        ProfileDialog profileDialog = new ProfileDialog(
            this, username, this.currentUser, this.userManager, this.posts, this
        );
        profileDialog.setVisible(true);
        refreshPosts();
    }
    
    public void setSearchText(String text) {
        searchField.setText(text);
    }
    
    // --- Save & Load Methods (Unchanged) ---
    public void saveAllData() {
        savePosts();
        userManager.saveUsers();
    }
    
    public void savePosts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(posts);
        } catch (Exception e) {
            System.err.println("Error saving posts: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadPosts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            posts = (ArrayList<Post>) ois.readObject();
            Post.updateNextId(posts);
        } catch (java.io.FileNotFoundException e) {
            posts = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error loading posts: " + e.getMessage());
            posts = new ArrayList<>();
        }
    }

    // --- UPDATED: main() method is now simpler and loops correctly ---
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) { /* Use default */ }
        
        // This loop now correctly handles switching accounts
        while (true) {
            UserManager userManager = new UserManager();
            LoginScreen loginScreen = new LoginScreen(null, userManager);
            
            User user = loginScreen.showLoginDialog();
            
            if (user != null) {
                // User logged in.
                // Create the main GUI as a MODAL JDialog.
                // This call will BLOCK until the GUI is closed.
                SocialFeedGUI gui = new SocialFeedGUI(null, user, userManager);
                gui.setVisible(true); 
                
                // After the GUI is closed, check the flag
                if (gui.shouldLogout()) {
                    System.out.println("Logout requested. Exiting.");
                    break; // Exit the while(true) loop
                }
                // If shouldLogout is false, it was a "Switch Account",
                // so the loop will simply restart, showing the login screen.
                
            } else {
                // User closed the login dialog.
                System.out.println("Login canceled. Exiting.");
                break; 
            }
        }
    }
}