package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.SchedulerDAO;
import be.ugent.degage.db.models.UserHeader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static be.ugent.degage.db.jdbc.JDBCUserDAO.USER_HEADER_FIELDS;

/**
 * Created by Stefaan Vermassen on 27/04/14.
 */
class JDBCSchedulerDAO extends AbstractDAO implements SchedulerDAO {

    public JDBCSchedulerDAO(JDBCDataAccessContext context) {
        super(context);
    }

    // TODO: refactor?
    private LazyStatement getReminderEmailListStatement = new LazyStatement(
            "SELECT * FROM " +
                    "   (SELECT " + USER_HEADER_FIELDS + ", user_last_notified, " +
                    "           number_of_notifications, COUNT(message_id) AS number_of_messages " +
                    "   FROM (SELECT " + USER_HEADER_FIELDS + ",  user_last_notified, " +
                    "             COUNT(notification_id) AS number_of_notifications FROM users " +
                    "         LEFT JOIN notifications ON notification_user_id = user_id " +
                    "         WHERE notification_read=0 " +
                    "         GROUP BY user_id" +
                    "        ) AS sub " +
                    "   LEFT JOIN messages ON message_to_user_id = user_id " +
                    "   WHERE message_read=0 " +
                    "   GROUP BY user_id" +
                    "   ) AS reminder " +
                    "WHERE (number_of_notifications > ? OR number_of_messages > ?) " +
                    "AND user_last_notified < DATE_SUB(NOW(),INTERVAL 7 DAY)"
    );

    @Override
    public Iterable<UserHeader> getReminderEmailList(int maxMessages) throws DataAccessException {
        try {
            PreparedStatement ps = getReminderEmailListStatement.value();
            ps.setInt(1, maxMessages);
            ps.setInt(2, maxMessages);
            Collection<UserHeader> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(JDBCUserDAO.populateUserHeader(rs, "reminder"));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve the email list.", e);
        }
    }

    private LazyStatement setRemindedStatement = new LazyStatement(
            "UPDATE users SET user_last_notified = NOW() WHERE user_id = ?"
    );

    @Override
    public void setReminded(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = setRemindedStatement.value();
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
