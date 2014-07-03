package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.dao.InfoSessionDAO;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 2/22/14.
 */
public class JDBCInfoSessionDAO implements InfoSessionDAO {

    private Connection connection;

    private static String INFOSESSION_FIELDS = "infosession_id, infosession_type, infosession_type_alternative, infosession_timestamp, infosession_max_enrollees, infosession_comments, " +
            "address_id, address_country, address_city, address_zipcode, address_street, address_street_number, address_street_bus, " +
            "user_id, user_firstname, user_lastname, user_phone, user_email, user_status";

    private static String INFOSESSION_SELECTOR = "SELECT " + INFOSESSION_FIELDS + " FROM infosessions " +
            "JOIN users ON infosession_host_user_id = user_id " +
            "JOIN addresses ON infosession_address_id = address_id";


    // Also includes the # of attendees, this query is way too complex
    private static String INFOSESSION_QUERY = "SELECT IFNULL(sub.total, 0) going, ses.infosession_id infosession_id, ses.infosession_type infosession_type, ses.infosession_type_alternative infosession_type_alternative, " +
            "ses.infosession_timestamp infosession_timestamp, ses.infosession_max_enrollees infosession_max_enrollees, ses.infosession_comments infosession_comments, " +
            "address_id, address_country, address_city, address_zipcode, address_street, address_street_number, address_street_bus, " +
            "user_id, user_firstname, user_lastname, user_phone, user_email, user_status FROM infosessions ses " +
            "JOIN users ON infosession_host_user_id = user_id " +
            "JOIN addresses ON infosession_address_id = address_id " +
            "LEFT JOIN (SELECT COUNT(*) total, infosession_id FROM infosessionenrollees GROUP BY infosession_id) sub ON (ses.infosession_id = sub.infosession_id) ";

