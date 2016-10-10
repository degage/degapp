/* JDBCReservationDAO.java
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
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationHeader;
import be.ugent.degage.db.models.ReservationStatus;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static be.ugent.degage.db.jdbc.JDBCUserDAO.USER_HEADER_FIELDS;

/**
 *
 */
class JDBCReservationDAO extends AbstractDAO implements ReservationDAO {

    public static final String RESERVATION_HEADER_FIELDS =
            "reservation_id, reservation_car_id, reservation_user_id, reservation_owner_id, reservation_from, reservation_to, " +
                    "reservation_message, reservation_status, reservation_privileged, reservation_created_at ";

    public JDBCReservationDAO(JDBCDataAccessContext context) {
        super(context);
    }

    public static ReservationHeader populateReservationHeader(ResultSet rs) throws SQLException {
        ReservationHeader reservation = new ReservationHeader(
                rs.getInt("reservation_id"),
                rs.getInt("reservation_car_id"),
                rs.getInt("reservation_user_id"),
                rs.getInt("reservation_owner_id"),
                rs.getTimestamp("reservation_from").toLocalDateTime(),
                rs.getTimestamp("reservation_to").toLocalDateTime(),
                rs.getString("reservation_message"),
                rs.getTimestamp("reservation_created_at").toInstant().plusSeconds(TimeUnit.DAYS.toSeconds(1)).isBefore(Instant.now())
        );
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
        reservation.setPrivileged(rs.getBoolean("reservation_privileged"));
        return reservation;
    }

