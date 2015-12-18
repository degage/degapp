/* JDBCMessageDAO.java
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
import be.ugent.degage.db.dao.MessageDAO;
import be.ugent.degage.db.models.Message;
import be.ugent.degage.db.models.Page;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
                rs.getTimestamp("message_created_at").toInstant()
        );
    }

    private Page<Message> listMessagesAux(String sql, int userId, int page, int pageSize) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, (page - 1) * pageSize);
            ps.setInt(3, pageSize);
            return toPage(ps, pageSize, JDBCMessageDAO::populateMessage);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public Page<Message> listMessagesFrom(int senderId, int page, int pageSize) throws DataAccessException {
        return listMessagesAux(
                "SELECT SQL_CALC_FOUND_ROWS message_id, message_from_user_id, message_to_user_id, " +
                        "message_read, message_subject, message_body, message_created_at, " +
                        USER_HEADER_FIELDS +
                        "FROM messages JOIN users ON user_id = message_to_user_id " +
                        "WHERE message_from_user_id = ? " +
                        "ORDER BY message_created_at DESC LIMIT ?,?",
                senderId, page, pageSize);
    }


    @Override
    public Page<Message> listMessagesTo(int receiverId, int page, int pageSize) throws DataAccessException {
        return listMessagesAux(
                "SELECT SQL_CALC_FOUND_ROWS message_id, message_from_user_id, message_to_user_id, " +
                        "message_read, message_subject, message_body, message_created_at, " +
                        USER_HEADER_FIELDS +
                        "FROM messages JOIN users ON user_id = message_from_user_id " +
                        "WHERE message_to_user_id = ? " +
                        "ORDER BY message_created_at DESC LIMIT ?,?",
                receiverId, page, pageSize);
    }

    @Override
    public Iterable<Message> listUnreadMessagesTo(int receiverId, int limit) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT message_id, message_from_user_id, message_to_user_id, " +
                        "message_read, message_subject, message_body, message_created_at, " +
                        USER_HEADER_FIELDS +
                        "FROM messages JOIN users ON user_id = message_from_user_id " +
                        "WHERE message_to_user_id = ? AND NOT message_read " +
                        "ORDER BY message_created_at DESC LIMIT ?"
        )) {
            ps.setInt(1, receiverId);
            ps.setInt(2, limit);
            return toList(ps, JDBCMessageDAO::populateMessage);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of messages", ex);
        }
    }

    private LazyStatement getNumberOfUnreadMessagesStatement = new LazyStatement(
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
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve the number of unread messages", e);
        }
    }

    @Override
    public void createMessage(int senderId, int receiverId, String subject, String body) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO messages (message_from_user_id, message_to_user_id, " +
                        "message_read, message_subject, message_body) VALUES (?,?,false,?,?)"
        )) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, subject);
            ps.setString(4, body);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create message", e);
        }
    }

    @Override
    public void markMessageAsRead(int messageID) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE messages SET message_read = true WHERE message_id = ?"
        )) {
            ps.setInt(1, messageID);
            // It is possible that no rows are affected but that is not a problem
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to mark message as read", e);
        }
    }

    @Override
    public void markAllMessagesAsRead(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE messages SET message_read = true WHERE message_to_user_id = ?"
        )) {
            ps.setInt(1, userId);
            // It is possible that no rows are affected but that is not a problem
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to mark message as read", e);
        }
    }

    @Override
    public Message getReplyHeader(int messageId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT message_subject, " + USER_HEADER_FIELDS +
                        "FROM messages JOIN users ON user_id = message_from_user_id " +
                        "WHERE message_id = ? "

        )) {
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
