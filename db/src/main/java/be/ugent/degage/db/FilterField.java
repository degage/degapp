/* FilterField.java
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

package be.ugent.degage.db;

import com.google.common.base.Strings;

/**
 * Fields we can filter on the DAOs
 */
public enum FilterField {

    NAME(true), BRAND(true), SEATS(true), AUTOMATIC(true), GPS(true), HOOK(true), CAR_ID(true), FUEL(true), CAR_ACTIVE(true),
    CAR_COST_DATE(true),
    REFUEL_USER_ID(true), REFUEL_OWNER_ID(true), REFUEL_CAR_ID(true),
    DAMAGE_FINISHED(true), DAMAGE_USER_ID(true), DAMAGE_CAR_ID(true), DAMAGE_OWNER_ID(true),
    USER_NAME(false), USER_FIRSTNAME(false), USER_LASTNAME(false), USER_ID(true),
    ZIPCODE(false),
    INFOSESSION_TYPE(false),
    RESERVATION_USER_OR_OWNER_ID(true), RESERVATION_CAR_ID(true),
    STATUS(true),
    MESSAGE_RECEIVER_ID(true), MESSAGE_SENDER_ID(true),
    NOTIFICATION_READ(true),
    INSTANT(true),
    FROM(true), UNTIL(true),
    YEAR(true),
    CONTRACT_ID(true),
    TYPE(true);

    boolean exactValue;

    private FilterField(boolean exactValue) {
        this.exactValue = exactValue;
    }

    public boolean useExactValue() {
        return exactValue;
    }

    /**
     *
     * @param string The string corresponding to the FilterField
     * @return The corresponding FilterField (or the given default value if there is none)
     */
    public static FilterField stringToField(String string, FilterField defaultValue) {
        if (Strings.isNullOrEmpty(string)) {
            return defaultValue;
        }
        try {
            return FilterField.valueOf(string.toUpperCase().trim());
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }
}
