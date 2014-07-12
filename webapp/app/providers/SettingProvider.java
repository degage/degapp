package providers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.dao.SettingDAO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Created by Cedric on 4/21/2014.
 */
public class SettingProvider {

    private static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String instantToString (Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault()).format(INSTANT_FORMATTER);
    }

    public static Instant stringToInstant (String string) {
        return LocalDateTime.parse(string, INSTANT_FORMATTER)
                .atZone(ZoneOffset.systemDefault()).toInstant();
    }

    private DataAccessProvider provider;

    public SettingProvider(DataAccessProvider provider) {
        this.provider = provider;
    }

    public void createSetting(String name, String value, Instant afterDate) {
        try (DataAccessContext context = provider.getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            dao.createSettingAfterDate(name, value, afterDate);
        }
    }

    public void createSetting(String name, boolean value, Instant afterDate) {
        createSetting(name, Boolean.toString(value), afterDate);
    }

    public void createSetting(String name, int value, Instant afterDate) {
        createSetting(name, Integer.toString(value), afterDate);
    }

    public void createSetting(String name, double value, Instant afterDate) {
        createSetting(name, Double.toString(value), afterDate);
    }

    public void createSetting(String name, Instant value, Instant afterDate) {
        createSetting(name, instantToString(value), afterDate);
    }

    public String getString (String name) {
        try (DataAccessContext context = provider.getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            return dao.getSettingForNow(name);
        }
    }

   public String getString (String name, Instant instant) {
        try (DataAccessContext context = provider.getDataAccessContext()) {
            return context.getSettingDAO().getSettingForDate(name, instant);
        }
    }

    public String getStringOrDefault(String name, String defaultValue) {
        String value = getString(name);
        if (value == null)
            return defaultValue;
        else
            return value;
    }

    public int getInt(String name) {
        return Integer.valueOf(getString(name));
    }

    public int getInt(String name, Instant instant) {
        return Integer.valueOf(getString(name,instant));
    }

    public int getIntOrDefault(String name, int defaultValue) {
        try {
            return getInt(name);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public double getDouble(String name) {
        return Double.valueOf(getString(name));
    }

   public double getDouble(String name, Instant instant) {
        return Double.valueOf(getString(name, instant));
    }

    public double getDoubleOrDefault(String name, double defaultValue) {
        try {
            return getDouble(name);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public boolean getBool(String name) {
        return Boolean.parseBoolean(getString(name));
    }

    public boolean getBoolOrDefault(String name, boolean defaultValue) {
        try {
            return getBool(name);
        } catch (Exception ex) {
            return defaultValue;
        }
    }


}
