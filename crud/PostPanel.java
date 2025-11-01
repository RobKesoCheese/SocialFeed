package crud;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.Cursor;
import java.awt.FlowLayout;


@SuppressWarnings("serial")
public class PostPanel extends RoundedPanel {

    private Post post;
    private SocialFeedGUI mainGUI;
    private User currentUser;

    private JTextArea contentArea;
    private JLabel likesLabel;
    private JLabel dislikesLabel;
    private JLabel repostsLabel;
    private JPanel commentsPanel;
    private JLabel imageLabel;
    
    private JButton likeButton;
    private JButton dislikeButton;
    
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color COMMENT_BACKGROUND = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR_BLUE = new Color(29, 161, 242);
    private static final Color ACCENT_COLOR_RED = new Color(224, 36, 94);
    private static final Color DEFAULT_COLOR_GRAY = Color.DARK_GRAY;

    public PostPanel(Post post, SocialFeedGUI mainGUI, User currentUser) {
        super(new BorderLayout(5, 5), 15, PANEL_BACKGROUND);
        
        this.post = post;
        this.mainGUI = mainGUI;
        this.currentUser = currentUser;
        
        setBorder(new EmptyBorder(5, 5, 5, 5));

        if (post.isRepost()) {
            setupRepostPanel();
        } else {
            setupStandardPanel();
        }
    }
    
