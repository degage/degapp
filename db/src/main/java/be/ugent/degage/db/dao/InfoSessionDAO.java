/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Data access object for information sessions
 */
public interface InfoSessionDAO {

    // info session

    public InfoSession createInfoSession(InfoSessionType type, String typeAlternative, User host, Address address, DateTime time, int maxEnrollees, String comments) throws DataAccessException;
    public InfoSession getInfoSession(int id) throws DataAccessException;

    public void updateInfoSession(InfoSession session) throws DataAccessException;
    public void deleteInfoSession(int id) throws DataAccessException;

    // filtered

    public Filter createInfoSessionFilter();

    public Iterable<InfoSession> getInfoSessions(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    public int getNumberOfInfoSessions(Filter filter) throws DataAccessException;

    // attendees

    public int getAmountOfAttendees(int infosessionId) throws DataAccessException;
    public Iterable<Enrollee> getEnrollees (int infosessionId) throws DataAccessException;

    public void registerUser(int sessionId, int userId) throws DataAccessException;
    public void unregisterUser(int sessionId, int userId) throws DataAccessException;

    public EnrollementStatus getUserEnrollmentStatus (int sessionId, int userId) throws DataAccessException;
    public void setUserEnrollmentStatus(int sessionId, int userId, EnrollementStatus status) throws DataAccessException;

    public InfoSession getAttendingInfoSession(User user) throws DataAccessException;

    public Tuple<InfoSession, EnrollementStatus> getLastInfoSession(User user) throws DataAccessException;
}
