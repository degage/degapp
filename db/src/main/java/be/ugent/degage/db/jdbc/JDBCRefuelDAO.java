/* JDBCRefuelDAO.java
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
import be.ugent.degage.db.dao.RefuelDAO;
import be.ugent.degage.db.models.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
class JDBCRefuelDAO extends AbstractDAO implements RefuelDAO {

    // TODO: reduce output from this query * -> actual fields
    private static final String REFUEL_QUERY = "SELECT * FROM refuels " +
            "LEFT JOIN carrides ON refuel_car_ride_id = car_ride_car_reservation_id " +
            "LEFT JOIN reservations ON refuel_car_ride_id = reservation_id " +
            "LEFT JOIN cars ON reservation_car_id = car_id " +
            "LEFT JOIN users ON reservation_user_id = user_id " +
            "LEFT JOIN users AS owners ON car_owner_user_id = owners.user_id " +
            "LEFT JOIN files ON refuel_file_id = file_id ";

    private static final String FILTER_FRAGMENT = " WHERE reservation_user_id LIKE ? AND car_owner_user_id LIKE ? AND car_id LIKE ? AND refuel_status <> ?";

    public JDBCRefuelDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
        String userId = filter.getValue(FilterField.REFUEL_USER_ID);
        if(userId.equals("") || userId.startsWith("-")) { // Not very nice programming, but works :D
            userId = "%%";
        }
        ps.setString(start, userId);

        String ownerId = filter.getValue(FilterField.REFUEL_OWNER_ID);
        if(ownerId.equals("") || ownerId.startsWith("-")) { // Not very nice programming, but works :D
            ownerId = "%%";
        }
        ps.setString(start+1, ownerId);

        String carId = filter.getValue(FilterField.REFUEL_CAR_ID);
        if(carId.equals("") || carId.startsWith("-")) { // Not very nice programming, but works :D
            carId = "%%";
        }
        ps.setString(start+2, carId);

        ps.setString(start+3, filter.getValue(FilterField.REFUEL_NOT_STATUS));
    }

    public static Refuel populateRefuel(ResultSet rs) throws SQLException {
        Refuel refuel;
        if(rs.getString("refuel_status").equals("CREATED")){
            refuel = new Refuel(rs.getInt("refuel_id"), JDBCCarRideDAO.populateCarRide(rs), RefuelStatus.valueOf(rs.getString("refuel_status")));
        }else{
            refuel = new Refuel(rs.getInt("refuel_id"), JDBCCarRideDAO.populateCarRide(rs), JDBCFileDAO.populateFile(rs),
                    rs.getInt("refuel_eurocents"), RefuelStatus.valueOf(rs.getString("refuel_status")));
        }

        Date refuelBilled = rs.getDate("refuel_billed");
        refuel.setBilled(refuelBilled == null ? null : refuelBilled.toLocalDate());

        refuel.getCarRide().getReservation().getCar().setOwner(JDBCUserDAO.populateUserHeader(rs, "owners"));

        return refuel;
    }



    private LazyStatement createRefuelStatement= new LazyStatement (
            "INSERT INTO refuels (refuel_car_ride_id) VALUES (?)",
            "refuel_id"
    );

    @Override
    public Refuel createRefuel(CarRide carRide) throws DataAccessException {
        try{
            PreparedStatement ps = createRefuelStatement.value();
            ps.setInt(1, carRide.getReservation().getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating refuel.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Refuel(keys.getInt(1), carRide, RefuelStatus.CREATED);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for refuel.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create refuel", e);
        }
    }

    private LazyStatement getStatusRefuelStatement= new LazyStatement (
            "UPDATE refuels SET refuel_status = ? WHERE refuel_id = ?"
    );

    @Override
    public void acceptRefuel(int refuelId) throws DataAccessException {
        try {
            PreparedStatement ps = getStatusRefuelStatement.value();
            ps.setString(1, "ACCEPTED");
            ps.setInt(2, refuelId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("CarCost update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update refuel", e);
        }
    }

    @Override
    public void rejectRefuel(int refuelId) throws DataAccessException {
        try {
            PreparedStatement ps = getStatusRefuelStatement.value();
            ps.setString(1, "REFUSED");
            ps.setInt(2, refuelId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("CarCost update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update refuel", e);
        }
    }

    private LazyStatement deleteRefuelStatement= new LazyStatement (
            "DELETE FROM refuels WHERE refuel_id = ?"
    );

    @Override
    public void deleteRefuel(int refuelId) throws DataAccessException {
        try {
            PreparedStatement ps = deleteRefuelStatement.value();
            ps.setInt(1, refuelId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting refuel.");
        } catch (SQLException e){
            throw new DataAccessException("Could not delete refuel.", e);
        }

    }

    private LazyStatement getRefuelStatement= new LazyStatement (REFUEL_QUERY + " WHERE refuel_id = ? ");

    @Override
    public Refuel getRefuel(int refuelId) throws DataAccessException {
        try {
            PreparedStatement ps = getRefuelStatement.value();
            ps.setInt(1, refuelId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateRefuel(rs);
                else return null;
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get reservation", e);
        }
    }

    private LazyStatement updateRefuelStatement= new LazyStatement (
            "UPDATE refuels SET refuel_file_id = ? , refuel_eurocents = ? , refuel_status = ? WHERE refuel_id = ?"
    );

    @Override
    public void updateRefuel(Refuel refuel) throws DataAccessException {
        try {
            PreparedStatement ps = updateRefuelStatement.value();
            ps.setInt(1, refuel.getProof().getId());
            ps.setInt(2, refuel.getEurocents());
            ps.setString(3, refuel.getStatus().name());
            ps.setInt(4, refuel.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Refuel update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update refuel", e);
        }

    }

    private LazyStatement getAmountOfRefuelsStatement= new LazyStatement (
             "SELECT count(refuel_id) AS amount_of_refuels FROM refuels " +
                     "LEFT JOIN carrides ON refuel_car_ride_id = car_ride_car_reservation_id " +
                     "LEFT JOIN reservations ON refuel_car_ride_id = reservation_id " +
                     "LEFT JOIN cars ON reservation_car_id = car_id " +
                     "LEFT JOIN users ON reservation_user_id = user_id " +
                     "LEFT JOIN files ON refuel_file_id = file_id " + FILTER_FRAGMENT
     );

    @Override
    public int getAmountOfRefuels(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfRefuelsStatement.value();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_refuels");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of refuels", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of refuels", ex);
        }
    }

    private LazyStatement getRefuelsStatement= new LazyStatement (
            REFUEL_QUERY + FILTER_FRAGMENT +
                    "ORDER BY CASE refuel_status WHEN 'CREATED' THEN 1 WHEN 'REQUEST' THEN 2 WHEN 'REFUSED' THEN 3 " +
                    "WHEN 'ACCEPTED' THEN 4 END ASC LIMIT ?,?"
    );

    @Override
    public List<Refuel> getRefuels(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            // TODO: more to orderBy, asc/desc
            PreparedStatement ps = getRefuelsStatement.value();
            fillFragment(ps, filter, 1);
            int first = (page-1)*pageSize;
            ps.setInt(5, first);
            ps.setInt(6, pageSize);
            return getRefuelList(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of refuels", ex);
        }
    }

    private LazyStatement getRefuelsForUserStatement= new LazyStatement (
            REFUEL_QUERY + " WHERE reservation_user_id = ? " +
                    "ORDER BY CASE refuel_status WHEN 'CREATED' THEN 1 WHEN 'REQUEST' THEN 2 WHEN 'REFUSED' THEN 3 " +
                    "WHEN 'ACCEPTED' THEN 4 END"
    );

    @Override
    public List<Refuel> getRefuelsForUser(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getRefuelsForUserStatement.value();
            ps.setInt(1, userId);
            return getRefuelList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of refuels for user.", e);
        }
    }

    private LazyStatement getRefuelsForOwnerStatement= new LazyStatement (
            REFUEL_QUERY + " WHERE car_owner_user_id = ? AND refuel_status <> 'CREATED' " +
                    "ORDER BY CASE refuel_status WHEN 'REQUEST' THEN 1 WHEN 'REFUSED' THEN 3 " +
                    "WHEN 'ACCEPTED' THEN 2 END"
    );

    @Override
    public List<Refuel> getRefuelsForOwner(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getRefuelsForOwnerStatement.value();
            ps.setInt(1, userId);
            return getRefuelList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of refuels for user.", e);
        }
    }

    private LazyStatement getAmountOfRefuelsWithStatusStatement= new LazyStatement (
            "SELECT COUNT(*) AS amount_of_refuels " +
                    "FROM refuels JOIN reservations ON refuel_car_ride_id = reservation_id " +
                    "WHERE refuel_status = ? AND reservation_user_id = ?"
    );

    @Override
    public int getAmountOfRefuelsWithStatus(RefuelStatus status, int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfRefuelsWithStatusStatement.value();
            ps.setString(1, status.name());
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_refuels");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of refuels with status", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of refuels with status", ex);
        }
    }

    private List<Refuel> getRefuelList(PreparedStatement ps) throws DataAccessException {
        List<Refuel> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateRefuel(rs));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading refuel resultset", e);
        }
    }

    private LazyStatement endPeriodStatement = new LazyStatement (
            "UPDATE refuels SET refuel_billed = CURDATE() " +
                    "FROM refuels INNER JOIN carrides ON refuels.refuel_car_ride_id = carrides.car_ride_car_reservation_id " +
                    "             INNER JOIN reservations ON carrides.car_ride_car_reservation_id = reservations.reservation_id " +
                    "WHERE refuels.refuel_billed = NULL AND refuels.refuel_status = 'ACCEPTED' AND carreservation.reservation_to < CURDATE()"
    );

    @Override
    public void endPeriod() throws DataAccessException {
        try {
            PreparedStatement ps = endPeriodStatement.value();

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Refuel update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update car refuel", e);
        }
    }

    private LazyStatement getBillRefuelsForLoanerStatement= new LazyStatement (
            REFUEL_QUERY + " WHERE refuel_billed = ? AND reservation_user_id = ?"
    );

    @Override
    public List<Refuel> getBillRefuelsForLoaner(LocalDate date, int user) throws DataAccessException {
        try {
            PreparedStatement ps = getBillRefuelsForLoanerStatement.value();
            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, user);
            return getRefuelList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of refuels for user.", e);
        }
    }

    private LazyStatement eurocentsSpentOnFuelStatement = new LazyStatement(
            "SELECT SUM(refuel_eurocents) AS s, reservation_privileged " +
            "FROM refuels JOIN reservations ON refuel_car_ride_id = reservation_id " +
                "WHERE refuel_billed = ? AND reservation_car_id = ? " +
            "GROUP BY refuel_eurocents, reservation_privileged "
    );

    public int[] eurocentsSpentOnFuel (LocalDate date, int carId) throws DataAccessException {
        try {
            PreparedStatement ps = eurocentsSpentOnFuelStatement.value();
            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, carId);

            try (ResultSet rs = ps.executeQuery()) {
                int[] result = new int[2];
                while (rs.next()) {
                    int index = rs.getBoolean ("reservation_privileged") ? 0 : 1;
                    result[index] = rs.getInt("s");
                }
                return result;
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of refuels for car.", e);
        }
    }
}
