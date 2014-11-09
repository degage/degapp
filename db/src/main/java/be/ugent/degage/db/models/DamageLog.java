package be.ugent.degage.db.models;

import org.joda.time.DateTime;

/**
 * Created by Stefaan Vermassen on 04/05/14.
 */
public class DamageLog {

    private String description;
    private DateTime created;

    public DamageLog(String description, DateTime created) {
        this.description = description;
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }
}
