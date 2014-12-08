package be.ugent.degage.db.models;

import java.time.Instant;

/**
 * Created by Stefaan Vermassen on 04/05/14.
 */
public class DamageLog {

    private String description;
    private Instant created;

    public DamageLog(String description, Instant created) {
        this.description = description;
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }
}
