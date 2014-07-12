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
class JDBCSettingDAO implements SettingDAO {

    private final Connection connection;
    private PreparedStatement getSettingForDateStatement;
    private PreparedStatement getSettingForNowStatement;
    private PreparedStatement createSettingAfterStatement;

    public JDBCSettingDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getGetSettingForNowStatement() throws SQLException {
        if (getSettingForNowStatement == null) {
            getSettingForNowStatement = connection.prepareStatement(
                    "SELECT setting_value FROM settings WHERE setting_name=? AND setting_after < NOW()" +
                            " ORDER BY setting_after DESC LIMIT 1");
        }
        return getSettingForNowStatement;
    }

    private PreparedStatement getGetSettingForDateStatement() throws SQLException {
        if (getSettingForDateStatement == null) {
            getSettingForDateStatement = connection.prepareStatement(
                    "SELECT setting_value FROM settings WHERE setting_name=? AND setting_after < ?" +
                            " ORDER BY setting_after DESC LIMIT 1");
        }
        return getSettingForDateStatement;
    }

    private PreparedStatement getCreateSettingAfterStatement() throws SQLException {
        if (createSettingAfterStatement == null) {
            createSettingAfterStatement = connection.prepareStatement(
                    "INSERT INTO settings(setting_name, setting_value, setting_after) " +
                            "VALUES(?, ?, ?)");
        }
        return createSettingAfterStatement;
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
    public String getSettingForNow(String name) throws DataAccessException {
        try {
            PreparedStatement ps = getGetSettingForNowStatement();
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

    @Override
    public String getSettingForDate(String name, Instant instant) throws DataAccessException {
        try {
            PreparedStatement ps = getGetSettingForDateStatement();
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

    @Override
    public void createSettingAfterDate(String name, String value, Instant after) {
        try {
            PreparedStatement ps = getCreateSettingAfterStatement();
            ps.setString(1, name);
            ps.setString(2, value);
            ps.setTimestamp(3, Timestamp.from(after));

            if (ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to create setting. Rows inserted != 1");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to prepare create setting statement.", ex);
        }
    }

    @Override
    public Iterable<Setting> getSettings() throws DataAccessException {
        try (Statement stat = connection.createStatement();
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
