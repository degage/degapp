package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.dao.CarCostDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/04/14.
 */
class JDBCCarCostDAO implements CarCostDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"carcost_id"};

            // TODO: replace * by actual fields
    public static final String CAR_COST_QUERY = "SELECT * FROM carcosts " +
            "JOIN cars ON car_cost_car_id = car_id ";

    public static final String FILTER_FRAGMENT = " WHERE car_cost_status LIKE ? AND car_cost_car_id LIKE ?";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
        ps.setString(start, filter.getValue(FilterField.CAR_COST_STATUS));
        String carId = filter.getValue(FilterField.CAR_ID);
        if(carId.equals("")) { // Not very nice programming, but works :D
            carId = "%%";
        }
        ps.setString(start+1, carId);
    }

    private Connection connection;
    private PreparedStatement createCarCostStatement;
    private PreparedStatement updateCarCostStatement;
    private PreparedStatement getCarCostStatement;
    private PreparedStatement getCarCostListPageByDateDescStatement;
    private PreparedStatement getGetAmountOfCarCostsStatement;
    private PreparedStatement endPeriodStatement;
    private PreparedStatement getBillCarCostsStatement;

    public JDBCCarCostDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getEndPeriodStatement() throws SQLException {
        if (endPeriodStatement == null) {
            endPeriodStatement = connection.prepareStatement("UPDATE carcosts SET car_cost_billed = CURDATE() " +
                    "WHERE car_cost_billed = NULL AND car_cost_status = 'ACCEPTED'");
            }
            return endPeriodStatement;
        }

    private PreparedStatement getGetCarCostStatement() throws SQLException {
        // TODO: replace * by actual fields
        if (getCarCostStatement == null) {
            getCarCostStatement = connection.prepareStatement("SELECT * FROM carcosts JOIN cars ON car_cost_car_id = car_id " +
                    "JOIN users ON car_owner_user_id = user_id LEFT JOIN addresses ON user_address_domicile_id = address_id " +
                    "LEFT JOIN technicalcardetails ON car_id = details_id " +
                    "LEFT JOIN files AS pictures ON pictures.file_id = cars.car_images_id " +
                    "WHERE car_cost_id=?");
        }
        return getCarCostStatement;
    }

    private PreparedStatement getGetBillCarCostsStatement() throws SQLException {
        if (getBillCarCostsStatement == null) {
            // TODO: replace * by actual fields
            getBillCarCostsStatement = connection.prepareStatement("SELECT * FROM carcosts JOIN cars ON car_cost_car_id = car_id " +
                    "JOIN users ON car_owner_user_id = user_id LEFT JOIN addresses ON user_address_domicile_id = address_id " +
                    "LEFT JOIN technicalcardetails ON car_id = details_id WHERE car_cost_billed = ? AND car_id = ?");
        }
        return getBillCarCostsStatement;
    }

    private PreparedStatement getCreateCarCostStatement() throws SQLException {
        if (createCarCostStatement == null) {
            createCarCostStatement = connection.prepareStatement("INSERT INTO carcosts (car_cost_car_id, car_cost_amount, " +
                    "car_cost_description, car_cost_time, car_cost_mileage, car_cost_proof) VALUES (?,?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createCarCostStatement;
    }

    private PreparedStatement getGetCarCostListPageByDateDescStatement() throws SQLException {
        if (getCarCostListPageByDateDescStatement == null) {
            getCarCostListPageByDateDescStatement = connection.prepareStatement(CAR_COST_QUERY + FILTER_FRAGMENT +
                    " ORDER BY car_cost_created_at DESC LIMIT ?, ?");
        }
        return getCarCostListPageByDateDescStatement;
    }

    private PreparedStatement getGetAmountOfCarCostsStatement() throws SQLException {
        if(getGetAmountOfCarCostsStatement == null) {
            getGetAmountOfCarCostsStatement = connection.prepareStatement("SELECT count(car_cost_id) AS amount_of_carcosts FROM carcosts " +
                    "JOIN cars ON car_cost_car_id = car_id " + FILTER_FRAGMENT);
        }
        return getGetAmountOfCarCostsStatement;
    }

    private PreparedStatement getUpdateCarCostStatement() throws SQLException {
        if (updateCarCostStatement == null) {
            updateCarCostStatement = connection.prepareStatement("UPDATE carcosts SET car_cost_amount = ? , car_cost_description = ? , car_cost_status = ? , car_cost_time = ? , car_cost_mileage = ?"
                    + " WHERE car_cost_id = ?");
        }
        return updateCarCostStatement;
    }

    public static CarCost populateCarCost(ResultSet rs, Car car) throws SQLException {
        CarCost carCost = new CarCost(rs.getInt("car_cost_id"), car, rs.getBigDecimal("car_cost_amount"), rs.getBigDecimal("car_cost_mileage"), rs.getString("car_cost_description"), new DateTime(rs.getTimestamp("car_cost_time")), rs.getInt("car_cost_proof"));
        carCost.setStatus(CarCostStatus.valueOf(rs.getString("car_cost_status")));
        carCost.setBilled(rs.getDate("car_cost_billed"));
        return carCost;
    }

    @Override
    public CarCost createCarCost(Car car, BigDecimal amount, BigDecimal mileage, String description, DateTime time, int fileId) throws DataAccessException {
        try{
            PreparedStatement ps = getCreateCarCostStatement();
            ps.setInt(1, car.getId());
            ps.setBigDecimal(2, amount);
            ps.setString(3, description);
            ps.setTimestamp(4, new Timestamp(time.getMillis()));
            ps.setBigDecimal(5, mileage);
            ps.setInt(6, fileId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating carcost.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new CarCost(keys.getInt(1), car, amount, mileage, description, time, fileId);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for carcost.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create carcost", e);
        }
    }

    @Override
    public int getAmountOfCarCosts(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfCarCostsStatement();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_carcosts");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of carcosts", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of carcosts", ex);
        }
    }

    @Override
    public List<CarCost> getCarCostList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = null;
            switch(orderBy) { // TODO: more to orderBy, asc/desc
                case CAR_COST_DATE:
                    ps = getGetCarCostListPageByDateDescStatement();
                    break;
            }
            if(ps == null) {
                throw new DataAccessException("Could not create getCarCostList statement");
            }

            fillFragment(ps, filter, 1);
            int first = (page-1)*pageSize;
            ps.setInt(3, first);
            ps.setInt(4, pageSize);
            return getCarCostList(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of carcosts", ex);
        }
    }

    @Override
    public void updateCarCost(CarCost carCost) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateCarCostStatement();
            ps.setBigDecimal(1, carCost.getAmount());
            ps.setString(2, carCost.getDescription());
            ps.setString(3, carCost.getStatus().toString());
            ps.setTimestamp(4, new Timestamp(carCost.getTime().getMillis()));
            ps.setBigDecimal(5, carCost.getMileage());
            ps.setInt(6, carCost.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("CarCost update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update CarCost", e);
        }

    }

    @Override
    public CarCost getCarCost(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetCarCostStatement();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateCarCost(rs, JDBCCarDAO.populateCar(rs, true));
                else return null;
            }catch (SQLException e){
                throw new DataAccessException("Error reading reservation resultset", e);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to get reservation", e);
        }
    }

    private List<CarCost> getCarCostList(PreparedStatement ps) throws DataAccessException {
        List<CarCost> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(populateCarCost(rs, JDBCCarDAO.populateCar(rs, false)));
            }
            return list;
        }catch (SQLException e){
            throw new DataAccessException("Error while reading carcost resultset", e);

        }
    }

    @Override
    public void endPeriod() throws DataAccessException {
        try {
            PreparedStatement ps = getEndPeriodStatement();

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Car Cost update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update car cost", e);
        }
    }

    @Override
    public List<CarCost> getBillCarCosts(Date date, int car) throws DataAccessException {
        List<CarCost> list = new ArrayList<>();
        try {
            PreparedStatement ps = getGetBillCarCostsStatement();
            ps.setDate(1, date);
            ps.setInt(2, car);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(populateCarCost(rs, JDBCCarDAO.populateCar(rs, true)));
            }
            return list;
        } catch (SQLException e){
            throw new DataAccessException("Unable to retrieve the list of car costs", e);
        }
    }
}
