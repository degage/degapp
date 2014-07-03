package be.ugent.degage.db.models;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public enum RefuelStatus {
    CREATED("Info verstrekken"),
    REQUEST("Wachten op goedkeuring"),
    ACCEPTED("Aanvraag goedgekeurd"),
    REFUSED("Aanvraag geweigerd");

    // Enum definition
    private String description;

    private RefuelStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
