package crud;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("serial")
public class SocialFeedGUI extends JDialog {

    private ArrayList<Post> posts;
    private UserManager userManager;
    private User currentUser;
    private DatabaseManager dbManager;

    private JPanel feedPanel;
    private JTextField searchField;
    
    // --- Post Creation Components ---
    private JPanel createContainer;
    private JButton triggerPostButton;
    private JPanel postFormPanel;
    private JTextField messageField;
    private JTextField imageUrlField;
    private JButton submitPostButton;
    private JButton cancelPostButton;
    
    // Settings Checkboxes
    private JCheckBox anonPostCheck;
    private JCheckBox followersOnlyPostCheck;
    private JCheckBox allowAnonCommentsCheck;
    private JCheckBox followersOnlyCommentsCheck;
    
    // Other UI
    private JToggleButton feedToggle;
    private JButton themeBtn, profileBtn, notifBtn;
    private JPanel topPanel, header, subHeader, trendingPanel;
    private JLabel titleLabel;
    private RoundedPanel searchBarContainer;
    private JLabel searchIconLabel;
    
    private boolean shouldLogout = false;
    private boolean isDarkMode = false;
    
    // Colors
    private static final Color LIGHT_BG = new Color(240, 242, 245);
    private static final Color LIGHT_SURFACE = Color.WHITE;
    private static final Color LIGHT_TEXT = new Color(5, 5, 5);
    private static final Color LIGHT_INPUT_BG = Color.WHITE; 
    
    private static final Color DARK_BG = new Color(21, 32, 43);
    private static final Color DARK_SURFACE = new Color(30, 44, 56);
    private static final Color DARK_TEXT = new Color(255, 255, 255);
    private static final Color DARK_INPUT_BG = new Color(39, 51, 64);
    
    private static final Color ACCENT = new Color(29, 161, 242);

