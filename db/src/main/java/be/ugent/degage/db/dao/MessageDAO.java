/* MessageDAO.java
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

package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Message;
import be.ugent.degage.db.models.Page;

/**
 *
 */
public interface MessageDAO {

    /**
     * List all messages with the given receiver
     */
    public Page<Message> listMessagesTo (int receiverId, int page, int pageSize) throws DataAccessException;

    /**
     * List all messages with the given sender
     */
    public Page<Message> listMessagesFrom (int senderId, int page, int pageSize) throws DataAccessException;

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
