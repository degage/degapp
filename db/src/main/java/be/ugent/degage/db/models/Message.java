package be.ugent.degage.db.models;

import org.joda.time.DateTime;

/**
 * Created by stefaan on 22/03/14.
 */
public class Message {

    private int id;
    private User sender;
    private User receiver;
    private boolean read;
    private String subject;
    private String body;
    private DateTime timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Message(int id, User sender, User receiver, boolean read, String subject, String body, DateTime timestamp){
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.read = read;
        this.subject = subject;
        this.body = body;
        this.timestamp = timestamp;
    }
}
