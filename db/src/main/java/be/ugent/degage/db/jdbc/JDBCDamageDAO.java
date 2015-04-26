/* JDBCDamageDAO.java
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
import be.ugent.degage.db.dao.DamageDAO;
import be.ugent.degage.db.models.Damage;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationHeader;
import be.ugent.degage.db.models.ReservationStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

/**
 * JDBC implementation of {@link DamageDAO}
 */
class JDBCDamageDAO extends AbstractDAO implements DamageDAO {

    public JDBCDamageDAO(JDBCDataAccessContext context) {
        super(context);
    }

    public static Damage populateDamage(ResultSet rs) throws SQLException {
        // TODO: use populateReservation?
        Reservation reservation = new Reservation(
                rs.getInt("reservation_id"),
                null, null,
                rs.getInt("reservation_owner_id"),
                rs.getTimestamp("reservation_from").toLocalDateTime(),
                rs.getTimestamp("reservation_to").toLocalDateTime(),
                null,
                false
        );
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
        reservation.setPrivileged(rs.getBoolean("reservation_privileged"));
        Date damageTime = rs.getDate("damage_time");
        return new Damage(
                rs.getInt("damage_id"),
                rs.getInt("reservation_car_id"),
                rs.getInt("reservation_user_id"),
                reservation,
                rs.getString("damage_description"),
                damageTime == null ? null : damageTime.toLocalDate(),
                rs.getBoolean("damage_finished"));
    }

    public static Damage populateDamageExtended(ResultSet rs) throws SQLException {
        Damage damage = populateDamage(rs);
        damage.setCarName(rs.getString("car_name"));
        damage.setDriverName(
                rs.getString("user_firstname") + " " + rs.getString("user_lastname")
        );
        return damage;
    }

    private LazyStatement createDamageStatement = new LazyStatement(
            "INSERT INTO damages(damage_car_ride_id, damage_time) VALUES(?, ?)",
            "damage_id"
    );

    @Override
    public void createDamage(ReservationHeader reservation) throws DataAccessException {
        try {
            PreparedStatement ps = createDamageStatement.value();
            ps.setInt(1, reservation.getId());
            LocalDate damageDate = reservation.getFrom().toLocalDate();
            ps.setDate(2, Date.valueOf(damageDate));
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating damage.");

        } catch (SQLException e) {
            throw new DataAccessException("Unable to create damage", e);
        }
    }

    private LazyStatement getDamageStatement = new LazyStatement(
            "SELECT damage_id, damage_description, damage_time, damage_finished, " +
                    "reservation_id, reservation_car_id, reservation_user_id, reservation_owner_id, " +
                    "reservation_status, reservation_privileged, reservation_from, reservation_to " +
            "FROM damages JOIN reservations ON damage_car_ride_id = reservation_id " +
            "WHERE damage_id = ?"
    );

    @Override
    public Damage getDamage(int damageId) throws DataAccessException {
        try {
            PreparedStatement ps = getDamageStatement.value();
            ps.setInt(1, damageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return populateDamage(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get damage", e);
        }
    }

    private LazyStatement updateDamageDetailsStatement = new LazyStatement(
        "UPDATE damages SET damage_description = ? , damage_time = ? WHERE damage_id = ?"
    );

    @Override
    public void updateDamageDetails(int damageId, String description, LocalDate date) throws DataAccessException {

        try {
            PreparedStatement ps = updateDamageDetailsStatement.value();
            ps.setString(1, description);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3, damageId);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Damage update affected 0 rows.");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update damage", e);
        }
    }

    private LazyStatement updateDamageFinishedStatement = new LazyStatement(
        "UPDATE damages SET damage_finished = ? WHERE damage_id = ?"
    );

