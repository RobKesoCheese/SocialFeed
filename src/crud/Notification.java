package crud;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("serial")
public class Notification implements Serializable {
    private String message;
    private String fromUser;
    private String timestamp;
    private boolean isRead;

    public Notification(String fromUser, String message) {
        this.fromUser = fromUser;
        this.message = message;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        this.isRead = false;
    }

    public String getMessage() { return message; }
    public String getFromUser() { return fromUser; }
    public String getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public void markRead() { this.isRead = true; }
    
    @Override
    public String toString() {
        return "<html><b>@" + fromUser + "</b> " + message + " <br><font size='2' color='gray'>" + timestamp + "</font></html>";
    }
}