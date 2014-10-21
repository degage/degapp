package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.*;
import be.ugent.degage.db.dao.NotificationDAO;
import be.ugent.degage.db.models.Notification;
import be.ugent.degage.db.models.User;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/03/14.
 */
class JDBCNotificationDAO extends AbstractDAO implements NotificationDAO {

    public static final String NOTIFICATION_QUERY = 
            "SELECT * FROM notifications JOIN users ON notification_user_id= user_id";

    public static final String FILTER_FRAGMENT = " WHERE notification_user_id=? AND notification_read = ? ";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if (filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
        ps.setString(start, filter.getValue(FilterField.USER_ID));
        ps.setString(start + 1, filter.getValue(FilterField.NOTIFICATION_READ));
    }

    public JDBCNotificationDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private LazyStatement createNotificationStatement = new LazyStatement(
            "INSERT INTO notifications (notification_user_id, " +
                    "notification_read, notification_subject,"
                    + "notification_body) VALUES (?,?,?,?)",
            "notification_id"
    );

    private LazyStatement getNotificationListByUseridStatement = new LazyStatement(
            "SELECT * FROM notifications JOIN users ON " +
                    "notification_user_id= user_id WHERE notification_user_id=? ORDER BY notification_created_at DESC"
    );

    private LazyStatement getNotificationListPageByTimestampDescStatement = new LazyStatement(
            NOTIFICATION_QUERY + FILTER_FRAGMENT + " ORDER BY notification_created_at desc LIMIT ?, ?"
    );

    private LazyStatement getNumberOfUnreadNotificationsStatement = new LazyStatement(
            "SELECT COUNT(*) AS unread_number FROM notifications JOIN users ON " +
                    "notification_user_id= user_id WHERE notification_user_id=? AND notification_read=0"
    );

    private LazyStatement getAmountOfNotificationsStatement = new LazyStatement(
            "SELECT count(*) as amount_of_notifications FROM notifications JOIN users ON " +
                    "notification_user_id= user_id" + FILTER_FRAGMENT
    );

    private LazyStatement setReadStatement = new LazyStatement(
            "UPDATE notifications SET notification_read = ? WHERE notification_id = ?"
    );

    private LazyStatement setAllReadStatement = new LazyStatement(
            "UPDATE notifications SET notification_read = ? WHERE notification_user_id = ?"
    );


    @Override
    public int getAmountOfNotifications(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfNotificationsStatement.value();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("amount_of_notifications");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of notifications", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of notifications", ex);
        }
    }

    @Override
    public List<Notification> getNotificationListForUser(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getNotificationListByUseridStatement.value();
            ps.setInt(1, userId);
            return getNotificationList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve the list of notifications", e);
        }
    }

    @Override
    public List<Notification> getNotificationList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getNotificationListPageByTimestampDescStatement.value();

            fillFragment(ps, filter, 1);
            int first = (page - 1) * pageSize;
            ps.setInt(3, first);
            ps.setInt(4, pageSize);
            return getNotificationList(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of notifications", ex);
        }
    }

    @Override
    public int getNumberOfUnreadNotifications(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getNumberOfUnreadNotificationsStatement.value();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("unread_number");
                } else {
                    return 0;
                }
            } catch (SQLException e) {
                throw new DataAccessException("Error while reading notification number resultset", e);

            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve the number of unread notifications", e);
        }
    }

    @Override
    public Notification createNotification(User user, String subject, String body) throws DataAccessException {
        try {
            PreparedStatement ps = createNotificationStatement.value();
            ps.setInt(1, user.getId());
            ps.setBoolean(2, false);
            ps.setString(3, subject);
            ps.setString(4, body);

            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating notification.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                // new DateTime() is not exactly correct, if you want the exact timestamp, do a getNotification()
                return new Notification(keys.getInt(1), user, false, subject, body, new DateTime());
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new notification.", ex);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create notification", e);
        }
    }

    @Override
    public void markNotificationAsRead(int notificationId) throws DataAccessException {
        try {
            PreparedStatement ps = setReadStatement.value();
            ps.setBoolean(1, true);
            ps.setInt(2, notificationId);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when updating notification.");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to mark notification as read", e);
        }
    }

    @Override
    public void markAllNotificationsAsRead(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = setAllReadStatement.value();
            ps.setBoolean(1, true);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to mark notification as read", e);
        }
    }


    private List<Notification> getNotificationList(PreparedStatement ps) throws DataAccessException {
        try (ResultSet rs = ps.executeQuery()) {
            List<Notification> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Notification(
                        rs.getInt("notification_id"),
                        JDBCUserDAO.populateUserPartial(rs),
                        rs.getBoolean("notification_read"),
                        rs.getString("notification_subject"),
                        rs.getString("notification_body"),
                        new DateTime(rs.getTimestamp("notification_created_at")
                        )
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error while reading notification resultset", e);

        }
    }
}
