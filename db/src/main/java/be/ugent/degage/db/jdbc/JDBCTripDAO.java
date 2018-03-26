/* JDBCTripDAO.java
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
import be.ugent.degage.db.dao.TripDAO;
import be.ugent.degage.db.models.CarHeader;
import be.ugent.degage.db.models.ReservationStatus;
import be.ugent.degage.db.models.Trip;
import be.ugent.degage.db.models.TripAndCar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link TripDAO} for JDBC
 */
class JDBCTripDAO extends AbstractDAO implements TripDAO {

    private static void populateExtras(ResultSet rs, Trip trip) throws SQLException {
        trip.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
        trip.setPrivileged(rs.getBoolean("reservation_privileged"));
        trip.setStartKm(rs.getInt("car_ride_start_km"));   // 0 if null
        trip.setEndKm(rs.getInt("car_ride_end_km"));       // 0 if null
        trip.setDamaged(rs.getBoolean("car_ride_damage")); // 0 if null

        trip.setDriverName(rs.getString("user_firstname") + " " + rs.getString("user_lastname"));
    }

    public static Trip populateTrip (ResultSet rs) throws SQLException {
        // TODO: has a lot of code in common populateReservationHeader and populateReservation
        Trip trip = new Trip(
                rs.getInt("reservation_id"),
                rs.getInt("reservation_car_id"),
                rs.getInt("reservation_user_id"),
                rs.getInt("reservation_owner_id"),
                rs.getTimestamp("reservation_from").toLocalDateTime(),
                rs.getTimestamp("reservation_to").toLocalDateTime(),
                JDBCReservationDAO.extractMessages(rs),
                rs.getTimestamp("reservation_created_at").toInstant().plusSeconds(TimeUnit.DAYS.toSeconds(1)).isBefore(Instant.now()),
                rs.getTimestamp("reservation_created_at").toLocalDateTime()
        );
        populateExtras(rs, trip);
        return trip;
    }

    public static TripAndCar populateTripWithCar (ResultSet rs, boolean withLocation) throws SQLException {
        // TODO: a lot of code in common with populateTrip
        CarHeader car = JDBCCarDAO.populateCarHeader(rs);
        if (withLocation) {
            car.setLocation(JDBCAddressDAO.populateAddress(rs));
        }
        TripAndCar trip = new TripAndCar(
                rs.getInt("reservation_id"),
                rs.getInt("reservation_car_id"),
                rs.getInt("reservation_user_id"),
                rs.getInt("reservation_owner_id"),
                rs.getTimestamp("reservation_from").toLocalDateTime(),
                rs.getTimestamp("reservation_to").toLocalDateTime(),
                JDBCReservationDAO.extractMessages(rs),
                rs.getTimestamp("reservation_created_at").toInstant().plusSeconds(TimeUnit.DAYS.toSeconds(1)).isBefore(Instant.now()),
                rs.getTimestamp("reservation_created_at").toLocalDateTime(),
                car
        );
        populateExtras(rs, trip);
        return trip;
    }

    public JDBCTripDAO(JDBCDataAccessContext context) {
        super(context);
    }

    public static final String TRIP_FIELDS =
            JDBCReservationDAO.RESERVATION_HEADER_FIELDS +
            ", car_ride_start_km, car_ride_end_km, car_ride_damage, " +
            "user_firstname, user_lastname ";

    @Override
    public Iterable<Trip> listTrips(int carId, LocalDateTime from, LocalDateTime until) {
        if (until.isAfter(LocalDateTime.now())) {
            until = LocalDateTime.now();
        }
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + TRIP_FIELDS +
                "FROM trips JOIN users ON user_id = reservation_user_id " +
                "WHERE reservation_status > 3 " + // [ENUM INDEX]
                "    AND reservation_from >= ? AND reservation_from <= ?" +
                "    AND reservation_car_id = ? ORDER BY reservation_from"
        )) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(until));
            ps.setInt (3, carId);
            return toList(ps, JDBCTripDAO::populateTrip);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not list trips", ex);
        }
    }

    @Override
    public void approveTrip(int tripId) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE reservations SET reservation_status = 'FINISHED' " +
                "WHERE reservation_id = ? AND reservation_status = 'DETAILS_PROVIDED'"
        )) {
            ps.setInt (1, tripId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not approve trip", ex);
        }
    }

    @Override
    public void updateTrip(int tripId, int start, int end) {
        // first update reservations table
        try (PreparedStatement ps = prepareStatement(
                "UPDATE reservations SET reservation_status = 'FINISHED' " +
                "WHERE reservation_id = ? AND " +
                        "(reservation_status = 'REQUEST_DETAILS' OR reservation_status = 'DETAILS_REJECTED')"
        )) {
            ps.setInt (1, tripId);
            if (ps.executeUpdate() == 0) {
                return; // bail out
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not update trip", ex);
        }

        // now update or insert into table
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO carrides(car_ride_car_reservation_id, car_ride_start_km, car_ride_end_km) " +
                        "VALUES (?,?,?) " +
                        "ON DUPLICATE KEY UPDATE car_ride_start_km = ?, car_ride_end_km = ? "
        )) {
            ps.setInt (1, tripId);
            ps.setInt (2, start);
            ps.setInt (3, end);
            ps.setInt (4, start);
            ps.setInt (5, end);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not update trip", ex);
        }
    }

    @Override
    public TripAndCar getTripAndCar(int id, boolean withLocation) {
        StringBuilder builder = new StringBuilder(
           "SELECT " + TRIP_FIELDS + ", " + JDBCCarDAO.CAR_HEADER_FIELDS
        );
        if (withLocation) {
            builder.append(",").append(JDBCAddressDAO.ADDRESS_FIELDS);
        }
        builder.append("FROM trips ")
                .append("JOIN users ON user_id = reservation_user_id ")
                .append("JOIN cars ON car_id = reservation_car_id ");
        if (withLocation) {
            builder.append("JOIN addresses ON address_id=car_location ");
        }
        builder.append("WHERE reservation_id = ? ");
        //System.err.println("SQL = " + builder.toString());
        try (PreparedStatement ps = prepareStatement( builder.toString() )) {
            ps.setInt(1,id);
            return toSingleObject(ps, rs -> populateTripWithCar(rs,withLocation));
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get trip and car", ex);
        }
    }
}
