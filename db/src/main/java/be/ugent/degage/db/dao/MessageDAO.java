package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Message;
import be.ugent.degage.db.models.User;

import java.util.List;

/**
 *
 */
public interface MessageDAO {

    /**
     * List all messages with the given receiver
     */
    public Iterable<Message> listMessagesTo (int receiverId, int page, int pageSize) throws DataAccessException;

    /**
     * Count all messages with the given receiver
     */
    public int countMessagesTo (int receiverId) throws DataAccessException;

    /**
     * List all messages with the given sender
     */
    public Iterable<Message> listMessagesFrom (int senderId, int page, int pageSize) throws DataAccessException;

   /**
     * Count all messages with the given sender
     */
    public int countMessagesFrom (int senderId) throws DataAccessException;

    /**
     * List all unread messages with the given receiver, but not paged and restricted to a maximum number
     */
    public Iterable<Message> listUnreadMessagesTo (int receiverId, int limit) throws DataAccessException;

    /**
     * Count all unread messages with the given receiver
     */
    public int countUnreadMessagesTo(int receiverId) throws DataAccessException;

    /**
     * Create a message
     */
    public void createMessage(int senderId, int receiverId, String subject, String body) throws DataAccessException;

    /**
     * Mark a single message as read
     */
    public void markMessageAsRead(int messageID) throws DataAccessException;

    /**
     * Mark all message for a given user as read.
     */
    public void markAllMessagesAsRead(int userId) throws DataAccessException;

    /**
     * Return sender and subject of the given message. Used for sending a reply
     */
    public Message getReplyHeader (int messageId) throws DataAccessException;

}
