package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.DamageDAO;
import be.ugent.degage.db.models.CarRide;
import be.ugent.degage.db.models.Damage;
import org.joda.time.DateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link DamageDAO}
 */
class JDBCDamageDAO extends AbstractDAO implements DamageDAO {

    private static final String DAMAGE_QUERY = "SELECT * FROM damages " +
            "JOIN carrides ON damage_car_ride_id = car_ride_car_reservation_id " +
            "JOIN carreservations ON damage_car_ride_id = reservation_id " +
            "JOIN cars ON reservation_car_id = car_id " +
            "JOIN users ON reservation_user_id = user_id ";

    public static final String FILTER_FRAGMENT = " WHERE (damage_finished = ? OR damage_finished LIKE ?) AND reservation_user_id LIKE ? " +
            "AND car_id LIKE ? AND car_owner_user_id LIKE ? ";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if (filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }

        String finished = filter.getValue(FilterField.DAMAGE_FINISHED);

        ps.setString(start, finished);

        String s = ""; // This will match nothing
        if (finished.equals("-1") || finished.equals("")) { // Not very nice programming, but works :D
            s = "%%"; // This will match everything
        }
        ps.setString(start + 1, s);

        String userId = filter.getValue(FilterField.DAMAGE_USER_ID);
        if (userId.equals("")) { // Not very nice programming, but works :D
            userId = "%%";
        }
        ps.setString(start + 2, userId);

        String carId = filter.getValue(FilterField.DAMAGE_CAR_ID);
        if (carId.equals("")) { // Not very nice programming, but works :D
            carId = "%%";
        }
        ps.setString(start + 3, carId);

        String ownerId = filter.getValue(FilterField.DAMAGE_OWNER_ID);
        if (ownerId.equals("")) { // Not very nice programming, but works :D
            ownerId = "%%";
        }
        ps.setString(start + 4, ownerId);
    }

    public JDBCDamageDAO(JDBCDataAccessContext context) {
        super(context);
    }

    public static Damage populateDamage(ResultSet rs) throws SQLException {
        return new Damage(rs.getInt("damage_id"), JDBCCarRideDAO.populateCarRide(rs),
                rs.getString("damage_description"), new DateTime(rs.getTimestamp("damage_time")),
                rs.getBoolean("damage_finished"));
    }

    private LazyStatement createDamageStatement = new LazyStatement(
            "INSERT INTO damages(damage_car_ride_id, damage_time) VALUES(?, ?)",
            "damage_id"
    );

    @Override
    public Damage createDamage(CarRide carRide) throws DataAccessException {
        try {
            PreparedStatement ps = createDamageStatement.value();
            ps.setInt(1, carRide.getReservation().getId());
            ps.setTimestamp(2, new Timestamp(carRide.getReservation().getFrom().getMillis()));
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating damage.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Damage(keys.getInt(1), carRide);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create damage", e);
        }
    }

    private LazyStatement getDamageStatement = new LazyStatement(
            DAMAGE_QUERY + " WHERE damage_id = ?"
    );

    @Override
    public Damage getDamage(int damageId) throws DataAccessException {
        try {
            PreparedStatement ps = getDamageStatement.value();
            ps.setInt(1, damageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return populateDamage(rs);
                else
                    return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get damage", e);
        }
    }

    private LazyStatement updateDamageStatement = new LazyStatement(
            "UPDATE damages SET damage_car_ride_id = ? ," +
                    "damage_description = ? , damage_finished = ? , damage_time = ? "
                    + "WHERE damage_id = ?"
    );

    @Override
    public void updateDamage(Damage damage) throws DataAccessException {
        try {
            PreparedStatement ps = updateDamageStatement.value();
            ps.setInt(1, damage.getCarRide().getReservation().getId());
            ps.setString(2, damage.getDescription());
            ps.setBoolean(3, damage.getFinished());
            ps.setTimestamp(4, new Timestamp(damage.getTime().getMillis()));
            ps.setInt(5, damage.getId());
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Damage update affected 0 rows.");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update damage", e);
        }
    }

    private LazyStatement getAmountOfDamagesStatement = new LazyStatement(
            "SELECT count(damage_id) as amount_of_damages FROM damages " +
                    "JOIN carrides ON damage_car_ride_id = car_ride_car_reservation_id " +
                    "JOIN carreservations ON damage_car_ride_id = reservation_id " +
                    "JOIN cars ON reservation_car_id = car_id " +
                    "JOIN users ON reservation_user_id = user_id " + FILTER_FRAGMENT
    );

    @Override
    public int getAmountOfDamages(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfDamagesStatement.value();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("amount_of_damages");
                else
                    return 0;

            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of damages", ex);
        }
    }

    private LazyStatement getDamageListPageStatement = new LazyStatement(
            DAMAGE_QUERY + FILTER_FRAGMENT + " ORDER BY damage_id desc LIMIT ?, ?"
    );

    @Override
    public List<Damage> getDamages(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        // TODO: orderBy not yet honoured
        try {
            PreparedStatement ps = getDamageListPageStatement.value();

            fillFragment(ps, filter, 1);
            int first = (page - 1) * pageSize;
            ps.setInt(6, first);
            ps.setInt(7, pageSize);
            List<Damage> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(populateDamage(rs));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of damages", ex);
        }
    }

    private LazyStatement deleteDamageStatement = new LazyStatement(
            "DELETE FROM damages WHERE damage_id = ?"
    );

    @Override
    public void deleteDamage(int damageId) {
        try {
            PreparedStatement ps = deleteDamageStatement.value();
            ps.setInt(1, damageId);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting damage.");
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete damage.", e);
        }
    }

    private LazyStatement getAmountOfOpenDamagesStatement = new LazyStatement(
            "SELECT COUNT(*) AS amount_of_damages FROM damages " +
                    "JOIN carreservations ON damage_car_ride_id = reservation_id WHERE damage_finished = 0 AND reservation_user_id = ?"
    );

    @Override
    public int getAmountOfOpenDamages(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfOpenDamagesStatement.value();
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("amount_of_damages");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of open damages", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of open damages", ex);
        }
    }

}
