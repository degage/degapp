package be.ugent.degage.db.models;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 01/03/14.
 */
public class EmailTemplate {

    private int id;
    private String title;
    private String subject;
    private String body;
    private List<String> tags;
    private boolean sendMail;
    private boolean sendMailChangeable;

    public EmailTemplate(int id, String title, String body, List<String> tags, String subject, boolean sendMail, boolean sendMailChangeable){
        this.id = id;
        this.title = title;
        this.body = body;
        this.tags = tags;
        this.subject = subject;
        this.sendMail = sendMail;
        this.sendMailChangeable = sendMailChangeable;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public boolean getSendMail() {
        return sendMail;
    }

    public boolean getSendMailChangeable() {
        return sendMailChangeable;
    }

    public String getBody() {
        return body;
    }

    public List<String> getUsableTags(){
        return tags;
    }

}
