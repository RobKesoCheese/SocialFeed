package crud;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {

    private String dbUrl = "jdbc:sqlite:socialfeed.db";

    public DatabaseManager() {
        createTables();
    }

    private void createTables() {
        String userTableSql = "CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, data BLOB)";
        String postTableSql = "CREATE TABLE IF NOT EXISTS posts (id INTEGER PRIMARY KEY, data BLOB)";
        // ---  Notifications Table ---
        String notifTableSql = "CREATE TABLE IF NOT EXISTS notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, recipient TEXT, data BLOB)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(userTableSql);
            stmt.execute(postTableSql);
            stmt.execute(notifTableSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- User & Post Methods (Keep existing ones) ---
    public void saveUser(User user) {
        String sql = "INSERT OR REPLACE INTO users(username, data) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername()); pstmt.setBytes(2, serialize(user)); pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT data FROM users")) {
            while (rs.next()) users.add((User) deserialize(rs.getBytes("data")));
        } catch (Exception e) { e.printStackTrace(); }
        return users;
    }

    public void savePost(Post post) {
        String sql = "INSERT OR REPLACE INTO posts(id, data) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, post.getId()); pstmt.setBytes(2, serialize(post)); pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void deletePost(Post post) {
        try (Connection conn = DriverManager.getConnection(dbUrl); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM posts WHERE id = ?")) {
            pstmt.setInt(1, post.getId()); pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public ArrayList<Post> loadPosts() {
        ArrayList<Post> posts = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT data FROM posts")) {
            while (rs.next()) posts.add((Post) deserialize(rs.getBytes("data")));
        } catch (Exception e) { e.printStackTrace(); }
        return posts;
    }

    // ---  Notification Methods ---
    
    public void sendNotification(String recipient, Notification notif) {
        String sql = "INSERT INTO notifications(recipient, data) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, recipient);
            pstmt.setBytes(2, serialize(notif));
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public ArrayList<Notification> getNotifications(String username) {
        ArrayList<Notification> list = new ArrayList<>();
        String sql = "SELECT data FROM notifications WHERE recipient = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add((Notification) deserialize(rs.getBytes("data")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        // Reverse to show newest first
        java.util.Collections.reverse(list);
        return list;
    }

    // --- Helpers ---
    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream so = new ObjectOutputStream(bo);
        so.writeObject(obj); so.flush(); return bo.toByteArray();
    }

    private Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        ObjectInputStream si = new ObjectInputStream(bi);
        return si.readObject();
    }
}