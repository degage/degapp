package controllers;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * Various utility functions and constants for use in controllers and views
 */
public final class Utils {

    private static Locale NL_be = new Locale("NL", "be");

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
            DateTimeFormatter.ofPattern("eee dd MMM yyyy HH:mm", NL_be).withZone(ZoneId.systemDefault());

    public static String toLocalizedString(Instant instant) {
        return LOCALIZED_DATETIME_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }

    public static String toLocalizedString(LocalDateTime dateTime) {
        return LOCALIZED_DATETIME_FORMATTER.format(dateTime);
    }

    private static DateTimeFormatter LOCALIZED_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("eee dd MMM yyyy", NL_be).withZone(ZoneId.systemDefault());

    public static String toLocalizedDateString(Instant instant) {
        return LOCALIZED_DATE_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }

    public static String toLocalizedDateString(LocalDateTime dateTime) {
        return LOCALIZED_DATE_FORMATTER.format(dateTime);
    }

    private static DateTimeFormatter LOCALIZED_LONG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", NL_be).withZone(ZoneId.systemDefault());

    public static String toLocalizedDateString(LocalDate localDate) {
        return LOCALIZED_LONG_DATE_FORMATTER.format(localDate);
    }

    public static Instant toInstant(String string) {
        return ZonedDateTime.parse(string, DATETIME_FORMATTER).toInstant();
    }

    public static LocalDate toLocalDate(String string) {
        return LocalDate.parse(string, DATE_FORMATTER);
    }
    public static LocalDateTime toLocalDateTime(String string) {
        return LocalDateTime.parse(string, DATETIME_FORMATTER);
    }
}
