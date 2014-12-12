/* Costs.java
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

import java.util.List;

/**
 * Information needed for cost calculations
 */
public class Costs {

    private double[] amounts;

    private int[] limits;

    private double deprecation;

    public Costs (String deprecationString, List<String> amountStrings, List<String> limitStrings) {
        this.deprecation = Double.parseDouble(deprecationString);
        amounts = new double[amountStrings.size()];
        limits = new int[amounts.length - 1];

        for (int i = 0; i < amounts.length; i++) {
            amounts[i] = Double.parseDouble(amountStrings.get(i));
        }
        for (int i = 0; i < limits.length; i++) {
            limits[i] = Integer.parseInt(limitStrings.get(i));
        }
    }

    public int getLevels () {
        return amounts.length;
    }

    public double getCost(int level) {
        return amounts[level];
    }

    public double getDeprecation () {
        return deprecation;
    }

    public int getLimit(int level) {
        return limits[level];
    }


}
