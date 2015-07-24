package be.ugent.degage.db.dao;

import be.ugent.degage.db.models.Announcement;

/**
 * Processes announcements
 */
public interface AnnouncementDAO {

    /**
     * Get the announcement for the given key. Does not contain
     * the markdown.
     */
    public Announcement getAnnouncement (String key);

    /**
     * Get the announcement for the given key, markdown included.
     */
    public Announcement getAnnouncementFull (String key);

    /**
     * Update HTML and Markdown for the given key
     */
    public void updateAnnouncement (String key, String html, String markdown);

    /**
     * Get the list of all announcements. Does not contain the markdown.
     */
    public Iterable<Announcement> listAnnouncements ();

}
