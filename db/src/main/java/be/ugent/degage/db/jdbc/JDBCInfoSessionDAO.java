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
import be.ugent.degage.db.dao.InfoSessionDAO;
import be.ugent.degage.db.models.*;

import java.sql.*;
import java.time.Instant;

import static be.ugent.degage.db.jdbc.JDBCUserDAO.USER_HEADER_FIELDS;

/**
 * JDBC implementation of {@link be.ugent.degage.db.dao.InfoSessionDAO}
 */
class JDBCInfoSessionDAO extends AbstractDAO implements InfoSessionDAO {

    JDBCInfoSessionDAO(JDBCDataAccessContext context) {
        super(context);
    }

    // TODO: check what kind of 'partial' is really needed in each case


    private static InfoSession populateInfoSession(ResultSet rs) throws SQLException {
        Address address = JDBCAddressDAO.populateAddress(rs);

        InfoSession infoSession = new InfoSession(
                rs.getInt("infosession_id"),
                InfoSessionType.valueOf(rs.getString("infosession_type")),
                rs.getTimestamp("infosession_timestamp").toInstant(),
                address,
                rs.getInt("user_id"),
                rs.getString("user_firstname") + " " + rs.getString("user_lastname"),
                rs.getInt("infosession_max_enrollees"),
                rs.getString("infosession_comments")
        );
        infoSession.setEnrolleeCount(rs.getInt("enrollee_count"));
        return infoSession;
    }

    // TODO: code below is only used from ApprovalDAO. Check whether it can be cleaned up
    static InfoSession populateInfoSessionPartial(ResultSet rs) throws SQLException {
        return new InfoSession(
                rs.getInt("ses.infosession_id"),
                InfoSessionType.valueOf(rs.getString("infosession_type")),
                rs.getTimestamp("infosession_timestamp").toInstant(),
                null,
                0, null,
                rs.getInt("infosession_max_enrollees"),
                rs.getString("infosession_comments")
        );
    }

