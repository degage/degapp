/* DayOfWeek.java
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

/**
 * Created by HannesM on 28/04/14.
 */
public enum DayOfWeek {
    // 1 = sunday, ..., 7 = saterday, same as MySQL
    SUNDAY(1, "Zondag"), MONDAY(2, "Maandag"), TUESDAY(3, "Dinsdag"), WEDNESDAY(4, "Woensdag"), THURSDAY(5, "Donderdag"), FRIDAY(6, "Vrijdag"), SATERDAY(7, "Zaterdag");

    private int i;
    private String description;

    DayOfWeek(int i, String description) {
        this.i = i;
        this.description = description;
    }

    public int getI() {
        return i;
    }

    public String getDescription() {
        return description;
    }

    public static DayOfWeek getDayFromString(String s) {
        for(DayOfWeek f : values()) {
            if(f.getDescription().equals(s)) {
                return f;
            }
        }
        return null;
    }

    public static DayOfWeek getDayFromInt(int i) {
        for(DayOfWeek f : values()) {
            if(f.getI() == i) {
                return f;
            }
        }
        return null;
    }
}
