/* JDBCCarRideDAO.java
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
import be.ugent.degage.db.dao.CarRideDAO;
import be.ugent.degage.db.models.CarRide;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationHeader;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
        CarRide carRide = new CarRide(
                JDBCReservationDAO.populateReservationHeader(rs),
                rs.getInt("car_ride_start_km"),
                rs.getInt("car_ride_end_km"),
                rs.getBoolean("car_ride_damage")
        );
        carRide.setCost(rs.getBigDecimal("car_ride_cost"));
        Date carRideBilled = rs.getDate("car_ride_billed");
        carRide.setBilled(carRideBilled == null ? null : carRideBilled.toLocalDate());

        return carRide;
    }

    private LazyStatement createCarRideStatement = new LazyStatement(
            "INSERT INTO carrides (car_ride_car_reservation_id, car_ride_start_km, " +
                    "car_ride_end_km, car_ride_damage) VALUE (?, ?, ?, ?)"
    );
    
    @Override
    public CarRide createCarRide(ReservationHeader reservation, int startKm, int endKm, boolean damaged) throws DataAccessException {
        try{
            PreparedStatement ps = createCarRideStatement.value();
            ps.setInt(1, reservation.getId());
            ps.setInt(2, startKm);
            ps.setInt(3, endKm);
            ps.setBoolean(4, damaged);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating car ride.");

            return new CarRide(reservation, startKm, endKm, damaged);
        } catch (SQLException e){
            throw new DataAccessException("Unable to create car ride", e);
        }
    }

    // replace * by actual fields!
    private LazyStatement getCarRideStatement = new LazyStatement (
            "SELECT * FROM carrides INNER JOIN reservations ON carrides.car_ride_car_reservation_id = reservations.reservation_id " +
                    "INNER JOIN cars ON reservations.reservation_car_id = cars.car_id INNER JOIN users ON reservations.reservation_user_id = users.user_id " +
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
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get car ride", e);
        }
    }

    /* no longer used

    private LazyStatement updateCarRideStatement = new LazyStatement(
            "UPDATE carrides SET car_ride_start_km = ? , " +
                    "car_ride_end_km = ? , car_ride_damage = ? , car_ride_cost = ? , car_ride_billed = ? " +
                    "WHERE car_ride_car_reservation_id = ?"
    );

    @Override
    public void updateCarRide(CarRide carRide) throws DataAccessException {
        try {
            PreparedStatement ps = updateCarRideStatement.value();
            ps.setInt(1, carRide.getStartKm());
            ps.setInt(2, carRide.getEndKm());
            ps.setBoolean(3, carRide.isDamaged());
            ps.setBigDecimal(4, carRide.getCost());
            ps.setDate(5, Date.valueOf(carRide.getBilled()));

            ps.setInt(6, carRide.getReservation().getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Car Ride update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update car ride", e);
        }
    }
    */

    private LazyStatement updateCarRideKmStatement = new LazyStatement(
            "UPDATE carrides SET car_ride_start_km = ? , car_ride_end_km = ? WHERE car_ride_car_reservation_id = ?"
    );

    @Override
    public void updateCarRideKm(int rideId, int startKm, int endKm) throws DataAccessException {
        try {
            PreparedStatement ps = updateCarRideKmStatement.value();
            ps.setInt(1, startKm);
            ps.setInt(2, endKm);
            ps.setInt(3,rideId);

            ps.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("Unable to update car ride", e);
        }
    }

    private LazyStatement getEndPeriodStatement = new LazyStatement (
            "UPDATE carrides" +
                    "  INNER JOIN reservations ON car_ride_car_reservation_id = reservation_id " +
                    "  SET car_ride_billed = CURDATE() " +
                    "  WHERE car_ride_billed IS NULL AND reservation_to < CURDATE() "
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
            "SELECT * FROM carrides INNER JOIN reservations ON carrides.car_ride_car_reservation_id = reservations.reservation_id " +
                    "INNER JOIN cars ON reservations.reservation_car_id = cars.car_id INNER JOIN users ON reservations.reservation_user_id = users.user_id " +
                    "WHERE car_ride_billed = ? AND reservation_user_id = ?"
    );

    @Override
    public List<CarRide> getBillRidesForLoaner(LocalDate date, int user) throws DataAccessException {
        List<CarRide> list = new ArrayList<>();
        try {
            PreparedStatement ps = getBillRidesForLoanerStatement.value();
            ps.setDate(1, Date.valueOf(date));
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
            "SELECT * FROM carrides INNER JOIN reservations ON carrides.car_ride_car_reservation_id = reservations.reservation_id " +
                    "INNER JOIN cars ON reservations.reservation_car_id = cars.car_id INNER JOIN users ON reservations.reservation_user_id = users.user_id " +
                    "WHERE car_ride_billed = ? AND reservation_car_id = ?"
    );

    @Override
    public List<CarRide> getBillRidesForCar(LocalDate date, int car) throws DataAccessException {
        List<CarRide> list = new ArrayList<>();
        try {
            PreparedStatement ps = getBillRidesForCarStatement.value();
            ps.setDate(1, Date.valueOf(date));
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

    private LazyStatement getNextStartKmStatement = new LazyStatement(
            "SELECT car_ride_start_km "+
            "FROM reservations AS r " +
            "LEFT JOIN carrides ON r.reservation_id = car_ride_car_reservation_id " +
            "JOIN reservations AS o ON r.reservation_car_id = o.reservation_car_id " +
                    "WHERE o.reservation_id = ? " +
                    "AND r.reservation_status > 5  " + // [ENUM_INDEX]
                    "AND r.reservation_from >= o.reservation_to  " +
                    "ORDER BY r.reservation_from ASC LIMIT 1"
    );

    @Override
    public int getNextStartKm(int reservationId) {
        try {
            PreparedStatement ps = getNextStartKmStatement.value();
            ps.setInt(1, reservationId);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt("car_ride_start_km");
                } else {
                    return 0;
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Error while retreiving next start km " + reservationId, ex);
        }
    }

    private LazyStatement getPrevEndKmStatement = new LazyStatement(
            "SELECT car_ride_end_km " +
            "FROM reservations AS r " +
            "LEFT JOIN carrides ON r.reservation_id = car_ride_car_reservation_id " +
            "JOIN reservations AS o ON r.reservation_car_id = o.reservation_car_id " +
                    "WHERE o.reservation_id = ? " +
                    "AND r.reservation_status > 5  " + // [ENUM_INDEX]
                    "AND r.reservation_to <= o.reservation_from  " +
                    "ORDER BY r.reservation_from DESC LIMIT 1"
    );

    @Override
    public int getPrevEndKm(int reservationId) {
        try {
            PreparedStatement ps = getPrevEndKmStatement.value();
            ps.setInt(1, reservationId);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt("car_ride_end_km");
                } else {
                    return 0;
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Error while retreiving previous start km " + reservationId, ex);
        }
    }


}
