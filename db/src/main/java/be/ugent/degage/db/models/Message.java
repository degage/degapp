package be.ugent.degage.db.models;


import java.time.Instant;

/**
 * Created by stefaan on 22/03/14.
 */
public class Message {

    private int id;

    // sender or receiver, depending on context
    private UserHeader user;

    private boolean read;
    private String subject;
    private String body;
    private Instant timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserHeader getUser() {
        return user;
    }

    public void setUser(UserHeader user) {
        this.user = user;
    }

    public boolean isRead() {
        return read;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Message(int id, boolean read, UserHeader user, String subject, String body, Instant timestamp){
        this.id = id;
        this.user = user;
        this.read = read;
        this.subject = subject;
        this.body = body;
        this.timestamp = timestamp;
    }
}
