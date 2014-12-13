/* Utils.java
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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * Various utility functions and constants for use in controllers and views
 */
public final class Utils {

    public static Locale DEFAULT_LOCALE = new Locale("NL", "be");

    private static DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public static String toString(TemporalAccessor ta) {
        return DATETIME_FORMATTER.format(ta);
    }

    private static DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    public static String toDateString(TemporalAccessor ta) {
        return DATE_FORMATTER.format(ta);
    }

    private static DateTimeFormatter LOCALIZED_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("eee dd MMM yyyy HH:mm", DEFAULT_LOCALE).withZone(ZoneId.systemDefault());

    public static String toLocalizedString(Instant instant) {
        return LOCALIZED_DATETIME_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }

    public static String toLocalizedString(LocalDateTime dateTime) {
        return LOCALIZED_DATETIME_FORMATTER.format(dateTime);
    }

    private static DateTimeFormatter LOCALIZED_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("eee dd MMM yyyy", DEFAULT_LOCALE).withZone(ZoneId.systemDefault());

    public static String toLocalizedDateString(Instant instant) {
        return LOCALIZED_DATE_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }

    public static String toLocalizedDateString(LocalDateTime dateTime) {
        return LOCALIZED_DATE_FORMATTER.format(dateTime);
    }

    private static DateTimeFormatter LOCALIZED_LONG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", DEFAULT_LOCALE).withZone(ZoneId.systemDefault());

    public static String toLocalizedDateString(LocalDate localDate) {
        return LOCALIZED_LONG_DATE_FORMATTER.format(localDate);
    }

    public static Instant toInstant(String string) {
        return (string == null || string.isEmpty())
                ? null
                : ZonedDateTime.parse(string, DATETIME_FORMATTER).toInstant();
    }

    public static LocalDate toLocalDate(String string) {
        return (string == null || string.isEmpty())
                ? null
                : LocalDate.parse(string, DATE_FORMATTER);
    }

    public static LocalDateTime toLocalDateTime(String string) {
        return (string == null || string.isEmpty())
                ? null
                : LocalDateTime.parse(string, DATETIME_FORMATTER);
    }
}
