package be.ugent.degage.db.models;

import org.joda.time.DateTime;

/**
 * Created by Stefaan Vermassen on 15/03/14.
 */
public class Notification {

    private int id;
    private UserHeader user;
    private boolean read;
    private String subject;
    private String body;
    private DateTime timestamp;

    public Notification(int id, UserHeader user, boolean read, String subject, String body, DateTime timestamp){
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

    public DateTime getTimestamp() {
        return timestamp;
    }

}
