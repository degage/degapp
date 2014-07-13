package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.SchedulerDAO;
import be.ugent.degage.db.models.User;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 27/04/14.
 */
class JDBCSchedulerDAO implements SchedulerDAO {

    private Connection connection;
    private PreparedStatement getReminderEmailListStatement;
    private PreparedStatement setRemindedStatement;

    public JDBCSchedulerDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getGetReminderEmailListStatement() throws SQLException {
        if (getReminderEmailListStatement == null) {
            getReminderEmailListStatement = connection.prepareStatement("SELECT * FROM " +
                    "(SELECT a.user_id, a.user_email, a.user_firstname, a.user_lastname, a.user_last_notified, " +
                    "a.number_of_notifications, COUNT(b.message_id) AS number_of_messages " +
                    "FROM (SELECT p.user_id AS user_id, p.user_email AS user_email, p.user_firstname " +
                    "AS user_firstname, p.user_lastname AS user_lastname, p.user_last_notified AS user_last_notified, " +
                    "COUNT(o.notification_id) AS number_of_notifications FROM users p " +
                    "LEFT JOIN (SELECT * FROM notifications WHERE notification_read=0) o " +
                    "ON o.notification_user_id = p.user_id GROUP BY p.user_id) a " +
                    "LEFT JOIN (SELECT * FROM messages WHERE message_read=0) b " +
                    "ON a.user_id = b.message_to_user_id GROUP BY a.user_id) AS reminder " +
                    "WHERE (number_of_notifications > ? OR number_of_messages > ?) " +
                    "AND user_last_notified < DATE_SUB(NOW(),INTERVAL 7 DAY)");
        }
        return getReminderEmailListStatement;
    }

    private PreparedStatement getSetRemindedStatement() throws SQLException {
        if (setRemindedStatement == null) {
            setRemindedStatement = connection.prepareStatement("UPDATE users SET user_last_notified = ? WHERE user_id = ?");
        }
        return setRemindedStatement;
    }



    @Override
    public List<User> getReminderEmailList(int maxMessages) throws DataAccessException {
        try {
            PreparedStatement ps = getGetReminderEmailListStatement();
            ps.setInt(1, maxMessages);
            ps.setInt(2, maxMessages);
            return getEmailList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the email list.", e);
        }
    }

    @Override
    public void setReminded(User user) throws DataAccessException {
        try {
            PreparedStatement ps = getSetRemindedStatement();
            ps.setTimestamp(1, new Timestamp(new DateTime().getMillis()));
            ps.setInt(2,user.getId());
            if(ps.executeUpdate() == 0)
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
        }catch (SQLException e){
            throw new DataAccessException("Error while reading email resultset", e);

        }
    }
}
