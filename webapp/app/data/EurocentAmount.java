/* EurocentAmount.java
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

package data;

import java.util.regex.Pattern;

/**
 * Contains an amount of eurocents. Provides parsing and formatting methods
 */
public class EurocentAmount {

    private int value;

    /**
     * Return the amount of eurocents
     */
    public int getValue() {
        return value;
    }

    public EurocentAmount() {
        this(0);
    }

    public EurocentAmount (int value) {
        this.value = value;
    }

    public EurocentAmount (int euro, int cent) {
        if (cent >= 0 && cent < 100) {
            if (euro >= 0)
                value = 100*euro + cent;
            else
                value = 100*euro - cent;
        } else {
            throw new IllegalArgumentException("Cent part must be in the range 0..99: " + cent);
        }
    }

    /**
     * Output in the form 15,20 0,03 ...
     */
    public String toString() {
        return toString(value);
    }

    public static String toString (int eurocents) {
        if (eurocents >= 0) {
            return String.format("%d,%02d", eurocents / 100, eurocents % 100);
        } else {

            return String.format("-%d,%02d", (-eurocents) / 100, (-eurocents) % 100);
        }
    }

    public static final Pattern PATTERN = Pattern.compile("-?[0-9]+[,.][0-9][0-9]?");

    /**
     * Parse a string into an amount of eurocents.
     * If the string contains a decimal point or comma then it must be
     * followed by one or two digits.
     * @throws java.lang.NumberFormatException
     */
    public static EurocentAmount parse (String str) {
        if (PATTERN.matcher(str).matches()) {
            int len = str.length();
            int pos = str.lastIndexOf(',');
            if (pos < 0) {
                pos = str.lastIndexOf('.');
            }
            if (pos == len - 3) {
                // two decimal digits
                return new EurocentAmount(
                        Integer.parseInt(str.substring(0, pos)),
                        Integer.parseInt(str.substring(pos + 1, len))
                );
            } else if (pos == len - 2) {
                 // one decimal digits
                return new EurocentAmount(
                        Integer.parseInt(str.substring(0, pos)),
                        10*Integer.parseInt(str.substring(pos + 1, len))
                );
            } else {
                // this should not happen
                throw new RuntimeException("Unexpected error in parsing eurocent amount");
            }
        } else {
            // no decimal point or comma
            return new EurocentAmount(
               Integer.parseInt(str), 0
            );
        }
    }
}