    public SocialFeedGUI(Frame parent, User user, UserManager manager, DatabaseManager dbManager) {
        super(parent, "Feed", true);
        this.currentUser = user;
        this.userManager = manager;
        this.dbManager = dbManager;
        
        this.posts = dbManager.loadPosts();
        Post.updateNextId(this.posts);
        
        setSize(800, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                savePosts(); userManager.saveUsers(); shouldLogout = true;
            }
        });
        
        setLayout(new BorderLayout());
        
        // --- 1. HEADER ---
        setupHeader();
        
        // --- 2. CENTER (Feed + Sidebar) ---
        JPanel centerContainer = new JPanel(new BorderLayout());
        
        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(feedPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        centerContainer.add(scroll, BorderLayout.CENTER);
        
        trendingPanel = new JPanel();
        trendingPanel.setLayout(new BoxLayout(trendingPanel, BoxLayout.Y_AXIS));
        trendingPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        trendingPanel.setPreferredSize(new Dimension(200, 0));
        centerContainer.add(trendingPanel, BorderLayout.EAST);
        
        add(centerContainer, BorderLayout.CENTER);

        // --- 3. CREATE POST ---
        setupCreatePost();

        applyTheme();
        refreshPosts();
        refreshTrending();
    }
    
    private void setupHeader() {
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(15, 20, 5, 20));
        
        titleLabel = new JLabel("Home");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);
        
        notifBtn = new JButton("🔔");
        styleIconBtn(notifBtn);
        notifBtn.addActionListener(e -> {
            ArrayList<Notification> notifs = dbManager.getNotifications(currentUser.getUsername());
            new NotificationDialog(this, notifs).setVisible(true);
        });
        
        themeBtn = new JButton("🌙");
        styleIconBtn(themeBtn);
        themeBtn.addActionListener(e -> toggleTheme());
        
        profileBtn = new JButton("👤");
        styleIconBtn(profileBtn);
        profileBtn.addActionListener(e -> openProfileEditor());

        headerActions.add(notifBtn);
        headerActions.add(themeBtn);
        headerActions.add(profileBtn);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(headerActions, BorderLayout.EAST);
        
        subHeader = new JPanel(new BorderLayout(10, 0));
        subHeader.setBorder(new EmptyBorder(5, 20, 15, 20));
        
        searchBarContainer = new RoundedPanel(new BorderLayout(5, 0), 30, Color.WHITE);
        searchBarContainer.setBorder(new EmptyBorder(5, 15, 5, 10));
        searchIconLabel = new JLabel("🔍");
        searchField = new JTextField();
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshPosts(); }
            public void removeUpdate(DocumentEvent e) { refreshPosts(); }
            public void changedUpdate(DocumentEvent e) { refreshPosts(); }
        });
        searchBarContainer.add(searchIconLabel, BorderLayout.WEST);
        searchBarContainer.add(searchField, BorderLayout.CENTER);

        feedToggle = new JToggleButton("Following");
        feedToggle.setFocusPainted(false);
        feedToggle.addActionListener(e -> refreshPosts());
        
        subHeader.add(searchBarContainer, BorderLayout.CENTER);
        subHeader.add(feedToggle, BorderLayout.EAST);
        
        topPanel.add(header);
        topPanel.add(subHeader);
        add(topPanel, BorderLayout.NORTH);
    }

    private void setupCreatePost() {
        createContainer = new JPanel(new BorderLayout());
        createContainer.setBorder(new EmptyBorder(15, 20, 20, 20));
        
        // 1. Trigger Button
        triggerPostButton = new ModernButton("Create Post", ACCENT, ACCENT.brighter(), Color.WHITE);
        triggerPostButton.setPreferredSize(new Dimension(100, 45));
        triggerPostButton.addActionListener(e -> showPostForm());
        
        // 2. Hidden Form
        createPostFormPanel();
        
        createContainer.add(triggerPostButton, BorderLayout.CENTER);
        add(createContainer, BorderLayout.SOUTH);
    }
    
    private void createPostFormPanel() {
        postFormPanel = new RoundedPanel(new BorderLayout(10, 10), 20, Color.WHITE);
        postFormPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel inputs = new JPanel(new GridLayout(2, 1, 0, 10));
        inputs.setOpaque(false);
        messageField = new JTextField();
        messageField.setBorder(BorderFactory.createTitledBorder("What's happening?"));
        imageUrlField = new JTextField();
        imageUrlField.setBorder(BorderFactory.createTitledBorder("Image URL (Optional)"));
        inputs.add(messageField);
        inputs.add(imageUrlField);
        
        // Settings
        JPanel settingsPanel = new JPanel(new GridLayout(2, 2, 5, 0));
        settingsPanel.setOpaque(false);
        
        // --- MAKE CHECKBOXES TRANSPARENT ---
        anonPostCheck = new JCheckBox("Post Anonymously");
        anonPostCheck.setOpaque(false);
        anonPostCheck.setFocusPainted(false);
        
        followersOnlyPostCheck = new JCheckBox("Followers Only");
        followersOnlyPostCheck.setOpaque(false);
        followersOnlyPostCheck.setFocusPainted(false);
        
        allowAnonCommentsCheck = new JCheckBox("Allow Anon Comments");
        allowAnonCommentsCheck.setOpaque(false);
        allowAnonCommentsCheck.setFocusPainted(false);
        
        followersOnlyCommentsCheck = new JCheckBox("Followers Only Comments");
        followersOnlyCommentsCheck.setOpaque(false);
        followersOnlyCommentsCheck.setFocusPainted(false);
        
        allowAnonCommentsCheck.setSelected(true);
        
        anonPostCheck.addActionListener(e -> {
            if(anonPostCheck.isSelected()) { allowAnonCommentsCheck.setSelected(true); allowAnonCommentsCheck.setEnabled(false); }
            else allowAnonCommentsCheck.setEnabled(true);
        });
        
        settingsPanel.add(anonPostCheck); settingsPanel.add(followersOnlyPostCheck);
        settingsPanel.add(allowAnonCommentsCheck); settingsPanel.add(followersOnlyCommentsCheck);
        
        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(settingsPanel, BorderLayout.CENTER);
        
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setOpaque(false);
        cancelPostButton = new JButton("Cancel");
        cancelPostButton.setBorderPainted(false);
        cancelPostButton.setContentAreaFilled(false);
        cancelPostButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelPostButton.addActionListener(e -> hidePostForm());
        
        submitPostButton = new ModernButton("Post", ACCENT, ACCENT.brighter(), Color.WHITE);
        submitPostButton.addActionListener(e -> addPost());
        
        btnWrap.add(cancelPostButton);
        btnWrap.add(submitPostButton);
        footer.add(btnWrap, BorderLayout.SOUTH);
        
        postFormPanel.add(inputs, BorderLayout.CENTER);
        postFormPanel.add(footer, BorderLayout.SOUTH);
    }
    
    private void showPostForm() {
        createContainer.remove(triggerPostButton);
        createContainer.add(postFormPanel, BorderLayout.CENTER);
        createContainer.revalidate(); createContainer.repaint();
    }

    private void hidePostForm() {
        messageField.setText(""); imageUrlField.setText("");
        anonPostCheck.setSelected(false); followersOnlyPostCheck.setSelected(false);
        createContainer.remove(postFormPanel);
        createContainer.add(triggerPostButton, BorderLayout.CENTER);
        createContainer.revalidate(); createContainer.repaint();
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        themeBtn.setText(isDarkMode ? "☀" : "🌙");
        applyTheme();
        refreshPosts(); refreshTrending();
    }

    private void applyTheme() {
        Color bg = isDarkMode ? DARK_BG : LIGHT_BG;
        Color surface = isDarkMode ? DARK_SURFACE : LIGHT_SURFACE;
        Color text = isDarkMode ? DARK_TEXT : LIGHT_TEXT;
        Color inputBg = isDarkMode ? DARK_INPUT_BG : LIGHT_INPUT_BG;
        
        getContentPane().setBackground(bg);
        topPanel.setBackground(bg);
        header.setBackground(bg);
        subHeader.setBackground(bg);
        feedPanel.setBackground(bg);
        createContainer.setBackground(bg);
        trendingPanel.setBackground(bg);
        
        if (postFormPanel instanceof RoundedPanel) {
            ((RoundedPanel)postFormPanel).setPanelBackgroundColor(surface);
        }
        
        titleLabel.setForeground(text);
        
        Color iconColor = isDarkMode ? Color.WHITE : Color.BLACK;
        themeBtn.setForeground(iconColor);
        profileBtn.setForeground(iconColor);
        notifBtn.setForeground(iconColor);
        
        searchBarContainer.setPanelBackgroundColor(inputBg);
        searchIconLabel.setForeground(Color.GRAY);
        searchField.setForeground(text);
        searchField.setCaretColor(text);
        
        styleInput(messageField, inputBg, text);
        styleInput(imageUrlField, inputBg, text);
        
        Color checkColor = text;
        anonPostCheck.setForeground(checkColor);
        followersOnlyPostCheck.setForeground(checkColor);
        allowAnonCommentsCheck.setForeground(checkColor);
        followersOnlyCommentsCheck.setForeground(checkColor);
        cancelPostButton.setForeground(text);
        
        feedToggle.setBackground(surface);
        feedToggle.setForeground(text);
        
        Color borderColor = isDarkMode ? Color.GRAY : Color.LIGHT_GRAY;
        messageField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(borderColor), "What's happening?", 
            0, 0, new Font("Segoe UI", Font.PLAIN, 12), text));
            
        imageUrlField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(borderColor), "Image URL (Optional)", 
            0, 0, new Font("Segoe UI", Font.PLAIN, 12), text));
            
        if (postFormPanel != null) postFormPanel.repaint();
        searchBarContainer.repaint();
        
        for(Component c : trendingPanel.getComponents()) {
            if(c instanceof JLabel) ((JLabel)c).setForeground(text);
        }
    }
    
    private void styleInput(JTextField field, Color bg, Color fg) {
        field.setBackground(bg);
        field.setForeground(fg);
        field.setCaretColor(fg);
    }
    
    private void styleIconBtn(JButton btn) {
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- LOGIC ---
    private void addPost() {
        String txt = messageField.getText();
        String img = imageUrlField.getText();
        if(txt.isEmpty() && img.isEmpty()) return;
        
        boolean isAnon = anonPostCheck.isSelected();
        String author = isAnon ? "Anonymous" : currentUser.getUsername();
        String avatar = isAnon ? "" : currentUser.getAvatarUrl();
        
        Post p = new Post(author, avatar, txt, img, followersOnlyPostCheck.isSelected(), allowAnonCommentsCheck.isSelected(), followersOnlyCommentsCheck.isSelected());
        posts.add(p);
        dbManager.savePost(p);
        messageField.setText(""); imageUrlField.setText("");
        
        refreshPosts();
        refreshTrending();
        hidePostForm();
    }
    
    public void refreshPosts() {
        feedPanel.removeAll();
        String term = searchField.getText().toLowerCase();
        ArrayList<String> following = currentUser.getFollowing();
        boolean showFollowingOnly = feedToggle.isSelected();
        
        for(int i = posts.size()-1; i >= 0; i--) {
            Post p = posts.get(i);
            boolean isMine = p.getAuthor().equals(currentUser.getUsername());
            boolean doIFollow = following.contains(p.getAuthor());
            
            if(showFollowingOnly && !isMine && !doIFollow) continue;
            if(!p.getContent().toLowerCase().contains(term) && !p.getAuthor().toLowerCase().contains(term)) continue;
            
            if (p.isFollowersOnly() && !p.getAuthor().equals("Anonymous")) {
                if (!isMine && !doIFollow) continue;
            }

            PostPanel panel = new PostPanel(p, this, currentUser, userManager, isDarkMode);
            feedPanel.add(panel);
            feedPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
        }
        feedPanel.revalidate(); feedPanel.repaint();
        feedPanel.add(Box.createVerticalGlue());
    }
    
    public void refreshTrending() {
        trendingPanel.removeAll();
        JLabel title = new JLabel("Trending");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        trendingPanel.add(title);
        trendingPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        Map<String, Integer> counts = new HashMap<>();
        for(Post p : posts) for(String tag : p.getHashtags()) counts.put(tag, counts.getOrDefault(tag, 0) + 1);
        
        counts.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).limit(5).forEach(entry -> {
            JButton tagBtn = new JButton(entry.getKey());
            tagBtn.setBorderPainted(false); tagBtn.setContentAreaFilled(false);
            tagBtn.setForeground(ACCENT); tagBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            tagBtn.setHorizontalAlignment(SwingConstants.LEFT);
            tagBtn.addActionListener(e -> setSearchText(entry.getKey()));
            trendingPanel.add(tagBtn);
        });
        trendingPanel.revalidate(); trendingPanel.repaint();
    }
    
    public void sendNotification(String recipient, String message) {
        if (!recipient.equals(currentUser.getUsername())) { 
            Notification n = new Notification(currentUser.getUsername(), message);
            dbManager.sendNotification(recipient, n);
        }
    }
    
    public void savePosts() { for(Post p : posts) dbManager.savePost(p); }
    
    public void repost(Post original) {
        String repostContent = "Shared from @" + original.getAuthor() + ":\n\n\"" + original.getContent() + "\"";
        Post p = new Post(currentUser.getUsername(), currentUser.getAvatarUrl(), repostContent, original.getImageUrl(), false, true, false);
        p.setAsRepost(original.getAuthor());
        posts.add(p);
        dbManager.savePost(p);
        original.addRepost();
        dbManager.savePost(original);
        sendNotification(original.getAuthor(), currentUser.getUsername() + " shared your post!");
        refreshPosts();
    }
    
    public void deletePost(Post p) { posts.remove(p); dbManager.deletePost(p); refreshPosts(); refreshTrending(); }
    public void setSearchText(String t) { searchField.setText(t); }
    public boolean shouldLogout() { return shouldLogout; }
    
    private void openProfileEditor() {
        ProfileEditDialog dialog = new ProfileEditDialog(this, currentUser, userManager);
        ProfileEditDialog.DialogResult result = dialog.showDialog();
        if(result == ProfileEditDialog.DialogResult.LOGOUT) { new File("login.conf").delete(); savePosts(); shouldLogout = true; dispose(); }
        else if(result == ProfileEditDialog.DialogResult.SWITCH) { new File("login.conf").delete(); savePosts(); shouldLogout = false; dispose(); }
        else if(result == ProfileEditDialog.DialogResult.SAVE) { refreshPosts(); }
    }
    
    public void showProfileFor(String username) {
        ProfileDialog d = new ProfileDialog(this, username, currentUser, userManager, posts, this);
        d.setVisible(true);
        refreshPosts();
    }
    
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        while (true) {
            DatabaseManager dbManager = new DatabaseManager();
            ArrayList<User> userList = dbManager.loadUsers();
            UserManager userManager = new UserManager(userList, dbManager);
            User user = null;
            File loginFile = new File("login.conf");
            if (loginFile.exists()) {
                try (FileInputStream in = new FileInputStream(loginFile)) {
                    Properties props = new Properties(); props.load(in);
                    user = userManager.login(props.getProperty("username"), props.getProperty("password"));
                } catch (Exception e) {}
            }
            if (user == null) {
                LoginScreen loginScreen = new LoginScreen(null, userManager);
                user = loginScreen.showLoginDialog();
            }
            if (user != null) {
                SocialFeedGUI gui = new SocialFeedGUI(null, user, userManager, dbManager);
                gui.setVisible(true);
                if (gui.shouldLogout()) break;
            } else { break; }
        }
        System.exit(0);
    }
}