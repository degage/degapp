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

/**
 * Created by Benjamin on 23/03/2014.
 */

/**
 * Fields we can filter on the DAOs
 */
public enum FilterField {

    CAR_NAME(false), CAR_BRAND(false), CAR_SEATS(true), CAR_AUTOMATIC(true), CAR_GPS(true), CAR_HOOK(true), CAR_ID(true), CAR_FUEL(true), CAR_ACTIVE(true),
    CAR_COST_STATUS(false), CAR_COST_DATE(true),
    REFUEL_USER_ID(true), REFUEL_OWNER_ID(true), REFUEL_CAR_ID(true), REFUEL_NOT_STATUS(true),
    DAMAGE_FINISHED(true), DAMAGE_USER_ID(true), DAMAGE_CAR_ID(true), DAMAGE_OWNER_ID(true),
    USER_NAME(false), USER_FIRSTNAME(false), USER_LASTNAME(false), USER_ID(true),
    ZIPCODE(false),
    INFOSESSION_DATE(true), INFOSESSION_TYPE(false),
    RESERVATION_USER_OR_OWNER_ID(true), RESERVATION_CAR_ID(true), RESERVATION_STATUS(true),
    MESSAGE_RECEIVER_ID(true), MESSAGE_SENDER_ID(true),
    NOTIFICATION_READ(true),
    TEMPLATE_NAME(false),
    RECEIPT_DATE(true),
    FROM(true), UNTIL(true);

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
     * @return The corresponding FilterField (or null if there is none)
     */
    public static FilterField stringToField(String string) {
        FilterField field = null;
        switch(string) {
            case "car_id":
                field = CAR_ID;
                break;
            case "name":
                field = CAR_NAME;
                break;
            case "brand":
                field = CAR_BRAND;
                break;
            case "zipcode":
                field = ZIPCODE;
                break;
            case "seats":
                field = CAR_SEATS;
                break;
            case "automatic":
                field = CAR_AUTOMATIC;
                break;
            case "gps":
                field = CAR_GPS;
                break;
            case "hook":
                field = CAR_HOOK;
                break;
            case "fuel":
                field = CAR_FUEL;
                break;
            case "from":
                field = FROM;
                break;
            case "until":
                field = UNTIL;
                break;
            case "infosession_date" :
                field = INFOSESSION_DATE;
                break;
            case "infosession_type" :
                field = INFOSESSION_TYPE;
                break;
            case "user_name":
                field = USER_NAME;
                break;
            case "user_firstname":
                field = USER_FIRSTNAME;
                break;
            case "user_lastname":
                field = USER_LASTNAME;
                break;
            case "message_receiver_id":
                field = MESSAGE_RECEIVER_ID;
                break;
            case "message_sender_id":
                field = MESSAGE_SENDER_ID;
                break;
            case "notification_read":
                field = NOTIFICATION_READ;
                break;
            case "reservation_user_or_owner_id":
                field = RESERVATION_USER_OR_OWNER_ID;
                break;
            case "reservation_car_id":
                field = RESERVATION_CAR_ID;
                break;
            case "status":
                field = RESERVATION_STATUS;
                break;
            case "car_cost_status":
                field = CAR_COST_STATUS;
                break;
            case "car_cost_date":
                field = CAR_COST_DATE;
                break;
            case "damage_finished":
                field = DAMAGE_FINISHED;
                break;
            case "damage_user_id":
                field = DAMAGE_USER_ID;
                break;
            case "damage_owner_id":
                field = DAMAGE_OWNER_ID;
                break;
            case "damage_car_id":
                field = DAMAGE_CAR_ID;
                break;
            case "refuel_user_id":
                field = REFUEL_USER_ID;
                break;
            case "refuel_owner_id":
                field = REFUEL_OWNER_ID;
                break;
            case "refuel_car_id":
                field = REFUEL_CAR_ID;
                break;
            case "template_name":
                field = TEMPLATE_NAME;
                break;
        }
        return field;
    }
}
