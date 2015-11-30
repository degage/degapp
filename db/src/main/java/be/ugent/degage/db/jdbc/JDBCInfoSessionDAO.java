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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import static be.ugent.degage.db.jdbc.JDBCUserDAO.USER_HEADER_FIELDS;

/**
 * JDBC implementation of {@link be.ugent.degage.db.dao.InfoSessionDAO}
 */
class JDBCInfoSessionDAO extends AbstractDAO implements InfoSessionDAO {

    private static String INFOSESSION_FIELDS = "ses.infosession_id, infosession_type, infosession_timestamp, infosession_max_enrollees, infosession_comments, " +
            "address_id, address_country, address_city, address_zipcode, address_street, address_number, " +
            USER_HEADER_FIELDS;

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
    public static InfoSession populateInfoSession(ResultSet rs) throws SQLException {
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

    @Override
    public InfoSession createInfoSession(InfoSessionType type, UserHeader host, Address address,
                                         Instant time, int maxEnrollees, String comments) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO infosessions(infosession_type, infosession_timestamp, " +
                        "infosession_host_user_id, infosession_max_enrollees, infosession_comments) " +
                        "VALUES (?,?,?,?,?)",
                "infosession_id"
        )) {
            ps.setString(1, type.name());
            ps.setTimestamp(2, Timestamp.from(time));
            ps.setInt(3, host.getId());
            ps.setInt(4, maxEnrollees);
            ps.setString(5, comments);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway

                int sessionId = keys.getInt(1);
                updateLocation(sessionId, address);
                return new InfoSession(sessionId, type, time, address, host, maxEnrollees, comments);

            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create infosession.", ex);
        }
    }

    private void updateLocation(int sessionId, Address location) {
        JDBCAddressDAO.updateLocation(
                getConnection(),
                "JOIN infosessions ON infosession_address_id=address_id", "infosession_id",
                sessionId, location
        );
    }

    @Override
    public InfoSession getInfoSession(int id) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                INFOSESSION_SELECTOR + " WHERE ses.infosession_id = ?"
        )) {
            ps.setInt(1, id);
            return toSingleObject(ps, JDBCInfoSessionDAO::populateInfoSession);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch infosession by id.", ex);
        }
    }


    @Override
    public int getAmountOfAttendees(int id) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT COUNT(*) FROM infosessionenrollees WHERE infosession_id = ?"
        )) {
            ps.setInt(1, id);
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get amount of attendees for infosession.", ex);
        }
    }

    @Override
    public void deleteInfoSession(int id) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "DELETE FROM infosessions WHERE infosession_id = ?"
        )) {
            ps.setInt(1, id);
            ps.executeUpdate();
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
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of infosessions", ex);
        }
    }

    private static String INFOSESSION_EXTENDED_FIELDS =
            "infosession_id, infosession_type, " +
                    "infosession_timestamp, infosession_max_enrollees, infosession_comments, " +
                    "address_id, address_country, address_city, address_zipcode, address_street, address_number, " +
                    USER_HEADER_FIELDS +
                    ", enrollee_count ";


    private static String GET_INFO_SESSIONS_HEAD =
            "SELECT " + INFOSESSION_EXTENDED_FIELDS + "FROM infosessions_extended ";

    private static InfoSession getInfoSessionFromResultSet(ResultSet rs) throws SQLException {
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
        String sql = GET_INFO_SESSIONS_HEAD;
        if (onlyUpcoming) {
            sql += " WHERE infosession_timestamp > NOW() ";
        }
        sql += " ORDER BY infosession_timestamp";
        try (PreparedStatement ps = prepareStatement(sql)) {
            return toList(ps, JDBCInfoSessionDAO::getInfoSessionFromResultSet);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of infosessions", ex);
        }
    }

    @Override
    public boolean registerUser(int sessionId, int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO infosessionenrollees(infosession_id, infosession_enrollee_id) VALUES (?,?)"
        )) {
            ps.setInt(1, sessionId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                return false;
            } else {
                throw new DataAccessException("Failed to register user.", ex);
            }
        }
    }

    @Override
    public EnrollementStatus getUserEnrollmentStatus(int sessionId, int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT infosession_enrollment_status FROM infosessionenrollees " +
                        "WHERE infosession_enrollee_id = ? AND infosession_id = ?"
        )) {
            ps.setInt(1, userId);
            ps.setInt(2, sessionId);
            return toSingleObject(ps,
                    rs -> EnrollementStatus.valueOf(rs.getString("infosession_enrollment_status")));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to retrieve enrollment status.", ex);
        }
    }

    @Override
    public void setUserEnrollmentStatus(int sessionId, int userId, EnrollementStatus status) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE infosessionenrollees SET infosession_enrollment_status = ? " +
                        "WHERE infosession_enrollee_id = ? AND infosession_id = ?"
        )) {
            ps.setString(1, status.name());
            ps.setInt(2, userId);
            ps.setInt(3, sessionId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update enrollment status.", ex);
        }
    }

    @Override
    public void unregisterUser(int infoSessionId, int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "DELETE FROM infosessionenrollees WHERE infosession_id = ? AND infosession_enrollee_id = ?"
        )) {
            ps.setInt(1, infoSessionId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Invalid unregister query for infosession.", ex);
        }
    }

    @Override
    public InfoSession getAttendingInfoSession(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                GET_INFO_SESSIONS_HEAD +
                        " JOIN infosessionenrollees USING(infosession_id) " +
                        " WHERE infosession_enrollee_id = ? AND infosession_timestamp > NOW() " +
                        " ORDER BY infosession_timestamp"
        )) {
            ps.setInt(1, userId);
            return toSingleObject(ps, JDBCInfoSessionDAO::getInfoSessionFromResultSet);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch infosession for user", ex);
        }
    }

    @Override
    public int getInfoSessionWherePresent(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT infosession_id FROM infosessionenrollees " +
                        " WHERE infosession_enrollee_id = ? AND infosession_enrollment_status = 'PRESENT' "
        )) {
            ps.setInt(1, userId);
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch infosession for user", ex);
        }
    }

    /*
     * Update infosession
     */
    @Override
    public void updateInfoSession(InfoSession session) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE infosessions SET infosession_type=?, infosession_max_enrollees=?, " +
                        "infosession_timestamp=?, infosession_host_user_id=?, infosession_comments=? " +
                        "WHERE infosession_id=?"
        )) {
            ps.setString(1, session.getType().name());
            ps.setInt(2, session.getMaxEnrollees());
            ps.setTimestamp(3, Timestamp.from(session.getTime()));
            ps.setInt(4, session.getHost().getId());
            ps.setString(5, session.getComments());

            int sessionId = session.getId();
            ps.setInt(6, sessionId);
            ps.executeUpdate();
            updateLocation(sessionId, session.getAddress());
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update infosession", ex);
        }
    }

    @Override
    public LastSessionResult getLastInfoSession(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + INFOSESSION_EXTENDED_FIELDS + ", infosession_enrollment_status " +
                        "FROM infosessions_extended " +
                        "JOIN infosessionenrollees USING (infosession_id) " +
                        "WHERE infosession_enrollee_id = ? " +
                        "ORDER BY infosession_timestamp DESC LIMIT 1"
        )) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                LastSessionResult iop = new LastSessionResult();
                if (rs.next()) {
                    iop.session = getInfoSessionFromResultSet(rs);
                    iop.present = "PRESENT".equals(rs.getString("infosession_enrollment_status"));
                    if (!iop.present && iop.session.getTime().isBefore(Instant.now())) {
                        iop.session = null; // a nonattended session in the past is ignored
                    }
                } else {
                    iop.session = null;
                    iop.present = false;
                }
                return iop;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch last for user", ex);
        }
    }

    @Override
    public Iterable<Enrollee> getEnrollees(int infosessionId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + USER_HEADER_FIELDS + ", infosession_enrollment_status " +
                        "FROM infosessionenrollees " +
                        "INNER JOIN users ON user_id = infosession_enrollee_id " +
                        "WHERE infosession_id = ?"
        )) {
            ps.setInt(1, infosessionId);
            return toList(ps, rs -> new Enrollee(
                    JDBCUserDAO.populateUserHeader(rs),
                    EnrollementStatus.valueOf(rs.getString("infosession_enrollment_status"))
            ));
        } catch (SQLException ex) {
            throw new DataAccessException("Could not find enrollees", ex);
        }
    }

}
