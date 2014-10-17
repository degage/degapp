package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.*;
import be.ugent.degage.db.dao.MessageDAO;
import be.ugent.degage.db.models.Message;
import be.ugent.degage.db.models.User;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 22/03/14.
 */
class JDBCMessageDAO implements MessageDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"message_id"};

    private static final String MESSAGE_QUERY = "SELECT * FROM messages " +
            "JOIN users AS Sender ON message_from_user_id = Sender.user_id " +
            "JOIN users AS Receiver ON message_to_user_id = Receiver.user_id ";

    public static final String FILTER_FRAGMENT = " WHERE message_to_user_id=? OR message_from_user_id=?";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
        String receiver = filter.getValue(FilterField.MESSAGE_RECEIVER_ID);
        if(receiver.equals("")) {
            receiver = "-1";
        }

        String sender = filter.getValue(FilterField.MESSAGE_SENDER_ID);
        if(sender.equals("")) {
            sender = "-1";
        }

        ps.setString(start, receiver);
        ps.setString(start+1, sender);
    }

    private Connection connection;
    private PreparedStatement createMessageStatement;
    private PreparedStatement getNumberOfUnreadMessagesStatement;
    private PreparedStatement setReadStatement;
    private PreparedStatement setAllReadStatement;
    private PreparedStatement getMessageListPageByTimestampStatement;
    private PreparedStatement getAmountOfMessagesStatement;

    public JDBCMessageDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getCreateMessageStatement() throws SQLException {
        if (createMessageStatement == null) {
            createMessageStatement = connection.prepareStatement("INSERT INTO messages (message_from_user_id, message_to_user_id, " +
                    "message_read, message_subject, message_body) VALUES (?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createMessageStatement;
    }

    private PreparedStatement getGetMessageListPageByTimestampStatement() throws SQLException {
        if (getMessageListPageByTimestampStatement == null) {
            getMessageListPageByTimestampStatement = connection.prepareStatement(MESSAGE_QUERY + FILTER_FRAGMENT + " ORDER BY message_created_at DESC LIMIT ?, ?");
        }
        return getMessageListPageByTimestampStatement;
    }

    private PreparedStatement getSetReadStatement() throws SQLException {
        if (setReadStatement == null) {
            setReadStatement = connection.prepareStatement("UPDATE messages SET message_read = ? WHERE message_id = ?;");
        }
        return setReadStatement;
    }

    private PreparedStatement getSetAllReadStatement() throws SQLException {
        if (setAllReadStatement == null) {
            setAllReadStatement = connection.prepareStatement("UPDATE messages SET message_read = ? WHERE message_to_user_id = ?;");
        }
        return setAllReadStatement;
    }

    private PreparedStatement getNumberOfUnreadMessagesStatement() throws SQLException {
        if (getNumberOfUnreadMessagesStatement == null) {
            getNumberOfUnreadMessagesStatement = connection.prepareStatement("SELECT COUNT(*) AS unread_number FROM messages " +
                    "WHERE message_to_user_id=? AND message_read=0;");
        }
        return getNumberOfUnreadMessagesStatement;
    }

    private PreparedStatement getGetAmountOfMessagesStatement() throws SQLException {
        if(getAmountOfMessagesStatement == null) {
            getAmountOfMessagesStatement = connection.prepareStatement("SELECT count(*) as amount_of_messages FROM messages " +
                    "JOIN users AS Sender ON message_from_user_id = Sender.user_id " +
                    "JOIN users AS Receiver ON message_to_user_id = Receiver.user_id "+ FILTER_FRAGMENT);
        }
        return getAmountOfMessagesStatement;
    }

    public static Message populateMessage(ResultSet rs) throws SQLException {
        Message message = new Message(rs.getInt("message_id"), JDBCUserDAO.populateUserPartial(rs, "Sender"),
                JDBCUserDAO.populateUserPartial(rs, "Receiver"), rs.getBoolean("message_read"),
                rs.getString("message_subject"), rs.getString("message_body"),
                new DateTime(rs.getTimestamp("message_created_at")));
        return message;
    }

    @Override
    public int getAmountOfMessages(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfMessagesStatement();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_messages");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of messages", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of messages", ex);
        }
    }

    @Override
    public List<Message> getMessageList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetMessageListPageByTimestampStatement();

            fillFragment(ps, filter, 1);
            int first = (page-1)*pageSize;
            ps.setInt(3, first);
            ps.setInt(4, pageSize);
            return getMessageList(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of messages", ex);
        }
    }

    @Override
    public int getNumberOfUnreadMessages(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getNumberOfUnreadMessagesStatement();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("unread_number");
                }else{
                    return 0;
                }
            }catch (SQLException e){
                throw new DataAccessException("Error while reading message number resultset", e);

            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the number of unread messages", e);
        }
    }

    @Override
    public Message createMessage(User sender, User receiver, String subject, String body) throws DataAccessException {
        try{
            PreparedStatement ps = getCreateMessageStatement();
            ps.setInt(1, sender.getId());
            ps.setInt(2, receiver.getId());
            ps.setBoolean(3, false);
            ps.setString(4, subject);
            ps.setString(5,body);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating message.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway

                // new DateTime() is not exactly correct, if you want the exact timestamp, do a getMessage()
                return new Message(keys.getInt(1), sender, receiver, false, subject, body, new DateTime());
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new message.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create message", e);
        }
    }

    @Override
    public void markMessageAsRead(int messageID) throws DataAccessException {
        try {
            PreparedStatement ps = getSetReadStatement();
            ps.setBoolean(1, true);
            ps.setInt(2,messageID);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when updating message.");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to mark message as read", e);
        }
    }

    @Override
    public void markAllMessagesAsRead(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getSetAllReadStatement();
            ps.setBoolean(1, true);
            ps.setInt(2,userId);
            // It is possible that no rows are affected but that is not a problem
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to mark message as read", e);
        }
    }

    private List<Message> getMessageList(PreparedStatement ps) throws DataAccessException {
        List<Message> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateMessage(rs));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading message resultset", e);

        }
    }
}
