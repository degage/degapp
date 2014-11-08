/* FilterImpl.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright (C) 2013 Universiteit Gent
 * 
 * This file is part of the Rasbeb project, an interactive web
 * application for the Belgian version of the international Bebras
 * competition.
 * 
 * Corresponding author:
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License can be found in the file
 * LICENSE.txt provided with the source distribution of this program (see
 * the META-INF directory in the source jar). This license can also be
 * found on the GNU website at http://www.gnu.org/licenses/gpl.html.
 * 
 * If you did not receive a copy of the GNU General Public License along
 * with this program, contact the lead developer, or write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package be.ugent.degage.db.jdbc;

/**
 * Utilities for appending filters to queries
 */
public final class FilterUtils {

    /**
     * Escape the given string and append it to the current builder.
     */
    public static void appendEscapedString(StringBuilder builder, String string) {
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            //noinspection StatementWithEmptyBody
            if (ch == '\0') {
                // silently ignore - although this should not happen
            } else if (ch == '\'' || ch == '%' || ch == '_' || ch == '\\' || ch == '"') {
                builder.append('\'');
            }
            builder.append(ch);
        }
    }


    /**
     * Append to the given builder the 'ILIKE'-string that corresponds to this filter.
     */
    public static void appendILikeString(StringBuilder builder, String key, String value) {
        if (value != null) {
            builder.append(" AND ")
                    .append(key)
                    .append(" ILIKE '") ;
            appendEscapedString(builder, value);
            builder.append('\'');
        }
    }

    /**
     * Append to a given builder the AND-clause for an integer
     */
    public static void appendIntFilter (StringBuilder builder, String key, String value) {
        if(! value.isEmpty()) {
            Integer.parseInt(value); // check that this is an integer - avoid SQL injection
            builder.append (" AND ").append(key).append(" = ").append(value);
        }
    }

    /**
     * Append to a given builder the AND-clause for a tristate
     */
    public static void appendTristateFilter (StringBuilder builder, String key, String value) {
        if(! value.isEmpty()) {
            int v = Integer.parseInt(value);
            if (v > 0) {
                builder.append (" AND ").append(key);
            } else if (v == 0) {
                builder.append (" AND NOT ").append(key);
            }
        }
    }
}
