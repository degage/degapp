package be.ugent.degage.db.models;

/**
 * Created by stefaan on 16/04/14.
 */
public enum CarCostStatus {
    REQUEST("Wachten op goedkeuring"),
    ACCEPTED("Aanvraag goedgekeurd"),
    REFUSED("Aanvraag geweigerd");

    // Enum definition
    private String description;

    private CarCostStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}