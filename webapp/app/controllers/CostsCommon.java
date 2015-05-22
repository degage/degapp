/* CostsCommon.java
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

import be.ugent.degage.db.models.CarCost;
import be.ugent.degage.db.models.CarHeaderShort;
import be.ugent.degage.db.models.UserRole;
import data.EurocentAmount;
import db.CurrentUser;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Common super class for cost related controllers
 */
public class CostsCommon extends Controller {

    protected static boolean isOwnerOrAdmin(CarHeaderShort car) {
        return CurrentUser.is(car.getOwnerId()) || CurrentUser.hasRole(UserRole.CAR_ADMIN);
    }

    protected static boolean isOwnerOrAdmin(CarCost cost) {
        return CurrentUser.is(cost.getOwnerId()) || CurrentUser.hasRole(UserRole.CAR_ADMIN);
    }

    public static class CostData {

        @Constraints.Required
        public int category;

        @Constraints.Required
        public String description;

        @Constraints.Required
        public EurocentAmount amount;

        public int mileage;

        @Constraints.Required
        public LocalDate time;

        public LocalDate start;  // not always used

        public int spread; // not always used

        public List<ValidationError> validate() {
            List<ValidationError> errors = new ArrayList<>();
            if (amount.getValue() <= 0) {
                errors.add(new ValidationError("amount", "Bedrag moet groter zijn dan 0"));
            }
            if (spread < 0) {
                errors.add(new ValidationError("spread", "Spreiding mag niet negatief zijn"));
            }
            if (errors.isEmpty()) {
                return null;
            } else {
                return errors;
            }
        }

        public CostData() {
            amount = new EurocentAmount();
            spread = 12;
        }

        public CostData(CarCost cost) {
            category = cost.getCategory().getId();
            description = cost.getDescription();
            amount = new EurocentAmount(cost.getAmount());
            mileage = cost.getKm();
            time = cost.getDate();
            spread = cost.getSpread();
        }

        public static CostData EMPTY = new CostData();
    }
}
