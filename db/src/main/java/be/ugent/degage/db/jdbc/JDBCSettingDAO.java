package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.SettingDAO;
import be.ugent.degage.db.models.Setting;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 4/21/2014.
 */
public class JDBCSettingDAO implements SettingDAO {

    private Connection connection;
    private PreparedStatement getSettingForDateStatement;
    private PreparedStatement createSettingAfterStatement;
    private PreparedStatement getSettingsStatement;
    private PreparedStatement getSettingStatement;
    private PreparedStatement updateSettingStatement;

    public JDBCSettingDAO(Connection connection){
        this.connection = connection;
    }

    private PreparedStatement getUpdateSettingStatement() throws SQLException {
        if(updateSettingStatement == null){
            updateSettingStatement = connection.prepareStatement("UPDATE settings SET setting_value=?, setting_name=?, setting_after=? WHERE setting_id = ?");
        }
        return updateSettingStatement;
    }

    private PreparedStatement getGetSettingForDateStatement() throws SQLException {
        if(getSettingForDateStatement == null){
            getSettingForDateStatement = connection.prepareStatement("SELECT setting_value FROM settings WHERE setting_name=? AND (setting_after < ? OR setting_after IS NULL) ORDER BY setting_after DESC LIMIT 1");
        }
        return getSettingForDateStatement;
    }

    private PreparedStatement getCreateSettingAfterStatement() throws SQLException {
        if(createSettingAfterStatement == null){
            createSettingAfterStatement = connection.prepareStatement("INSERT INTO settings(setting_name, setting_value, setting_after) VALUES(?, ?, ?)");
        }
        return createSettingAfterStatement;
    }

    private PreparedStatement getGetSettingsStatement() throws SQLException {
        if(getSettingsStatement == null){
            getSettingsStatement = connection.prepareStatement("SELECT setting_id, setting_name, setting_value, setting_after FROM settings ORDER BY setting_name ASC");
        }
        return getSettingsStatement;
    }

    private PreparedStatement getGetSettingStatement() throws SQLException {
        if(getSettingStatement == null){
            getSettingStatement = connection.prepareStatement("SELECT setting_id, setting_name, setting_value, setting_after FROM settings WHERE setting_id = ?");
        }
        return getSettingStatement;
    }

    private static Setting populateSetting(ResultSet rs) throws SQLException {
        return new Setting(rs.getInt("setting_id"), rs.getString("setting_name"), rs.getString("setting_value"), rs.getObject("setting_after") == null ? null : new DateTime(rs.getDate("setting_after")));
    }

    @Override
    public String getSettingForDate(String name, DateTime date) throws DataAccessException {
        try {
            PreparedStatement ps = getGetSettingForDateStatement();
            ps.setString(1, name);
            ps.setDate(2, new java.sql.Date(date.getMillis()));

            try(ResultSet rs = ps.executeQuery()) {
                if(!rs.next())
                    return null;
                else
                    return rs.getString("setting_value");
            } catch(SQLException ex){
                throw new DataAccessException("Failed to get setting from resultset.", ex);
            }

        } catch(SQLException ex){
            throw new DataAccessException("Failed to get setting.", ex);
        }
    }

    @Override
    public void createSettingAfterDate(String name, String value, DateTime after) {
        try {
            PreparedStatement ps = getCreateSettingAfterStatement();
            ps.setString(1, name);
            ps.setString(2, value);
            ps.setDate(3, new java.sql.Date(after.getMillis()));

            if(ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to create setting. Rows inserted != 1");

        } catch(SQLException ex){
            throw new DataAccessException("Failed to prepare create setting statement.", ex);
        }
    }

    @Override
    public Setting getSetting(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetSettingStatement();
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next())
                    return null;
                else
                    return populateSetting(rs);
            } catch(SQLException ex){
                throw new DataAccessException("Failed to read overview resultset.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to prepare statement for overview.");
        }
    }

    @Override
    public void updateSetting(int id, String name, String value, DateTime after) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateSettingStatement();
            ps.setString(1, value);
            ps.setString(2, name);
            if(after == null)
                ps.setNull(3, Types.DATE);
            else
                ps.setDate(3, new java.sql.Date(after.getMillis()));
            ps.setInt(4, id);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Failed to update setting. No rows affected.");
        } catch(SQLException ex){
            throw new DataAccessException("Failed to prepare setting update statement.", ex);
        }
    }

    @Override
    public List<Setting> getSettings() throws DataAccessException {
        try {
            PreparedStatement ps = getGetSettingsStatement(); //TODO: add timestamp filtering
            try(ResultSet rs = ps.executeQuery()){
                List<Setting> settings = new ArrayList<>();
                while(rs.next()){
                    settings.add(populateSetting(rs));
                }
                return settings;
            } catch(SQLException ex){
                throw new DataAccessException("Failed to read overview resultset.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to prepare statement for overview.");
        }
    }
}
