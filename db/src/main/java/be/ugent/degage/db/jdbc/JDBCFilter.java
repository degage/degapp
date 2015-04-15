/* JDBCFilter.java
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

package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by HannesM on 20/03/14.
 */
public class JDBCFilter implements Filter {

    // TODO: this class should not be public!

    private Map<FilterField, String> content = new EnumMap<>(FilterField.class);

    /**
     * Associate with a given filterfield a specified value.
     * @param field The filterfield
     * @param string The value
     */
    @Override
    public void putValue(FilterField field, String string) {
        content.put(field, string);
    }

    @Override
    public void putValue(FilterField field, int number) {
        putValue(field, Integer.toString(number));
    }

    /**
     * Retrieve the value contained in the specified filterfield.
     * @param field The filterfield for which you want to retrieve the value
     * @return A JDBC-SQL-representation (or exact representation) of the value contained in the specified filterfield
     */
    @Override
    public String getValue(FilterField field) {
        String value = "";
        if (content.containsKey(field))
            value = content.get(field);
        if (field.useExactValue())
            return value;
        else
            return "%" + value + "%";
    }
}
