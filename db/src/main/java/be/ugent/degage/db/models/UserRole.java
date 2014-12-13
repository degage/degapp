/* UserRole.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.models;

import java.util.EnumSet;
import java.util.Set;

/**
 * User roles recognized by the system.
 *
 * Note: by convention the value returned by {@link #toString} is used to display a description of the
 * enum value in the web interface.
 */
public enum UserRole {

    // at most 36 values are allowed for toString(Set) to work properly

    USER("Gebruiker"),
    SUPER_USER("Superuser"),
    CAR_OWNER("Auto-eigenaar"),
    CAR_USER("Autolener"),
    INFOSESSION_ADMIN("Infosessiebeheerder"),
    MAIL_ADMIN("E-mailbeheerder"),
    PROFILE_ADMIN("Profielbeheerder"),
    RESERVATION_ADMIN("Reservatiebeheerder"),
    CAR_ADMIN("Autobeheerder");

    // Enum implementation
    private String description;

    private UserRole(String description) {
        this.description = description;
    }

    public String toString() {
        return description;
    }

    /**
     * Converts a set of roles to a short string, e.g., to be stored in a session
     */
    public static String toString(Set<UserRole> set) {
        if (set == null) {
            return null;
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
            return null;
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
