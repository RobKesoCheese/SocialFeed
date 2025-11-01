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

public class SocialFeedGUI extends JFrame {

    private static final long serialVersionUID = 1L; // --- FIX for yellow warning ---
    private ArrayList<Post> posts;
    private final String SAVE_FILE = "posts.dat"; 
    
    private User currentUser;

    // --- GUI Components ---
    private JPanel feedPanel;
    private JTextField searchField;
    private JTextField messageField;
    private JTextField imageUrlField;
    private JButton addButton;
    
    // --- NEW: Define our color palette ---
    private static final Color APP_BACKGROUND = new Color(240, 242, 245); // Facebook's grey

    public SocialFeedGUI(User user) {
        this.currentUser = user;
        
        loadPosts();
        
        setTitle("My Social Feed - Logged in as: " + currentUser.getUsername());
        setSize(700, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                savePosts();
                System.exit(0);
            }
        });
        setLayout(new BorderLayout(5, 5));
        getContentPane().setBackground(APP_BACKGROUND); // --- UPDATED ---

        // --- TOP: Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(APP_BACKGROUND); // --- UPDATED ---
        searchField = new JTextField(30);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // --- CENTER: The Scrollable Feed Panel ---
        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(APP_BACKGROUND); // --- UPDATED ---
        
        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM: "Add Post" Panel (UPDATED for new look) ---
        JPanel addPostPanel = new JPanel();
        addPostPanel.setLayout(new BoxLayout(addPostPanel, BoxLayout.Y_AXIS));
        addPostPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        addPostPanel.setBackground(new Color(230, 230, 230)); // Slightly darker grey

        // Message input row
        JPanel messageRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messageRow.setOpaque(false);
        messageField = new JTextField(35);
        messageRow.add(new JLabel("Message:"));
        messageRow.add(messageField);
        addPostPanel.add(messageRow);

        // Image URL input row
        JPanel imageUrlRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imageUrlRow.setOpaque(false);
        imageUrlField = new JTextField(35);
        imageUrlRow.add(new JLabel("Image URL:"));
        imageUrlRow.add(imageUrlField);
        addPostPanel.add(imageUrlRow);
        
        // Button row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.setOpaque(false);
        addButton = new JButton("Add Post");
        buttonRow.add(addButton);
        addPostPanel.add(buttonRow);

        add(addPostPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        addButton.addActionListener(e -> addPost());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshPosts(); }
            public void removeUpdate(DocumentEvent e) { refreshPosts(); }
            public void changedUpdate(DocumentEvent e) { refreshPosts(); }
        });
        
        refreshPosts();
    }

    // --- Main Action Methods ---

    private void addPost() {
        String content = messageField.getText();
        String imageUrl = imageUrlField.getText();
        
        if (content.isEmpty() && imageUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Post must have content or an image URL.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        posts.add(new Post(currentUser.getUsername(), content, imageUrl));
        
        messageField.setText("");
        imageUrlField.setText("");
        refreshPosts();
        savePosts();
    }
    
    public void deletePost(Post post) {
        posts.remove(post);
        refreshPosts();
        savePosts();
    }

    // --- refreshPosts() Method (Unchanged) ---
    public void refreshPosts() {
        feedPanel.removeAll();
        
        String searchText = searchField.getText().toLowerCase();
        
        for (int i = posts.size() - 1; i >= 0; i--) {
            Post post = posts.get(i);
            
            boolean matchesSearch = post.getContent().toLowerCase().contains(searchText) ||
                                    post.getAuthor().toLowerCase().contains(searchText);
            
            if (matchesSearch) {
                PostPanel panel = new PostPanel(post, this, currentUser);
                feedPanel.add(panel);
                // --- UPDATED: Use a standard border for spacing ---
                feedPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        
        feedPanel.revalidate();
        feedPanel.repaint();
        feedPanel.add(Box.createVerticalGlue());
    }
    
    // --- Profile/Hashtag Methods (Unchanged) ---
    
    public void showProfileFor(String username) {
        ProfileDialog profileDialog = new ProfileDialog(this, username, this.posts, this.currentUser, this);
        profileDialog.setVisible(true);
    }
    
    public void setSearchText(String text) {
        searchField.setText(text);
    }
    
    // --- Save & Load Methods (Unchanged) ---
    
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

    // --- main() Method (Unchanged) ---
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) { /* Use default */ }
        
        UserManager userManager = new UserManager();
        LoginScreen loginScreen = new LoginScreen(null, userManager);
        
        User user = loginScreen.showLoginDialog();
        
        if (user != null) {
            final User currentUser = user;
            SwingUtilities.invokeLater(() -> {
                SocialFeedGUI gui = new SocialFeedGUI(currentUser);
                gui.setVisible(true);
            });
        } else {
            System.out.println("Login canceled. Exiting.");
            System.exit(0);
        }
    }
}