    private static String FILTER_FRAGMENT = "WHERE ses.infosession_timestamp > ? AND ses.infosession_timestamp < ?"; // TODO: get something to filter on

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = createInfoSessionFilter();
        }

        ps.setString(start, filter.getValue(FilterField.FROM));
        ps.setString(start+1, filter.getValue(FilterField.UNTIL));
        // TODO get something to filter on
    }

    private PreparedStatement deleteInfoSession;
    private PreparedStatement createInfoSessionStatement;
    private PreparedStatement getInfoSessionsAfterPageByDateAscStatement;
    private PreparedStatement getInfoSessionsAfterPageByDateDescStatement;
    private PreparedStatement getInfoSessionById;
    private PreparedStatement getInfosessionForUser;
    private PreparedStatement registerUserForSession;
    private PreparedStatement unregisterUserForSession;
    private PreparedStatement getAttendeesForSession;
    private PreparedStatement getAmountOfAttendeesForSession;
    private PreparedStatement setUserEnrollmentStatusForSession;
    private PreparedStatement updateInfoSession;
    private PreparedStatement getGetAmountOfInfoSessionsStatement;
    private PreparedStatement setInfosessionHostStatement;
    private PreparedStatement getLastInfoSessionForUserStatement;

    public JDBCInfoSessionDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getDeleteInfoSessionStatement() throws SQLException {
        if (deleteInfoSession == null) {
            deleteInfoSession = connection.prepareStatement("DELETE FROM infosessions WHERE infosession_id = ?");
        }
        return deleteInfoSession;
    }
    
    private PreparedStatement getUpdateInfoSessionStatement() throws SQLException {
    	if(updateInfoSession==null){
    		updateInfoSession = connection.prepareStatement("UPDATE infosessions SET infosession_type=?, infosession_type_alternative=?, infosession_max_enrollees=?, infosession_timestamp=?, infosession_address_id=?, infosession_host_user_id=?, infosession_comments=? WHERE infosession_id=?");
    	}
    	return updateInfoSession;
    }

    private PreparedStatement getGetLastInfoSessionForUserStatement() throws SQLException {
        if(getLastInfoSessionForUserStatement == null){
            getLastInfoSessionForUserStatement = connection.prepareStatement("SELECT IFNULL(sub.total, 0) going, ie.infosession_enrollment_status status, ie.infosession_id, infosession_type, infosession_type_alternative, infosession_timestamp, infosession_max_enrollees, infosession_comments, " +
                    "address_id, address_country, address_city, address_zipcode, address_street, address_street_number, address_street_bus, " +
                    "user_id, user_firstname, user_lastname, user_phone, user_email, user_status FROM infosessionenrollees ie " +
                    "JOIN infosessions ON ie.infosession_id = infosessions.infosession_id " +
                    "JOIN users ON infosession_host_user_id = user_id " +
                    "JOIN addresses ON infosession_address_id = address_id " +
                    "LEFT JOIN ( SELECT COUNT(*) total, ie2.infosession_id FROM infosessionenrollees ie2 GROUP BY ie2.infosession_id) sub ON (ie.infosession_id = sub.infosession_id) " +
                    "WHERE infosession_enrollee_id = ? ORDER BY infosession_timestamp DESC LIMIT 1");
        }
        return getLastInfoSessionForUserStatement;
    }

    private PreparedStatement getSetUserEnrollmentStatusForSession() throws SQLException {
        if (setUserEnrollmentStatusForSession == null) {
            setUserEnrollmentStatusForSession = connection.prepareStatement("UPDATE infosessionenrollees SET infosession_enrollment_status = ? WHERE infosession_enrollee_id = ? AND infosession_id = ?");
        }
        return setUserEnrollmentStatusForSession;
    }

    private PreparedStatement getGetInfoSessionForUserStatement() throws SQLException {
        if (getInfosessionForUser == null) {
            getInfosessionForUser = connection.prepareStatement("SELECT IFNULL(sub.total, 0) going, ie.infosession_id, infosession_type, infosession_type_alternative, infosession_timestamp, infosession_max_enrollees, infosession_comments, " +
                    "address_id, address_country, address_city, address_zipcode, address_street, address_street_number, address_street_bus, " +
                    "user_id, user_firstname, user_lastname, user_phone, user_email, user_status FROM infosessionenrollees ie " +
                    "JOIN infosessions ON ie.infosession_id = infosessions.infosession_id " +
                    "JOIN users ON infosession_host_user_id = user_id " +
                    "JOIN addresses ON infosession_address_id = address_id " +
                    "LEFT JOIN ( SELECT COUNT(*) total, ie2.infosession_id FROM infosessionenrollees ie2 GROUP BY ie2.infosession_id) sub ON (ie.infosession_id = sub.infosession_id) " +
                    "WHERE infosession_enrollee_id = ? AND infosession_timestamp > ?");
        }
        return getInfosessionForUser;
    }

    public PreparedStatement getGetAttendeesForSession() throws SQLException {
        if (getAttendeesForSession == null) {
            getAttendeesForSession = connection.prepareStatement("SELECT user_id, user_firstname, user_email, user_lastname, infosession_enrollment_status " +
                    "FROM infosessionenrollees INNER JOIN users ON user_id = infosession_enrollee_id WHERE infosession_id = ?");
        }
        return getAttendeesForSession;
    }

    public PreparedStatement getGetAmountOfAttendeesForSession() throws SQLException {
        if (getAmountOfAttendeesForSession == null) {
            getAmountOfAttendeesForSession = connection.prepareStatement("SELECT COUNT(*) AS amount_of_attendees " +
                    "FROM infosessionenrollees WHERE infosession_id = ?");
        }
        return getAmountOfAttendeesForSession;
    }

    private PreparedStatement getRegisterUserForSession() throws SQLException {
        if (registerUserForSession == null) {
            registerUserForSession = connection.prepareStatement("INSERT INTO infosessionenrollees(infosession_id, infosession_enrollee_id) VALUES (?,?)");
        }
        return registerUserForSession;
    }

    private PreparedStatement getUnregisterUserForSession() throws SQLException {
        if (unregisterUserForSession == null) {
            unregisterUserForSession = connection.prepareStatement("DELETE FROM infosessionenrollees WHERE infosession_id = ? AND infosession_enrollee_id = ?");
        }
        return unregisterUserForSession;
    }

    private PreparedStatement getCreateInfoSessionStatement() throws SQLException {
        if (createInfoSessionStatement == null) {
            createInfoSessionStatement = connection.prepareStatement("INSERT INTO infosessions(infosession_type, infosession_type_alternative, infosession_timestamp, infosession_address_id, infosession_host_user_id, infosession_max_enrollees, infosession_comments) VALUES (?,?,?,?,?,?,?)",
                    new String[]{"infosession_id"});
        }
        return createInfoSessionStatement;
    }

    private PreparedStatement getGetInfoSessionsAfterPageByDateAscStatement() throws SQLException {
        if (getInfoSessionsAfterPageByDateAscStatement == null) {
            // Also includes the # of attendees, this query is way too complex
            getInfoSessionsAfterPageByDateAscStatement = connection.prepareStatement(INFOSESSION_QUERY + FILTER_FRAGMENT +
                    "ORDER BY infosession_timestamp ASC LIMIT ?, ?");
        }
        return getInfoSessionsAfterPageByDateAscStatement;
    }
    private PreparedStatement getGetInfoSessionsAfterPageByDateDescStatement() throws SQLException {
        if (getInfoSessionsAfterPageByDateDescStatement == null) {
            // Also includes the # of attendees, this query is way too complex
            getInfoSessionsAfterPageByDateDescStatement = connection.prepareStatement(INFOSESSION_QUERY + FILTER_FRAGMENT +
                    "ORDER BY infosession_timestamp DESC LIMIT ?, ?");
        }
        return getInfoSessionsAfterPageByDateDescStatement;
    }

    private PreparedStatement getGetInfoSessionById() throws SQLException {
        if (getInfoSessionById == null) {
            getInfoSessionById = connection.prepareStatement(INFOSESSION_SELECTOR + " WHERE infosession_id = ?");
        }
        return getInfoSessionById;
    }

    private PreparedStatement getGetAmountOfInfoSessionsStatement() throws SQLException {
        if(getGetAmountOfInfoSessionsStatement == null) {
            // TODO: filter the WHERE statement
            getGetAmountOfInfoSessionsStatement = connection.prepareStatement("SELECT COUNT(ses.infosession_id) AS amount_of_infosessions FROM infosessions ses " + FILTER_FRAGMENT);
        }
        return getGetAmountOfInfoSessionsStatement;
    }

    /**
     *
     * @param rs The resultset that contains the necessary information
     * @return An Infosession with the information from the resultset without the list of enrollees
     * @throws SQLException
     */
    public static InfoSession populateInfoSession(ResultSet rs) throws SQLException {
        return populateInfoSession(rs, false, true);
    }

    /**
     *
     * @param rs The resultset that contains the necessary information
     * @param includesGoing Includes the enrollees too?
     * @return An Infosession with the information from the resultset
     * @throws SQLException
     */
    public static InfoSession populateInfoSession(ResultSet rs, boolean includesGoing, boolean withJoins) throws SQLException {
        int id = rs.getInt("infosession_id");
        InfoSessionType type = InfoSessionType.valueOf(rs.getString("infosession_type"));
        String typeAlternative = rs.getString("infosession_type_alternative");
        DateTime timestamp = new DateTime(rs.getTimestamp("infosession_timestamp"));
        Address address = null;
        User host = null;
        if(withJoins) {
            address = JDBCAddressDAO.populateAddress(rs);
            host = JDBCUserDAO.populateUser(rs, false, false);
        }
        int maxEnrollees = rs.getInt("infosession_max_enrollees");

        String commentaar = rs.getString("infosession_comments");
        InfoSession infoSession;
        if (includesGoing) {
            infoSession = new InfoSession(id, type, timestamp, address, host, rs.getInt("going"), maxEnrollees, commentaar);
        } else {
            infoSession = new InfoSession(id, type, timestamp, address, host, maxEnrollees, commentaar);
        }
        infoSession.setTypeAlternative(typeAlternative);

        return infoSession;
    }

    @Override
    public Filter createInfoSessionFilter() {
        return new JDBCFilter();
    }

    @Override
    public InfoSession createInfoSession(InfoSessionType type, String typeAlternative, User host, Address address, DateTime time, int maxEnrollees, String comments) throws DataAccessException {
        if (host.getId() == 0 || address.getId() == 0)
            throw new DataAccessException("Tried to create infosession without be.ugent.degage.database user / be.ugent.degage.database address");

        try {
            PreparedStatement ps = getCreateInfoSessionStatement();
            ps.setString(1, type.name());
            if(type.equals(InfoSessionType.OTHER)) {
                ps.setString(2, typeAlternative);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setTimestamp(3, new Timestamp(time.getMillis())); //TODO: timezones?? convert to datetime see below
            ps.setInt(4, address.getId());
            ps.setInt(5, host.getId());
            ps.setInt(6, maxEnrollees);
            ps.setString(7, comments);

            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating infosession.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new InfoSession(keys.getInt(1), type, time, address, host, InfoSession.NO_ENROLLEES, maxEnrollees, comments);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new infosession.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create infosession.", ex);
        }
    }

    @Override
    public InfoSession getInfoSession(int id, boolean withAttendees) throws DataAccessException {
        try {
            PreparedStatement ps = getGetInfoSessionById();
            ps.setInt(1, id);
            InfoSession is;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    is = populateInfoSession(rs);
                } else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading infosession resultset", ex);
            }

            if (withAttendees) {
                PreparedStatement ps2 = getGetAttendeesForSession();
                ps2.setInt(1, id);
                try (ResultSet rs = ps2.executeQuery()) {
                    while (rs.next()) {
                        is.addEnrollee(new Enrollee(new User(rs.getInt("user_id"), rs.getString("user_email"), rs.getString("user_firstname"), rs.getString("user_lastname")),
                                Enum.valueOf(EnrollementStatus.class, rs.getString("infosession_enrollment_status"))));
                    }
                } catch (SQLException ex) {
                    throw new DataAccessException("Failed to get attendees for infosession", ex);
                }
            }
            return is;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch infosession by id.", ex);
        }
    }

    @Override
    public int getAmountOfAttendees(int infosessionId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfAttendeesForSession();
            ps.setInt(1, infosessionId);
            int amount;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    amount = rs.getInt("amount_of_attendees");
                } else throw new DataAccessException("Could not get amount of attendees for infosession");
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading amount of attendees for infosession resultset", ex);
            }
            return amount;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get amount of attendees for infosession.", ex);
        }
    }

    @Override
    public boolean deleteInfoSession(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteInfoSessionStatement();
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting infosession.");
        } catch (SQLException ex) {
            throw new DataAccessException("Could not delete infosession", ex);
        }

        // Why do we have to return a boolean?
        return true;
    }

    /**
     *
     * @param filter The filter to apply to
     * @return The amount of filtered infosessions, SINCE TODAY
     * @throws DataAccessException
     */
    @Override
    public int getAmountOfInfoSessions(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfInfoSessionsStatement();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_infosessions");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of infosessions", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of infosessions", ex);
        }
    }

    /**
     *
     * @param orderBy The field you want to order by
     * @param asc Ascending
     * @param page The page you want to see
     * @param pageSize The page size
     * @param filter The filter you want to apply
     * @return List of infosessions with custom ordering and filtering
     * @throws DataAccessException
     */
    @Override
    public List<InfoSession> getInfoSessions(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = null;
            switch(orderBy) {
                case INFOSESSION_DATE:
                    ps = asc ? getGetInfoSessionsAfterPageByDateAscStatement() : getGetInfoSessionsAfterPageByDateDescStatement();
                    break;
            }
            if(ps == null) {
                throw new DataAccessException("Could not create getInfoSessions statement");
            }

            fillFragment(ps, filter, 1);
            int first = (page-1)*pageSize;
            ps.setInt(3, first);
            ps.setInt(4, pageSize);
            return getInfoSessions(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of infosessions", ex);
        }
    }

    @Override
    public void registerUser(InfoSession session, User user) throws DataAccessException {
        try {
            PreparedStatement ps = getRegisterUserForSession();
            ps.setInt(1, session.getId());
            ps.setInt(2, user.getId());
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Failed to register user to infosession. 0 rows affected.");
            session.addEnrollee(new Enrollee(user, EnrollementStatus.ENROLLED));

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to prepare statement for user registration with infosession.", ex);
        }
    }

    @Override
    public void setUserEnrollmentStatus(InfoSession session, User user, EnrollementStatus status) throws DataAccessException {
        if (session.getId() == 0 || user.getId() == 0)
            throw new DataAccessException("Cannot update enrollmentstatus for unsaved session or user.");
        try {
            PreparedStatement ps = getSetUserEnrollmentStatusForSession();
            ps.setString(1, status.name());
            ps.setInt(2, user.getId());
            ps.setInt(3, session.getId());
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Failed to update enrollment status. Affected rows = 0");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update enrollment status.", ex);
        }
    }

    @Override
    public void unregisterUser(InfoSession session, User user) throws DataAccessException {
        unregisterUser(session.getId(), user.getId());
    }

    @Override
    public void unregisterUser(int infoSessionId, int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getUnregisterUserForSession();
            ps.setInt(1, infoSessionId);
            ps.setInt(2, userId);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Failed to unregister user from infosession.");

        } catch (SQLException ex) {
            throw new DataAccessException("Invalid unregister query for infosession.", ex);
        }
    }

    /**
     *
     * @param user The user
     * @return The infosession after this time, the user is enrolled in
     * @throws DataAccessException
     */
    @Override
    public InfoSession getAttendingInfoSession(User user) throws DataAccessException {
        try {
            PreparedStatement ps = getGetInfoSessionForUserStatement();
            ps.setInt(1, user.getId());
            ps.setTimestamp(2, new Timestamp(DateTime.now().getMillis())); //TODO: pass date as argument instead of 'now' ??

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                else return populateInfoSession(rs, true, true);
            } catch (SQLException ex) {
                throw new DataAccessException("Invalid query for attending infosession.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch infosession for user", ex);
        }
    }
    
    /*
     * Updates timestamp, type and max enrollees 
     * for updating address see updateInfoSessionAddress(InfoSession)
     */
	@Override
	public void updateInfoSession(InfoSession session) throws DataAccessException {
        if (session.getId() == 0)
            throw new DataAccessException("Failed to update session. Session doesn't exist in be.ugent.degage.database.");
		try {
            PreparedStatement ps = getUpdateInfoSessionStatement();
            ps.setString(1,session.getType().toString());
            if(session.getType().equals(InfoSessionType.OTHER)) {
                ps.setString(2, session.getTypeAlternative());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setInt(3, session.getMaxEnrollees());
            ps.setTimestamp(4, new Timestamp(session.getTime().getMillis()));
            ps.setInt(5, session.getAddress().getId());
            ps.setInt(6, session.getHost().getId());
            ps.setString(7, session.getComments());
            ps.setInt(8, session.getId());
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("InfoSession update did not affect any row.");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch infosession for user", ex);
        }		
	}

    @Override
    public Tuple<InfoSession, EnrollementStatus> getLastInfoSession(User user) throws DataAccessException {
        try {
            PreparedStatement ps = getGetLastInfoSessionForUserStatement();
            ps.setInt(1, user.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                } else {
                    return new Tuple(populateInfoSession(rs, true, true), Enum.valueOf(EnrollementStatus.class, rs.getString("status")));
                }
            } catch (SQLException ex) {
                throw new DataAccessException("Invalid query for last infosession.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch last for user", ex);
        }
    }

    private List<InfoSession> getInfoSessions(PreparedStatement ps) {
        List<InfoSession> infosessions = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                infosessions.add(populateInfoSession(rs, true, true));
            }
            return infosessions;
        } catch (SQLException ex) {
            throw new DataAccessException("Error reading infosession resultset", ex);
        }
    }
}
