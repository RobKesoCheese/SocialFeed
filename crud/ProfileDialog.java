package crud;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class ProfileDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a dialog window to show a user's profile and posts.
     * @param parent The main GUI frame to center on.
     * @param userToShow The User whose profile is being viewed.
     * @param allPosts The list of all posts, to be filtered.
     * @param currentUser The currently logged-in user (for PostPanel permissions).
     * @param mainGUI The reference to the main GUI (for PostPanel actions).
     */
    public ProfileDialog(JFrame parent, String usernameToShow, ArrayList<Post> allPosts, User currentUser, SocialFeedGUI mainGUI) {
        super(parent, usernameToShow + "'s Profile", true); // Modal dialog
        setSize(700, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(5, 5));

        // --- 1. Header ---
        JLabel headerLabel = new JLabel(usernameToShow + "'s Posts", JLabel.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(headerLabel, BorderLayout.NORTH);

        // --- 2. Feed Panel (to hold this user's posts) ---
        JPanel feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(Color.LIGHT_GRAY);

        int postCount = 0;
        // Filter the posts to find only those by this user
        for (int i = allPosts.size() - 1; i >= 0; i--) { // Newest first
            Post post = allPosts.get(i);
            
            if (post.getAuthor().equals(usernameToShow)) {
                // Re-use the same PostPanel, passing all necessary info
                PostPanel panel = new PostPanel(post, mainGUI, currentUser);
                feedPanel.add(panel);
                feedPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                postCount++;
            }
        }
        
        // Add a message if the user has no posts
        if (postCount == 0) {
            JLabel noPostsLabel = new JLabel("This user has no posts.", JLabel.CENTER);
            noPostsLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
            feedPanel.add(noPostsLabel);
        }

        // Add the invisible spacer to push posts to the top
        feedPanel.add(Box.createVerticalGlue());
        
        // --- 3. Scroll Pane ---
        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // --- 4. Close Button ---
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose()); // dispose() closes the dialog
        JPanel southPanel = new JPanel(); // Use a panel to prevent button stretching
        southPanel.add(closeButton);
        add(southPanel, BorderLayout.SOUTH);
    }
}