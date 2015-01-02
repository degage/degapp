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

import be.ugent.degage.db.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationHeader;
import be.ugent.degage.db.models.ReservationStatus;


import static be.ugent.degage.db.jdbc.JDBCUserDAO.USER_HEADER_FIELDS;

/**
 *
 */
class JDBCReservationDAO extends AbstractDAO implements ReservationDAO {

    public static final String RESERVATION_HEADER_FIELDS =
            "reservation_id, reservation_car_id, reservation_user_id, reservation_from, reservation_to, " +
                    "reservation_message, reservation_status, reservation_privileged ";


            // TODO: replace * by actual fields
    public static final String RESERVATION_QUERY = "SELECT * FROM reservations " +
            "INNER JOIN cars ON reservations.reservation_car_id = cars.car_id " +
            "INNER JOIN users ON reservations.reservation_user_id = users.user_id ";

    public JDBCReservationDAO(JDBCDataAccessContext context) {
        super(context);
    }

    public static ReservationHeader populateReservationHeader (ResultSet rs) throws SQLException {
        ReservationHeader reservation = new ReservationHeader(
                rs.getInt("reservation_id"),
                rs.getInt("reservation_car_id"),
                rs.getInt("reservation_user_id"),
                rs.getTimestamp("reservation_from").toLocalDateTime(),
                rs.getTimestamp("reservation_to").toLocalDateTime(),
                rs.getString("reservation_message")
        );
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
        reservation.setPrivileged(rs.getBoolean ("reservation_privileged"));
        return reservation;
    }

