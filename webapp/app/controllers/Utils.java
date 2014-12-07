package controllers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * Various utility functions and constants for use in controllers and views
 */
public final class Utils {

    public static Locale NL_be = new Locale("NL", "be");

    public static DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public static DateTimeFormatter LOCAL_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("eee dd MMM yyyy HH:mm", NL_be).withZone(ZoneId.systemDefault());

    public static String toString (TemporalAccessor ta) {
        return DATETIME_FORMATTER.format(ta);
    }

    public static String toLocalString (Instant instant) {
        return LOCAL_DATETIME_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }

    public static String toLocalString (LocalDateTime dateTime) {
        return LOCAL_DATETIME_FORMATTER.format(dateTime);
    }

    public static Instant toInstant (String string) {
        return ZonedDateTime.parse(string, DATETIME_FORMATTER).toInstant();
    }
}
