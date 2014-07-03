/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

/**
 *
 * @author Laurent
 */
public enum ReservationStatus {
    REQUEST("Wachten op goedkeuring"),
    ACCEPTED("Aanvraag goedgekeurd"),
    REFUSED("Aanvraag geweigerd"),
    CANCELLED("Aanvraag geannuleerd"),
    REQUEST_DETAILS("Wachten op informatie rit"),
    DETAILS_PROVIDED("Wachten op goedkeuring informatie"),
    FINISHED("Rit beëindigd");

    // Enum definition
    private String description;

    private ReservationStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
