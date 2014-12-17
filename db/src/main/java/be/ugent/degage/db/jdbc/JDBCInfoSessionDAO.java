/* JDBCInfoSessionDAO.java
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
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.InfoSessionDAO;
import be.ugent.degage.db.models.*;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

import static be.ugent.degage.db.jdbc.JDBCUserDAO.USER_HEADER_FIELDS;

/**
 * JDBC implementation of {@link be.ugent.degage.db.dao.InfoSessionDAO}
 */
class JDBCInfoSessionDAO extends AbstractDAO implements InfoSessionDAO {

    private static String INFOSESSION_FIELDS = "ses.infosession_id, infosession_type, infosession_timestamp, infosession_max_enrollees, infosession_comments, " +
            "address_id, address_country, address_city, address_zipcode, address_street, address_number, " +
            USER_HEADER_FIELDS ;

    private static String SUBTTOTAL_QUERY =
                "LEFT JOIN (SELECT COUNT(*) AS total, infosession_id " +
                "           FROM infosessionenrollees GROUP BY infosession_id) AS sub ON (ses.infosession_id = sub.infosession_id) ";

    private static String INFOSESSION_SELECTOR = "SELECT " + INFOSESSION_FIELDS + ", sub.total FROM infosessions AS ses " +
            "JOIN users ON infosession_host_user_id = user_id " +
            "JOIN addresses ON infosession_address_id = address_id " + SUBTTOTAL_QUERY;

    private static String FILTER_FRAGMENT = "WHERE ses.infosession_timestamp > ? AND ses.infosession_timestamp < ?"; // TODO: get something to filter on

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if (filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = createInfoSessionFilter();
        }

