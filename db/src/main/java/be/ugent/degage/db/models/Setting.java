package be.ugent.degage.db.models;

import java.time.Instant;

/**
 * Represents a system variable stored in the database. The same variable can be stored several times, but with a different
 * date for which it is valid.,
 */
public class Setting {

    private String name;
    private String value;
    private Instant afterDate;

    public Setting(String name, String value, Instant afterDate) {
        this.name = name;
        this.value = value;
        this.afterDate = afterDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getAfterDate() {
        return afterDate;
    }

    public void setAfterDate(Instant afterDate) {
        this.afterDate = afterDate;
    }
}
