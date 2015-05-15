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
import be.ugent.degage.db.models.CarCost;
import be.ugent.degage.db.models.ApprovalStatus;
import be.ugent.degage.db.models.CarCostCategory;

import java.sql.*;
import java.time.LocalDate;

/**
 */
class JDBCCarCostDAO extends AbstractDAO implements CarCostDAO {

    private static final String CAR_COST_FIELDS =
            "car_cost_id, car_cost_car_id,	car_cost_proof,	car_cost_amount, car_cost_description, " +
                    "car_cost_status, car_cost_time, car_cost_mileage, car_cost_billed, car_name, car_owner_user_id, " +
                    "category_id, category_description, car_cost_spread, car_cost_comment ";

    public static final String CAR_COST_QUERY =
            "SELECT " + CAR_COST_FIELDS + " FROM carcosts " +
                    "JOIN carcostcategories ON car_cost_category_id = category_id " +
                    "JOIN cars ON car_cost_car_id = car_id ";

    public JDBCCarCostDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static CarCostCategory populateCategory(ResultSet rs) throws SQLException {
        return new CarCostCategory(rs.getInt("category_id"), rs.getString("category_description"));
    }

    public static CarCost populateCarCost(ResultSet rs) throws SQLException {
        Date carCostTime = rs.getDate("car_cost_time");
        CarCost carCost = new CarCost(
                rs.getInt("car_cost_id"),
                rs.getInt("car_cost_amount"),
                rs.getInt("car_cost_mileage"),
                rs.getString("car_cost_description"),
                carCostTime == null ? null : carCostTime.toLocalDate(),
                rs.getInt("car_cost_proof"),
                rs.getInt("car_cost_car_id"),
                rs.getString("car_name"),
                rs.getInt("car_owner_user_id"),
                populateCategory(rs),
                rs.getInt("car_cost_spread"),
                rs.getString("car_cost_comment")
        );
        carCost.setStatus(ApprovalStatus.valueOf(rs.getString("car_cost_status")));

        Date carCostBilled = rs.getDate("car_cost_billed");
        carCost.setBilled(carCostBilled == null ? null : carCostBilled.toLocalDate());
        return carCost;
    }

