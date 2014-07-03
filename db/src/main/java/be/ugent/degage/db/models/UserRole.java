package be.ugent.degage.db.models;

/**
 * Created by Cedric on 2/16/14.
 */
public enum UserRole {
    USER("gebruiker"),
    SUPER_USER("super user"),
    CAR_OWNER("auto eigenaar"),
    CAR_USER("autolener"),
    INFOSESSION_ADMIN("infosessie beheerder"),
    MAIL_ADMIN("mail beheerder"),
    PROFILE_ADMIN("profiel beheerder"),
    RESERVATION_ADMIN("reservatie beheerder"),
    CAR_ADMIN("auto beheerder");

    // Enum implementation
    private String description;

    private UserRole(String description) {
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
