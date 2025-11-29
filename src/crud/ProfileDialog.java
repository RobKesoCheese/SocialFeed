package crud;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ProfileDialog extends JDialog {

    private User currentUser;
    private String usernameToShow;
    private UserManager userManager;

    public ProfileDialog(Dialog parent, String usernameToShow, User currentUser, UserManager userManager, ArrayList<Post> allPosts, SocialFeedGUI mainGUI) {
        super(parent, usernameToShow + "'s Profile", true);
        
        this.currentUser = currentUser;
        this.usernameToShow = usernameToShow;
        this.userManager = userManager;
        
        setSize(700, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(5, 5));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(60, 60));
        avatarLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        loadAvatar(userManager.getUser(usernameToShow), avatarLabel); 
        headerPanel.add(avatarLabel, BorderLayout.WEST);

        JLabel headerLabel = new JLabel(usernameToShow + "'s Posts");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        if (!currentUser.getUsername().equals(usernameToShow)) {
            JButton followButton = new JButton();
            updateFollowButtonText(followButton);
            
            followButton.addActionListener(e -> {
                if (currentUser.isFollowing(usernameToShow)) {
                    currentUser.unfollowUser(usernameToShow);
                } else {
                    currentUser.followUser(usernameToShow);
                }
                userManager.saveUsers();
                updateFollowButtonText(followButton);
            });
            headerPanel.add(followButton, BorderLayout.EAST);
        }
        
        add(headerPanel, BorderLayout.NORTH);

        // Feed
        JPanel feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(Color.LIGHT_GRAY);

        int postCount = 0;
        for (int i = allPosts.size() - 1; i >= 0; i--) {
            Post post = allPosts.get(i);
            if (post.getAuthor().equals(usernameToShow)) {
                // --- PASS false (Light Mode default) or change constructor to accept theme ---
                PostPanel panel = new PostPanel(post, mainGUI, currentUser, userManager, false);
                feedPanel.add(panel);
                feedPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                postCount++;
            }
        }
        
        if (postCount == 0) {
            JLabel noPostsLabel = new JLabel("This user has no posts.", JLabel.CENTER);
            noPostsLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
            feedPanel.add(noPostsLabel);
        }

        feedPanel.add(Box.createVerticalGlue());
        
        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel southPanel = new JPanel();
        southPanel.add(closeButton);
        add(southPanel, BorderLayout.SOUTH);
    }
    
    private void updateFollowButtonText(JButton button) {
        if (currentUser.isFollowing(usernameToShow)) {
            button.setText("Unfollow");
            button.setBackground(Color.LIGHT_GRAY);
        } else {
            button.setText("Follow");
            button.setBackground(new Color(29, 161, 242)); 
            button.setForeground(Color.WHITE);
        }
    }
    
    private void loadAvatar(User user, JLabel avatarLabel) {
        if (user == null || user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            avatarLabel.setText("N/A");
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            return;
        }
        String avatarUrlString = user.getAvatarUrl();
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                BufferedImage img = null;
                try {
                    if (avatarUrlString.startsWith("data:image")) {
                        int commaIndex = avatarUrlString.indexOf(',');
                        if (commaIndex != -1) {
                            String base64Data = avatarUrlString.substring(commaIndex + 1);
                            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                            img = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
                        }
                    } else {
                        img = ImageIO.read(new URL(avatarUrlString));
                    }
                    if (img != null) {
                        Image scaledImg = img.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImg);
                    }
                } catch (Exception e) {}
                return null;
            }
            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        avatarLabel.setIcon(icon);
                        avatarLabel.setText(null);
                    }
                } catch (Exception e) {
                    avatarLabel.setText("N/A");
                }
            }
        };
        worker.execute();
    }
}