    @Override
    public void updateDamageFinished(int damageId, boolean finished) throws DataAccessException {
        try {
            PreparedStatement ps = updateDamageFinishedStatement.value();
            ps.setBoolean(1, finished);
            ps.setInt(2, damageId);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Damage update affected 0 rows.");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update damage", e);
        }
    }

    private static final String LIST_DAMAGES_QUERY =
        "SELECT  damage_id, damage_description, damage_time, damage_finished, " +
                    "reservation_id, reservation_car_id, reservation_user_id, reservation_owner_id, " +
                    "reservation_status, reservation_privileged, reservation_from, reservation_to, " +
                    "car_name, user_lastname, user_firstname " +
            "FROM damages " +
            "JOIN reservations ON damage_car_ride_id = reservation_id " +
            "JOIN cars ON car_id = reservation_car_id " +
            "JOIN users ON user_id = reservation_user_id ";

    private LazyStatement getListDamagesForDriverStatement = new LazyStatement(
             LIST_DAMAGES_QUERY +
                     "WHERE reservation_user_id = ? ORDER BY damage_id desc"
    );

    @Override
    public Iterable<Damage> listDamagesForDriver(int driverId) throws DataAccessException {
        try {
            PreparedStatement ps = getListDamagesForDriverStatement.value();

            ps.setInt (1, driverId);

            Collection<Damage> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(populateDamageExtended(rs));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of damages", ex);
        }
    }

    private LazyStatement getListDamagesForOwnerStatement = new LazyStatement(
             LIST_DAMAGES_QUERY +
                     "WHERE car_owner_user_id = ? ORDER BY damage_id desc"
    );

    @Override
    public Iterable<Damage> listDamagesForOwner(int ownerId) throws DataAccessException {
        try {
            PreparedStatement ps = getListDamagesForOwnerStatement.value();

            ps.setInt (1, ownerId);

            Collection<Damage> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(populateDamageExtended(rs));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of damages", ex);
        }
    }

    private void appendDamagesFilter(StringBuilder builder, Filter filter) {
        FilterUtils.appendTristateFilter(builder, "damage_finished", filter.getValue(FilterField.DAMAGE_FINISHED));
        FilterUtils.appendIdFilter(builder, "reservation_car_id", filter.getValue(FilterField.DAMAGE_CAR_ID));
        FilterUtils.appendIdFilter (builder, "reservation_user_id", filter.getValue(FilterField.DAMAGE_USER_ID));
        FilterUtils.appendIdFilter (builder, "car_owner_user_id", filter.getValue(FilterField.DAMAGE_OWNER_ID));
    }

    @Override
    public Iterable<Damage> getDamages(int page, int pageSize, Filter filter) throws DataAccessException {
        // build query condition
        StringBuilder builder = new StringBuilder();

        appendDamagesFilter(builder, filter);

        String condition = builder.toString();
        builder = new StringBuilder(LIST_DAMAGES_QUERY);
        if (! condition.isEmpty()) {
            builder.append (" WHERE").append (condition.substring(4)); // remove leading 'AND'
        }
        builder.append (" ORDER BY damage_id desc LIMIT ?, ?");

        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            int first = (page - 1) * pageSize;
            ps.setInt(1, first);
            ps.setInt(2, pageSize);

            Collection<Damage> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(populateDamageExtended(rs));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of damages", ex);
        }
    }

   private static final String COUNT_DAMAGES_QUERY =
        "SELECT  count(*) AS amount_of_damages " +
            "FROM damages " +
            "JOIN reservations ON damage_car_ride_id = reservation_id " +
            "JOIN cars ON car_id = reservation_car_id " +
            "JOIN users ON user_id = reservation_user_id ";

    @Override
    public int getAmountOfDamages(Filter filter) throws DataAccessException {
        // build query
        StringBuilder builder = new StringBuilder();

        appendDamagesFilter(builder, filter);
        String condition = builder.toString();
        builder = new StringBuilder(COUNT_DAMAGES_QUERY);
        if (! condition.isEmpty()) {
            builder.append (" WHERE").append (condition.substring(4)); // remove leading 'AND'
        }

        try (Statement stat = createStatement();
             ResultSet rs = stat.executeQuery(builder.toString())) {
            if (rs.next())
                return rs.getInt("amount_of_damages");
            else
                return 0;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of damages", ex);
        }
    }

    private LazyStatement getAmountOfOpenDamagesStatement = new LazyStatement(
            "SELECT COUNT(*) AS amount_of_damages FROM damages " +
                "JOIN reservations ON damage_car_ride_id = reservation_id " +
                "WHERE NOT damage_finished AND reservation_user_id = ?"
    );

    @Override
    public int getAmountOfOpenDamages(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfOpenDamagesStatement.value();
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("amount_of_damages");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of open damages", ex);
        }
    }

}