    @Override
    public void createCarCost(int carId, String carName, int amount, int km,
                              String description, LocalDate date,
                              ApprovalStatus status, int spread,
                              int fileId, int categoryId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO carcosts (car_cost_car_id, car_cost_amount, " +
                        "car_cost_description, car_cost_time, car_cost_mileage, car_cost_proof, car_cost_category_id, " +
                        "car_cost_status, car_cost_spread) VALUES (?,?,?,?,?,?,?,?,?)"
        )) {
            ps.setInt(1, carId);
            ps.setInt(2, amount);
            ps.setString(3, description);
            ps.setDate(4, Date.valueOf(date));
            ps.setInt(5, km);
            if (fileId == 0) {
                ps.setNull(6, Types.INTEGER); // 0 not allowed because of foreign key constraint
            } else {
                ps.setInt(6, fileId);
            }
            ps.setInt(7, categoryId);
            ps.setString(8, status.name());
            ps.setInt(9, spread);
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("No rows were affected when creating carcost.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create carcost", e);
        }
    }

    private static void appendCostFilter(StringBuilder builder, Filter filter) {
        StringBuilder b = new StringBuilder();
        FilterUtils.appendIdFilter(b, "car_cost_car_id", filter.getValue(FilterField.CAR_ID));
        FilterUtils.appendStringFilter(b, "car_cost_status", filter.getValue(FilterField.STATUS));
        if (b.length() > 0) {
            builder.append(" WHERE ").append(b.substring(4));
        }
    }

    @Override
    public int getAmountOfCarCosts(Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder("SELECT count(*) AS amount_of_carcosts FROM carcosts ");
        appendCostFilter(builder, filter);
        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of carcosts", ex);
        }
    }

    @Override
    public Iterable<CarCost> getCarCostList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(CAR_COST_QUERY);
        appendCostFilter(builder, filter);
        builder.append(" ORDER BY car_cost_created_at DESC LIMIT ?, ?");
        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            ps.setInt(1, (page - 1) * pageSize);
            ps.setInt(2, pageSize);
            return toList(ps, JDBCCarCostDAO::populateCarCost);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of carcosts", ex);
        }
    }

    @Override
    public Iterable<CarCost> listCostsOfCar(int carId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                CAR_COST_QUERY +
                        " WHERE car_cost_car_id = ?  ORDER BY car_cost_created_at"
        )) {
            ps.setInt(1, carId);
            return toList(ps, JDBCCarCostDAO::populateCarCost);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of carcosts", ex);
        }
    }

    private LazyStatement getUpdateCarCostStatement = new LazyStatement(
            "UPDATE carcosts SET car_cost_amount = ? , car_cost_description = ? , car_cost_status = ? , car_cost_time = ? , car_cost_mileage = ?"
                    + " WHERE car_cost_id = ?"
    );

    @Override
    public void updateCarCost(CarCost carCost) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateCarCostStatement.value();
            ps.setInt(1, carCost.getAmount());
            ps.setString(2, carCost.getDescription());
            ps.setString(3, carCost.getStatus().name());
            ps.setDate(4, Date.valueOf(carCost.getDate()));
            ps.setInt(5, carCost.getKm());
            ps.setInt(6, carCost.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update CarCost", e);
        }

    }

    @Override
    public CarCost getCarCost(int id) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                CAR_COST_QUERY + " WHERE car_cost_id = ?"
        )) {
            ps.setInt(1, id);
            return toSingleObject(ps, JDBCCarCostDAO::populateCarCost);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get car cost", e);
        }
    }

    /*
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
    */


    @Override
    public CarCostCategory getCategory(int id) {
        // TODO: cache this
        try (PreparedStatement ps = prepareStatement(
                "SELECT category_id, category_description FROM carcostcategories WHERE category_id = ?"
        )) {
            ps.setInt(1, id);
            return toSingleObject(ps, JDBCCarCostDAO::populateCategory);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not list cost categories", ex);
        }
    }

    @Override
    public Iterable<CarCostCategory> listCategories() {
        // TODO: cache this
        try (PreparedStatement ps = prepareStatement(
                "SELECT category_id, category_description FROM carcostcategories ORDER BY category_id "
        )) {
            return toList(ps, JDBCCarCostDAO::populateCategory);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not list cost categories", ex);
        }
    }

    public int getNextCostId(int costId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT r.car_cost_id FROM carcosts AS r JOIN carcosts AS o " +
                        "USING(car_cost_car_id ) " +
                        "WHERE o.car_cost_id = ? AND r.car_cost_id > o.car_cost_id  " +
                        "ORDER BY r.car_cost_id ASC LIMIT 1"
        )) {
            ps.setInt(1, costId);
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Error while retrieving the next cost", ex);
        }
    }


    public int getPreviousCostId(int costId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT r.car_cost_id FROM carcosts AS r JOIN carcosts AS o " +
                        "USING(car_cost_car_id) " +
                        "WHERE o.car_cost_id = ? AND r.car_cost_id < o.car_cost_id  " +
                        "ORDER BY r.car_cost_id DESC LIMIT 1"
        )) {
            ps.setInt(1, costId);
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Error while retrieving the previous cost", ex);
        }

    }

    @Override
    public void approveCost(int costId, int spread) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE carcosts SET car_cost_status = 'ACCEPTED', car_cost_spread = ? " +
                        "WHERE car_cost_id = ?"
        )) {
            ps.setInt(1, spread);
            ps.setInt(2, costId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error while updating cost status ", ex);
        }
    }

    @Override
    public void rejectCost(int costId, String message) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE carcosts SET car_cost_status = 'REFUSED', car_cost_comment = ? " +
                        "WHERE car_cost_id = ?"
        )) {
            ps.setString(1, message);
            ps.setInt(2, costId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error while updating cost status ", ex);
        }
    }
}
