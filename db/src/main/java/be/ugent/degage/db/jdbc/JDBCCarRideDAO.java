package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.dao.CarRideDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.CarRide;
import be.ugent.degage.db.models.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HannesM on 10/03/14.
 */
class JDBCCarRideDAO extends AbstractDAO implements CarRideDAO {

    public JDBCCarRideDAO(JDBCDataAccessContext context) {
        super(context);
    }

    public static CarRide populateCarRide(ResultSet rs) throws SQLException {
        CarRide carRide = new CarRide(JDBCReservationDAO.populateReservation(rs));
        carRide.setStatus(rs.getBoolean("car_ride_status"));
        carRide.setStartMileage(rs.getInt("car_ride_start_mileage"));
        carRide.setEndMileage(rs.getInt("car_ride_end_mileage"));
        carRide.setDamaged(rs.getBoolean("car_ride_damage"));
        carRide.setRefueling(rs.getInt("car_ride_refueling"));
        carRide.setCost(rs.getBigDecimal("car_ride_cost"));
        carRide.setBilled(rs.getDate("car_ride_billed"));

        return carRide;
    }

    private LazyStatement createCarRideStatement = new LazyStatement(
            "INSERT INTO carrides (car_ride_car_reservation_id, car_ride_start_mileage, " +
                    "car_ride_end_mileage, car_ride_damage, car_ride_refueling) VALUE (?, ?, ?, ?, ?)"
    );
    
    @Override
    public CarRide createCarRide(Reservation reservation, int startMileage, int endMileage, boolean damaged, int refueling) throws DataAccessException {
        try{
            PreparedStatement ps = createCarRideStatement.value();
            ps.setInt(1, reservation.getId());
            ps.setInt(2, startMileage);
            ps.setInt(3, endMileage);
            ps.setBoolean(4, damaged);
            ps.setInt(5, refueling);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating car ride.");

                return new CarRide(reservation);
        } catch (SQLException e){
            throw new DataAccessException("Unable to create car ride", e);
        }
    }

    private LazyStatement getCarRideStatement = new LazyStatement (
            "SELECT * FROM carrides INNER JOIN carreservations ON carrides.car_ride_car_reservation_id = carreservations.reservation_id " +
                    "INNER JOIN cars ON carreservations.reservation_car_id = cars.car_id INNER JOIN users ON carreservations.reservation_user_id = users.user_id " +
                    " WHERE car_ride_car_reservation_id = ?"
    );

    @Override
    public CarRide getCarRide(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getCarRideStatement.value();
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateCarRide(rs);
                else return null;
            }catch (SQLException e){
                throw new DataAccessException("Error reading car ride resultset", e);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get car ride", e);
        }
    }

    private LazyStatement updateCarRideStatement = new LazyStatement(
            "UPDATE carrides SET car_ride_status = ? , car_ride_start_mileage = ? , " +
                    "car_ride_end_mileage = ? , car_ride_damage = ? , car_ride_refueling = ? , car_ride_cost = ? , car_ride_billed = ? " +
                    "WHERE car_ride_car_reservation_id = ?"
    );

    @Override
    public void updateCarRide(CarRide carRide) throws DataAccessException {
        try {
            PreparedStatement ps = updateCarRideStatement.value();
            ps.setBoolean(1, carRide.isStatus());
            ps.setInt(2, carRide.getStartMileage());
            ps.setInt(3, carRide.getEndMileage());
            ps.setBoolean(4, carRide.isDamaged());
            ps.setInt(5, carRide.getRefueling());
            ps.setBigDecimal(6, carRide.getCost());
            ps.setDate(7, carRide.getBilled());

            ps.setInt(8, carRide.getReservation().getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Car Ride update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update car ride", e);
        }
    }

    private LazyStatement getEndPeriodStatement = new LazyStatement (
            "UPDATE carrides" +
                    "  INNER JOIN carreservations ON car_ride_car_reservation_id = reservation_id " +
                    "  SET car_ride_billed = CURDATE() " +
                    "  WHERE car_ride_billed IS NULL AND car_ride_status = 1 AND reservation_to < CURDATE() " 
    );

    @Override
    public void endPeriod() throws DataAccessException {
        try {
            PreparedStatement ps = getEndPeriodStatement.value();

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Car Ride update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update car ride", e);
        }
    }

    private LazyStatement getBillRidesForLoanerStatement = new LazyStatement(
            "SELECT * FROM carrides INNER JOIN carreservations ON carrides.car_ride_car_reservation_id = carreservations.reservation_id " +
                    "INNER JOIN cars ON carreservations.reservation_car_id = cars.car_id INNER JOIN users ON carreservations.reservation_user_id = users.user_id " +
                    "WHERE car_ride_billed = ? AND reservation_user_id = ?"
    );

    @Override
    public List<CarRide> getBillRidesForLoaner(Date date, int user) throws DataAccessException {
        List<CarRide> list = new ArrayList<>();
        try {
            PreparedStatement ps = getBillRidesForLoanerStatement.value();
            ps.setDate(1, date);
            ps.setInt(2, user);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(populateCarRide(rs));
            }
            return list;
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of reservations", e);
        }
    }

    private LazyStatement getBillRidesForCarStatement = new LazyStatement(
            "SELECT * FROM carrides INNER JOIN carreservations ON carrides.car_ride_car_reservation_id = carreservations.reservation_id " +
                    "INNER JOIN cars ON carreservations.reservation_car_id = cars.car_id INNER JOIN users ON carreservations.reservation_user_id = users.user_id " +
                    "WHERE car_ride_billed = ? AND reservation_car_id = ?"
    );

    @Override
    public List<CarRide> getBillRidesForCar(Date date, int car) throws DataAccessException {
        List<CarRide> list = new ArrayList<>();
        try {
            PreparedStatement ps = getBillRidesForCarStatement.value();
            ps.setDate(1, date);
            ps.setInt(2, car);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(populateCarRide(rs));
            }
            return list;
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of reservations", e);
        }
    }
}
