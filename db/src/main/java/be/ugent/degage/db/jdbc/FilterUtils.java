/* FilterUtils.java
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
     * Append to the given builder the AND-clause for containing a string
     */
    public static void appendContainsFilter(StringBuilder builder, String field, String str) {
        if (str != null && ! str.isEmpty()) {
            builder.append(" AND ")
                    .append(field)
                    .append(" LIKE '%") ;
            appendEscapedString(builder, str);
            builder.append("%'");
        }
    }

    /**
     * Append to the given builder the AND-clause for containing a string
     */
    public static void appendOrContainsFilter(StringBuilder builder, String field, String str) {
        if (str != null && ! str.isEmpty()) {
            if (builder.length() > 1) {
              builder.append(" OR ");
            }
            builder.append(field)
                    .append(" LIKE '%") ;
            appendEscapedString(builder, str);
            builder.append("%'");
        }
    }

    public static void appendOrBooleanFilter (StringBuilder builder, String field, boolean value) {
      if (builder.length() > 1) {
        builder.append(" OR ");
      }
        builder.append(field).append(" = ").append(value ? "true" : "false");
    }

    public static void appendStringFilter(StringBuilder builder, String field, String str) {
        if (str != null) {
            builder.append(" AND ").append(field).append("= '") ;
            appendEscapedString(builder, str);
            builder.append('\'');
        }
    }

    /**
     * Append to a given builder the AND-clause for an integer
     */
    public static void appendIntFilter (StringBuilder builder, String field, String value) {
        if(! value.isEmpty()) {
            Integer.parseInt(value); // check that this is an integer - avoid SQL injection
            builder.append (" AND ").append(field).append(" = ").append(value);
        }
    }


    /**
     * Append to a given builder the AND-clause for an id. Negative ids are considered shorthand
     * for null.
     */
    public static void appendIdFilter (StringBuilder builder, String field, String value) {
        if(! value.isEmpty()) {
            if (Integer.parseInt(value) >= 0) {
                builder.append(" AND ").append(field).append(" = ").append(value);
            }
        }
    }

    /**
     * Append to a given builder the AND-clause for a boolean, when one
     */
    public static void appendWhenOneFilter (StringBuilder builder, String field, String value) {
        if(value.equals("1")) {
            builder.append (" AND ").append(field);
        }
    }

    /**
     * Append to a given builder the AND-clause for a boolean, when not one
     */
    public static void appendNotWhenOneFilter (StringBuilder builder, String field, String value) {
        if(value.equals("1")) {
            builder.append (" AND NOT ").append(field);
        }
    }

    /**
     * Append to a given builder the AND-clause for a tristate
     */
    public static void appendTristateFilter (StringBuilder builder, String field, String value) {
        if(! value.isEmpty()) {
            int v = Integer.parseInt(value);
            if (v > 0) {
                builder.append (" AND ").append(field);
            } else if (v == 0) {
                builder.append (" AND NOT ").append(field);
            }
        }
    }
}
