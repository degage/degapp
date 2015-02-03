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

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.RefuelDAO;
import be.ugent.degage.db.models.Refuel;
import be.ugent.degage.db.models.RefuelStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

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
            "LEFT JOIN users AS owners ON car_owner_user_id = owners.user_id ";

    public JDBCRefuelDAO(JDBCDataAccessContext context) {
        super(context);
    }

    public static Refuel populateRefuel(ResultSet rs) throws SQLException {
        Refuel refuel = new Refuel(
                rs.getInt("refuel_id"),
                JDBCCarRideDAO.populateCarRide(rs),
                rs.getInt("refuel_file_id"),
                rs.getInt("refuel_eurocents"),
                RefuelStatus.valueOf(rs.getString("refuel_status")),
                rs.getInt("refuel_km"),
                rs.getString ("refuel_amount"),
                rs.getString ("refuel_message")
        );

        Date refuelBilled = rs.getDate("refuel_billed");
        refuel.setBilled(refuelBilled == null ? null : refuelBilled.toLocalDate());

        refuel.getCarRide().getReservation().getCar().setOwner(JDBCUserDAO.populateUserHeader(rs, "owners"));

        return refuel;
    }

    private LazyStatement createRefuelStatement = new LazyStatement (
            "INSERT INTO refuels (refuel_car_ride_id, refuel_file_id, refuel_eurocents, refuel_status," +
                    "refuel_km, refuel_amount) " +
                    "VALUES (?,?,?,?,?,?)",
            "refuel_id"
    );


    @Override
    public int createRefuel(int reservationId, int eurocents, int fileId, RefuelStatus status,
                             int km, String amount) {
        try{
            PreparedStatement ps = createRefuelStatement.value();
            ps.setInt(1, reservationId);
            ps.setInt(2, fileId);
            ps.setInt(3, eurocents);
            ps.setString(4, status.name());
            ps.setInt(5, km);
            ps.setString(6, amount);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return keys.getInt(1);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create refuel", e);
        }
    }

    private static final String ACCEPT_OR_REJECT_REFUEL_STATEMENT
            = "UPDATE refuels SET refuel_status = ?, refuel_message = ? WHERE refuel_id = ? ";

    @Override
    public void acceptOrRejectRefuel(RefuelStatus status, int refuelId, String message) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(ACCEPT_OR_REJECT_REFUEL_STATEMENT)) {
            ps.setString(1, status.name());
            ps.setString(2,message);
            ps.setInt(3, refuelId);
            ps.executeUpdate();
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

    /* Currently nog used. If introduced again, some field must be added
    private LazyStatement updateRefuelStatement= new LazyStatement (
            "UPDATE refuels SET refuel_file_id = ? , refuel_eurocents = ? , refuel_status = ? WHERE refuel_id = ?"
    );

    @Override
    public void updateRefuel(Refuel refuel) throws DataAccessException {
        try {
            PreparedStatement ps = updateRefuelStatement.value();
            ps.setInt(1, refuel.getProofId());
            ps.setInt(2, refuel.getEurocents());
            ps.setString(3, refuel.getStatus().name());
            ps.setInt(4, refuel.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Refuel update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update refuel", e);
        }

    }
    */

    private static void appendRefuelFilter (StringBuilder builder, Filter filter) {
        // build clause
        StringBuilder b = new StringBuilder();

        FilterUtils.appendIdFilter(b, "reservation_user_id", filter.getValue(FilterField.REFUEL_USER_ID));
        FilterUtils.appendIdFilter(b, "reservation_car_id", filter.getValue(FilterField.REFUEL_CAR_ID));

        String ownerFilter = filter.getValue(FilterField.REFUEL_OWNER_ID);
        if (! ownerFilter.isEmpty()) {
            if (Integer.parseInt(ownerFilter) >= 0) {
                b.append(" AND (reservation_user_id = ").append(ownerFilter).
                        append(" OR reservation_owner_id = ").append(ownerFilter).
                        append(")");
            }
        }

        if (b.length() > 0) {
            builder.append(" WHERE ").append(b.substring(4));
        }
    }

    private static final String AMOUNT_OF_REFUELS_STATEMENT =
             "SELECT count(*) AS amount_of_refuels FROM refuels " +
                     "LEFT JOIN reservations ON refuel_car_ride_id = reservation_id ";

    @Override
    public int getAmountOfRefuels(Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(AMOUNT_OF_REFUELS_STATEMENT);
        appendRefuelFilter(builder, filter);
        //System.err.println("SQL = " + builder.toString());
        try (Statement stat = createStatement();
             ResultSet rs = stat.executeQuery(builder.toString())) {
            if (rs.next()) {
                return rs.getInt("amount_of_refuels");
            } else {
                return 0;
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of refuels", ex);
        }
    }

    @Override
    public Iterable<Refuel> getRefuels(int page, int pageSize, Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(REFUEL_QUERY);
        appendRefuelFilter(builder, filter);
        builder.append(" ORDER BY refuel_created_at DESC LIMIT ?,?");

        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            ps.setInt(1, (page-1)*pageSize);
            ps.setInt(2, pageSize);
            return getRefuelList(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of refuels", ex);
        }
    }

    private LazyStatement getRefuelsForCarRideStatement= new LazyStatement (
            REFUEL_QUERY + " WHERE refuel_car_ride_id = ? ORDER BY refuel_id DESC "
    );

    @Override
    public Iterable<Refuel> getRefuelsForCarRide(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getRefuelsForCarRideStatement.value();
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

    private Iterable<Refuel> getRefuelList(PreparedStatement ps) throws DataAccessException {
        Collection<Refuel> list = new ArrayList<>();
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
    public Iterable<Refuel> getBillRefuelsForLoaner(LocalDate date, int user) throws DataAccessException {
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
