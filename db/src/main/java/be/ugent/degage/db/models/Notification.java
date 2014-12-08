package be.ugent.degage.db.models;

import java.time.Instant;

/**
 * Created by Stefaan Vermassen on 15/03/14.
 */
public class Notification {

    private int id;
    private UserHeader user;
    private boolean read;
    private String subject;
    private String body;
    private Instant timestamp;

    public Notification(int id, UserHeader user, boolean read, String subject, String body, Instant timestamp){
        this.id = id;
        this.user = user;
        this.read = read;
        this.subject = subject;
        this.body = body;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public UserHeader getUser() {
        return user;
    }

    public boolean getRead() {
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

}
