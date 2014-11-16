package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.*;
import be.ugent.degage.db.dao.RefuelDAO;
import be.ugent.degage.db.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
class JDBCRefuelDAO extends AbstractDAO implements RefuelDAO {

    // TODO: reduce output from this query * -> actual fields
    private static final String REFUEL_QUERY = "SELECT * FROM refuels " +
            "LEFT JOIN carrides ON refuel_car_ride_id = car_ride_car_reservation_id " +
            "LEFT JOIN carreservations ON refuel_car_ride_id = reservation_id " +
            "LEFT JOIN cars ON reservation_car_id = car_id " +
            "LEFT JOIN users ON reservation_user_id = user_id " +
            "LEFT JOIN users owners ON car_owner_user_id = owners.user_id " +
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
        if(userId.equals("")) { // Not very nice programming, but works :D
            userId = "%%";
        }
        ps.setString(start, userId);

        String ownerId = filter.getValue(FilterField.REFUEL_OWNER_ID);
        if(ownerId.equals("")) { // Not very nice programming, but works :D
            ownerId = "%%";
        }
        ps.setString(start+1, ownerId);

        String carId = filter.getValue(FilterField.REFUEL_CAR_ID);
        if(carId.equals("")) { // Not very nice programming, but works :D
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

        refuel.setBilled(rs.getDate("refuel_billed"));

        refuel.getCarRide().getReservation().getCar().setOwner(JDBCUserDAO.populateUserPartial(rs, "owners"));

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
            ps.setString(1, RefuelStatus.ACCEPTED.toString());
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
            ps.setString(1, RefuelStatus.REFUSED.toString());
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
            }catch (SQLException e){
                throw new DataAccessException("Error reading reservation resultset", e);
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
            ps.setString(3, refuel.getStatus().toString());
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
                     "LEFT JOIN carreservations ON refuel_car_ride_id = reservation_id " +
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
                    "FROM refuels JOIN carreservations ON refuel_car_ride_id = reservation_id " +
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
                    "             INNER JOIN carreservations ON carrides.car_ride_car_reservation_id = carreservations.reservation_id " +
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
    public List<Refuel> getBillRefuelsForLoaner(Date date, int user) throws DataAccessException {
        try {
            PreparedStatement ps = getBillRefuelsForLoanerStatement.value();
            ps.setDate(1, date);
            ps.setInt(2, user);
            return getRefuelList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of refuels for user.", e);
        }
    }

    private LazyStatement getBillRefuelsForCarStatement= new LazyStatement (
            REFUEL_QUERY + " WHERE refuel_billed = ? AND reservation_car_id = ?"
    );

    @Override
    public List<Refuel> getBillRefuelsForCar(Date date, int car) throws DataAccessException {
        try {
            PreparedStatement ps = getBillRefuelsForCarStatement.value();
            ps.setDate(1, date);
            ps.setInt(2, car);
            return getRefuelList(ps);
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of refuels for car.", e);
        }
    }
}
