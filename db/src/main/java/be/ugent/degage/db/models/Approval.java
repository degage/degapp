package be.ugent.degage.db.models;

import org.joda.time.DateTime;

/**
 * Created by Cedric on 3/30/2014.
 */
public class Approval {

    public enum ApprovalStatus {
        PENDING,
        ACCEPTED,
        DENIED
    }

    private int id;
    private UserHeader user;
    private UserHeader admin;
    private DateTime submitted;
    private DateTime reviewed;
    private InfoSession session;
    private ApprovalStatus status;
    private String userMessage;
    private String adminMessage;

    public Approval(int id, UserHeader user, UserHeader admin, DateTime submitted, DateTime reviewed, InfoSession session, ApprovalStatus status, String userMessage, String adminMessage) {
        this.id = id;
        this.user = user;
        this.admin = admin;
        this.submitted = submitted;
        this.reviewed = reviewed;
        this.session = session;
        this.status = status;
        this.userMessage = userMessage;
        this.adminMessage = adminMessage;
    }

    public int getId(){
        return id;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAdminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }


    public UserHeader getUser() {
        return user;
    }

    public void setUser(UserHeader user) {
        this.user = user;
    }

    public UserHeader getAdmin() {
        return admin;
    }

    public void setAdmin(UserHeader admin) {
        this.admin = admin;
    }

    public DateTime getSubmitted() {
        return submitted;
    }

    public void setSubmitted(DateTime submitted) {
        this.submitted = submitted;
    }

    public DateTime getReviewed() {
        return reviewed;
    }

    public void setReviewed(DateTime reviewed) {
        this.reviewed = reviewed;
    }

    public InfoSession getSession() {
        return session;
    }

    public void setSession(InfoSession session) {
        this.session = session;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
}
