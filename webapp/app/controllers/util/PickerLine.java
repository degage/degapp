/* PickerLine.java
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

package controllers.util;

/**
 * Contains information needed for a single line in a user/carpicker dropdownmenu
 */
public class PickerLine {
    protected String prefix;

    protected String suffix;

    protected String strong;

    protected int id;

    public PickerLine(String value, String search, int id) {
        this.id = id;
        search = search.trim().toLowerCase();
        int len = search.length();
        int pos = value.toLowerCase().indexOf(search);
        if (pos >= 0) {
            this.prefix = value.substring(0, pos);
            this.strong = value.substring(pos, pos + len);
            this.suffix = value.substring(pos + len);
        } else {
            // should not happen
            this.prefix = value;
            this.strong = "";
            this.suffix = "";
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getStrong() {
        return strong;
    }

    public int getId() {
        return id;
    }
}
