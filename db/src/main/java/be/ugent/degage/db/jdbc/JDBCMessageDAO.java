package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.*;
import be.ugent.degage.db.dao.MessageDAO;
import be.ugent.degage.db.models.Message;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import static be.ugent.degage.db.jdbc.JDBCUserDAO.USER_HEADER_FIELDS;

/**
 * Created by Stefaan Vermassen on 22/03/14.
 */
class JDBCMessageDAO extends AbstractDAO implements MessageDAO {

    public JDBCMessageDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static Message populateMessage(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("message_id"),
                rs.getBoolean("message_read"),
                JDBCUserDAO.populateUserHeader(rs),
                rs.getString("message_subject"),
                rs.getString("message_body"),
                new DateTime(rs.getTimestamp("message_created_at")
                )
        );
    }

    private Iterable<Message> listMessagesAux(LazyStatement stat, int userId, int page, int pageSize) throws DataAccessException {
        try {
            PreparedStatement ps = stat.value();
            ps.setInt(1, userId);
            int first = (page-1)*pageSize;
            ps.setInt(2, first);
            ps.setInt(3, pageSize);
            Collection<Message> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(populateMessage(rs));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of messages", ex);
        }
    }

    private int countMessagesAux(LazyStatement stat, int userId) throws DataAccessException {
        try {
            PreparedStatement ps = stat.value();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt ("number_of_messages");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve count of messages", ex);
        }
    }

    private LazyStatement listMessagesFromStatement = new LazyStatement (
            "SELECT message_id, message_from_user_id, message_to_user_id, " +
                    "message_read, message_subject, message_body, message_created_at, " +
                    USER_HEADER_FIELDS +
            "FROM messages JOIN users ON user_id = message_to_user_id "  +
            "WHERE message_from_user_id = ? " +
            "ORDER BY message_created_at DESC LIMIT ?,?"
    );

    @Override
    public Iterable<Message> listMessagesFrom(int senderId, int page, int pageSize) throws DataAccessException {
        return listMessagesAux(listMessagesFromStatement, senderId, page, pageSize);
    }

    private LazyStatement countMessagesFromStatement = new LazyStatement (
            "SELECT count(*) AS number_of_messages FROM messages "  +
            "WHERE message_from_user_id = ?"
    );

    @Override
    public int countMessagesFrom(int senderId) throws DataAccessException {
        return countMessagesAux(countMessagesFromStatement, senderId);
    }


    private LazyStatement listMessagesToStatement = new LazyStatement (
            "SELECT message_id, message_from_user_id, message_to_user_id, " +
                    "message_read, message_subject, message_body, message_created_at, " +
                    USER_HEADER_FIELDS +
            "FROM messages JOIN users ON user_id = message_from_user_id "  +
            "WHERE message_to_user_id = ? " +
            "ORDER BY message_created_at DESC LIMIT ?,?"
    );


    @Override
    public Iterable<Message> listMessagesTo(int receiverId, int page, int pageSize) throws DataAccessException {
        return listMessagesAux(listMessagesToStatement, receiverId, page, pageSize);
    }


    private LazyStatement countMessagesToStatement = new LazyStatement (
            "SELECT count(*) AS number_of_messages FROM messages "  +
            "WHERE message_to_user_id = ?"
    );

    @Override
    public int countMessagesTo(int receiverId) throws DataAccessException {
        return countMessagesAux(countMessagesToStatement, receiverId);
    }

    // TODO: lot of code in common with listMessagesTo
    private LazyStatement listUnreadMessagesToStatement = new LazyStatement (
            "SELECT message_id, message_from_user_id, message_to_user_id, " +
                    "message_read, message_subject, message_body, message_created_at, " +
                    USER_HEADER_FIELDS +
            "FROM messages JOIN users ON user_id = message_from_user_id "  +
            "WHERE message_to_user_id = ? AND NOT message_read " +
            "ORDER BY message_created_at DESC LIMIT ?"
    );

    @Override
    public Iterable<Message> listUnreadMessagesTo(int receiverId, int limit) throws DataAccessException {
        try {
            PreparedStatement ps = listUnreadMessagesToStatement.value();
            ps.setInt(1, receiverId);
            ps.setInt(2, limit);
            Collection<Message> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(populateMessage(rs));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of messages", ex);
        }
    }
    private LazyStatement getNumberOfUnreadMessagesStatement = new LazyStatement (
            "SELECT COUNT(*) AS number_of_messages FROM messages " +
                    "WHERE message_to_user_id=? AND message_read=0"
    );

    @Override
    public int countUnreadMessagesTo(int receiverId) throws DataAccessException {
        try {
            PreparedStatement ps = getNumberOfUnreadMessagesStatement.value();
            ps.setInt(1, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("number_of_messages");
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the number of unread messages", e);
        }
    }

    private LazyStatement createMessageStatement = new LazyStatement (
            "INSERT INTO messages (message_from_user_id, message_to_user_id, " +
                    "message_read, message_subject, message_body) VALUES (?,?,false,?,?)"
    );

    @Override
    public void createMessage(int senderId, int receiverId, String subject, String body) throws DataAccessException {
        try{
            PreparedStatement ps = createMessageStatement.value();
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, subject);
            ps.setString(4,body);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating message.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to create message", e);
        }
    }

    private LazyStatement setReadStatement = new LazyStatement (
            "UPDATE messages SET message_read = true WHERE message_id = ?"
    );

    @Override
    public void markMessageAsRead(int messageID) throws DataAccessException {
        try {
            PreparedStatement ps = setReadStatement.value();
            ps.setInt(1,messageID);
            // It is possible that no rows are affected but that is not a problem
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to mark message as read", e);
        }
    }

    private LazyStatement setAllReadStatement = new LazyStatement (
            "UPDATE messages SET message_read = true WHERE message_to_user_id = ?"
    );

    @Override
    public void markAllMessagesAsRead(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = setAllReadStatement.value();
            ps.setInt(1,userId);
            // It is possible that no rows are affected but that is not a problem
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to mark message as read", e);
        }
    }

    private LazyStatement getReplyHeaderStatement = new LazyStatement(
            "SELECT message_subject, " + USER_HEADER_FIELDS +
            "FROM messages JOIN users ON user_id = message_from_user_id "  +
            "WHERE message_id = ? "
    );

    @Override
    public Message getReplyHeader(int messageId) throws DataAccessException {
        try {
            PreparedStatement ps = getReplyHeaderStatement.value();
            ps.setInt(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Message(
                        messageId,
                        false,
                        JDBCUserDAO.populateUserHeader(rs),
                        rs.getString("message_subject"),
                        null,
                        null
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve message", e);
        }
    }
}
