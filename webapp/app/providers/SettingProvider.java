package providers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.dao.SettingDAO;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by Cedric on 4/21/2014.
 */
public class SettingProvider {

    //TODO: provide caching for all overview - WARNING - dates may vary on request!
    private static final String SETTING_KEY = "setting:%s";
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private DataAccessProvider provider;

    public SettingProvider(DataAccessProvider provider) {
        this.provider = provider;
    }

    public void createSetting(String name, String value, DateTime afterDate) {
        try (DataAccessContext context = provider.getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            dao.createSettingAfterDate(name, value, afterDate);
        }
    }

    public void createSetting(String name, boolean value, DateTime afterDate) {
        createSetting(name, Boolean.toString(value), afterDate);
    }

    public void createSetting(String name, int value, DateTime afterDate) {
        createSetting(name, Integer.toString(value), afterDate);
    }

    public void createSetting(String name, double value, DateTime afterDate) {
        createSetting(name, Double.toString(value), afterDate);
    }

    public void createSetting(String name, DateTime value, DateTime afterDate) {
        createSetting(name, value.toString(DATE_FORMAT), afterDate);
    }

    public String getString(String name, DateTime forDate) {
        try (DataAccessContext context = provider.getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            return dao.getSettingForDate(name, forDate);
        }
    }

    public String getStringOrDefault(String name, String defaultValue, DateTime forDate) {
        String value = getString(name, forDate);
        if (value == null)
            return defaultValue;
        else
            return value;
    }

    public int getInt(String name, DateTime forDate) {
        return Integer.valueOf(getString(name, forDate));
    }

    public int getIntOrDefault(String name, int defaultValue, DateTime forDate) {
        try {
            return getInt(name, forDate);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public double getDouble(String name, DateTime forDate) {
        return Double.valueOf(getString(name, forDate));
    }

    public double getDoubleOrDefault(String name, double defaultValue, DateTime forDate) {
        try {
            return getDouble(name, forDate);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public DateTime getDate(String name, DateTime forDate) {
        return DATE_FORMAT.parseDateTime(getString(name, forDate));
    }

    public DateTime getDateOrDefault(String name, DateTime defaultValue, DateTime forDate) {
        try {
            return getDate(name, forDate);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public boolean getBool(String name, DateTime forDate) {
        return Boolean.parseBoolean(getString(name, forDate));
    }

    public boolean getBoolOrDefault(String name, boolean defaultValue, DateTime forDate) {
        try {
            return getBool(name, forDate);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Gets setting based on current timestamp
     *
     * @param name
     * @return
     */
    public String getString(String name) {
        return getString(name, DateTime.now());
    }

    public String getStringOrDefault(String name, String defaultValue){
        return getStringOrDefault(name, defaultValue, DateTime.now());
    }

    /**
     * Gets setting based on current timestamp
     *
     * @param name
     * @return
     */
    public int getInt(String name) {
        return getInt(name, DateTime.now());
    }

    public int getIntOrDefault(String name, int defaultValue){
        return getIntOrDefault(name, defaultValue, DateTime.now());
    }

    /**
     * Gets setting based on current timestamp
     *
     * @param name
     * @return
     */
    public double getDouble(String name) {
        return getDouble(name, DateTime.now());
    }

    public double getDoubleOrDefault(String name, double defaultValue){
        return getDoubleOrDefault(name, defaultValue, DateTime.now());
    }

    /**
     * Gets setting based on current timestamp
     *
     * @param name
     * @return
     */
    public DateTime getDate(String name) {
        return getDate(name, DateTime.now());
    }

    public DateTime getDateOrDefault(String name, DateTime defaultValue){
        return getDateOrDefault(name, defaultValue, DateTime.now());
    }

    /**
     * Gets setting based on current timestamp
     *
     * @param name
     * @return
     */
    public boolean getBool(String name) {
        return getBool(name, DateTime.now());
    }

    public boolean getBoolOrDefault(String name, boolean defaultValue){
        return getBoolOrDefault(name, defaultValue, DateTime.now());
    }

}