    @Override
    public int createInfoSession(InfoSessionType type, int hostId, Address address,
                                         Instant time, int maxEnrollees, String comments) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO infosessions(infosession_type, infosession_timestamp, " +
                        "infosession_host_user_id, infosession_max_enrollees, infosession_comments) " +
                        "VALUES (?,?,?,?,?)",
                "infosession_id"
        )) {
            ps.setString(1, type.name());
            ps.setTimestamp(2, Timestamp.from(time));
            ps.setInt(3, hostId);
            ps.setInt(4, maxEnrollees);
            ps.setString(5, comments);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway

                int sessionId = keys.getInt(1);
                updateLocation(sessionId, address);
                return sessionId;
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
                "SELECT " + INFOSESSION_EXTENDED_FIELDS + "FROM infosessions_extended WHERE infosession_id = ?"
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

    private static String INFOSESSION_EXTENDED_FIELDS =
            "infosession_id, infosession_type, " +
                    "infosession_timestamp, infosession_max_enrollees, infosession_comments, " +
                    "address_id, address_country, address_city, address_zipcode, address_street, address_number, " +
                    "user_id, user_firstname, user_lastname, enrollee_count ";


    /**
     */
    @Override
    public Iterable<InfoSession> getUpcomingInfoSessions() throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + INFOSESSION_EXTENDED_FIELDS + "FROM infosessions_extended " +
                    "WHERE infosession_timestamp > NOW() ORDER BY infosession_timestamp"
        )) {
            return toList(ps, JDBCInfoSessionDAO::populateInfoSession);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of infosessions", ex);
        }
    }

    @Override
    public Page<InfoSession> getFutureInfoSessions(int page, int pageSize) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
             "SELECT SQL_CALC_FOUND_ROWS " + INFOSESSION_EXTENDED_FIELDS + " FROM infosessions_extended " +
             "WHERE infosession_timestamp > NOW() ORDER BY infosession_timestamp ASC LIMIT ?, ?")
        ) {
            ps.setInt(1, (page-1)*pageSize);
            ps.setInt(2, pageSize);
            return toPage(ps, pageSize, JDBCInfoSessionDAO::populateInfoSession);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of infosessions", ex);
        }
    }

    private static InfoSession populatePastInfoSession(ResultSet rs) throws SQLException {
        InfoSession infoSession = new InfoSession(
                rs.getInt("infosession_id"),
                InfoSessionType.valueOf(rs.getString("infosession_type")),
                rs.getTimestamp("infosession_timestamp").toInstant(),
                null,
                rs.getInt("h.user_id"),
                rs.getString("h.user_firstname") + " " + rs.getString("h.user_lastname"),
                rs.getInt("infosession_max_enrollees"),
                null
        );
        infoSession.setEnrolleeCount(rs.getInt("enrollee_count"));
        infoSession.setMembershipCount(rs.getInt("member_count"));
        return infoSession;
    }

    @Override
    public Page<InfoSession> getPastInfoSessions(int page, int pageSize) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
            "SELECT SQL_CALC_FOUND_ROWS " +
                    "infosession_id, infosession_type, infosession_timestamp, infosession_max_enrollees, " +
                    "h.user_id, h.user_firstname, h.user_lastname, " +
                    "count(infosession_enrollee_id) AS enrollee_count, " +
                    "count(u.user_date_joined) AS member_count " +
            "FROM infosessions " +
                    "JOIN infosessionenrollees USING(infosession_id) " +
                    "JOIN users AS h ON infosession_host_user_id = h.user_id " +
                    "JOIN users AS u ON infosession_enrollee_id = u.user_id " +
            "WHERE infosession_timestamp <= NOW() " +
            "GROUP BY infosession_id " +
                    "ORDER BY infosession_timestamp DESC LIMIT ?, ?")
        ) {
            ps.setInt(1, (page-1)*pageSize);
            ps.setInt(2, pageSize);
            return toPage(ps, pageSize, JDBCInfoSessionDAO::populatePastInfoSession);
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
            if (ex.getErrorCode() == MYSQL_ERROR_DUPLICATE_ENTRY) {
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
                ("SELECT " + INFOSESSION_EXTENDED_FIELDS + "FROM infosessions_extended ") +
                        " JOIN infosessionenrollees USING(infosession_id) " +
                        " WHERE infosession_enrollee_id = ? AND infosession_timestamp > NOW() " +
                        " ORDER BY infosession_timestamp"
        )) {
            ps.setInt(1, userId);
            return toSingleObject(ps, JDBCInfoSessionDAO::populateInfoSession);
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
    public void updateInfoSession(int sessionId, InfoSessionType type, int maxEnrollees, Instant time, int hostId, String comments, Address address) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE infosessions SET infosession_type=?, infosession_max_enrollees=?, " +
                        "infosession_timestamp=?, infosession_host_user_id=?, infosession_comments=? " +
                        "WHERE infosession_id=?"
        )) {
            ps.setString(1, type.name());
            ps.setInt(2, maxEnrollees);
            ps.setTimestamp(3, Timestamp.from(time));
            ps.setInt(4, hostId);
            ps.setString(5, comments);

            ps.setInt(6, sessionId);
            ps.executeUpdate();
            updateLocation(sessionId, address);
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
                    iop.session = populateInfoSession(rs);
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

    private static Enrollee populateEnrollee (ResultSet rs) throws SQLException {
        Date dateJoined = rs.getDate("user_date_joined");
        return new Enrollee(
                    JDBCUserDAO.populateUserHeader(rs),
                    EnrollementStatus.valueOf(rs.getString("infosession_enrollment_status")),
                    dateJoined == null ? null : dateJoined.toLocalDate()
        );
    }

    @Override
    public Iterable<Enrollee> getEnrollees(int infosessionId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + USER_HEADER_FIELDS + ", user_date_joined, infosession_enrollment_status " +
                        "FROM infosessionenrollees " +
                        "INNER JOIN users ON user_id = infosession_enrollee_id " +
                        "WHERE infosession_id = ?"
        )) {
            ps.setInt(1, infosessionId);
            return toList(ps, JDBCInfoSessionDAO::populateEnrollee);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not find enrollees", ex);
        }
    }

}
