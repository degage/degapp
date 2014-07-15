package be.ugent.degage.db.models;

import java.util.EnumSet;
import java.util.Set;

/**
 * User roles recognized by the system.
 */
public enum UserRole {

    // at most 36 values are allowed for toString to work properly

    USER("gebruiker"),
    SUPER_USER("superuser"),
    CAR_OWNER("auto-eigenaar"),
    CAR_USER("autolener"),
    INFOSESSION_ADMIN("infosessiebeheerder"),
    MAIL_ADMIN("e-mailbeheerder"),
    PROFILE_ADMIN("profielbeheerder"),
    RESERVATION_ADMIN("reservatiebeheerder"),
    CAR_ADMIN("autobeheerder");

    // Enum implementation
    private String description;

    private UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Converts a set of roles to a short string, e.g., to be stored in a session
     */
    public static String toString(Set<UserRole> set) {
        if (set == null) {
            return  null;
        } else {
            StringBuilder builder = new StringBuilder();
            for (UserRole userRole : set) {
                int value = userRole.ordinal();
                builder.append(value <= 9 ? (char) ('0' + value) : (char) ('A' - 10 + value));
            }
            return builder.toString();
        }
    }

    /**
     * Converts a string to a set of roles. Converse of {@link #toString(java.util.Set)}
     */
    public static Set<UserRole> fromString(String rolesString) {
        if (rolesString == null) {
            return  null;
        } else {
            EnumSet<UserRole> set = EnumSet.noneOf(UserRole.class);
            for (int i = 0; i < rolesString.length(); i++) {
                char ch = rolesString.charAt(i);
                int value = ch >= '0' && ch <= '9' ? ch - '0' : ch + 10 - 'A';
                set.add(UserRole.values()[value]);
            }
            return set;
        }
    }
}
