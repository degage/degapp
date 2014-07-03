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
 *
 * @author Laurent
 */
public interface InfoSessionDAO {

    public Filter createInfoSessionFilter();

    public InfoSession createInfoSession(InfoSessionType type, String typeAlternative, User host, Address address, DateTime time, int maxEnrollees, String comments) throws DataAccessException;
    public InfoSession getInfoSession(int id, boolean withAttendees) throws DataAccessException;
    public int getAmountOfAttendees(int infosessionId) throws DataAccessException;
    public boolean deleteInfoSession(int id) throws DataAccessException;

    public int getAmountOfInfoSessions(Filter filter) throws DataAccessException;
    public List<InfoSession> getInfoSessions(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    public void registerUser(InfoSession session, User user) throws DataAccessException;
    public void setUserEnrollmentStatus(InfoSession session, User user, EnrollementStatus status) throws DataAccessException;
    public void unregisterUser(InfoSession session, User user) throws DataAccessException;
    public void unregisterUser(int infoSessionId, int userId) throws DataAccessException;
    public InfoSession getAttendingInfoSession(User user) throws DataAccessException;

    public void updateInfoSession(InfoSession session) throws DataAccessException;
    public Tuple<InfoSession, EnrollementStatus> getLastInfoSession(User user) throws DataAccessException;
}
