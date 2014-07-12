package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.SettingDAO;
import be.ugent.degage.db.models.Setting;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of SettingDAO
 */
class JDBCSettingDAO extends AbstractDAO implements SettingDAO {

    public JDBCSettingDAO(JDBCDataAccessContext context) {
        super(context);
    }


    private LazyStatement getSettingForNowStatement = new LazyStatement(
        "SELECT setting_value FROM settings WHERE setting_name=? AND setting_after < NOW()" +
                            " ORDER BY setting_after DESC LIMIT 1"
    );

    @Override
    public String getSettingForNow(String name) throws DataAccessException {
        try {
            PreparedStatement ps = getSettingForNowStatement.value();
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                else
                    return rs.getString("setting_value");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get setting.", ex);
        }

    }

    private LazyStatement getSettingForDateStatement= new LazyStatement(
            "SELECT setting_value FROM settings WHERE setting_name=? AND setting_after < ?" +
                            " ORDER BY setting_after DESC LIMIT 1"
    );

    @Override
    public String getSettingForDate(String name, Instant instant) throws DataAccessException {
        try {
            PreparedStatement ps = getSettingForDateStatement.value();
            ps.setString(1, name);
            ps.setTimestamp(2, Timestamp.from(instant));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                else
                    return rs.getString("setting_value");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get setting.", ex);
        }

    }

    private LazyStatement createSettingAfterStatement = new LazyStatement(
        "INSERT INTO settings(setting_name, setting_value, setting_after) " +
                            "VALUES(?, ?, ?)"
    );

    @Override
    public void createSettingAfterDate(String name, String value, Instant after) {
        try {
            PreparedStatement ps = createSettingAfterStatement.value();
            ps.setString(1, name);
            ps.setString(2, value);
            ps.setTimestamp(3, Timestamp.from(after));

            if (ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to create setting. Rows inserted != 1");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to prepare create setting statement.", ex);
        }
    }

    private static final String GET_SETTINGS_STATEMENT =
            "SELECT s.setting_name, s.setting_value, s.setting_after " +
                    "FROM ( SELECT setting_name, MAX(setting_after) AS m " +
                    "       FROM settings GROUP BY setting_name ) AS t " +
                    "JOIN settings AS s " +
                    "  WHERE t.setting_name = s.setting_name " +
                    "    AND t.m = s.setting_after " +
                    "ORDER BY s.setting_name ASC";


    @Override
    public Iterable<Setting> getSettings() throws DataAccessException {
        try (Statement stat = createStatement();
                ResultSet rs = stat.executeQuery(GET_SETTINGS_STATEMENT)){
            List<Setting> settings = new ArrayList<>();
            while (rs.next()) {
                settings.add(
                        new Setting(rs.getString("setting_name"),
                                rs.getString("setting_value"),
                                rs.getTimestamp("setting_after").toInstant()
                        )
                );
            }
            return settings;
        } catch(SQLException ex){
            throw new DataAccessException("Failed to read overview resultset.", ex);
        }
    }
}
