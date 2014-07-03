package be.ugent.degage.db.models;

import org.joda.time.DateTime;

/**
 * Created by Stefaan Vermassen on 04/05/14.
 */
public class DamageLog {

    private Integer id;
    private Damage damage;
    private String description;
    private DateTime created;

    public DamageLog(Integer id, Damage damage, String description, DateTime created) {
        this.id = id;
        this.damage = damage;
        this.description = description;
        this.created = created;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Damage getDamage() {
        return damage;
    }

    public void setDamage(Damage damage) {
        this.damage = damage;
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
