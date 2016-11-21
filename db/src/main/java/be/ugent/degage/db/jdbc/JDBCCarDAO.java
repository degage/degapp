/* JDBCCarDAO.java
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
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.models.*;

import java.sql.*;

/**
 * @author Laurent
 */
class JDBCCarDAO extends AbstractDAO implements CarDAO {

    // TODO: remove lazy statements

    public JDBCCarDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static CarHeaderShort populateCarHeaderShort(ResultSet rs) throws SQLException {
        return new CarHeaderShort(
                rs.getInt("car_id"),
                rs.getString("car_name"),
                rs.getInt("car_owner_user_id")
        );
    }

    private static Car populateCar(ResultSet rs) throws SQLException {
        Car car = new Car(
                rs.getInt("car_id"), rs.getString("car_name"),
                rs.getString("car_email"), rs.getString("car_brand"), rs.getString("car_type"),
                (Integer) rs.getObject("car_seats"), (Integer) rs.getObject("car_doors"),
                (Integer) rs.getObject("car_year"),
                rs.getBoolean("car_manual"), rs.getBoolean("car_gps"), rs.getBoolean("car_hook"),
                CarFuel.valueOf(rs.getString("car_fuel")),
                (Integer) rs.getObject("car_fuel_economy"),
                (Integer) rs.getObject("car_estimated_value"),
                (Integer) rs.getObject("car_owner_annual_km"),
                rs.getString("car_comments"),
                rs.getBoolean("car_active"),
                rs.getDate("car_created_at") == null ? null : rs.getDate("car_created_at").toLocalDate(),
                JDBCUserDAO.populateUserHeader(rs)
        );

        car.setLocation(JDBCAddressDAO.populateAddress(rs));
        car.setTechnicalCarDetails(
                new TechnicalCarDetails(
                        rs.getString("details_car_license_plate"),
                        rs.getInt("details_car_registration"),
                        rs.getString("details_car_chassis_number")
                ));
        Date insuranceExpiration = rs.getDate("insurance_expiration");
        car.setInsurance(
                new CarInsurance(
                        rs.getString("insurance_name"),
                        insuranceExpiration == null ? null : insuranceExpiration.toLocalDate(),
                        rs.getString("insurance_bonus_malus"),
                        rs.getString("insurance_contract_id")
                ));
        Date assistanceExpiration = rs.getDate("assistance_expiration");
        car.setAssistance(
                new CarAssistance(
                        rs.getString("assistance_name"),
                        assistanceExpiration == null ? null : assistanceExpiration.toLocalDate(),
                        CarAssistanceType.valueOf(rs.getString("assistance_type")),
                        rs.getString("assistance_contract_id")
                ));

        car.setFuel(CarFuel.valueOf(rs.getString("car_fuel")));

        return car;
    }

