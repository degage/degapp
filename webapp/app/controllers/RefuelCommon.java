/* RefuelCommon.java
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
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package controllers;

import be.ugent.degage.db.models.RefuelExtended;
import be.ugent.degage.db.models.ReservationHeader;
import be.ugent.degage.db.models.UserRole;
import data.EurocentAmount;
import db.CurrentUser;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.Collections;
import java.util.List;

/**
 * Common superclass of refuel related controllers
 */
public class RefuelCommon extends WFCommon {


    public static class RefuelData {

        @Constraints.Required
        public EurocentAmount amount;

        public String picture; // only used to enable field error messages

        @Constraints.Required
        public String fuelAmount;

        @Constraints.Required
        @Constraints.Min(value = 1, message = "Ongeldige kilometerstand")
        public int km;

        public static RefuelData with(EurocentAmount amount, String fuelAmount, int km) {
            RefuelData data = new RefuelData();
            data.amount = amount;
            data.fuelAmount = fuelAmount;
            data.km = km;
            return data;
        }

        public List<ValidationError> validate() {
            if (amount.getValue() <= 0) {
                return Collections.singletonList(new ValidationError("amount", "Bedrag moet groter zijn dan 0"));
            } else {
                return null;
            }
        }

        public static RefuelData EMPTY;

        static {
            EMPTY = new RefuelData();
            EMPTY.amount = new EurocentAmount();
        }
    }

    /**
     * Checks whether current user is authorized to manage refuels for the given reservation
     *
     * @param ownerFlow indicates whether this check is part of an 'owner' workflow or not
     * @return
     */
    protected static boolean isAuthorized(ReservationHeader reservation, boolean ownerFlow) {
        if (ownerFlow)
            return isOwnerOrAdmin(reservation);
        else
            return isDriverOrOwnerOrAdmin(reservation);
    }

    /**
     * Checks whether the current uses is owner or administrator for the reservation related to
     * the given refuel
     */
    protected static boolean isOwnerOrAdmin(RefuelExtended refuel) {
        return CurrentUser.hasRole(UserRole.RESERVATION_ADMIN) || CurrentUser.is(refuel.getOwnerId());
    }

    /**
     * Checks whether the current uses is driver, owner or administrator for the reservation related to
     * the given refuel
     */
    protected static boolean isDriverOrOwnerOrAdmin(RefuelExtended refuel) {
        return CurrentUser.hasRole(UserRole.RESERVATION_ADMIN) ||
                CurrentUser.is(refuel.getOwnerId()) || CurrentUser.is(refuel.getDriverId());
    }
}