    private void setupStandardPanel() {
        // --- Header Panel ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(40, 40));
        avatarLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        loadAvatar(post.getAuthorAvatarUrl(), avatarLabel);
        headerPanel.add(avatarLabel, BorderLayout.WEST);
        
        JButton authorButton = new JButton(String.format(
            "<html><div style='margin-left: 10px;'><b>%s</b> <font color='gray'>@%s • %s</font></div></html>",
            post.getAuthor(), post.getAuthor().toLowerCase(), post.getTimestampString()
        ));
        authorButton.setHorizontalAlignment(SwingConstants.LEFT);
        authorButton.setBorderPainted(false);
        authorButton.setOpaque(false);
        authorButton.setBackground(PANEL_BACKGROUND);
        authorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        authorButton.setToolTipText("View " + post.getAuthor() + "'s profile");
        authorButton.addActionListener(e -> mainGUI.showProfileFor(post.getAuthor()));
        headerPanel.add(authorButton, BorderLayout.CENTER);

        if (currentUser.getUsername().equals(post.getAuthor())) {
            JPanel editDeletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            editDeletePanel.setBackground(PANEL_BACKGROUND);
            
            if (!post.isRepost()) {
                JButton editButton = createSmallButton("Edit");
                editDeletePanel.add(editButton);
                editButton.addActionListener(e -> editPost());
            }
            
            JButton deleteButton = createSmallButton("Delete");
            editDeletePanel.add(deleteButton);
            deleteButton.addActionListener(e -> deletePost());
            
            headerPanel.add(editDeletePanel, BorderLayout.EAST);
        }
        
        add(headerPanel, BorderLayout.NORTH);

        // --- Content Panel ---
        JPanel contentHolderPanel = new JPanel();
        contentHolderPanel.setLayout(new BoxLayout(contentHolderPanel, BoxLayout.Y_AXIS));
        contentHolderPanel.setBackground(PANEL_BACKGROUND);
        contentHolderPanel.setBorder(new EmptyBorder(0, 15, 10, 15));

        imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentHolderPanel.add(imageLabel);
        contentHolderPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        loadImage();

        contentArea = new JTextArea(post.getContent());
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contentArea.setBackground(PANEL_BACKGROUND);
        contentHolderPanel.add(contentArea);

        // Hashtag Panel
        Set<String> hashtags = post.getHashtags();
        if (!hashtags.isEmpty()) {
            JPanel hashtagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            hashtagsPanel.setBackground(PANEL_BACKGROUND);
            for (String tag : hashtags) {
                JButton tagButton = createLinkButton(tag);
                tagButton.addActionListener(e -> mainGUI.setSearchText(tag));
                hashtagsPanel.add(tagButton);
            }
            contentHolderPanel.add(hashtagsPanel);
        }
        
        add(contentHolderPanel, BorderLayout.CENTER);

        // --- Footer & Comments Panel ---
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(PANEL_BACKGROUND);
        southPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        footerPanel.setBackground(PANEL_BACKGROUND);

        likeButton = createSmallButton("Like");
        likesLabel = new JLabel(String.valueOf(post.getLikes()));
        
        dislikeButton = createSmallButton("Dislike");
        dislikesLabel = new JLabel(String.valueOf(post.getDislikes()));

        JButton commentButton = createSmallButton("Comment");
        
        JButton repostButton = createSmallButton("Share");
        repostsLabel = new JLabel(String.valueOf(post.getRepostCount()));

        footerPanel.add(likeButton);
        footerPanel.add(likesLabel);
        footerPanel.add(dislikeButton);
        footerPanel.add(dislikesLabel);
        footerPanel.add(repostButton);
        footerPanel.add(repostsLabel);
        footerPanel.add(commentButton);
        
        southPanel.add(footerPanel, BorderLayout.NORTH);
        
        updateLikeButtonStates();

        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setBackground(COMMENT_BACKGROUND);
        commentsPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        refreshComments();
        southPanel.add(commentsPanel, BorderLayout.CENTER);
        
        add(southPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        
        likeButton.addActionListener(e -> {
            post.toggleLike(currentUser.getUsername());
            updateLikeButtonStates();
            mainGUI.savePosts();
        });

        dislikeButton.addActionListener(e -> {
            post.toggleDislike(currentUser.getUsername());
            updateLikeButtonStates();
            mainGUI.savePosts();
        });
        
        repostButton.addActionListener(e -> {
            mainGUI.repost(post);
        });

        commentButton.addActionListener(e -> addComment());
    }
    
    private void updateLikeButtonStates() {
        likesLabel.setText(String.valueOf(post.getLikes()));
        dislikesLabel.setText(String.valueOf(post.getDislikes()));

        if (post.didUserLike(currentUser.getUsername())) {
            likeButton.setForeground(ACCENT_COLOR_BLUE);
            dislikeButton.setForeground(DEFAULT_COLOR_GRAY);
        } else if (post.didUserDislike(currentUser.getUsername())) {
            likeButton.setForeground(DEFAULT_COLOR_GRAY);
            dislikeButton.setForeground(ACCENT_COLOR_RED);
        } else {
            likeButton.setForeground(DEFAULT_COLOR_GRAY);
            dislikeButton.setForeground(DEFAULT_COLOR_GRAY);
        }
    }
    
    // --- THIS METHOD IS NOW CORRECTED ---
    private void setupRepostPanel() {
        // A wrapper panel that uses BorderLayout
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.setBorder(new EmptyBorder(5, 10, 5, 10));

        // --- Repost Label (on the left) ---
        JLabel repostLabel = new JLabel(String.format(
            "<html><font color='gray'>Shared by <b>%s</b> • %s</font></html>", 
            post.getAuthor(), post.getTimestampString()
        ));
        headerWrapper.add(repostLabel, BorderLayout.WEST);
        
        // --- Delete Button (on the right) ---
        if (currentUser.getUsername().equals(post.getAuthor())) {
            JButton deleteButton = createSmallButton("Delete");
            deleteButton.addActionListener(e -> deletePost());
            
            JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            deletePanel.setOpaque(false);
            deletePanel.add(deleteButton);
            
            headerWrapper.add(deletePanel, BorderLayout.EAST);
        } 
        
        add(headerWrapper, BorderLayout.NORTH); // Add the wrapper to the main panel
        
        // --- Content (Original Post) ---
        JPanel quotePanel = new JPanel(new BorderLayout(5, 5));
        quotePanel.setBackground(new Color(245, 245, 245));
        quotePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JPanel quoteHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quoteHeader.setOpaque(false);
        JLabel quoteAuthorLabel = new JLabel(String.format(
            "<html><b>%s</b> <font color='gray'>@%s</font></html>",
            post.getOriginalPostAuthor(), post.getOriginalPostAuthor().toLowerCase()
        ));
        quoteHeader.add(quoteAuthorLabel);
        quotePanel.add(quoteHeader, BorderLayout.NORTH);
        
        JPanel contentHolderPanel = new JPanel();
        contentHolderPanel.setLayout(new BoxLayout(contentHolderPanel, BoxLayout.Y_AXIS));
        contentHolderPanel.setOpaque(false);
        contentHolderPanel.setBorder(new EmptyBorder(0, 15, 10, 15));

        imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentHolderPanel.add(imageLabel);
        contentHolderPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        loadImage();

        contentArea = new JTextArea(post.getContent());
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contentArea.setOpaque(false);
        contentHolderPanel.add(contentArea);
        
        quotePanel.add(contentHolderPanel, BorderLayout.CENTER);
        
        add(quotePanel, BorderLayout.CENTER);
    }
    
 // --- NEW: Helper to load avatar ---
    private void loadAvatar(String avatarUrlString, JLabel avatarLabel) {
        if (avatarUrlString == null || avatarUrlString.isEmpty()) {
            avatarLabel.setText("N/A");
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            return;
        }

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
                            img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                        }
                    } else {
                        img = ImageIO.read(new URL(avatarUrlString));
                    }

