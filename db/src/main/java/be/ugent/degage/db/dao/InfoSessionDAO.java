/* InfoSessionDAO.java
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
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

import java.time.Instant;
import java.util.List;

/**
 * Data access object for information sessions
 */
public interface InfoSessionDAO {

    // info session

    /**
     * Create a new info session.
     * @return the id of the new session
     */
    public int createInfoSession(InfoSessionType type, int hostId, Address address,
                                         Instant time, int maxEnrollees, String comments, String description) throws DataAccessException;

    public InfoSession getInfoSession(int id) throws DataAccessException;

    public void updateInfoSession(int sessionId, InfoSessionType type, int maxEnrollees, Instant time, int hostId,
                                  String comments, Address address, String description) throws DataAccessException;

    public void deleteInfoSession(int id) throws DataAccessException;

    // filtered

    /**
     * Return the list of upcoming infosessions, ordered by date
     */
    public Iterable<InfoSession> getUpcomingInfoSessions() throws DataAccessException;

    /**
     * Return a page of infosessions. Only those infosessions are listed that will occur after the current instant,
     * ordered with dates increasing, address field filled in, but not the membership count
     */
    public Page<InfoSession> getFutureInfoSessions(FilterField orderBy, boolean asc, int page, int pageSize) throws DataAccessException;

    /**
     * Return a page of infosessions. Only those infosessions are listed that occurred in the past, ordered with
     * dates decreasing, membership count is filled in, address and comment fields are not
     */
    public Page<InfoSession> getPastInfoSessions(FilterField orderBy, boolean asc, int page, int pageSize) throws DataAccessException;

    // attendees

    public int getAmountOfAttendees(int infosessionId) throws DataAccessException;

    // enrollees

    public int getAmountOfEnrollees(int infosessionId) throws DataAccessException;
    public Iterable<Enrollee> getEnrollees (int infosessionId) throws DataAccessException;

    /**
     * @return false if user already registered for this session
     *
     * @throws DataAccessException
     */
    public boolean registerUser(int sessionId, int userId) throws DataAccessException;
    public void unregisterUser(int sessionId, int userId) throws DataAccessException;

    public EnrollmentStatus getUserEnrollmentStatus (int sessionId, int userId) throws DataAccessException;
    public void setUserEnrollmentStatus(int sessionId, int userId, EnrollmentStatus status) throws DataAccessException;

    /**
     * @param userId The user
     * @return The infosession the user is enrolled in or null if no such session. Only future sessions are considered
     * @throws DataAccessException
     */
    public InfoSession getAttendingInfoSession(int userId) throws DataAccessException;

    /**
     * Return the id of the infosession where the given user was present, or 0 if
     * none found.
     */
    public int getInfoSessionWherePresent(int userId) throws DataAccessException;

    public static class LastSessionResult {
        public InfoSession session;
        public boolean present;
    }

    /**
     * Retreive the last infosession for which a user enrolled and information on whether
     * the user was present on that session. If the user was not present and the session was
     * in the past, then no session is returned.
     */
    public LastSessionResult getLastInfoSession(int userId) throws DataAccessException;

    public List<InfoSession> getInfoSessionsOfYesterday() throws DataAccessException;

}
