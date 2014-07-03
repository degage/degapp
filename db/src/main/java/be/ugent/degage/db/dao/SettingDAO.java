package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Setting;
import org.joda.time.DateTime;


import java.util.List;

/**
 * Created by Cedric on 4/21/2014.
 */
public interface SettingDAO {
    public String getSettingForDate(String name, DateTime date) throws DataAccessException;
    public void createSettingAfterDate(String name, String value, DateTime after) throws DataAccessException;
    public Setting getSetting(int id) throws DataAccessException;
    public void updateSetting(int id, String name, String value, DateTime after) throws DataAccessException;
    public List<Setting> getSettings() throws DataAccessException;
}
