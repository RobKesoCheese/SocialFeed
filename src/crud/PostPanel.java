package crud;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

@SuppressWarnings("serial")
public class PostPanel extends RoundedPanel {

    private Post post;
    private SocialFeedGUI mainGUI;
    private User currentUser;

    private JTextArea contentArea;
    private JLabel imageLabel;
    private JPanel commentsPanel;
    private JButton likeButton, dislikeButton;
    
    // Colors & Fonts (Same as before)
    private static final Color LIGHT_PANEL_BG = Color.WHITE;
    private static final Color DARK_PANEL_BG = new Color(30, 33, 36);
    private static final Color LIGHT_TEXT = new Color(20, 20, 20);
    private static final Color DARK_TEXT = new Color(230, 230, 230);
    private static final Color LIGHT_SUBTEXT = new Color(100, 100, 100);
    private static final Color DARK_SUBTEXT = new Color(160, 160, 160);
    private static final Color ACCENT_BLUE = new Color(29, 161, 242);
    private static final Color ACCENT_RED = new Color(224, 36, 94);
    
    private boolean isDarkMode;
    private static final Font NAME_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font META_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BTN_FONT = new Font("Segoe UI", Font.BOLD, 13);

    public PostPanel(Post post, SocialFeedGUI mainGUI, User currentUser, UserManager userManager, boolean isDarkMode) {
        super(new BorderLayout(0, 0), 15, isDarkMode ? DARK_PANEL_BG : LIGHT_PANEL_BG);
        this.post = post;
        this.mainGUI = mainGUI;
        this.currentUser = currentUser;
        this.isDarkMode = isDarkMode;
        setBorder(new EmptyBorder(15, 15, 15, 15));

        if (post.isRepost()) setupRepostPanel();
        else setupStandardPanel();
    }
    
    private Color getTextColor() { return isDarkMode ? DARK_TEXT : LIGHT_TEXT; }
    private Color getSubTextColor() { return isDarkMode ? DARK_SUBTEXT : LIGHT_SUBTEXT; }

