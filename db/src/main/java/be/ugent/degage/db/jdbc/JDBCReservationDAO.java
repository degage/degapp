/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationStatus;
import be.ugent.degage.db.models.User;

import org.joda.time.DateTime;

/**
 *
 * @author Laurent
 */
class JDBCReservationDAO extends AbstractDAO implements ReservationDAO {


            // TODO: replace * by actual fields
    public static final String RESERVATION_QUERY = "SELECT * FROM carreservations " +
            "INNER JOIN cars ON carreservations.reservation_car_id = cars.car_id " +
            "INNER JOIN users ON carreservations.reservation_user_id = users.user_id ";

    public JDBCReservationDAO(JDBCDataAccessContext context) {
        super(context);
    }

    public static Reservation populateReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation(
                rs.getInt("reservation_id"),
                JDBCCarDAO.populateCar(rs, false),
                JDBCUserDAO.populateUserPartial(rs),
                new DateTime(rs.getTimestamp("reservation_from")),
                new DateTime(rs.getTimestamp("reservation_to")),
                rs.getString("reservation_message")
        );
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
        return reservation;
    }

    private LazyStatement createReservationStatement = new LazyStatement (
            "INSERT INTO carreservations (reservation_user_id, reservation_car_id, reservation_status,"
                    + "reservation_from, reservation_to, reservation_message) VALUES (?,?,?,?,?,?)",
            "reservation_id"
    );

    @Override
    public Reservation createReservation(DateTime from, DateTime to, Car car, User user, String message) throws DataAccessException {
        try{
            PreparedStatement ps = createReservationStatement.value();
            ps.setInt(1, user.getId());
            ps.setInt(2, car.getId());
            ps.setString(3, ReservationStatus.REQUEST.toString());
            ps.setTimestamp(4, new Timestamp(from.getMillis()));
            ps.setTimestamp(5, new Timestamp(to.getMillis()));
            ps.setString(6, message);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating reservation.");
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Reservation(keys.getInt(1), car, user, from, to, message);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new reservation.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create reservation", e);
        }
    }


    private LazyStatement getUpdateReservationStatement = new LazyStatement (
            "UPDATE carreservations SET reservation_user_id=? , reservation_car_id=? , reservation_status =? ,"
                    + "reservation_from=? , reservation_to=?, reservation_message = ? WHERE reservation_id = ?"
    );

    @Override
    public void updateReservation(Reservation reservation) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateReservationStatement.value();
            ps.setInt(1, reservation.getUser().getId());
            ps.setInt(2, reservation.getCar().getId());
            ps.setString(3, reservation.getStatus().toString());
            ps.setTimestamp(4, new Timestamp(reservation.getFrom().getMillis()));
            ps.setTimestamp(5, new Timestamp(reservation.getTo().getMillis()));
            ps.setString(6, reservation.getMessage());
            ps.setInt(7, reservation.getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Reservation update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update reservation", e);
        }
    }

    private LazyStatement getReservationStatement = new LazyStatement (
            "SELECT * FROM carreservations" +
                    " INNER JOIN cars ON carreservations.reservation_car_id = cars.car_id" +
                    " INNER JOIN users ON carreservations.reservation_user_id = users.user_id" +
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

    private LazyStatement getNextReservationStatement = new LazyStatement (
            RESERVATION_QUERY +
                    " WHERE reservation_from >= ? AND reservation_id != ? AND carreservations.reservation_status = '"
                    + ReservationStatus.ACCEPTED.toString() + "' ORDER BY reservation_to ASC LIMIT 1"
    );

    @Override
    public Reservation getNextReservation(Reservation reservation) throws DataAccessException {
        try {
            PreparedStatement ps = getNextReservationStatement.value();
            ps.setTimestamp(1, new Timestamp(reservation.getTo().getMillis()));
            ps.setInt(2, reservation.getId());
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateReservation(rs);
                return null;
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Error while retrieve the reservation following reservation with id" + reservation.getId(), ex);
        }
    }

    private LazyStatement getPreviousReservationStatement = new LazyStatement (
            RESERVATION_QUERY +
                    " WHERE reservation_to <= ? AND reservation_id != ? AND carreservations.reservation_status = '"
                    + ReservationStatus.ACCEPTED.toString() + "' ORDER BY reservation_to DESC LIMIT 1"
    );

    @Override
    public Reservation getPreviousReservation(Reservation reservation) throws DataAccessException {
        try {
            PreparedStatement ps = getPreviousReservationStatement.value();
            ps.setTimestamp(1, new Timestamp(reservation.getFrom().getMillis()));
            ps.setInt(2, reservation.getId());
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateReservation(rs);
                return null;
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Error while retrieve the reservation following reservation with id" + reservation.getId(), ex);
        }
    }
    
    private LazyStatement deleteReservationStatement = new LazyStatement (
            "DELETE FROM carreservations WHERE reservation_id=?"
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
        if(filter.getValue(FilterField.RESERVATION_USER_OR_OWNER_ID).equals("")) {
            id = "'%%'";
        } else {
            id = filter.getValue(FilterField.RESERVATION_USER_OR_OWNER_ID);
        }
        String carId;
        if(filter.getValue(FilterField.RESERVATION_CAR_ID).equals("")) {
            carId = "'%%'";
        } else {
            carId = filter.getValue(FilterField.RESERVATION_CAR_ID);
        }
            // TODO: replace * by actual fields
        String sql = "SELECT " + (getAmount ? " COUNT(reservation_id) AS " + amount : " * ") +
                " FROM carreservations INNER JOIN cars ON carreservations.reservation_car_id = cars.car_id " +
                " INNER JOIN users ON carreservations.reservation_user_id = users.user_id " +
                " WHERE (car_owner_user_id LIKE " + id +
                " OR reservation_user_id LIKE " + id + ") AND " +
                " reservation_car_id LIKE " + carId + " AND ";
        if("".equals(filter.getValue(FilterField.RESERVATION_STATUS)))
            sql += " reservation_status != '" + ReservationStatus.ACCEPTED.toString() +
                    "' AND reservation_status != '" + ReservationStatus.REQUEST.toString() + "' ";
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
    public List<Reservation> getReservationListPage(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            Statement statement = context.getConnection().createStatement();            // TODO: do not use connection (also below)
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
                return getList(rs);
            }
        } catch (Exception ex) {
            throw new DataAccessException("Could not retrieve a list of reservations", ex);
        }
    }

    @Override
    public int numberOfReservationsWithStatus(ReservationStatus status, int userId, boolean userIsOwner, boolean userIsLoaner) {
        try {
            Statement statement = context.getConnection().createStatement();
            String sql = "SELECT COUNT(*) as result FROM carreservations " +
                    "INNER JOIN cars ON carreservations.reservation_car_id = cars.car_id " +
                    "WHERE carreservations.reservation_status = '" + status.toString() + "'";
            boolean both = userIsLoaner && userIsOwner;
            if(both)
                sql += " AND (car_owner_user_id = " + userId + " OR reservation_user_id = " + userId + ")";
            else if(userIsOwner)
                sql += " AND car_owner_user_id = " + userId;
            else if(userIsLoaner)
                sql += " AND reservation_user_id = " + userId;
            try (ResultSet rs = statement.executeQuery(sql)) {
                if(rs.next())
                    return rs.getInt("result");
                else return 0;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of reservations", ex);
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Could not count number of reservations");
        }
    }

    private LazyStatement getReservationListByUseridStatement = new LazyStatement (
            "SELECT * FROM carreservations" +
                    " INNER JOIN cars ON carreservations.reservation_car_id = cars.car_id" +
                    " INNER JOIN users ON carreservations.reservation_user_id = users.user_id " +
                    " WHERE (car_owner_user_id = ? OR reservation_user_id = ? ) " +
                    " AND reservation_status != '" + ReservationStatus.REFUSED.toString() +
                    "' AND reservation_status != '" + ReservationStatus.CANCELLED.toString() + "'"
    );

    @Override
    public List<Reservation> getReservationListForUser(int userID) throws DataAccessException {
        try {
            PreparedStatement ps = getReservationListByUseridStatement.value();
            ps.setInt(1, userID);
            ps.setInt(2, userID);
            return getReservationList(ps);
        } catch (Exception e){
            throw new DataAccessException("Unable to retrieve the list of reservations", e);
        }
    }

    private LazyStatement getReservationListByCaridStatement = new LazyStatement (
            "SELECT * FROM carreservations " +
                    "INNER JOIN cars ON carreservations.reservation_car_id = cars.car_id " +
                    "INNER JOIN users ON carreservations.reservation_user_id = users.user_id " +
                    "WHERE car_id=?"
    );

    @Override
    public List<Reservation> getReservationListForCar(int carId) throws DataAccessException {
        try {
            PreparedStatement ps = getReservationListByCaridStatement.value();
            ps.setInt(1, carId);
            return getReservationList(ps);
        } catch (Exception e){
            throw new DataAccessException("Unable to retrieve the list of reservations", e);
        }
    }

    private List<Reservation> getReservationList(PreparedStatement ps) throws DataAccessException {
        try (ResultSet rs = ps.executeQuery()) {
            return getList(rs);
        } catch (Exception e){
            throw new DataAccessException("Error while reading reservation resultset", e);
        }
    }

    private List<Reservation> getList(ResultSet rs) throws DataAccessException {
        List<Reservation> list = new ArrayList<>();
        try {
            while (rs.next()) {
                list.add(populateReservation(rs));
            }
            return list;
        } catch (SQLException e){
            throw new DataAccessException("Error while reading reservation resultset", e);
        }
    }

    private LazyStatement getUpdateTableStatement = new LazyStatement (
            "UPDATE carreservations SET reservation_status=?" +
                    " WHERE carreservations.reservation_to < NOW() AND carreservations.reservation_status = ?"
    );

    @Override
    public void updateTable() {
        try {
            PreparedStatement ps = getUpdateTableStatement.value();
            ps.setString(1, ReservationStatus.REQUEST_DETAILS.toString());
            ps.setString(2, ReservationStatus.ACCEPTED.toString());
            ps.executeUpdate(); // it is possible that no records are affected
        } catch (SQLException ex) {
            throw new DataAccessException("Error while updating the reservations table", ex);
        }
    }
}
