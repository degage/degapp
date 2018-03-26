package be.ugent.degage.db.models;

/**
 * The various user statuses. (Database default: REGISTERED)
 *
 * Note: by convention the value returned by {@link #toString} is used to display a description of the
 * enum value in the web interface.
 */
public enum CarStatus {
    REGISTERED ("Geregistreerd"),
    FULL ("Volwaardig lid"),
    REFUSED("Geweigerd");

    private String description;

    private CarStatus(String description) {
        this.description = description;
    }

    public String toString(){
        return description;
    }
}