    private void setupStandardPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(48, 48));
        loadAvatar(post.getAuthorAvatarUrl(), post.getAuthor(), avatarLabel);
        
        JPanel avatarWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarWrapper.setOpaque(false);
        avatarWrapper.add(avatarLabel);
        headerPanel.add(avatarWrapper, BorderLayout.WEST);
        
        JPanel metaPanel = new JPanel(new GridLayout(2, 1));
        metaPanel.setOpaque(false);
        
        // --- NAME & LOCK ICON ---
        String lockIcon = post.isFollowersOnly() ? " 🔒" : "";
        JLabel nameLbl = new JLabel(post.getAuthor() + lockIcon);
        nameLbl.setFont(NAME_FONT);
        nameLbl.setForeground(getTextColor());
        
        if (!post.getAuthor().equals("Anonymous")) {
            nameLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            nameLbl.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) { mainGUI.showProfileFor(post.getAuthor()); }
            });
        }
        
        JLabel timeLbl = new JLabel("@" + post.getAuthor().toLowerCase() + " · " + post.getTimestampString());
        timeLbl.setFont(META_FONT);
        timeLbl.setForeground(getSubTextColor());
        
        metaPanel.add(nameLbl);
        metaPanel.add(timeLbl);
        headerPanel.add(metaPanel, BorderLayout.CENTER);

        if (currentUser.getUsername().equals(post.getAuthor())) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            actions.setOpaque(false);
            if (!post.isRepost()) {
                JButton editBtn = createSimpleButton("Edit");
                editBtn.addActionListener(e -> editPost());
                actions.add(editBtn);
            }
            JButton delBtn = createSimpleButton("Delete");
            delBtn.setForeground(Color.RED);
            delBtn.addActionListener(e -> deletePost());
            actions.add(delBtn);
            headerPanel.add(actions, BorderLayout.EAST);
        }
        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        contentArea = new JTextArea(post.getContent());
        contentArea.setFont(TEXT_FONT);
        contentArea.setForeground(getTextColor());
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setBorder(null); 
        contentPanel.add(contentArea);

        imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(imageLabel);
        loadImage();

        Set<String> hashtags = post.getHashtags();
        if (!hashtags.isEmpty()) {
            JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            tagPanel.setOpaque(false);
            tagPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
            for (String tag : hashtags) {
                JLabel tagLbl = new JLabel(tag);
                tagLbl.setFont(META_FONT);
                tagLbl.setForeground(ACCENT_BLUE);
                tagLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                tagLbl.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) { mainGUI.setSearchText(tag); }
                });
                tagPanel.add(tagLbl);
            }
            contentPanel.add(tagPanel);
        }
        add(contentPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        actionsRow.setOpaque(false);
        actionsRow.setBorder(new EmptyBorder(5, 0, 10, 0));

        likeButton = createActionButton("Like", post.getLikes(), post.didUserLike(currentUser.getUsername()) ? ACCENT_RED : getSubTextColor());
        likeButton.addActionListener(e -> {
            post.toggleLike(currentUser.getUsername());
            updateButton(likeButton, "Like", post.getLikes(), post.didUserLike(currentUser.getUsername()) ? ACCENT_RED : getSubTextColor());
            updateButton(dislikeButton, "Dislike", post.getDislikes(), getSubTextColor()); 
            mainGUI.savePosts();
            
            if (post.didUserLike(currentUser.getUsername())) {
                mainGUI.sendNotification(post.getAuthor(), currentUser.getUsername() + " liked your post.");
            }
        });

        dislikeButton = createActionButton("Dislike", post.getDislikes(), post.didUserDislike(currentUser.getUsername()) ? Color.ORANGE : getSubTextColor());
        dislikeButton.addActionListener(e -> {
            post.toggleDislike(currentUser.getUsername());
            updateButton(dislikeButton, "Dislike", post.getDislikes(), post.didUserDislike(currentUser.getUsername()) ? Color.ORANGE : getSubTextColor());
            updateButton(likeButton, "Like", post.getLikes(), getSubTextColor()); 
            mainGUI.savePosts();
        });

        JButton shareBtn = createActionButton("Share", post.getRepostCount(), getSubTextColor());
        shareBtn.addActionListener(e -> mainGUI.repost(post));

        JButton commentBtn = createActionButton("Comment", post.getComments().size(), ACCENT_BLUE);
        commentBtn.addActionListener(e -> addComment());

        actionsRow.add(likeButton);
        actionsRow.add(dislikeButton);
        actionsRow.add(shareBtn);
        actionsRow.add(commentBtn);
        
        southPanel.add(actionsRow, BorderLayout.NORTH);

        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setOpaque(false);
        
        refreshComments();
        southPanel.add(commentsPanel, BorderLayout.CENTER);
        
        add(southPanel, BorderLayout.SOUTH);
    }

    // --- REPOST LOGIC ---
    private void setupRepostPanel() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 5, 0));
        JLabel label = new JLabel("Shared by @" + post.getAuthor());
        label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        label.setForeground(getSubTextColor());
        topBar.add(label, BorderLayout.WEST);
        if (currentUser.getUsername().equals(post.getAuthor())) {
            JButton del = createSimpleButton("Delete");
            del.setForeground(Color.RED);
            del.addActionListener(e -> deletePost());
            topBar.add(del, BorderLayout.EAST);
        }
        add(topBar, BorderLayout.NORTH);
        
        JPanel quoteBox = new JPanel(new BorderLayout(10, 5));
        quoteBox.setBackground(isDarkMode ? new Color(45, 45, 45) : new Color(245, 245, 245));
        quoteBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? Color.DARK_GRAY : new Color(220, 220, 220), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        JLabel quoteAuthor = new JLabel(post.getOriginalPostAuthor());
        quoteAuthor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        quoteAuthor.setForeground(getTextColor());
        quoteBox.add(quoteAuthor, BorderLayout.NORTH);
        
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        JTextArea txt = new JTextArea(post.getContent());
        txt.setFont(TEXT_FONT);
        txt.setForeground(getTextColor());
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setEditable(false);
        txt.setOpaque(false);
        center.add(txt);
        
        imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(Box.createRigidArea(new Dimension(0, 5)));
        center.add(imageLabel);
        loadImage();
        
        quoteBox.add(center, BorderLayout.CENTER);
        add(quoteBox, BorderLayout.CENTER);
    }

    // ---  Comment Logic ---
    private void addComment() {
        // 1. Check if Followers Only Comments
        boolean isMine = post.getAuthor().equals(currentUser.getUsername());
        boolean isFollower = currentUser.isFollowing(post.getAuthor());
        
        if (post.isFollowersOnlyComments() && !isMine && !isFollower) {
            JOptionPane.showMessageDialog(mainGUI, 
                "The author has limited comments to followers only.", 
                "Cannot Comment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Setup Comment Dialog
        JCheckBox anonymousCheck = new JCheckBox("Comment Anonymously");
        
        // --- Disable Anon Checkbox if author disabled it ---
        if (!post.isAllowAnonComments()) {
            anonymousCheck.setEnabled(false);
            anonymousCheck.setToolTipText("Author has disabled anonymous comments.");
        }
        
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
                String author = (anonymousCheck.isSelected()) ? "Anonymous" : currentUser.getUsername();
                post.addComment(new Comment(author, content));
                
                // --- NEW: Send Notification ---
                if (!anonymousCheck.isSelected()) {
                    mainGUI.sendNotification(post.getAuthor(), author + " commented: " + content);
                }
                
                refreshComments();
                mainGUI.savePosts();
            }
        }
    }
    
    
    private void loadAvatar(String urlStr, String username, JLabel label) {
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override protected ImageIcon doInBackground() {
                try {
                    BufferedImage raw = null;
                    if (urlStr != null && !urlStr.isEmpty()) {
                        if (urlStr.startsWith("data:image")) {
                            String b64 = urlStr.substring(urlStr.indexOf(",") + 1);
                            raw = ImageIO.read(new ByteArrayInputStream(java.util.Base64.getDecoder().decode(b64)));
                        } else {
                            raw = ImageIO.read(new URL(urlStr));
                        }
                    }
                    BufferedImage circle = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = circle.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (raw != null) {
                        g2.setClip(new Ellipse2D.Float(0, 0, 48, 48));
                        g2.drawImage(raw, 0, 0, 48, 48, null);
                    } else {
                        g2.setColor(generateColor(username));
                        g2.fillOval(0, 0, 48, 48);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Arial", Font.BOLD, 22));
                        String letter = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
                        FontMetrics fm = g2.getFontMetrics();
                        int x = (48 - fm.stringWidth(letter)) / 2;
                        int y = ((48 - fm.getHeight()) / 2) + fm.getAscent();
                        g2.drawString(letter, x, y);
                    }
                    g2.dispose();
                    return new ImageIcon(circle);
                } catch (Exception e) { return null; }
            }
            @Override protected void done() { try { if (get() != null) label.setIcon(get()); } catch (Exception e) {} }
        };
        worker.execute();
    }
    
    private Color generateColor(String name) {
        int hash = name.hashCode();
        return new Color((hash & 0xFF0000) >> 16, (hash & 0x00FF00) >> 8, hash & 0x0000FF).brighter();
    }

    private JButton createSimpleButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(getSubTextColor());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0,0,0,0));
        return btn;
    }

    private JButton createActionButton(String text, int count, Color color) {
        String label = text + (count > 0 ? " (" + count + ")" : "");
        JButton btn = new JButton(label);
        btn.setFont(BTN_FONT);
        btn.setForeground(color);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void updateButton(JButton btn, String text, int count, Color color) {
        btn.setText(text + (count > 0 ? " (" + count + ")" : ""));
        btn.setForeground(color);
    }

    private void loadImage() {
        String urlStr = post.getImageUrl();
        if (urlStr == null || urlStr.isEmpty()) {
            imageLabel.setVisible(false);
            return;
        }
        imageLabel.setText("Loading...");
        imageLabel.setForeground(getSubTextColor());
        imageLabel.setVisible(true);
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override protected ImageIcon doInBackground() throws Exception {
                BufferedImage img = null;
                try {
                    if (urlStr.startsWith("data:image")) {
                        String b64 = urlStr.substring(urlStr.indexOf(",") + 1);
                        img = ImageIO.read(new ByteArrayInputStream(java.util.Base64.getDecoder().decode(b64)));
                    } else {
                        img = ImageIO.read(new URL(urlStr));
                    }
                    if (img != null) {
                        int w = 450; 
                        int h = (int) ((double) img.getHeight() * w / img.getWidth());
                        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                } catch (Exception e) {}
                return null;
            }
            @Override protected void done() {
                try { 
                    if (get() != null) { imageLabel.setIcon(get()); imageLabel.setText(""); }
                    else { imageLabel.setVisible(false); }
                } catch (Exception e) { imageLabel.setVisible(false); }
                revalidate(); repaint();
            }
        };
        worker.execute();
    }
    
    private void editPost() {
        String txt = JOptionPane.showInputDialog(mainGUI, "Edit post:", post.getContent());
        if(txt != null) { 
            post.setContent(txt); 
            mainGUI.savePosts(); 
            mainGUI.refreshPosts(); 
        }
    }
    
    private void deletePost() {
        if(JOptionPane.showConfirmDialog(mainGUI, "Delete this post?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
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
        
        for (Comment c : post.getComments()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
            row.setOpaque(false);
            
            String authorColor = isDarkMode ? "#E4E6EB" : "#000000";
            String contentColor = isDarkMode ? "#B0B3B8" : "#000000";
            
            String html = String.format("<html><b style='color:%s'>%s</b>: <span style='color:%s'>%s</span></html>", 
                authorColor, c.getAuthor(), contentColor, c.getContent());
                
            JLabel lbl = new JLabel(html);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            row.add(lbl);
            
            if (currentUser.getUsername().equals(c.getAuthor())) {
                JButton del = new JButton("x");
                del.setBorderPainted(false);
                del.setContentAreaFilled(false);
                del.setForeground(Color.GRAY);
                del.setMargin(new Insets(0, 5, 0, 0));
                del.addActionListener(e -> {
                    post.deleteComment(c);
                    mainGUI.savePosts();
                    mainGUI.refreshPosts();
                });
                row.add(del);
            }
            commentsPanel.add(row);
        }
        revalidate();
    }
}