        ps.setString(start, filter.getValue(FilterField.FROM));
        ps.setString(start + 1, filter.getValue(FilterField.UNTIL));
        // TODO get something to filter on
    }

    public JDBCInfoSessionDAO(JDBCDataAccessContext context) {
        super(context);
    }

    // TODO: check what kind of 'partial' is really needed in each case

    /**
     * @param rs The resultset that contains the necessary information
     * @return An Infosession with the information from the resultset
     * @throws SQLException
     */
    public static InfoSession populateInfoSession(ResultSet rs)  throws SQLException {
        InfoSession result = populateInfoSessionPartial(rs);
        result.setAddress(JDBCAddressDAO.populateAddress(rs));
        result.setHost(JDBCUserDAO.populateUserHeader(rs));
        result.setEnrolleeCount(rs.getInt("sub.total"));
        return result;
    }

    public static InfoSession populateInfoSessionPartial(ResultSet rs) throws SQLException {

        return new InfoSession(
                rs.getInt("ses.infosession_id"),
                InfoSessionType.valueOf(rs.getString("infosession_type")),
                rs.getTimestamp("infosession_timestamp").toInstant(),
                null,
                null,
                rs.getInt("infosession_max_enrollees"),
                rs.getString("infosession_comments"));
    }

    @Override
    public Filter createInfoSessionFilter() {
        return new JDBCFilter();
    }

    private LazyStatement createInfoSessionStatement = new LazyStatement(
            "INSERT INTO infosessions(infosession_type, infosession_timestamp, " +
                    "infosession_address_id, infosession_host_user_id, infosession_max_enrollees, infosession_comments) " +
                    "VALUES (?,?,?,?,?,?)",
            "infosession_id"
    );

    @Override
    public InfoSession createInfoSession(InfoSessionType type, UserHeader host, Address address,
                                         Instant time, int maxEnrollees, String comments) throws DataAccessException {
        if (host.getId() == 0 || address.getId() == 0)
            throw new DataAccessException("Tried to create infosession without user or address");

        try {
            PreparedStatement ps = createInfoSessionStatement.value();
            ps.setString(1, type.name());
            ps.setTimestamp(2, Timestamp.from(time));
            ps.setInt(3, address.getId());
            ps.setInt(4, host.getId());
            ps.setInt(5, maxEnrollees);
            ps.setString(6, comments);

            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating infosession.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new InfoSession(keys.getInt(1), type, time, address, host, maxEnrollees, comments);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new infosession.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create infosession.", ex);
        }
    }

    private LazyStatement getInfoSessionById = new LazyStatement(
            INFOSESSION_SELECTOR + " WHERE ses.infosession_id = ?"
    );

    @Override
    public InfoSession getInfoSession(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getInfoSessionById.value();
            ps.setInt(1, id);
            InfoSession is;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    is = populateInfoSession(rs);
                } else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading infosession resultset", ex);
            }
            return is;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch infosession by id.", ex);
        }
    }

    private LazyStatement getAmountOfAttendeesForSession = new LazyStatement(
            "SELECT COUNT(*) AS amount_of_attendees " +
                    "FROM infosessionenrollees WHERE infosession_id = ?"
    );

    @Override
    public int getAmountOfAttendees(int infosessionId) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfAttendeesForSession.value();
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

    private LazyStatement deleteInfoSessionStatement = new LazyStatement(
            "DELETE FROM infosessions WHERE infosession_id = ?"
    );

    @Override
    public void deleteInfoSession(int id) throws DataAccessException {
        try {
            PreparedStatement ps = deleteInfoSessionStatement.value();
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting infosession.");
        } catch (SQLException ex) {
            throw new DataAccessException("Could not delete infosession", ex);
        }
    }

    private LazyStatement getAmountOfInfoSessionsStatement = new LazyStatement(
            "SELECT COUNT(ses.infosession_id) AS amount_of_infosessions FROM infosessions AS ses " + FILTER_FRAGMENT
    );

    /**
     * @param filter The filter to apply to
     * @return The amount of filtered infosessions, SINCE TODAY
     * @throws DataAccessException
     */
    @Override
    public int getNumberOfInfoSessions(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfInfoSessionsStatement.value();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("amount_of_infosessions");
                else
                    return 0;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of infosessions", ex);
        }
    }

    private static String GET_INFO_SESSIONS_HEAD =
            "SELECT infosession_id, infosession_type, " +
                    "infosession_timestamp, infosession_max_enrollees, infosession_comments, " +
                    "address_id, address_country, address_city, address_zipcode, address_street, address_number, " +
                    USER_HEADER_FIELDS +
                    ", enrollee_count " +
                    "FROM infosessions_extended ";

    private static String GET_INFO_SESSIONS_ALL =
            GET_INFO_SESSIONS_HEAD + " ORDER BY infosession_timestamp";

    private static String GET_INFO_SESSIONS_UPCOMING =
            GET_INFO_SESSIONS_HEAD + " WHERE infosession_timestamp > NOW() ORDER BY infosession_timestamp";


    private static InfoSession getInfoSessionFromResultSet (ResultSet rs) throws SQLException{
        Address address = new Address(
                rs.getInt("address_id"),
                rs.getString("address_country"),
                rs.getString("address_zipcode"),
                rs.getString("address_city"),
                rs.getString("address_street"),
                rs.getString("address_number")
        );

        UserHeader host = JDBCUserDAO.populateUserHeader(rs);

        InfoSession infoSession = new InfoSession(
                rs.getInt("infosession_id"),
                InfoSessionType.valueOf(rs.getString("infosession_type")),
                rs.getTimestamp("infosession_timestamp").toInstant(),
                address, host,
                rs.getInt("infosession_max_enrollees"),
                rs.getString("infosession_comments")
        );
        infoSession.setEnrolleeCount(rs.getInt("enrollee_count"));
        return infoSession;
    }


    /**
     */
    @Override
    public Iterable<InfoSession> getInfoSessions(boolean onlyUpcoming) throws DataAccessException {
        String query = onlyUpcoming ? GET_INFO_SESSIONS_UPCOMING : GET_INFO_SESSIONS_ALL;
        try (Statement stat = createStatement();
             ResultSet rs = stat.executeQuery(query)) {
            Collection<InfoSession> infosessions = new ArrayList<>();
            while (rs.next()) {
                infosessions.add(getInfoSessionFromResultSet(rs));
            }
            return infosessions;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of infosessions", ex);
        }
    }

    private LazyStatement registerUserForSessionStatement = new LazyStatement(
            "INSERT INTO infosessionenrollees(infosession_id, infosession_enrollee_id) VALUES (?,?)"
    );

    @Override
    public void registerUser(int sessionId, int userId) throws DataAccessException {
        // TODO: do not allow registration if already registered
        try {
            PreparedStatement ps = registerUserForSessionStatement.value();
            ps.setInt(1, sessionId);
            ps.setInt(2, userId);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Failed to register user to infosession. 0 rows affected.");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to prepare statement for user registration with infosession.", ex);
        }
    }

    private LazyStatement getUserEnrollmentStatusForSession = new LazyStatement(
        "SELECT infosession_enrollment_status FROM infosessionenrollees " +
                "WHERE infosession_enrollee_id = ? AND infosession_id = ?"
    );

    @Override
    public EnrollementStatus getUserEnrollmentStatus(int sessionId, int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getUserEnrollmentStatusForSession.value();
            ps.setInt(1, userId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return EnrollementStatus.valueOf(rs.getString("infosession_enrollment_status"));
                } else
                    return null;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to retrieve enrollment status.", ex);
        }
    }

    private LazyStatement setUserEnrollmentStatusForSession = new LazyStatement(
            "UPDATE infosessionenrollees SET infosession_enrollment_status = ? " +
                    "WHERE infosession_enrollee_id = ? AND infosession_id = ?"
    );

    @Override
    public void setUserEnrollmentStatus(int sessionId, int userId, EnrollementStatus status) throws DataAccessException {
        try {
            PreparedStatement ps = setUserEnrollmentStatusForSession.value();
            ps.setString(1, status.name());
            ps.setInt(2, userId);
            ps.setInt(3, sessionId);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Failed to update enrollment status. Affected rows = 0");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update enrollment status.", ex);
        }
    }

    private LazyStatement unregisterUserForSessionStatement = new LazyStatement(
            "DELETE FROM infosessionenrollees WHERE infosession_id = ? AND infosession_enrollee_id = ?"
    );

    @Override
    public void unregisterUser(int infoSessionId, int userId) throws DataAccessException {
        try {
            PreparedStatement ps = unregisterUserForSessionStatement.value();
            ps.setInt(1, infoSessionId);
            ps.setInt(2, userId);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Failed to unregister user from infosession.");

        } catch (SQLException ex) {
            throw new DataAccessException("Invalid unregister query for infosession.", ex);
        }
    }

    private LazyStatement getAttendingInfosessionStatement = new LazyStatement(
            GET_INFO_SESSIONS_HEAD +
                    " JOIN infosessionenrollees USING(infosession_id) " +
                    " WHERE infosession_enrollee_id = ? AND infosession_timestamp > NOW() " +
                    " ORDER BY infosession_timestamp"
    );

    /**
     * @param userId The user
     * @return The infosession after this time, the user is enrolled in
     * @throws DataAccessException
     */
    @Override
    public InfoSession getAttendingInfoSession(int userId) throws DataAccessException {
        try (PreparedStatement ps = getAttendingInfosessionStatement.value()) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getInfoSessionFromResultSet(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch infosession for user", ex);
        }
    }

    private LazyStatement updateInfoSessionStatement = new LazyStatement(
            "UPDATE infosessions SET infosession_type=?, infosession_max_enrollees=?, " +
                    "infosession_timestamp=?, infosession_address_id=?, infosession_host_user_id=?, infosession_comments=? " +
                    "WHERE infosession_id=?"
    );

    /*
     * Updates timestamp, type and max enrollees 
     * for updating address see updateInfoSessionAddress(InfoSession)
     */
    @Override
    public void updateInfoSession(InfoSession session) throws DataAccessException {
        if (session.getId() == 0)
            throw new DataAccessException("Failed to update session. Session doesn't exist in be.ugent.degage.database.");
        try {
            PreparedStatement ps = updateInfoSessionStatement.value();
            ps.setString(1, session.getType().name());
            ps.setInt(2, session.getMaxEnrollees());
            ps.setTimestamp(3, Timestamp.from(session.getTime()));
            ps.setInt(4, session.getAddress().getId());
            ps.setInt(5, session.getHost().getId());
            ps.setString(6, session.getComments());
            ps.setInt(7, session.getId());
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("InfoSession update did not affect any row.");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch infosession for user", ex);
        }
    }

    private LazyStatement getLastInfoSessionForUserStatement = new LazyStatement(
            "SELECT sub.total, ie.infosession_enrollment_status status, ses.infosession_id, infosession_type, infosession_timestamp, infosession_max_enrollees, infosession_comments, " +
                    "address_id, address_country, address_city, address_zipcode, address_street, address_number, " +
                    USER_HEADER_FIELDS + " FROM infosessionenrollees ie " +
                    "JOIN infosessions AS ses ON ie.infosession_id = ses.infosession_id " +
                    "JOIN users ON infosession_host_user_id = user_id " +
                    "JOIN addresses ON infosession_address_id = address_id " +
                    "LEFT JOIN ( SELECT COUNT(*) AS total, ie2.infosession_id " +
                    "            FROM infosessionenrollees ie2 GROUP BY ie2.infosession_id) sub ON (ie.infosession_id = sub.infosession_id) " +
                    "WHERE infosession_enrollee_id = ? ORDER BY infosession_timestamp DESC LIMIT 1"
    );

    @Override
    public Tuple<InfoSession, EnrollementStatus> getLastInfoSession(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getLastInfoSessionForUserStatement.value();
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Tuple<>(populateInfoSession(rs), EnrollementStatus.valueOf(EnrollementStatus.class, rs.getString("status")));
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch last for user", ex);
        }
    }

    private LazyStatement getAttendeesForSessionStatement = new LazyStatement(
            "SELECT " + USER_HEADER_FIELDS +  ", infosession_enrollment_status " +
                    "FROM infosessionenrollees " +
                    "INNER JOIN users ON user_id = infosession_enrollee_id " +
                    "WHERE infosession_id = ?"
    );

    @Override
    public Iterable<Enrollee> getEnrollees(int infosessionId) throws DataAccessException {
        try {
            PreparedStatement ps = getAttendeesForSessionStatement.value();
            ps.setInt(1, infosessionId);
            try (ResultSet rs = ps.executeQuery()) {
                Collection<Enrollee> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Enrollee(
                            JDBCUserDAO.populateUserHeader(rs),
                            EnrollementStatus.valueOf(rs.getString("infosession_enrollment_status"))
                    ));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not find enrollees", ex);
        }
    }

}
