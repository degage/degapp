/* JDBCNotificationDAO.java
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
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.NotificationDAO;
import be.ugent.degage.db.models.Notification;
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.models.UserHeader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/**
 * JDBC implementation of {@link NotificationDAO}
 */
class JDBCNotificationDAO extends AbstractDAO implements NotificationDAO {

    private static Notification populateNotification(ResultSet rs) throws SQLException {
        return new Notification(
                rs.getInt("notification_id"),
                JDBCUserDAO.populateUserHeader(rs),
                rs.getBoolean("notification_read"),
                rs.getString("notification_subject"),
                rs.getString("notification_body"),
                rs.getTimestamp("notification_created_at").toInstant()
        );
    }

    public JDBCNotificationDAO(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public List<Notification> getNotificationListForUser(int userId, int count) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT * FROM notifications JOIN users ON " +
                        "notification_user_id= user_id WHERE notification_user_id=? ORDER BY notification_created_at DESC LIMIT ?"
        )){
            ps.setInt(1, userId);
            ps.setInt (2, count);
            return toList(ps, JDBCNotificationDAO::populateNotification);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve the list of notifications", e);
        }
    }
    @Override
    public Page<Notification> getNotificationList(FilterField orderBy, boolean asc, int page, int pageSize, int userId, boolean read) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT SQL_CALC_FOUND_ROWS * " +
                    "FROM notifications JOIN users ON notification_user_id= user_id " +
                    "WHERE notification_user_id=? AND notification_read = ? " +
                    "ORDER BY notification_created_at desc LIMIT ?, ?"
        )) {
            ps.setInt(1, userId);
            ps.setBoolean(2, read);
            ps.setInt(3, (page - 1) * pageSize);
            ps.setInt(4, pageSize);
            return toPage(ps, pageSize, JDBCNotificationDAO::populateNotification);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of notifications", ex);
        }
    }

    @Override
    public int getNumberOfUnreadNotifications(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT COUNT(*) FROM notifications JOIN users ON " +
                        "notification_user_id= user_id WHERE notification_user_id=? AND notification_read=0"
        )) {
            ps.setInt(1, userId);
            return toSingleInt(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve the number of unread notifications", e);
        }
    }

    @Override
    public Notification createNotification(UserHeader user, String subject, String body) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO notifications (notification_user_id, " +
                        "notification_read, notification_subject,"
                        + "notification_body) VALUES (?,?,?,?)",
                "notification_id"
        )) {
            ps.setInt(1, user.getId());
            ps.setBoolean(2, false);
            ps.setString(3, subject);
            ps.setString(4, body);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                // new DateTime() is not exactly correct, if you want the exact timestamp, do a getNotification()
                return new Notification(keys.getInt(1), user, false, subject, body, Instant.now());
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new notification.", ex);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create notification", e);
        }
    }

    @Override
    public void markNotificationAsRead(int notificationId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE notifications SET notification_read = ? WHERE notification_id = ?"
        )) {
            ps.setBoolean(1, true);
            ps.setInt(2, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to mark notification as read", e);
        }
    }

    @Override
    public void markAllNotificationsAsRead(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE notifications SET notification_read = ? WHERE notification_user_id = ?"
        )) {
            ps.setBoolean(1, true);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to mark notification as read", e);
        }
    }


}