    @Override
    public CarHeaderShort getCarHeaderShort(int carId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT car_id, car_name, car_owner_user_id FROM cars WHERE car_id = ?"
        )) {
            ps.setInt(1, carId);
            return toSingleObject(ps, JDBCCarDAO::populateCarHeaderShort);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve name of car", ex);
        }
    }

    public static CarHeader populateCarHeader(ResultSet rs) throws SQLException {
        return new CarHeader(
                rs.getInt("car_id"),
                rs.getString("car_name"),
                rs.getString("car_brand"),
                rs.getString("car_type"),
                rs.getString("car_email"),
                rs.getBoolean("car_active"),
                rs.getInt("car_owner_user_id"),
                rs.getInt("car_year")
        );
    }

    private static CarHeaderLong populateCarHeaderLong(ResultSet rs) throws SQLException {
        CarHeaderLong result = new CarHeaderLong(
                rs.getInt("car_id"),
                rs.getString("car_name"),
                rs.getString("car_brand"),
                rs.getString("car_type"),
                rs.getString("car_email"),
                true,
                (Integer) rs.getObject("car_seats"),
                (Integer) rs.getObject("car_doors"),
                rs.getBoolean("car_manual"),
                rs.getBoolean("car_gps"),
                rs.getBoolean("car_hook"),
                CarFuel.valueOf(rs.getString("car_fuel")),
                rs.getString("car_comments"),
                rs.getInt("car_year")
        );
        result.setLocation(JDBCAddressDAO.populateAddress(rs));
        return result;
    }

    private static CarHeaderLong populateCarHeaderLongAndOwner(ResultSet rs) throws SQLException {
        CarHeaderLong result = populateCarHeaderLong(rs);
        result.setOwner(JDBCUserDAO.populateUserHeader(rs));
        return result;
    }

    @Override
    public Car createCar(String name, String email, String brand, String type, Address location, Integer seats, Integer doors, Integer year, boolean manual,
                         boolean gps, boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm,
                         TechnicalCarDetails technicalCarDetails, CarInsurance insurance, CarAssistance assistance, UserHeader owner, String comments, boolean active) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO cars(car_name, car_type, car_brand, " +
                        "car_seats, car_doors, car_year, car_manual, car_gps, car_hook, car_fuel, " +
                        "car_fuel_economy, car_estimated_value, car_owner_annual_km, " +
                        "car_owner_user_id, car_comments, car_active, car_email) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                "car_id"
        )) {

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, brand);

            ps.setObject(4, seats, Types.INTEGER);
            ps.setObject(5, doors, Types.INTEGER);
            ps.setObject(6, year, Types.INTEGER);

            ps.setBoolean(7, manual);
            ps.setBoolean(8, gps);
            ps.setBoolean(9, hook);
            ps.setString(10, fuel.name());

            ps.setObject(11, fuelEconomy, Types.INTEGER);
            ps.setObject(12, estimatedValue, Types.INTEGER);
            ps.setObject(13, ownerAnnualKm, Types.INTEGER);
            ps.setObject(14, owner.getId(), Types.INTEGER);
            // Owner cannot be null according to SQL script so this will throw an Exception

            ps.setString(15, comments);
            ps.setBoolean(16, active);

            ps.setString(17, email);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);

                // records have been automatically created by db trigger
                updateTechnicalCarDetails(id, technicalCarDetails);
                updateInsurance(id, insurance);
                updateAssistance(id, assistance);
                updateLocation(id, location);

                Car car = new Car(id, name, email, brand, type,
                        seats, doors, year, manual, gps, hook, fuel,
                        fuelEconomy, estimatedValue, ownerAnnualKm,
                        comments, active, owner);
                car.setLocation(location);
                car.setTechnicalCarDetails(technicalCarDetails);
                car.setInsurance(insurance);
                car.setAssistance(assistance);
                return car;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create new car.", ex);
        }
    }

    private LazyStatement updateTechnicalCarDetailsStatement = new LazyStatement(
            "UPDATE technicalcardetails SET details_car_license_plate=?, " +
                    "details_car_registration=?, details_car_chassis_number=? WHERE details_id = ?");


    private void updateTechnicalCarDetails(int id, TechnicalCarDetails technicalCarDetails) throws SQLException {

        PreparedStatement ps = updateTechnicalCarDetailsStatement.value();

        ps.setString(1, technicalCarDetails.getLicensePlate());
        int registrationId = technicalCarDetails.getRegistrationId();
        if (registrationId == 0) {
            ps.setNull(2, Types.INTEGER); // cannot store 0 because of foreign key constraint
        } else {
            ps.setInt(2, registrationId);
        }
        ps.setString(3, technicalCarDetails.getChassisNumber());
        ps.setInt(4, id);

        if (ps.executeUpdate() == 0) {
            throw new DataAccessException("No rows were affected when updating technicalCarDetails.");
        }
    }

    private LazyStatement updateInsuranceStatement = new LazyStatement(
            "UPDATE carinsurances SET insurance_name=?, insurance_expiration=?, " +
                    "insurance_contract_id=?, insurance_bonus_malus=? WHERE insurance_id = ?"
    );

    private void updateInsurance(int id, CarInsurance insurance) throws SQLException {
        PreparedStatement ps = updateInsuranceStatement.value();
        ps.setString(1, insurance.getName());
        if (insurance.getExpiration() == null) {
            ps.setDate(2, null);
        } else {
            ps.setDate(2, Date.valueOf(insurance.getExpiration()));
        }
        ps.setString(3, insurance.getPolisNr());
        ps.setString(4, insurance.getBonusMalus());
        ps.setInt(5, id);

        if (ps.executeUpdate() == 0) {
            throw new DataAccessException("No rows were affected when updating carInsurance.");
        }
    }

    private LazyStatement updateAssistanceStatement = new LazyStatement(
            "UPDATE carassistances SET assistance_name=?, assistance_expiration=?, " +
                    "assistance_contract_id=?, assistance_type=? WHERE assistance_id = ?"
    );

    private void updateAssistance(int id, CarAssistance assistance) throws SQLException {
        PreparedStatement ps = updateAssistanceStatement.value();
        ps.setString(1, assistance.getName());
        if (assistance.getExpiration() == null) {
            ps.setDate(2, null);
        } else {
            ps.setDate(2, Date.valueOf(assistance.getExpiration()));
        }
        ps.setString(3, assistance.getContractNr());
        ps.setString(4, assistance.getType().name());
        ps.setInt(5, id);

        if (ps.executeUpdate() == 0) {
            throw new DataAccessException("No rows were affected when updating carInsurance.");
        }
    }

    private void updateLocation(int carId, Address location) {
        JDBCAddressDAO.updateLocation(
                getConnection(),
                "JOIN cars ON car_location=address_id", "car_id",
                carId, location
        );
    }

    @Override
    public void updateCar(Car car) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE cars SET car_name=?, car_type=? , car_brand=? ,  " +
                        "car_seats=? , car_doors=? , car_year=? , car_manual=?, car_gps=? , car_hook=? , car_fuel=? , " +
                        "car_fuel_economy=? , car_estimated_value=? , car_owner_annual_km=? , " +
                        "car_owner_user_id=? , car_comments=?, car_active=?, car_email=? WHERE car_id = ?"

        )) {
            ps.setString(1, car.getName());
            ps.setString(2, car.getType());
            ps.setString(3, car.getBrand());

            ps.setObject(4, car.getSeats(), Types.INTEGER);
            ps.setObject(5, car.getDoors(), Types.INTEGER);
            ps.setObject(6, car.getYear(), Types.INTEGER);

            ps.setBoolean(7, car.isManual());
            ps.setBoolean(8, car.isGps());
            ps.setBoolean(9, car.isHook());
            ps.setString(10, car.getFuel().name());

            ps.setObject(11, car.getFuelEconomy(), Types.INTEGER);
            ps.setObject(12, car.getEstimatedValue(), Types.INTEGER);
            ps.setObject(13, car.getOwnerAnnualKm(), Types.INTEGER);

            if (car.getOwner() != null) {
                ps.setInt(14, car.getOwner().getId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }
            ps.setString(15, car.getComments());
            ps.setBoolean(16, car.isActive());

            ps.setString(17, car.getEmail());

            int carId = car.getId();

            ps.setInt(18, carId);

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("No rows were affected when updating car.");
            }

            updateTechnicalCarDetails(carId, car.getTechnicalCarDetails());
            updateInsurance(carId, car.getInsurance());
            updateAssistance(carId, car.getAssistance());
            updateLocation(carId, car.getLocation());

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update car.", ex);
        }
    }

    @Override
    public Car getCar(int id) throws DataAccessException {
        try (
            PreparedStatement ps = prepareStatement(
                "SELECT * FROM cars " +
                    "LEFT JOIN addresses ON addresses.address_id=cars.car_location " +
                    "LEFT JOIN users ON users.user_id=cars.car_owner_user_id " +
                    "LEFT JOIN technicalcardetails ON technicalcardetails.details_id = cars.car_id " +
                    "LEFT JOIN carinsurances ON carinsurances.insurance_id = cars.car_id " +
                    "LEFT JOIN carassistances ON carassistances.assistance_id = cars.car_id " +
                    "WHERE car_id=?"
            )) {
            ps.setInt(1, id);
            return toSingleObject(ps, JDBCCarDAO::populateCar);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch car by id.", ex);
        }
    }

    @Override
    public CarHeaderLong getCarHeaderLong(int id) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
           "SELECT * FROM cars " +
                    "LEFT JOIN addresses ON addresses.address_id=cars.car_location " +
                    "LEFT JOIN users ON users.user_id=cars.car_owner_user_id " +
                    "WHERE car_id=?"
        )) {
            ps.setInt(1, id);
            return toSingleObject(ps, JDBCCarDAO::populateCarHeaderLongAndOwner);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch car by id.", ex);
        }
    };

    private static void appendCarFilter(StringBuilder builder, Filter filter) {

        FilterUtils.appendIdFilter(builder, "car_id", filter.getValue(FilterField.CAR_ID));

        // filter on car_seats
        String carSeats = filter.getValue(FilterField.SEATS);
        if (!carSeats.isEmpty()) {
            Integer.parseInt(carSeats); // check that this is an integer - avoid SQL injection
            builder.append(" AND car_seats >= ").append(carSeats);
        }

        // filter on car_fuel
        String carFuel = filter.getValue(FilterField.FUEL);
        if (!carFuel.isEmpty() && !carFuel.equals("ALL")) {
            CarFuel.valueOf(carFuel);  // protects against SQL injection
            builder.append(" AND car_fuel = '").append(carFuel).append('\'');
        }

        FilterUtils.appendWhenOneFilter(builder, "car_gps", filter.getValue(FilterField.GPS));
        FilterUtils.appendWhenOneFilter(builder, "car_hook", filter.getValue(FilterField.HOOK));
        FilterUtils.appendNotWhenOneFilter(builder, "car_manual", filter.getValue(FilterField.AUTOMATIC));

    }

    private static final String NEW_CAR_QUERY =
            "SELECT SQL_CALC_FOUND_ROWS cars.car_id, car_name, car_email, car_type, car_brand, car_seats, car_doors, " +
                    "car_manual, car_gps, car_hook, car_active, car_fuel, car_comments, car_owner_user_id, car_year, " +
                    JDBCAddressDAO.ADDRESS_FIELDS +
                    "FROM cars JOIN addresses ON address_id=car_location " +
                    "LEFT JOIN carpreferences ON cars.car_id = carpreferences.car_id AND user_id = ? ";

    private static final String SELECT_NOT_OVERLAP =
            " AND cars.car_id NOT IN (" +
                    "SELECT reservation_car_id FROM reservations " +
                    "WHERE reservation_to >= ? AND reservation_from <= ? " +
                    "AND reservation_status > 3 " +  // [ENUM INDEX]
                    ") ";

    /**
     * @param orderBy  The field you want to order by
     * @param asc      Ascending
     * @param page     The page you want to see
     * @param pageSize The page size
     * @param filter   The filter you want to apply
     * @return List of cars with custom ordering and filtering
     */
    @Override
    public Page<CarHeaderLong> listActiveCars(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter, int userId) throws DataAccessException {
        // build query
        StringBuilder builder = new StringBuilder(NEW_CAR_QUERY);
        builder.append(" WHERE car_active ");

        appendCarFilter(builder, filter);

        builder.append(SELECT_NOT_OVERLAP);
        builder.append(" ORDER BY IFNULL(user_id,0) DESC");

        if (orderBy == FilterField.NAME) {
            builder.append(", car_name ");
            builder.append(asc ? "ASC" : "DESC");
        } else if (orderBy == FilterField.BRAND) {
            builder.append(", car_brand ");
            builder.append(asc ? "ASC" : "DESC");
        }

        builder.append(" LIMIT ?, ?");

        //System.err.println("QUERY = " + builder.toString());

        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            ps.setInt(1, userId);
            ps.setString(2, filter.getValue(FilterField.FROM));
            ps.setString(3, filter.getValue(FilterField.UNTIL));   // TODO: use time stamps instead of strings
            ps.setInt(4, (page - 1) * pageSize);
            ps.setInt(5, pageSize);

            return toPage(ps, pageSize, JDBCCarDAO::populateCarHeaderLong);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars", ex);
        }
    }

    @Override
    public Iterable<CarHeaderLong> listAllActiveCars(int userId) {
        try (PreparedStatement ps = prepareStatement(
                NEW_CAR_QUERY + "WHERE car_active ORDER BY IFNULL(USER_ID,0) desc, car_name asc"
        )) {
            ps.setInt(1, userId);
            return toList(ps, JDBCCarDAO::populateCarHeaderLong);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get list of cars", ex);
        }
    }

    /**
     * @param orderBy  The field you want to order by
     * @param asc      Ascending
     * @param page     The page you want to see
     * @param pageSize The page size
     * @param filter   The filter you want to apply
     * @return List of cars with custom ordering and filtering
     */
    @Override
    public Page<CarHeaderAndOwner> listCarsAndOwners(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(
                "SELECT SQL_CALC_FOUND_ROWS " + CAR_HEADER_FIELDS + ", user_firstname, user_lastname " +
                        "FROM cars JOIN users ON car_owner_user_id = user_id "
        );

        // add filters
        StringBuilder filterBuilder = new StringBuilder();
        FilterUtils.appendContainsFilter(filterBuilder, "car_name", filter.getValue(FilterField.NAME));
        FilterUtils.appendContainsFilter(filterBuilder, "car_brand", filter.getValue(FilterField.BRAND));
                if (filterBuilder.length() > 0) {
            builder.append("WHERE ").append(filterBuilder.substring(4));
        }

        // add order
        switch (orderBy) {
            case NAME:
                builder.append(" ORDER BY car_name ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case BRAND:
                builder.append(" ORDER BY car_brand ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case ACTIVE:
                builder.append(" ORDER BY car_active ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case OWNER:
                builder.append(" ORDER BY user_lastname ");
                builder.append(asc ? "ASC" : "DESC");
                builder.append(" , user_firstname ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case YEAR:
                builder.append(" ORDER BY car_year ");
                builder.append(asc ? "ASC" : "DESC");
                break;
        }

        builder.append (" LIMIT ").append (pageSize).append(" OFFSET ").append((page-1)*pageSize);

        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            return toPage(ps, pageSize, rs -> new CarHeaderAndOwner(
                    rs.getInt("car_id"),
                    rs.getString("car_name"),
                    rs.getString("car_brand"),
                    rs.getString("car_type"),
                    rs.getString("car_email"),
                    rs.getBoolean("car_active"),
                    rs.getInt("car_owner_user_id"),
                    rs.getString("user_firstname") + " " + rs.getString("user_lastname"),
                    rs.getInt("car_year")
            ));
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    public static final String CAR_HEADER_FIELDS =
            "car_id, car_name, car_brand, car_type, car_email, car_active, car_owner_user_id, car_year ";


    @Override
    public Iterable<CarHeader> listCarsOfUser(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + CAR_HEADER_FIELDS +
                        "FROM cars WHERE car_owner_user_id = ?")) {
            ps.setInt(1, userId);
            return toList(ps, JDBCCarDAO::populateCarHeader);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars for user with id " + userId, ex);
        }
    }

    @Override
    public boolean isCarOfUser(int carId, int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT 1 FROM cars WHERE car_id = ? AND car_owner_user_id = ?"
        )) {
            ps.setInt(1, carId);
            ps.setInt(2, userId);
            return isNonEmpty(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not determine car ownership");
        }
    }

    @Override
    public UserHeader getOwnerOfCar(int carId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + JDBCUserDAO.USER_HEADER_FIELDS +
                        "FROM cars JOIN users ON user_id=car_owner_user_id " +
                        "WHERE car_id = ?"
        )) {
            ps.setInt(1, carId);
            return toSingleObject(ps, JDBCUserDAO::populateUserHeader);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not determine car ownwer");
        }
    }

    @Override
    public CarDepreciation getDepreciation(int carId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT car_deprec, car_deprec_limit, car_deprec_last FROM cars WHERE car_id = ?"
        )) {
            ps.setInt(1, carId);
            return toSingleObject(ps, rs ->
                    new CarDepreciation(rs.getInt("car_deprec_limit"),
                            rs.getInt("car_deprec"),
                            rs.getInt("car_deprec_last")
                    ));
        } catch (SQLException ex) {
            throw new DataAccessException("Could not determine car depreciation info");
        }
    }

    @Override
    public int getCarPicture(int carId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT car_images_id FROM cars WHERE car_id = ?"
        )) {
            ps.setInt(1, carId);
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get car picture", ex);
        }
    }

    @Override
    public void updateCarPicture(int carId, int fileId) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE cars SET car_images_id = ? WHERE car_id = ?"
        )) {
            ps.setInt(1, fileId);
            ps.setInt(2, carId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update car picture", ex);
        }
    }

    @Override
    public void updateDepreciation(int carId, int cents, int limit, int last) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE cars SET car_deprec = ?, car_deprec_limit = ?, car_deprec_last = ? WHERE car_id = ?"
        )) {
            ps.setInt(1, cents);
            ps.setInt(2, limit);
            ps.setInt(3, last);
            ps.setInt(4, carId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update car deprecation info", ex);
        }

    }

    @Override
    public Iterable<CarHeaderShort> listCarByName(String str, int limit) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT car_id, car_name, car_owner_user_id FROM cars " +
                        "WHERE car_active AND car_name LIKE CONCAT ('%', ?, '%') " +
                        "ORDER BY car_name ASC " +
                        "LIMIT ?"
        )) {
            ps.setString(1, str);
            ps.setInt(2, limit);
            return toList(ps, JDBCCarDAO::populateCarHeaderShort);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}