    public static Reservation populateReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation(
                rs.getInt("reservation_id"),
                JDBCCarDAO.populateCar(rs, false),
                JDBCUserDAO.populateUserHeader(rs),
                rs.getTimestamp("reservation_from").toLocalDateTime(),
                rs.getTimestamp("reservation_to").toLocalDateTime(),
                rs.getString("reservation_message")
        );
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
        reservation.setPrivileged(rs.getBoolean ("reservation_privileged"));
        return reservation;
    }

    private LazyStatement createReservationStatement = new LazyStatement (
            "INSERT INTO reservations (reservation_user_id, reservation_car_id, "
                    + "reservation_from, reservation_to, reservation_message) VALUES (?,?,?,?,?)",
            "reservation_id"
    );

    private LazyStatement retreiveStatusStatement = new LazyStatement (
            "SELECT reservation_status, reservation_privileged FROM reservations WHERE reservation_id = ?"
    );

    @Override
    public ReservationHeader createReservation(LocalDateTime from, LocalDateTime until, int carId, int userId, String message) throws DataAccessException {
        try {
            // TODO: find a way to do this with a single SQL statement
            PreparedStatement ps = createReservationStatement.value();
            ps.setInt(1, userId);
            ps.setInt(2, carId);
            ps.setTimestamp(3, Timestamp.valueOf(from));
            ps.setTimestamp(4, Timestamp.valueOf(until));
            ps.setString(5, message);

            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating reservation.");

            // create
            int id;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                id = keys.getInt(1);
            }

            // retrieve status
            PreparedStatement ps2 = retreiveStatusStatement.value();
            ps2.setInt(1, id);
            try (ResultSet rs = ps2.executeQuery()) {
                rs.next();
                ReservationHeader reservation = new ReservationHeader (
                        id,
                        userId, carId,
                        from, until,
                        message);
                reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
                reservation.setPrivileged(rs.getBoolean("reservation_privileged"));
                return reservation;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create reservation", e);
        }
    }

    private LazyStatement getUpdateReservationStatusStatement = new LazyStatement(
            "UPDATE reservations SET reservation_status =?  WHERE reservation_id = ?"
    );

    @Override
    public void updateReservationStatus(int reservationId, ReservationStatus status) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateReservationStatusStatement.value();
            ps.setString(1, status.name());
            ps.setInt(2, reservationId);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update reservation", e);
        }
    }

    private LazyStatement getUpdateReservationStatement = new LazyStatement (
            "UPDATE reservations SET reservation_user_id=? , reservation_car_id=? , reservation_status =? ,"
                    + "reservation_from=? , reservation_to=?, reservation_message = ? WHERE reservation_id = ?"
    );

    @Override
    public void updateReservation(Reservation reservation) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateReservationStatement.value();
            ps.setInt(1, reservation.getUser().getId());
            ps.setInt(2, reservation.getCar().getId());
            ps.setString(3, reservation.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(reservation.getFrom()));
            ps.setTimestamp(5, Timestamp.valueOf(reservation.getUntil()));
            ps.setString(6, reservation.getMessage());
            ps.setInt(7, reservation.getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Reservation update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update reservation", e);
        }
    }

    private LazyStatement getReservationStatement = new LazyStatement (
            "SELECT * FROM reservations" +
                    " INNER JOIN cars ON reservations.reservation_car_id = cars.car_id" +
                    " INNER JOIN users ON reservations.reservation_user_id = users.user_id" +
                    " WHERE reservation_id=?"
    );

    @Override
    public Reservation getReservation(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getReservationStatement.value();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateReservation(rs);
                else return null;
            }catch (SQLException e){
                throw new DataAccessException("Error reading reservation resultset", e);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get reservation", e);
        }
    }


    private Reservation populateNextPrevious (ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("r.reservation_id"),
                null,
                JDBCUserDAO.populateUserHeader(rs),
                rs.getTimestamp("r.reservation_from").toLocalDateTime(),
                rs.getTimestamp("r.reservation_to").toLocalDateTime(),
                null
        );
    }

    private LazyStatement getNextReservationStatement = new LazyStatement(
            "SELECT r.reservation_id, r.reservation_from, r.reservation_to, " +
                    USER_HEADER_FIELDS +
            "FROM reservations AS r JOIN reservations AS o " +
                    "ON r.reservation_car_id = o.reservation_car_id " +
            "JOIN users ON r.reservation_user_id = user_id " +
                    "WHERE o.reservation_id = ? " +
                    "AND r.reservation_status = 'ACCEPTED' " +
                    "AND r.reservation_from > o.reservation_to  " +
                    "AND r.reservation_from < o.reservation_to + INTERVAL 1 DAY " +
                    "ORDER BY r.reservation_from ASC LIMIT 1"
    );

    @Override
    public Reservation getNextReservation(int reservationId) throws DataAccessException {
        try {
            PreparedStatement ps = getNextReservationStatement.value();
            ps.setInt(1, reservationId);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return populateNextPrevious(rs);
                } else {
                    return null;
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Error while retrieve the reservation following reservation with id" + reservationId, ex);
        }
    }

    private LazyStatement getPreviousReservationStatement = new LazyStatement(
            "SELECT r.reservation_id, r.reservation_from, r.reservation_to, " +
                    USER_HEADER_FIELDS +
            "FROM reservations AS r JOIN reservations AS o " +
                    "ON r.reservation_car_id = o.reservation_car_id " +
            "JOIN users ON r.reservation_user_id = user_id " +
                    "WHERE o.reservation_id = ? " +
                    "AND r.reservation_status = 'ACCEPTED' " +
                    "AND r.reservation_to < o.reservation_from  " +
                    "AND r.reservation_to + INTERVAL 1 DAY > o.reservation_from " +
                    "ORDER BY r.reservation_to DESC LIMIT 1"
    );

    @Override
    public Reservation getPreviousReservation(int reservationId) throws DataAccessException {
        try {
            PreparedStatement ps = getPreviousReservationStatement.value();
            ps.setInt(1, reservationId);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return populateNextPrevious(rs);
                } else {
                    return null;
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Error while retrieve the reservation preceeding reservation with id " + reservationId, ex);
        }
    }
    
    private LazyStatement deleteReservationStatement = new LazyStatement (
            "DELETE FROM reservations WHERE reservation_id=?"
    );

    @Override
    public void deleteReservation(Reservation reservation){
    	try {
			PreparedStatement ps = deleteReservationStatement.value();
			ps.setInt(1, reservation.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting reservation.");
		} catch (SQLException ex){
			throw new DataAccessException("Could not delete reservation",ex);
		}
    }

    // TODO: make two different methods depending on value of getAmount
    private String getReservationsPageStatement(boolean getAmount, String amount, Filter filter) {
        String id;
        String userOrOwner = filter.getValue(FilterField.RESERVATION_USER_OR_OWNER_ID);
        if(userOrOwner.equals("") || userOrOwner.startsWith("-")) {
            id = "'%%'";
        } else {
            id = userOrOwner;
        }
        String carId;
        String car = filter.getValue(FilterField.RESERVATION_CAR_ID);
        if(car.equals("") || car.startsWith("-")) {
            carId = "'%%'";
        } else {
            carId = car;
        }
            // TODO: replace * by actual fields
        String sql = "SELECT " + (getAmount ? " COUNT(reservation_id) AS " + amount : " * ") +
                " FROM reservations INNER JOIN cars ON reservations.reservation_car_id = cars.car_id " +
                " INNER JOIN users ON reservations.reservation_user_id = users.user_id " +
                " WHERE (car_owner_user_id LIKE " + id +
                " OR reservation_user_id LIKE " + id + ") AND " +
                " reservation_car_id LIKE " + carId + " AND ";
        if("".equals(filter.getValue(FilterField.RESERVATION_STATUS)))
            sql += " reservation_status != 'ACCEPTED'  AND reservation_status != 'REQUEST' ";
        else
            sql += " reservation_status = '" + filter.getValue(FilterField.RESERVATION_STATUS) + "' ";
        return sql;
    }

    private String getReservationsPageStatement(Filter filter) {
        return getReservationsPageStatement(false, "", filter);
    }

    @Override
    public int getAmountOfReservations(Filter filter) throws DataAccessException {
        try {
            String amount = "amount";
            Statement statement = context.getConnection().createStatement();
            String sql = getReservationsPageStatement(true, amount, filter);
            try (ResultSet rs = statement.executeQuery(sql)) {
                if(rs.next())
                    return rs.getInt("amount");
                else return 0;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of reservations", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of reservations", ex);
        }
    }
    @Override
    public Iterable<Reservation> getReservationListPage(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            Statement statement = createStatement();
            String sql = getReservationsPageStatement(filter);
            sql += " ORDER BY ";
            switch(orderBy) {
                // TODO: get some other things to sort on
                default:
                    sql += " reservation_from " + (asc ? " asc " : " dec ");
                    break;
            }
            sql += " LIMIT " + (page-1)*pageSize + ", " + pageSize;
            try (ResultSet rs = statement.executeQuery(sql)) {
                Collection<Reservation> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(populateReservation(rs));
                }
                return list;
            }
        } catch (Exception ex) {
            throw new DataAccessException("Could not retrieve a list of reservations", ex);
        }
    }

    @Override
    public int numberOfReservationsWithStatus(ReservationStatus status, int userId, boolean userIsLoaner) {
        try (Statement statement = createStatement()) {
            String sql = "SELECT COUNT(*) as result FROM reservations " +
                    "INNER JOIN cars ON reservations.reservation_car_id = cars.car_id " +
                    "WHERE reservations.reservation_status = '" + status.name() + "'";
            if(userIsLoaner)
                sql += " AND (car_owner_user_id = " + userId + " OR reservation_user_id = " + userId + ")";
            else
                sql += " AND car_owner_user_id = " + userId;
            try (ResultSet rs = statement.executeQuery(sql)) {
                if(rs.next())
                    return rs.getInt("result");
                else
                    return 0;
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Could not count number of reservations");
        }
    }

    private LazyStatement getReservationListByUseridStatement = new LazyStatement (
            "SELECT * FROM reservations" +
                    " INNER JOIN cars ON reservations.reservation_car_id = cars.car_id" +
                    " INNER JOIN users ON reservations.reservation_user_id = users.user_id " +
                    " WHERE (car_owner_user_id = ? OR reservation_user_id = ? ) " +
                    " AND reservation_status != 'REFUSED' AND reservation_status != 'CANCELLED'"
    );

    @Override
    public Iterable<Reservation> getReservationListForUser(int userID) throws DataAccessException {
        try {
            PreparedStatement ps = getReservationListByUseridStatement.value();
            ps.setInt(1, userID);
            ps.setInt(2, userID);
            try (ResultSet rs = ps.executeQuery()) {
                Collection<Reservation> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(populateReservation(rs));
                }
                return list;
            }
        } catch (Exception e){
            throw new DataAccessException("Unable to retrieve the list of reservations", e);
        }
    }

    private LazyStatement getReservationListByCaridStatement = new LazyStatement (
            "SELECT  " + RESERVATION_HEADER_FIELDS + "FROM reservations WHERE reservation_car_id=?"
    );

    private Iterable<ReservationHeader> listReservations(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            Collection<ReservationHeader> list = new ArrayList<>();
            while (rs.next()) {
                list.add(populateReservationHeader(rs));
            }
            return list;
        }
    }


    @Override
    public Iterable<ReservationHeader> listReservationsForCar (int carId) throws DataAccessException {
        try {
            PreparedStatement ps = getReservationListByCaridStatement.value();
            ps.setInt(1, carId);
            return listReservations(ps);
        } catch (Exception e){
            throw new DataAccessException("Unable to retrieve the list of reservations", e);
        }
    }

    private static final String ADJUST_STATEMENT =
            "UPDATE reservations SET reservation_status='REQUEST_DETAILS' " +
                    " WHERE reservation_to < NOW() AND reservation_status = 'ACCEPTED' ";

    @Override
    public void adjustReservationStatuses() {
        try (Statement stat = createStatement()) {
            stat.executeUpdate(ADJUST_STATEMENT); // it is possible that no records are affected
        } catch (SQLException ex) {
            throw new DataAccessException("Error while updating the reservations statuses", ex);
        }
    }


    private LazyStatement listCRInfoStatement = new LazyStatement(
        "SELECT car_id, car_name, " + RESERVATION_HEADER_FIELDS + " FROM cars " +
                "LEFT JOIN reservations ON reservation_car_id = car_id " + OVERLAP_CLAUSE +
                "WHERE car_active "  +
                "ORDER BY car_name, reservation_from"
    );

    public Iterable<CRInfo> listCRInfo (LocalDateTime from, LocalDateTime until) {
        try {
            PreparedStatement ps = listCRInfoStatement.value();
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(until));
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
                        crInfo.carName = rs.getString ("car_name");     // TODO: add car info GPS, etc.
                        crInfo.reservations = new ArrayList<>();
                        result.add(crInfo);
                    }
                    rs.getInt("reservation_id");
                    if (! rs.wasNull()) {
                        crInfo.reservations.add(populateReservationHeader(rs));
                    }
                }
                return result;
            }
        } catch (SQLException ex) {
            throw new DataAccessException( "Could not retreive reservation information", ex);
        }

    }

    public static final String OVERLAP_CLAUSE =
        "AND reservation_to >= ? AND reservation_from <= ? " +
        "AND reservation_status != 'CANCELED' AND reservation_status != 'REFUSED' ";

    private LazyStatement listRCFIPStatement = new LazyStatement(
        "SELECT " + RESERVATION_HEADER_FIELDS +
                "FROM reservations WHERE reservation_car_id = ? " + OVERLAP_CLAUSE +
                "ORDER BY reservation_from"
    );

    @Override
    public Iterable<ReservationHeader> listReservationsForCarInPeriod (int carId, LocalDateTime from, LocalDateTime until) {
        try {
            PreparedStatement ps = listRCFIPStatement.value();
            ps.setInt(1, carId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(until));
            return listReservations(ps);
        } catch (SQLException ex) {
            throw new DataAccessException( "Could not retreive reservation information", ex);
        }
    }

    private LazyStatement hasOverlapStatement = new LazyStatement(
        "SELECT count(*) FROM reservations WHERE reservation_car_id = ? " + OVERLAP_CLAUSE
    );

    @Override
    public boolean hasOverlap(int carId, LocalDateTime from, LocalDateTime until) {
        try {
            PreparedStatement ps = hasOverlapStatement.value();
            ps.setInt (1, carId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(until));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) != 0;
            }
        } catch (SQLException ex) {
            throw new DataAccessException( "Could not obtain reservation overlap information", ex);
        }
    }
}
