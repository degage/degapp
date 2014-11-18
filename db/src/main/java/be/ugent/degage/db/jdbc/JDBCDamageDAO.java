package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.DamageDAO;
import be.ugent.degage.db.models.Damage;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationStatus;
import org.joda.time.DateTime;

import java.sql.*;
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
                new DateTime(rs.getTimestamp("reservation_from")),
                new DateTime(rs.getTimestamp("reservation_to")),
                null
        );
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
        reservation.setPrivileged(rs.getBoolean("reservation_privileged"));
        return new Damage(
                rs.getInt("damage_id"),
                rs.getInt("reservation_car_id"),
                rs.getInt("reservation_user_id"),
                reservation,
                rs.getString("damage_description"),
                new DateTime(rs.getTimestamp("damage_time")),
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
    public Damage createDamage(Reservation reservation) throws DataAccessException {
        try {
            PreparedStatement ps = createDamageStatement.value();
            ps.setInt(1, reservation.getId());
            ps.setTimestamp(2, new Timestamp(reservation.getFrom().getMillis()));
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating damage.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Damage(
                        keys.getInt(1),
                        reservation.getCar().getId(),
                        reservation.getUser().getId(),
                        reservation,
                        null,
                        reservation.getFrom(),
                        false
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create damage", e);
        }
    }

    private LazyStatement getDamageStatement = new LazyStatement(
            "SELECT damage_id, damage_description, damage_time, damage_finished, " +
                    "reservation_id, reservation_car_id, reservation_user_id, " +
                    "reservation_status, reservation_privileged, reservation_from, reservation_to " +
            "FROM damages JOIN carreservations ON damage_car_ride_id = reservation_id " +
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
    public void updateDamageDetails(int damageId, String description, DateTime time) throws DataAccessException {

        try {
            PreparedStatement ps = updateDamageDetailsStatement.value();
            ps.setString(1, description);
            ps.setTimestamp(2, new Timestamp(time.getMillis()));
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
                    "reservation_id, reservation_car_id, reservation_user_id, " +
                    "reservation_status, reservation_privileged, reservation_to, " +
                    "car_name, user_lastname, user_firstname " +
            "FROM damages " +
            "JOIN carreservations ON damage_car_ride_id = reservation_id " +
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
        FilterUtils.appendIntFilter(builder, "reservation_car_id", filter.getValue(FilterField.DAMAGE_CAR_ID));
        FilterUtils.appendIntFilter (builder, "reservation_user_id", filter.getValue(FilterField.DAMAGE_USER_ID));
        FilterUtils.appendIntFilter (builder, "car_owner_user_id", filter.getValue(FilterField.DAMAGE_OWNER_ID));
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
            "JOIN carreservations ON damage_car_ride_id = reservation_id " +
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
                "JOIN carreservations ON damage_car_ride_id = reservation_id " +
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
