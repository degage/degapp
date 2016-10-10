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
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.models.Refuel;
import be.ugent.degage.db.models.RefuelExtended;
import be.ugent.degage.db.models.ApprovalStatus;

import java.sql.*;

/**
 * JDBC implementation of {@link RefuelDAO}
 */
class JDBCRefuelDAO extends AbstractDAO implements RefuelDAO {

    private static final String REFUEL_FIELDS =
            "refuel_id, refuel_file_id, refuel_eurocents, refuel_status, " +
                    "refuel_km, refuel_amount, refuel_message ";

    private static final String REFUEL_EXTENDED_QUERY =
            "SELECT SQL_CALC_FOUND_ROWS " + REFUEL_FIELDS +
                    ", reservation_id, reservation_from, reservation_to, reservation_user_id, reservation_owner_id," +
                    "car_id, car_name, user_firstname, user_lastname, car_ride_start_km, car_ride_end_km " +
            "FROM refuels " +
            "LEFT JOIN reservations ON refuel_car_ride_id = reservation_id " +
            "LEFT JOIN carrides ON refuel_car_ride_id = car_ride_car_reservation_id " +
            "LEFT JOIN cars ON reservation_car_id = car_id " +
            "LEFT JOIN users ON reservation_user_id = user_id ";


    public JDBCRefuelDAO(JDBCDataAccessContext context) {
        super(context);
    }

    public static Refuel populateRefuel(ResultSet rs) throws SQLException {
        return new Refuel(
                rs.getInt("refuel_id"),
                rs.getInt("refuel_file_id"),
                rs.getInt("refuel_eurocents"),
                ApprovalStatus.valueOf(rs.getString("refuel_status")),
                rs.getInt("refuel_km"),
                rs.getString("refuel_amount"),
                rs.getString("refuel_message")
        );
    }

    public static RefuelExtended populateRefuelExtended(ResultSet rs) throws SQLException {
        return new RefuelExtended(
                rs.getInt("refuel_id"),
                rs.getInt("refuel_file_id"),
                rs.getInt("refuel_eurocents"),
                ApprovalStatus.valueOf(rs.getString("refuel_status")),
                rs.getInt("refuel_km"),
                rs.getString("refuel_amount"),
                rs.getString("refuel_message"),
                rs.getInt("car_id"),
                rs.getString("car_name"),
                rs.getInt("reservation_id"),
                rs.getTimestamp("reservation_from").toLocalDateTime(),
                rs.getTimestamp("reservation_to").toLocalDateTime(),
                rs.getInt("reservation_user_id"),
                rs.getString("user_lastname") + " " + rs.getString("user_firstname"),
                rs.getInt("reservation_owner_id"),
                rs.getInt("car_ride_start_km"),
                rs.getInt("car_ride_end_km")
        );
    }


    private LazyStatement createRefuelStatement = new LazyStatement(
            "INSERT INTO refuels (refuel_car_ride_id, refuel_file_id, refuel_eurocents, refuel_status," +
                    "refuel_km, refuel_amount) " +
                    "VALUES (?,?,?,?,?,?)",
            "refuel_id"
    );


    @Override
    public int createRefuel(int reservationId, int eurocents, int fileId, ApprovalStatus status,
                            int km, String amount) {
        try {
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
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create refuel", e);
        }
    }

    @Override
    public void updateRefuelStatus(ApprovalStatus status, int refuelId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE refuels SET refuel_status = ? WHERE refuel_id = ? ")
        ) {
            ps.setString(1, status.name());
            ps.setInt(2, refuelId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update refuel", e);
        }
    }

    @Override
    public void rejectRefuel(int refuelId, String message) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE refuels SET refuel_status = 'REFUSED', refuel_message = ? WHERE refuel_id = ? ")
        ) {
            ps.setString(1, message);
            ps.setInt(2, refuelId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update refuel", e);
        }
    }

    @Override
    public Refuel getRefuel(int refuelId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + REFUEL_FIELDS + " FROM refuels WHERE refuel_id = ? "
        )) {
            ps.setInt(1, refuelId);
            return toSingleObject(ps, JDBCRefuelDAO::populateRefuel);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get reservation", e);
        }
    }

    @Override
    public RefuelExtended getRefuelExtended (int refuelId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                REFUEL_EXTENDED_QUERY + " WHERE refuel_id = ? "
        )) {
            ps.setInt(1, refuelId);
            return toSingleObject(ps, JDBCRefuelDAO::populateRefuelExtended);
        } catch (SQLException e) {
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

    private static void appendRefuelFilter(StringBuilder builder, Filter filter) {
        // build clause
        builder.append(" WHERE NOT refuel_archived ");

        FilterUtils.appendIdFilter(builder, "reservation_user_id", filter.getValue(FilterField.REFUEL_USER_ID));
        FilterUtils.appendIdFilter(builder, "reservation_car_id", filter.getValue(FilterField.REFUEL_CAR_ID));

        String ownerFilter = filter.getValue(FilterField.REFUEL_OWNER_ID);
        if (!ownerFilter.isEmpty()) {
            if (Integer.parseInt(ownerFilter) >= 0) {
                builder.append(" AND (reservation_user_id = ").append(ownerFilter).
                        append(" OR reservation_owner_id = ").append(ownerFilter).
                        append(")");
            }
        }
    }

    @Override
    public Page<RefuelExtended> getRefuels(int page, int pageSize, Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(REFUEL_EXTENDED_QUERY);
        appendRefuelFilter(builder, filter);
        builder.append(" ORDER BY refuel_created_at DESC LIMIT ?,?");

        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            ps.setInt(1, (page - 1) * pageSize);
            ps.setInt(2, pageSize);
            return toPage(ps, pageSize, JDBCRefuelDAO::populateRefuelExtended);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of refuels", ex);
        }
    }

    @Override
    public Page<RefuelExtended> getRefuels(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(REFUEL_EXTENDED_QUERY);
        appendRefuelFilter(builder, filter);
        // add order
        switch (orderBy) {
            case FROM:
                builder.append(" ORDER BY reservation_from ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case AMOUNT:
                builder.append(" ORDER BY refuel_eurocents ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case KM:
                builder.append(" ORDER BY refuel_km ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case LITER:
                builder.append(" ORDER BY refuel_amount ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case STATUS:
                builder.append(" ORDER BY refuel_status ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case DRIVER:
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
            return toPage(ps, pageSize, JDBCRefuelDAO::populateRefuelExtended);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of refuels", ex);
        }
    }

    @Override
    public Iterable<Refuel> getRefuelsForCarRide(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + REFUEL_FIELDS + " FROM refuels  WHERE NOT refuel_archived AND refuel_car_ride_id = ? ORDER BY refuel_id DESC ")
        ) {
            ps.setInt(1, userId);
            return toList(ps, JDBCRefuelDAO::populateRefuel);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve the list of refuels for user.", e);
        }
    }

    @Override
    public int numberOfRefuelRequests(int ownerId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT COUNT(*) " +
                        "FROM refuels JOIN reservations ON refuel_car_ride_id = reservation_id " +
                        "WHERE refuel_status = 'REQUEST' AND reservation_owner_id = ?"
        )) {
            ps.setInt(1, ownerId);
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get number of refuel requests", ex);
        }
    }
}
