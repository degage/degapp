package be.ugent.degage.db.models;

/**
 * Represent a (static) message that is displayed on a certain web page, e.g., on the login page,
 * or on the main dashboard page.
 */
public class Announcement {

    private String key;

    private String description;

    private String html;

    private String markdown;

    public Announcement(String key, String description, String html, String markdown) {
        this.key = key;
        this.description = description;
        this.html = html;
        this.markdown = markdown;
    }

    /**
     * Key by which this message is known.
     */
    public String getKey() {
        return key;
    }

    /**
     * Short description of where this message is used
     */
    public String getDescription() {
        return description;
    }

    /**
     * HTML version of this message. (Read only)
     */
    public String getHtml() {
        return html;
    }

    /**
     * Markdown version of this message. Can be edited by the user.
     */
    public String getMarkdown() {
        return markdown;
    }
}
