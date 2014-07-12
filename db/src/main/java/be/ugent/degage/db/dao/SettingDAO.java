package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Setting;
import org.joda.time.DateTime;


import java.time.Instant;
import java.util.List;

/**
 * Data access object for system settings
 */
public interface SettingDAO {

    /**
     * Create a new setting which will be valid after the given date and time. Also use this method
     * for 'updating' the settings.
     */
    public void createSettingAfterDate(String name, String value, Instant after) throws DataAccessException;

    /**
     * Retrieve the current value of a setting.
     */
    public String getSettingForNow(String name) throws DataAccessException;

    /**
     * Retrieve the value of a setting valid at a given instant
     */
    public String getSettingForDate(String name, Instant instant) throws DataAccessException;
    // TODO: do we really need the above? (ask customer - for bills?)

    /**
     * Return a list of all settings with their newest value.
     */
    public Iterable<Setting> getSettings() throws DataAccessException;
}
