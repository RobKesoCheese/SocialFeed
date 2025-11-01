package crud;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.Base64;
import java.io.ByteArrayInputStream;

// --- UPDATED: We are now using our new RoundedPanel ---
public class PostPanel extends RoundedPanel {

    private static final long serialVersionUID = 1L;
    private Post post;
    private SocialFeedGUI mainGUI;
    private User currentUser;

    // --- UPDATED: Back to JTextArea to fix overflow ---
    private JTextArea contentArea;
    private JLabel likesLabel;
    private JLabel dislikesLabel;
    private JPanel commentsPanel;
    private JLabel imageLabel;
    
    // --- NEW: Define our new color palette ---
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color COMMENT_BACKGROUND = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR_BLUE = new Color(29, 161, 242);

    public PostPanel(Post post, SocialFeedGUI mainGUI, User currentUser) {
        // --- UPDATED: Call the RoundedPanel constructor ---
        super(new BorderLayout(5, 5), 15, PANEL_BACKGROUND);
        
        this.post = post;
        this.mainGUI = mainGUI;
        this.currentUser = currentUser;
        
        // Use an outer border for margin *between* posts
        setBorder(new EmptyBorder(5, 5, 5, 5));

        // --- 2. Header Panel (Author is now a clickable button) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JButton authorButton = new JButton(String.format(
            "<html><b>%s</b> <font color='gray'>@%s • %s</font></html>",
            post.getAuthor(), post.getAuthor().toLowerCase(), post.getTimestampString()
        ));
        authorButton.setHorizontalAlignment(SwingConstants.LEFT);
        authorButton.setBorderPainted(false);
        authorButton.setOpaque(false);
        authorButton.setBackground(PANEL_BACKGROUND);
        authorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        authorButton.setToolTipText("View " + post.getAuthor() + "'s profile");
        
        authorButton.addActionListener(e -> mainGUI.showProfileFor(post.getAuthor()));
        headerPanel.add(authorButton, BorderLayout.WEST);

        // Edit/Delete buttons (only if user is the author)
        if (currentUser.getUsername().equals(post.getAuthor())) {
            JPanel editDeletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            editDeletePanel.setBackground(PANEL_BACKGROUND);
            
            // Create small, icon-like buttons
            JButton editButton = createSmallButton("Edit");
            JButton deleteButton = createSmallButton("Delete");
            
            editDeletePanel.add(editButton);
            editDeletePanel.add(deleteButton);
            headerPanel.add(editDeletePanel, BorderLayout.EAST);

            editButton.addActionListener(e -> editPost());
            deleteButton.addActionListener(e -> deletePost());
        }
        
        add(headerPanel, BorderLayout.NORTH);

        // --- 3. Content Panel (Image, Text, Hashtags) ---
        JPanel contentHolderPanel = new JPanel();
        contentHolderPanel.setLayout(new BoxLayout(contentHolderPanel, BoxLayout.Y_AXIS));
        contentHolderPanel.setBackground(PANEL_BACKGROUND);
        contentHolderPanel.setBorder(new EmptyBorder(0, 15, 10, 15));

        // Image Panel
        imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentHolderPanel.add(imageLabel);
        contentHolderPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        loadImage(); // Load the image

        // --- FIXED: Use JTextArea for perfect word wrapping ---
        contentArea = new JTextArea(post.getContent());
        contentArea.setEditable(false);
        contentArea.setLineWrap(true); // This is the fix!
        contentArea.setWrapStyleWord(true); // Wraps at word boundaries
        contentArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contentArea.setBackground(PANEL_BACKGROUND);
        contentHolderPanel.add(contentArea);

        // --- NEW: Hashtag Panel ---
        Set<String> hashtags = post.getHashtags();
        if (!hashtags.isEmpty()) {
            JPanel hashtagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            hashtagsPanel.setBackground(PANEL_BACKGROUND);
            
            for (String tag : hashtags) {
                JButton tagButton = createLinkButton(tag); // Use new link-style button
                tagButton.addActionListener(e -> mainGUI.setSearchText(tag));
                hashtagsPanel.add(tagButton);
            }
            contentHolderPanel.add(hashtagsPanel);
        }
        
        add(contentHolderPanel, BorderLayout.CENTER);

        // --- 4. Footer & Comments Panel (Combined) ---
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(PANEL_BACKGROUND);
        southPanel.setBorder(new EmptyBorder(5, 10, 10, 10)); // Add padding

        // --- UPDATED: Footer with new buttons ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        footerPanel.setBackground(PANEL_BACKGROUND);

        JButton likeButton = createSmallButton("Like");
        likesLabel = new JLabel(String.valueOf(post.getLikes()));
        
        JButton dislikeButton = createSmallButton("Dislike");
        dislikesLabel = new JLabel(String.valueOf(post.getDislikes()));

        JButton commentButton = createSmallButton("Comment");
        
        footerPanel.add(likeButton);
        footerPanel.add(likesLabel);
        footerPanel.add(dislikeButton);
        footerPanel.add(dislikesLabel);
        footerPanel.add(commentButton);
        
        southPanel.add(footerPanel, BorderLayout.NORTH);

        // --- 5. Comments Section ---
        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setBackground(COMMENT_BACKGROUND);
        commentsPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        refreshComments(); // Build comments
        southPanel.add(commentsPanel, BorderLayout.CENTER);
        
        add(southPanel, BorderLayout.SOUTH);

        // --- 6. Action Listeners ---
        likeButton.addActionListener(e -> {
            post.addLike();
            likesLabel.setText(String.valueOf(post.getLikes()));
            mainGUI.savePosts();
        });

        dislikeButton.addActionListener(e -> {
            post.addDislike();
            dislikesLabel.setText(String.valueOf(post.getDislikes()));
            mainGUI.savePosts();
        });

        commentButton.addActionListener(e -> addComment());
    }
    
    // --- NEW: Helper methods to create styled buttons ---
    
    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(Color.DARK_GRAY);
        return button;
    }
    
    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setForeground(ACCENT_COLOR_BLUE); // Twitter-blue
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

 // --- NEW: Method to load and display image (handles Data URLs) ---
    private void loadImage() {
        String imageUrlString = post.getImageUrl();
        if (imageUrlString != null && !imageUrlString.isEmpty()) {
            imageLabel.setText("Loading image...");
            imageLabel.setIcon(null); 

            // Use SwingWorker for background image loading
            SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    BufferedImage img = null; // Declare img outside the try/if
                    
                    try {
                        // --- NEW LOGIC: Check for Data URL ---
                        if (imageUrlString.startsWith("data:image")) {
                            // This is a Base64 Data URL
                            // 1. Find the comma
                            int commaIndex = imageUrlString.indexOf(',');
                            if (commaIndex == -1) {
                                throw new IOException("Invalid Data URL: missing comma.");
                            }
                            // 2. Get the Base64 part (everything after the comma)
                            String base64Data = imageUrlString.substring(commaIndex + 1);
                            
                            // 3. Decode the Base64 string into bytes
                            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                            
                            // 4. Read the bytes as an image
                            img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                        
                        } else {
                            // This is a standard (http/https) URL
                            URL url = new URL(imageUrlString);
                            img = ImageIO.read(url);
                        }

                        // --- SCALING LOGIC (Unchanged) ---
                        if (img != null) {
                            // Scale image to fit within PostPanel width
                            int panelWidth = PostPanel.this.getWidth() > 0 ? PostPanel.this.getWidth() - 30 : 600;
                            if (img.getWidth() > panelWidth) {
                                int newHeight = (int) ((double) img.getHeight() * panelWidth / img.getWidth());
                                Image scaledImg = img.getScaledInstance(panelWidth, newHeight, Image.SCALE_SMOOTH);
                                return new ImageIcon(scaledImg);
                            }
                            return new ImageIcon(img); // Return original if it's small enough
                        }
                    } catch (Exception e) { 
                        // This will catch errors from Base64, IO, or URL
                        System.err.println("Error loading image. URL/Data: " + imageUrlString.substring(0, Math.min(imageUrlString.length(), 50)) + "... - " + e.getMessage());
                    }
                    return null; // Return null if anything failed
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon icon = get();
                        if (icon != null) {
                            imageLabel.setIcon(icon);
                            imageLabel.setText(null); // Clear "Loading..."
                        } else {
                            imageSettingFailed();
                        }
                    } catch (Exception e) {
                        imageSettingFailed();
                    }
                    // Revalidate the panel to show the image
                    PostPanel.this.revalidate();
                    PostPanel.this.repaint();
                }
                
                private void imageSettingFailed() {
                    imageLabel.setText("Image failed to load");
                    imageLabel.setIcon(null);
                    // Hide the label if it fails so it doesn't take up space
                    imageLabel.setVisible(false); 
                }
            };
            worker.execute();
        } else {
            // No image URL, hide the label
            imageLabel.setIcon(null);
            imageLabel.setText(null);
            imageLabel.setVisible(false);
        }
    }
    
    // --- Action Helper Methods (Unchanged) ---
    private void addComment() {
        JCheckBox anonymousCheck = new JCheckBox("Comment Anonymously");
        JTextField commentField = new JTextField(25);
        
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(commentField, BorderLayout.CENTER);
        panel.add(anonymousCheck, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(
            mainGUI, panel, "Add Comment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String content = commentField.getText();
            if (content != null && !content.trim().isEmpty()) {
                String author = (anonymousCheck.isSelected()) ? null : currentUser.getUsername();
                post.addComment(new Comment(author, content));
                refreshComments();
                mainGUI.savePosts();
            }
        }
    }
    
    private void editPost() {
        String newContent = (String) JOptionPane.showInputDialog(
            mainGUI, "Edit your post:", "Edit Post",
            JOptionPane.PLAIN_MESSAGE, null, null, post.getContent()
        );
        String newImageUrl = (String) JOptionPane.showInputDialog(
            mainGUI, "Edit image URL (leave blank for none):", "Edit Image URL",
            JOptionPane.PLAIN_MESSAGE, null, null, post.getImageUrl()
        );

        boolean changed = false;
        if (newContent != null && !newContent.equals(post.getContent())) {
            post.setContent(newContent);
            contentArea.setText(newContent); // Update text area
            changed = true;
            // Note: We'd also need to refresh the hashtag panel here
            // The simplest way is to tell the mainGUI to refresh everything
            mainGUI.refreshPosts(); 
        }
        if (newImageUrl != null && !newImageUrl.equals(post.getImageUrl())) {
            post.setImageUrl(newImageUrl);
            loadImage();
            changed = true;
        }

        if (changed) {
            mainGUI.savePosts();
        }
    }

    private void deletePost() {
        int confirm = JOptionPane.showConfirmDialog(
            mainGUI, "Are you sure you want to delete this post?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            mainGUI.deletePost(post);
        }
    }
    
    // --- refreshComments() Method (Updated for new look) ---
    private void refreshComments() {
        commentsPanel.removeAll();
        
        if (post.getComments().isEmpty()) {
            commentsPanel.setVisible(false);
            return;
        }

        commentsPanel.setVisible(true);
        for (Comment comment : post.getComments()) {
            JPanel commentRowPanel = new JPanel(new BorderLayout(5, 0));
            commentRowPanel.setBackground(COMMENT_BACKGROUND);
            commentRowPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JLabel commentLabel = new JLabel(String.format(
                "<html><b>%s:</b> %s</html>",
                comment.getAuthor(), comment.getContent()
            ));
            commentRowPanel.add(commentLabel, BorderLayout.CENTER);

            if (currentUser.getUsername().equals(comment.getAuthor())) {
                JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                buttonsPanel.setOpaque(false);
                
                JButton editCommentButton = createSmallButton("Edit");
                JButton deleteCommentButton = createSmallButton("Delete");
                
                buttonsPanel.add(editCommentButton);
                buttonsPanel.add(deleteCommentButton);
                
                commentRowPanel.add(buttonsPanel, BorderLayout.EAST);
                
                editCommentButton.addActionListener(e -> editComment(comment));
                deleteCommentButton.addActionListener(e -> deleteComment(comment));
            }

            commentsPanel.add(commentRowPanel);
        }
        
        commentsPanel.revalidate();
        commentsPanel.repaint();
    }
    
    private void editComment(Comment comment) {
        String newCommentText = (String) JOptionPane.showInputDialog(
            mainGUI, "Edit your comment:", "Edit Comment",
            JOptionPane.PLAIN_MESSAGE, null, null, comment.getContent()
        );
        
        if (newCommentText != null && !newCommentText.trim().isEmpty()) {
            post.editComment(comment, newCommentText);
            refreshComments();
            mainGUI.savePosts();
        }
    }

    private void deleteComment(Comment comment) {
        int confirm = JOptionPane.showConfirmDialog(
            mainGUI, "Delete this comment?\n\"" + comment.getContent() + "\"",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            post.deleteComment(comment);
            refreshComments();
            mainGUI.savePosts();
        }
    }
}