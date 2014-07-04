package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.dao.DamageDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.CarRide;
import be.ugent.degage.db.models.Damage;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 02/05/14.
 */
class JDBCDamageDAO implements DamageDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"damage_id"};
    private Connection connection;
    private PreparedStatement createDamageStatement;
    private PreparedStatement deleteDamageStatement;
    private PreparedStatement getDamageStatement;
    private PreparedStatement updateDamageStatement;
    private PreparedStatement getGetAmountOfDamagesStatement;
    private PreparedStatement getGetAmountOfOpenDamagesStatement;
    private PreparedStatement getGetDamageListPageStatement;

    private static final String DAMAGE_QUERY = "SELECT * FROM damages " +
            "JOIN carrides ON damage_car_ride_id = car_ride_car_reservation_id " +
            "JOIN carreservations ON damage_car_ride_id = reservation_id " +
            "LEFT JOIN filegroups ON damage_filegroup_id = file_group_id " +
            "JOIN cars ON reservation_car_id = car_id " +
            "JOIN users ON reservation_user_id = user_id ";

    public static final String FILTER_FRAGMENT = " WHERE (damage_finished = ? OR damage_finished LIKE ?) AND reservation_user_id LIKE ? " +
            "AND car_id LIKE ? AND car_owner_user_id LIKE ? ";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }

        String finished = filter.getValue(FilterField.DAMAGE_FINISHED);

        ps.setString(start, finished);

        String s = ""; // This will match nothing
        if(finished.equals("-1") || finished.equals("")) { // Not very nice programming, but works :D
            s = "%%"; // This will match everything
        }
        ps.setString(start+1, s);

        String userId = filter.getValue(FilterField.DAMAGE_USER_ID);
        if(userId.equals("")) { // Not very nice programming, but works :D
            userId = "%%";
        }
        ps.setString(start+2, userId);

        String carId = filter.getValue(FilterField.DAMAGE_CAR_ID);
        if(carId.equals("")) { // Not very nice programming, but works :D
            carId = "%%";
        }
        ps.setString(start+3, carId);

        String ownerId = filter.getValue(FilterField.DAMAGE_OWNER_ID);
        if(ownerId.equals("")) { // Not very nice programming, but works :D
            ownerId = "%%";
        }
        ps.setString(start+4, ownerId);
    }

    public JDBCDamageDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getGetDamageStatement() throws SQLException {
        if (getDamageStatement == null) {
            getDamageStatement = connection.prepareStatement(DAMAGE_QUERY +
                    "WHERE damage_id = ?");
        }
        return getDamageStatement;
    }

    private PreparedStatement getCreateDamageStatement() throws SQLException {
        if (createDamageStatement == null) {
            createDamageStatement = connection.prepareStatement("INSERT INTO damages " +
                    "(damage_car_ride_id, damage_time) VALUES(?, ?)", AUTO_GENERATED_KEYS);
        }
        return createDamageStatement;
    }

    private PreparedStatement getDeleteDamageStatement() throws SQLException {
        if (deleteDamageStatement == null) {
            deleteDamageStatement = connection.prepareStatement("DELETE FROM damages WHERE damage_id = ?");
        }
        return deleteDamageStatement;
    }

    private PreparedStatement getGetAmountOfDamagesStatement() throws SQLException {
        if(getGetAmountOfDamagesStatement == null) {
            getGetAmountOfDamagesStatement = connection.prepareStatement("SELECT count(damage_id) as amount_of_damages FROM Damages " +
                    "JOIN CarRides ON damage_car_ride_id = car_ride_car_reservation_id " +
                    "JOIN CarReservations ON damage_car_ride_id = reservation_id " +
                    "LEFT JOIN FileGroups ON damage_filegroup_id = file_group_id " +
                    "JOIN Cars ON reservation_car_id = car_id " +
                    "JOIN Users ON reservation_user_id = user_id " + FILTER_FRAGMENT);
        }
        return getGetAmountOfDamagesStatement;
    }

    private PreparedStatement getGetAmountOfOpenDamagesStatement() throws SQLException {
        if(getGetAmountOfOpenDamagesStatement == null) {
            getGetAmountOfOpenDamagesStatement = connection.prepareStatement("SELECT COUNT(*) AS amount_of_damages FROM damages " +
                    "JOIN carreservations ON damage_car_ride_id = reservation_id WHERE damage_finished = 0 AND reservation_user_id = ?");
        }
        return getGetAmountOfOpenDamagesStatement;
    }

    private PreparedStatement getGetDamageListPageDescStatement() throws SQLException {
        if(getGetDamageListPageStatement == null) {
            getGetDamageListPageStatement = connection.prepareStatement(DAMAGE_QUERY + FILTER_FRAGMENT + " ORDER BY damage_id desc LIMIT ?, ?");
        }
        return getGetDamageListPageStatement;
    }

    private PreparedStatement getUpdateDamageStatement() throws SQLException {
        if (updateDamageStatement == null) {
            updateDamageStatement = connection.prepareStatement("UPDATE damages SET damage_car_ride_id = ? ," +
                    "damage_description = ? , damage_finished = ? , damage_time = ?, damage_filegroup_id = ? "
                    + "WHERE damage_id = ?");
        }
        return updateDamageStatement;
    }

    public static Damage populateDamage(ResultSet rs) throws SQLException {
        return new Damage(rs.getInt("damage_id"), JDBCCarRideDAO.populateCarRide(rs), rs.getInt("damage_filegroup_id"),
                rs.getString("damage_description"), new DateTime(rs.getTimestamp("damage_time")),
                rs.getBoolean("damage_finished"));
    }

    @Override
    public Damage createDamage(CarRide carRide) throws DataAccessException {
        try{
            PreparedStatement ps = getCreateDamageStatement();
            ps.setInt(1, carRide.getReservation().getId());
            ps.setTimestamp(2, new Timestamp(carRide.getReservation().getFrom().getMillis()));
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating damage.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Damage(keys.getInt(1), carRide);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for damage.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create damage", e);
        }
    }

    @Override
    public Damage getDamage(int damageId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetDamageStatement();
            ps.setInt(1, damageId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateDamage(rs);
                else return null;
            }catch (SQLException e){
                throw new DataAccessException("Error reading damage resultset", e);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get damage", e);
        }
    }

    @Override
    public void updateDamage(Damage damage) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateDamageStatement();
            ps.setInt(1, damage.getCarRide().getReservation().getId());
            ps.setString(2, damage.getDescription());
            ps.setBoolean(3, damage.getFinished());
            ps.setTimestamp(4, new Timestamp(damage.getTime().getMillis()));
            if(damage.getProofId() != 0){
                ps.setInt(5, damage.getProofId());
            }else{
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, damage.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Damage update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update damage", e);
        }
    }

    @Override
    public int getAmountOfDamages(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfDamagesStatement();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_damages");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of damages", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of damages", ex);
        }
    }

    @Override
    public List<Damage> getDamages(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = null;
            switch(orderBy) {
                default: // TODO: get something to order on + asc/desc
                    ps = getGetDamageListPageDescStatement();
                    break;
            }
            if(ps == null) {
                throw new DataAccessException("Could not create getDamageList statement");
            }

            fillFragment(ps, filter, 1);
            int first = (page-1)*pageSize;
            ps.setInt(6, first);
            ps.setInt(7, pageSize);
            return getDamageList(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of damages", ex);
        }
    }

    @Override
    public void deleteDamage(int damageId) {
        try {
            PreparedStatement ps = getDeleteDamageStatement();
            ps.setInt(1, damageId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting damage.");
        } catch (SQLException e){
            throw new DataAccessException("Could not delete damage.", e);
        }
    }

    @Override
    public int getAmountOfOpenDamages(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfOpenDamagesStatement();
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_damages");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of open damages", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of open damages", ex);
        }
    }

    private List<Damage> getDamageList(PreparedStatement ps) throws DataAccessException {
        List<Damage> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateDamage(rs));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading damage resultset", e);
        }
    }
}
