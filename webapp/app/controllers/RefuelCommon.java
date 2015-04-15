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

import data.EurocentAmount;
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
        @Constraints.Min(value=1, message="Ongeldige kilometerstand")
        public int km;

        public RefuelData populate(EurocentAmount amount, String fuelAmount, int km) {
            this.amount = amount;
            this.fuelAmount = fuelAmount;
            this.km = km;
            return this;
        }

        public List<ValidationError> validate () {
            if (amount.getValue() <= 0) {
                return Collections.singletonList(new ValidationError("amount", "Bedrag moet groter zijn dan 0"));
            } else {
                return null;
            }
        }
    }
}