    public static Reservation populateReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation(
                rs.getInt("reservation_id"),
                JDBCCarDAO.populateCarHeader (rs),
                JDBCUserDAO.populateUserHeader(rs),
                rs.getInt("reservation_owner_id"),
                rs.getTimestamp("reservation_from").toLocalDateTime(),
                rs.getTimestamp("reservation_to").toLocalDateTime(),
                rs.getString("reservation_message"),
                rs.getTimestamp("reservation_created_at").toInstant().plusSeconds(TimeUnit.DAYS.toSeconds(1)).isBefore(Instant.now())
        );
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
        reservation.setPrivileged(rs.getBoolean("reservation_privileged"));
        return reservation;
    }

    private LazyStatement createReservationStatement = new LazyStatement(
            "INSERT INTO reservations (reservation_user_id, reservation_car_id, "
                    + "reservation_from, reservation_to) VALUES (?,?,?,?)",
            "reservation_id"
    );

    private LazyStatement retreiveCreatedStatement = new LazyStatement(
            "SELECT reservation_status, reservation_privileged, reservation_owner_id FROM reservations WHERE reservation_id = ?"
    );

    @Override
    public ReservationHeader createReservation(LocalDateTime from, LocalDateTime until, int carId, int userId) throws DataAccessException {
        try {
            // TODO: find a way to do this with a single SQL statement
            PreparedStatement ps = createReservationStatement.value();
            ps.setInt(1, userId);
            ps.setInt(2, carId);
            ps.setTimestamp(3, Timestamp.valueOf(from));
            ps.setTimestamp(4, Timestamp.valueOf(until));

            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating reservation.");

            // create
            int id;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                id = keys.getInt(1);
            }

            // retrieve status
            PreparedStatement ps2 = retreiveCreatedStatement.value();
            ps2.setInt(1, id);
            try (ResultSet rs = ps2.executeQuery()) {
                rs.next();
                ReservationHeader reservation = new ReservationHeader(
                        id,
                        userId, carId, rs.getInt("reservation_owner_id"),
                        from, until, null, false);
                reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
                reservation.setPrivileged(rs.getBoolean("reservation_privileged"));
                return reservation;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create reservation", e);
        }
    }

    @Override
    public void updateReservationStatus(int reservationId, ReservationStatus status, String message) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE reservations SET reservation_status=?, reservation_message = ?  WHERE reservation_id = ?"
        )) {
            ps.setString(1, status.name());
            ps.setString(2, message);
            ps.setInt(3, reservationId);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update reservation", e);
        }
    }

    @Override
    public void updateReservationStatus(int reservationId, ReservationStatus status) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE reservations SET reservation_status=? WHERE reservation_id = ?"
        )) {
            ps.setString(1, status.name());
            ps.setInt(2, reservationId);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update reservation", e);
        }
    }

    private LazyStatement getUpdateReservationTimeStatement = new LazyStatement(
            "UPDATE reservations SET reservation_from=? , reservation_to=? WHERE reservation_id = ?"
    );

    @Override
    public void updateReservationTime(int reservationId, LocalDateTime from, LocalDateTime until) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateReservationTimeStatement.value();
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(until));
            ps.setInt(3, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update reservation time ", e);
        }
    }

    @Override
    public Reservation getReservation(int id) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
        // TODO: list actual fields
                "SELECT * FROM reservations" +
                    " INNER JOIN cars ON reservations.reservation_car_id = cars.car_id" +
                    " INNER JOIN users ON reservations.reservation_user_id = users.user_id" +
                    " WHERE reservation_id=?"
        )) {
            ps.setInt(1, id);
            return toSingleObject(ps, JDBCReservationDAO::populateReservation);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get reservation", e);
        }
    }

    @Override
    public Reservation getReservationExtended(int id) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
        // TODO: replace * with actual parameters
           "SELECT * FROM reservations" +
                " INNER JOIN cars ON reservations.reservation_car_id = cars.car_id" +
                " INNER JOIN users ON reservations.reservation_user_id = users.user_id" +
                " JOIN addresses ON car_location = address_id" +
                " WHERE reservation_id=?"
        )) {
            ps.setInt(1, id);
            return toSingleObject(ps, rs -> {
                    Reservation reservation = populateReservation(rs);
                    reservation.getCar().setLocation(JDBCAddressDAO.populateAddress(rs));
                    return reservation;
                });
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get reservation", e);
        }
    }

    private LazyStatement getReservationHeaderStatement = new LazyStatement(
            "SELECT " + RESERVATION_HEADER_FIELDS + " FROM reservations WHERE reservation_id=?"
    );

    @Override
    public ReservationHeader getReservationHeader(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getReservationHeaderStatement.value();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return populateReservationHeader(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get reservation header", e);
        }
    }

    private Reservation populateNextPrevious(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("r.reservation_id"),
                null,
                JDBCUserDAO.populateUserHeader(rs),
                rs.getInt("r.reservation_owner_id"),
                rs.getTimestamp("r.reservation_from").toLocalDateTime(),
                rs.getTimestamp("r.reservation_to").toLocalDateTime(),
                null,
                false // not important
        );
    }

    private LazyStatement getNextReservationStatement = new LazyStatement(
            "SELECT r.reservation_id, r.reservation_from, r.reservation_to, r.reservation_owner_id, " +
                    USER_HEADER_FIELDS +
                    "FROM reservations AS r JOIN reservations AS o " +
                    "ON r.reservation_car_id = o.reservation_car_id " +
                    "JOIN users ON r.reservation_user_id = user_id " +
                    "WHERE o.reservation_id = ? " +
                    "AND NOT r.reservation_archived " +
                    "AND r.reservation_status = 'ACCEPTED' " +
                    "AND r.reservation_from >= o.reservation_to  " +
                    "AND r.reservation_from <= o.reservation_to + INTERVAL 1 DAY " +
                    "ORDER BY r.reservation_from ASC LIMIT 1"
    );

    @Override
    public Reservation getNextReservation(int reservationId) throws DataAccessException {
        try {
            PreparedStatement ps = getNextReservationStatement.value();
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return populateNextPrevious(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error while retrieve the reservation following reservation with id" + reservationId, ex);
        }
    }

    private LazyStatement getPreviousReservationStatement = new LazyStatement(
            "SELECT r.reservation_id, r.reservation_from, r.reservation_to, r.reservation_owner_id, " +
                    USER_HEADER_FIELDS +
                    "FROM reservations AS r JOIN reservations AS o " +
                    "ON r.reservation_car_id = o.reservation_car_id " +
                    "JOIN users ON r.reservation_user_id = user_id " +
                    "WHERE o.reservation_id = ? " +
                    "AND NOT r.reservation_archived " +
                    "AND r.reservation_status > 4 " +  // [ENUM INDEX] = accepted or already in the past
                    "AND r.reservation_to <= o.reservation_from  " +
                    "AND r.reservation_to + INTERVAL 1 DAY >= o.reservation_from " +
                    "ORDER BY r.reservation_to DESC LIMIT 1"
    );

    @Override
    public Reservation getPreviousReservation(int reservationId) throws DataAccessException {
        try {
            PreparedStatement ps = getPreviousReservationStatement.value();
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return populateNextPrevious(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error while retrieve the reservation preceeding reservation with id " + reservationId, ex);
        }
    }

    private LazyStatement deleteReservationStatement = new LazyStatement(
            "DELETE FROM reservations WHERE reservation_id=?"
    );

    @Override
    public int getNextTripId(int reservationId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT r.reservation_id FROM reservations AS r JOIN reservations AS o " +
                        "ON r.reservation_car_id = o.reservation_car_id " +
                        "WHERE o.reservation_id = ? " +
                        "AND NOT r.reservation_archived " +
                        "AND r.reservation_status > 5  " +       // [ENUM INDEX]
                        "AND r.reservation_from > o.reservation_from  " +
                        "ORDER BY r.reservation_from ASC LIMIT 1"
        )) {
            ps.setInt(1, reservationId);
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Error while retrieving the next trip", ex);
        }
    }

    @Override
    public int getPreviousTripId(int reservationId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT r.reservation_id FROM reservations AS r JOIN reservations AS o " +
                        "ON r.reservation_car_id = o.reservation_car_id " +
                        "WHERE o.reservation_id = ? " +
                        "AND NOT r.reservation_archived " +
                        "AND r.reservation_status > 5  " +       // [ENUM INDEX]
                        "AND r.reservation_from < o.reservation_from  " +
                        "ORDER BY r.reservation_from DESC LIMIT 1"
        )) {
            ps.setInt(1, reservationId);
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Error while retrieving the previous trip", ex);
        }
    }

    @Override
    public int getFirstTripAfterDate(int carId, LocalDate date) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT reservation_id FROM reservations " +
                        "WHERE reservation_from >= ? " +
                        "AND NOT reservation_archived " +
                        "AND reservation_car_id = ? " +
                        "AND reservation_status > 5  " +       // [ENUM INDEX]
                        "ORDER BY reservation_from ASC LIMIT 1"
        )) {
            ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            ps.setInt(2, carId);
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Error while retrieving the first trip", ex);
        }
    }

    @Override
    public void deleteReservation(Reservation reservation) {
        try {
            PreparedStatement ps = deleteReservationStatement.value();
            ps.setInt(1, reservation.getId());
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting reservation.");
        } catch (SQLException ex) {
            throw new DataAccessException("Could not delete reservation", ex);
        }
    }

    private void appendStatusFilter(StringBuilder builder, String searchString) {
        if (searchString != null) {
            String[] names = searchString.split("\\|");
            if (names.length != 0) {
                builder.append(" AND ( reservation_status = '").append(names[0]).append("' ");
                for (int i = 1; i < names.length; i++) {
                    builder.append(" OR reservation_status = '").append(names[i]).append("' ");
                }
                builder.append(") ");
            }
        }
    }

    private String getReservationsPageStatement(Filter filter) {
        String id;
        String userOrOwner = filter.getValue(FilterField.RESERVATION_USER_OR_OWNER_ID);
        if (userOrOwner.equals("") || userOrOwner.startsWith("-")) {
            id = "'%%'";
        } else {
            id = userOrOwner;
        }
        String carId;
        String car = filter.getValue(FilterField.RESERVATION_CAR_ID);
        if (car.equals("") || car.startsWith("-")) {
            carId = "'%%'";
        } else {
            carId = car;
        }
        // TODO: replace * by actual fields
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT SQL_CALC_FOUND_ROWS * FROM reservations INNER JOIN cars ON reservations.reservation_car_id = cars.car_id " +
                                " INNER JOIN users ON reservations.reservation_user_id = users.user_id " +
                                " WHERE NOT reservation_archived  AND (car_owner_user_id LIKE ").append(id).
                append(" OR reservation_user_id LIKE ").append(id).
                append(") AND reservation_car_id LIKE ").append(carId);
        appendStatusFilter(builder, filter.getValue(FilterField.STATUS));
        return builder.toString();
    }

    @Override
    public Page<Reservation> getReservationListPage(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(getReservationsPageStatement(filter));
        // add order
        switch (orderBy) {
            case NAME:
                builder.append(" ORDER BY car_name ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case FROM:
                builder.append(" ORDER BY reservation_from ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case UNTIL:
                builder.append(" ORDER BY reservation_to ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case STATUS:
                builder.append(" ORDER BY reservation_status ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case BORROWER:
                builder.append(" ORDER BY user_lastname ");
                builder.append(asc ? "ASC" : "DESC");
                builder.append(" , user_firstname ");
                builder.append(asc ? "ASC" : "DESC");
                break;
        }
        builder.append(" LIMIT ?,?");
        System.out.println(builder.toString());
        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            ps.setInt(1, (page - 1) * pageSize);
            ps.setInt(2, pageSize);
            return toPage(ps, pageSize, JDBCReservationDAO::populateReservation);
        } catch (Exception ex) {
            throw new DataAccessException("Could not retrieve a list of reservations", ex);
        }
    }

    @Override
    public int numberOfReservationsWithStatus(ReservationStatus status, int userId, boolean userIsLoaner) {
        try (Statement statement = createStatement()) {
            String sql = "SELECT COUNT(*) as result FROM reservations " +
                    "INNER JOIN cars ON reservations.reservation_car_id = cars.car_id " +
                    "WHERE NOT reservation_archived AND reservations.reservation_status = '" + status.name() + "'";
            if (userIsLoaner)
                sql += " AND (car_owner_user_id = " + userId + " OR reservation_user_id = " + userId + ")";
            else
                sql += " AND car_owner_user_id = " + userId;
            try (ResultSet rs = statement.executeQuery(sql)) {
                if (rs.next())
                    return rs.getInt("result");
                else
                    return 0;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not count number of reservations");
        }
    }

    public Iterable<CRInfo> listCRInfo(LocalDateTime from, LocalDateTime until, int userId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT cars.car_id, car_name, " + RESERVATION_HEADER_FIELDS + " FROM cars " +
                        "LEFT JOIN carpreferences ON cars.car_id = carpreferences.car_id AND user_id = ? " +
                        "LEFT JOIN reservations ON reservation_car_id = cars.car_id " + OVERLAP_CLAUSE_WIDE +
                        "WHERE car_active " +
                        "ORDER BY ifnull(user_id,0) DESC, car_name ASC, reservation_from ASC"
        )) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(until));
            try (ResultSet rs = ps.executeQuery()) {
                Collection<CRInfo> result = new ArrayList<>();
                CRInfo crInfo = null;
                int currentId = -1;
                while (rs.next()) {
                    int thisId = rs.getInt("car_id");
                    if (thisId != currentId) {
                        currentId = thisId;
                        crInfo = new CRInfo();
                        crInfo.carId = thisId;
                        crInfo.carName = rs.getString("car_name");     // TODO: add car info GPS, etc.
                        crInfo.reservations = new ArrayList<>();
                        result.add(crInfo);
                    }
                    rs.getInt("reservation_id");
                    if (!rs.wasNull()) {
                        crInfo.reservations.add(populateReservationHeader(rs));
                    }
                }
                return result;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retreive reservation information", ex);
        }

    }

    public static final String OVERLAP_CLAUSE_WIDE =
            "AND reservation_to >= ? AND reservation_from <= ? " +
                    "AND reservation_status > 3 ";  // [ENUM INDEX]

    private LazyStatement listRCFIPStatement = new LazyStatement(
            "SELECT " + RESERVATION_HEADER_FIELDS +
                    "FROM reservations WHERE reservation_car_id = ? " + OVERLAP_CLAUSE_WIDE +
                    "ORDER BY reservation_from"
    );

    @Override
    public Iterable<ReservationHeader> listReservationsForCarInPeriod(int carId, LocalDateTime from, LocalDateTime until) {
        try {
            PreparedStatement ps = listRCFIPStatement.value();
            ps.setInt(1, carId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(until));
            try (ResultSet rs = ps.executeQuery()) {
                Collection<ReservationHeader> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(populateReservationHeader(rs));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retreive reservation information", ex);
        }
    }

    public static final String OVERLAP_CLAUSE_NARROW =
            "AND reservation_to > ? AND reservation_from < ? " +
                    "AND reservation_status > 3 ";  // [ENUM INDEX]

    private LazyStatement hasOverlapStatement = new LazyStatement(
            "SELECT count(*) FROM reservations WHERE reservation_car_id = ? " + OVERLAP_CLAUSE_NARROW
    );

    @Override
    public boolean hasOverlap(int carId, LocalDateTime from, LocalDateTime until) {
        try {
            PreparedStatement ps = hasOverlapStatement.value();
            ps.setInt(1, carId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(until));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) != 0;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not obtain reservation overlap information", ex);
        }
    }
}
