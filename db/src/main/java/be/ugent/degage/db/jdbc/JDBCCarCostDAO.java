/* JDBCCarCostDAO.java
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
import be.ugent.degage.db.dao.CarCostDAO;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.CarCost;
import be.ugent.degage.db.models.CarCostStatus;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 */
class JDBCCarCostDAO extends AbstractDAO implements CarCostDAO {

            // TODO: replace * by actual fields
    public static final String CAR_COST_QUERY = "SELECT * FROM carcosts " +
            "JOIN cars ON car_cost_car_id = car_id ";

    public static final String FILTER_FRAGMENT = " WHERE car_cost_status LIKE ? AND car_cost_car_id LIKE ?";

    public JDBCCarCostDAO(JDBCDataAccessContext context) {
        super(context);
    }

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


    public static CarCost populateCarCost(ResultSet rs, Car car) throws SQLException {
        Date carCostTime = rs.getDate("car_cost_time");
        CarCost carCost = new CarCost(
                rs.getInt("car_cost_id"),
                car,
                rs.getBigDecimal("car_cost_amount"),
                rs.getBigDecimal("car_cost_mileage"),
                rs.getString("car_cost_description"),
                carCostTime == null ? null : carCostTime.toLocalDate(),
                rs.getInt("car_cost_proof"));
        carCost.setStatus(CarCostStatus.valueOf(rs.getString("car_cost_status")));

        Date carCostBilled = rs.getDate("car_cost_billed");
        carCost.setBilled(carCostBilled == null ? null : carCostBilled.toLocalDate());
        return carCost;
    }

    private LazyStatement createCarCostStatement = new LazyStatement (
            "INSERT INTO carcosts (car_cost_car_id, car_cost_amount, " +
                    "car_cost_description, car_cost_time, car_cost_mileage, car_cost_proof) VALUES (?,?,?,?,?,?)",
            "car_cost_id"
    );

    @Override
    public CarCost createCarCost(Car car, BigDecimal amount, BigDecimal mileage, String description, LocalDate date, int fileId) throws DataAccessException {
        try{
            PreparedStatement ps = createCarCostStatement.value();
            ps.setInt(1, car.getId());
            ps.setBigDecimal(2, amount);
            ps.setString(3, description);
            ps.setDate(4, Date.valueOf(date));
            ps.setBigDecimal(5, mileage);
            ps.setInt(6, fileId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating carcost.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new CarCost(keys.getInt(1), car, amount, mileage, description, date, fileId);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for carcost.", ex);
            }
        } catch (SQLException e){
            throw new DataAccessException("Unable to create carcost", e);
        }
    }

    private LazyStatement getAmountOfCarCostsStatement = new LazyStatement (
            "SELECT count(car_cost_id) AS amount_of_carcosts FROM carcosts " +
                    "JOIN cars ON car_cost_car_id = car_id " + FILTER_FRAGMENT
    );

    @Override
    public int getAmountOfCarCosts(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfCarCostsStatement.value();
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

    private LazyStatement getCarCostListPageByDateDescStatement = new LazyStatement (
            CAR_COST_QUERY + FILTER_FRAGMENT + " ORDER BY car_cost_created_at DESC LIMIT ?, ?"
    );

    @Override
    public List<CarCost> getCarCostList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = null;
            switch(orderBy) { // TODO: more to orderBy, asc/desc
                case CAR_COST_DATE:
                    ps = getCarCostListPageByDateDescStatement.value();
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

    private LazyStatement getUpdateCarCostStatement = new LazyStatement (
            "UPDATE carcosts SET car_cost_amount = ? , car_cost_description = ? , car_cost_status = ? , car_cost_time = ? , car_cost_mileage = ?"
                    + " WHERE car_cost_id = ?"
    );

    @Override
    public void updateCarCost(CarCost carCost) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateCarCostStatement.value();
            ps.setBigDecimal(1, carCost.getAmount());
            ps.setString(2, carCost.getDescription());
            ps.setString(3, carCost.getStatus().name());
            ps.setDate(4, Date.valueOf(carCost.getDate()));
            ps.setBigDecimal(5, carCost.getMileage());
            ps.setInt(6, carCost.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("CarCost update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update CarCost", e);
        }

    }

    private LazyStatement getCarCostStatement = new LazyStatement (
            "SELECT * FROM carcosts JOIN cars ON car_cost_car_id = car_id " +
                    "JOIN users ON car_owner_user_id = user_id LEFT JOIN addresses ON user_address_domicile_id = address_id " +
                    "LEFT JOIN technicalcardetails ON car_id = details_id " +
                    "LEFT JOIN files AS pictures ON pictures.file_id = cars.car_images_id " +
                    "WHERE car_cost_id=?"
    );

    @Override
    public CarCost getCarCost(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getCarCostStatement.value();
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

    private LazyStatement getEndPeriodStatement = new LazyStatement (
            "UPDATE carcosts SET car_cost_billed = CURDATE() " +
                    "WHERE car_cost_billed = NULL AND car_cost_status = 'ACCEPTED'"
    );

    @Override
    public void endPeriod() throws DataAccessException {
        try {
            PreparedStatement ps = getEndPeriodStatement.value();

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Car Cost update affected 0 rows.");
        } catch (SQLException e){
            throw new DataAccessException("Unable to update car cost", e);
        }
    }

    private LazyStatement getBillCarCostsStatement = new LazyStatement (
            "SELECT * FROM carcosts JOIN cars ON car_cost_car_id = car_id " +
                    "JOIN users ON car_owner_user_id = user_id LEFT JOIN addresses ON user_address_domicile_id = address_id " +
                    "LEFT JOIN technicalcardetails ON car_id = details_id WHERE car_cost_billed = ? AND car_id = ?"
    );

    @Override
    public List<CarCost> getBillCarCosts(LocalDate date, int car) throws DataAccessException {
        List<CarCost> list = new ArrayList<>();
        try {
            PreparedStatement ps = getBillCarCostsStatement.value();
            ps.setDate(1, Date.valueOf(date));
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
