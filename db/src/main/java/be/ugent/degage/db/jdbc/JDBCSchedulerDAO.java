package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.SchedulerDAO;
import be.ugent.degage.db.models.User;
import org.joda.time.DateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 27/04/14.
 */
class JDBCSchedulerDAO extends AbstractDAO implements SchedulerDAO {

    public JDBCSchedulerDAO(JDBCDataAccessContext context) {
        super(context);
    }


    private LazyStatement getReminderEmailListStatement = new LazyStatement(
            "SELECT * FROM " +
                    "   (SELECT user_id, user_email, user_firstname, user_lastname, user_status, user_last_notified, " +
                    "           number_of_notifications, COUNT(message_id) AS number_of_messages " +
                    "   FROM (SELECT user_id, user_email, user_firstname, user_lastname, user_status, user_last_notified, " +
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
    public List<User> getReminderEmailList(int maxMessages) throws DataAccessException {
        try {
            PreparedStatement ps = getReminderEmailListStatement.value();
            ps.setInt(1, maxMessages);
            ps.setInt(2, maxMessages);
            return getEmailList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve the email list.", e);
        }
    }

    private LazyStatement setRemindedStatement = new LazyStatement(
            "UPDATE users SET user_last_notified = ? WHERE user_id = ?"
    );

    @Override
    public void setReminded(User user) throws DataAccessException {
        try {
            PreparedStatement ps = setRemindedStatement.value();
            ps.setTimestamp(1, new Timestamp(new DateTime().getMillis()));
            ps.setInt(2, user.getId());
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when updating user.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<User> getEmailList(PreparedStatement ps) throws DataAccessException {
        List<User> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(JDBCUserDAO.populateUserPartial(rs, "reminder"));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error while reading email resultset", e);

        }
    }
}