                    if (img != null) {
                        // Scale to the 40x40 size of the label
                        Image scaledImg = img.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImg);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load avatar: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        avatarLabel.setIcon(icon);
                        avatarLabel.setText(null);
                    } else {
                        avatarLabel.setText("N/A");
                        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    }
                } catch (Exception e) {
                    avatarLabel.setText("N/A");
                    avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
                }
            }
        };
        worker.execute();
    }
    
    // --- Helper to create styled buttons ---
    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(DEFAULT_COLOR_GRAY);
        return button;
    }
    
    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setForeground(ACCENT_COLOR_BLUE);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // --- loadImage() Method (Unchanged) ---
    private void loadImage() {
        String imageUrlString = post.getImageUrl();
        if (imageUrlString != null && !imageUrlString.isEmpty()) {
            imageLabel.setText("Loading image...");
            imageLabel.setIcon(null);
            imageLabel.setVisible(true);

            SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    BufferedImage img = null;
                    try {
                        if (imageUrlString.startsWith("data:image")) {
                            int commaIndex = imageUrlString.indexOf(',');
                            if (commaIndex != -1) {
                                String base64Data = imageUrlString.substring(commaIndex + 1);
                                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                                img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                            }
                        } else {
                            img = ImageIO.read(new URL(imageUrlString));
                        }

                        if (img != null) {
                            int panelWidth = PostPanel.this.getWidth() > 0 ? PostPanel.this.getWidth() - 30 : 600;
                            if (img.getWidth() > panelWidth) {
                                int newHeight = (int) ((double) img.getHeight() * panelWidth / img.getWidth());
                                Image scaledImg = img.getScaledInstance(panelWidth, newHeight, Image.SCALE_SMOOTH);
                                return new ImageIcon(scaledImg);
                            }
                            return new ImageIcon(img);
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading image: " + e.getMessage());
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon icon = get();
                        if (icon != null) {
                            imageLabel.setIcon(icon);
                            imageLabel.setText(null);
                        } else {
                            imageSettingFailed();
                        }
                    } catch (Exception e) {
                        imageSettingFailed();
                    }
                    PostPanel.this.revalidate();
                    PostPanel.this.repaint();
                }
                
                private void imageSettingFailed() {
                    imageLabel.setText("Image failed to load");
                    imageLabel.setIcon(null);
                    imageLabel.setVisible(false);
                }
            };
            worker.execute();
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText(null);
            imageLabel.setVisible(false);
        }
    }
    
    // --- Other Helper Methods (Unchanged) ---
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
            contentArea.setText(newContent);
            changed = true;
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