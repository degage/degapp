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

import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import static be.ugent.degage.db.jdbc.JDBCUserDAO.USER_HEADER_FIELDS;

/**
 *
 * @author Laurent
 */
class JDBCCarDAO extends AbstractDAO implements CarDAO{

    public static final String LIST_CAR_QUERY =
            "SELECT car_id, car_name, car_email, car_brand, car_active, " +
                    USER_HEADER_FIELDS +
            "FROM cars JOIN users ON car_owner_user_id = user_id " +
            "WHERE car_name LIKE ? AND car_brand LIKE ? ";

/*
    public static final String FILTER_FRAGMENT = " WHERE cars.car_name LIKE ? AND cars.car_id LIKE ? AND cars.car_brand LIKE ? " +
            "AND ( cars.car_manual = ? OR cars.car_manual LIKE ? ) " +
            "AND cars.car_gps >= ? AND cars.car_hook >= ? AND cars.car_seats >= ? AND addresses.address_zipcode LIKE ? AND cars.car_fuel LIKE ? " +
            "AND cars.car_id NOT IN (SELECT DISTINCT(car_id) FROM cars INNER JOIN reservations " +
            "ON reservations.reservation_car_id = cars.car_id " +
            "WHERE ? < reservations.reservation_to AND ? > reservations.reservation_from) " +
            "AND ( cars.car_active = ? OR cars.car_active LIKE ? )" +
            // FILTER ON CAR AVAILABILITY
            // We want all cars or car doesn't have any availabilities specified
            "AND (? OR caravailabilities.car_availability_id IS NULL " +
            // Car is always available (e.g. Mondag 0:00 -> Monday 0:00 or Monday 1:00 -> Monday 0:55)
            "OR (caravailabilities.car_availability_begin_day_of_week = caravailabilities.car_availability_end_day_of_week " +
            "AND caravailabilities.car_availability_begin_time >= caravailabilities.car_availability_end_time " +
            "AND TIMEDIFF(caravailabilities.car_availability_begin_time, caravailabilities.car_availability_end_time) <= TIME('0:05')) " +
            // Car is always available (e.g. Monday 0:00 -> Sunday 23:55)
            "OR (caravailabilities.car_availability_begin_day_of_week - 1 = caravailabilities.car_availability_end_day_of_week % 7 " +
            "AND caravailabilities.car_availability_begin_time = TIME('0:00') AND caravailabilities.car_availability_end_time >= TIME('23:55')) " +
            // Car is only available in certain intervals
            "OR (DATE_SUB(?, INTERVAL 1 WEEK) < ? " +
            "AND (NOT(DAYOFWEEK(?) = caravailabilities.car_availability_begin_day_of_week AND TIME(?) < caravailabilities.car_availability_begin_time) " +
            "OR (caravailabilities.car_availability_begin_day_of_week = caravailabilities.car_availability_end_day_of_week AND caravailabilities.car_availability_begin_time > caravailabilities.car_availability_end_time AND TIME(?) BETWEEN TIME(?) AND caravailabilities.car_availability_end_time)) " +
            "AND (NOT(DAYOFWEEK(?) = caravailabilities.car_availability_end_day_of_week AND TIME(?) > caravailabilities.car_availability_end_time) " +
            "OR (caravailabilities.car_availability_begin_day_of_week = caravailabilities.car_availability_end_day_of_week AND caravailabilities.car_availability_begin_time > caravailabilities.car_availability_end_time AND TIME(?) BETWEEN caravailabilities.car_availability_begin_time AND TIME(?))) " +
            "AND DATEDIFF(DATE_ADD(?, INTERVAL (caravailabilities.car_availability_end_day_of_week - DAYOFWEEK(?) + 7) % 7 DAY), DATE_SUB(?, INTERVAL (DAYOFWEEK(?) - caravailabilities.car_availability_begin_day_of_week + 7) % 7 DAY)) " +
            "< IF(caravailabilities.car_availability_begin_day_of_week = caravailabilities.car_availability_end_day_of_week AND caravailabilities.car_availability_end_time < caravailabilities.car_availability_begin_time, 8, 7))) ";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
        ps.setString(start, filter.getValue(FilterField.CAR_NAME));
        String carId = filter.getValue(FilterField.CAR_ID);
        if(carId.equals("")) { // Not very nice programming, but works :D
            carId = "%%";
        }
        ps.setString(start+1, carId);

        ps.setString(start+2, filter.getValue(FilterField.CAR_BRAND));

        String manual = filter.getValue(FilterField.CAR_AUTOMATIC);
        ps.setString(start+3, manual);
        String s = ""; // This will match nothing
        if(manual.equals("-1") || manual.equals("")) { // Not very nice programming, but works :D
            s = "%%"; // This will match everything
        }
        ps.setString(start+4, s);

        ps.setString(start+5, filter.getValue(FilterField.CAR_GPS));
        ps.setString(start+6, filter.getValue(FilterField.CAR_HOOK));
        ps.setString(start+7, filter.getValue(FilterField.CAR_SEATS));
        ps.setString(start+8, filter.getValue(FilterField.ZIPCODE));
        String fuel = filter.getValue(FilterField.CAR_FUEL);
        if(fuel.equals("All") || fuel.equals(""))
            fuel = "%%";
        ps.setString(start+9, fuel);
        ps.setString(start+10, filter.getValue(FilterField.FROM));
        ps.setString(start+11, filter.getValue(FilterField.UNTIL));
        String active = filter.getValue(FilterField.CAR_ACTIVE);
        ps.setString(start+12, active);
        String s2 = ""; // This will match nothing
        if(active.equals("-1") || active.equals("")) { // Not very nice programming, but works :D
            s2 = "%%"; // This will match everything
        }
        ps.setString(start+13, s2);
        if (filter.getValue(FilterField.FROM).equals("")) { // Do we want a list of all cars or only available ones?
            ps.setBoolean(start + 14, true);
        } else {
            ps.setBoolean(start + 14, false);
        }
        ps.setString(start+15, filter.getValue(FilterField.UNTIL));
        ps.setString(start+16, filter.getValue(FilterField.FROM));
        ps.setString(start+17, filter.getValue(FilterField.FROM));
        ps.setString(start+18, filter.getValue(FilterField.FROM));
        ps.setString(start+19, filter.getValue(FilterField.UNTIL));
        ps.setString(start+20, filter.getValue(FilterField.FROM));
        ps.setString(start+21, filter.getValue(FilterField.UNTIL));
        ps.setString(start+22, filter.getValue(FilterField.UNTIL));
        ps.setString(start+23, filter.getValue(FilterField.FROM));
        ps.setString(start+24, filter.getValue(FilterField.UNTIL));
        ps.setString(start+25, filter.getValue(FilterField.UNTIL));
        ps.setString(start+26, filter.getValue(FilterField.UNTIL));
        ps.setString(start+27, filter.getValue(FilterField.FROM));
        ps.setString(start+28, filter.getValue(FilterField.FROM));
    }
*/
    public JDBCCarDAO(JDBCDataAccessContext context) {
        super (context);
    }

    /**
     * Retreive a car object from a result set storing only id and name
     */
    public static Car populateCarMinimal (ResultSet rs) throws SQLException {
        return new Car(
                rs.getInt("car_id"), rs.getString("car_name")
        );
    }

    public static Car populateCar(ResultSet rs, boolean withRest) throws SQLException {
        // Extra check if car actually exists
        if(rs.getObject("car_id") != null) {
            Car car = populateCarMinimal(rs);
            car.setEmail(rs.getString("car_email"));
            car.setBrand(rs.getString("car_brand"));
            car.setType(rs.getString("car_type"));
            car.setSeats((Integer) rs.getObject("car_seats"));
            car.setDoors((Integer) rs.getObject("car_doors"));
            car.setManual(rs.getBoolean("car_manual"));
            car.setGps(rs.getBoolean("car_gps"));
            car.setHook(rs.getBoolean("car_hook"));
            car.setYear((Integer) rs.getObject("car_year"));
            car.setEstimatedValue((Integer) rs.getObject("car_estimated_value"));
            car.setFuelEconomy((Integer) rs.getObject("car_fuel_economy"));
            car.setOwnerAnnualKm((Integer) rs.getObject("car_owner_annual_km"));
            car.setComments(rs.getString("car_comments"));
            car.setActive(rs.getBoolean("car_active"));
            int photoId = 0;
            Address location = null;
            UserHeader user = null;
            TechnicalCarDetails technicalCarDetails = null;
            CarInsurance insurance = null;
            if(withRest) {
                photoId = rs.getInt("car_images_id");
                location = JDBCAddressDAO.populateAddress(rs);
                user = JDBCUserDAO.populateUserHeader(rs);

                technicalCarDetails = new TechnicalCarDetails(
                        rs.getString("details_car_license_plate"),
                        rs.getInt("details_car_registration"),
                        rs.getString("details_car_chassis_number")
                );

                String bonusMalus = rs.getString("insurance_bonus_malus");
                String contractId = rs.getString("insurance_contract_id");

                Date insuranceExpiration = rs.getDate("insurance_expiration");
                insurance = new CarInsurance(
                        rs.getString("insurance_name"),
                        insuranceExpiration == null ? null : insuranceExpiration.toLocalDate(),
                        bonusMalus,
                        contractId);
            }
            car.setPhotoId(photoId);
            car.setLocation(location);
            car.setOwner(user);
            car.setTechnicalCarDetails(technicalCarDetails);
            car.setInsurance(insurance);

            car.setFuel(CarFuel.valueOf(rs.getString("car_fuel")));

            return car;
        } else {
            return null;
        }
    }

    private LazyStatement createCarStatement = new LazyStatement (
            "INSERT INTO cars(car_name, car_type, car_brand, " +
                    "car_seats, car_doors, car_year, car_manual, car_gps, car_hook, car_fuel, " +
                    "car_fuel_economy, car_estimated_value, car_owner_annual_km, " +
                    "car_owner_user_id, car_comments, car_active, car_images_id, car_email) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            "car_id");

    @Override
    public Car createCar(String name, String email, String brand, String type, Address location, Integer seats, Integer doors, Integer year, boolean manual,
                         boolean gps, boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm,
                         TechnicalCarDetails technicalCarDetails, CarInsurance insurance, UserHeader owner, String comments, boolean active, int photoId) throws DataAccessException {
        try {
            PreparedStatement ps = createCarStatement.value();
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

            if (photoId == 0) {
                ps.setNull (17, Types.INTEGER); // 0 not allowed because of foreign key constraint
            } else {
                ps.setInt(17, photoId);
            }
            ps.setString (18, email);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating car.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);

                // records have been automatically created by db trigger
                updateTechnicalCarDetails(id, technicalCarDetails);
                updateInsurance(id, insurance);
                updateLocation (id, location);

                Car car = new Car(id, name, email, brand, type, location, seats, doors, year, manual, gps, hook, fuel,
                        fuelEconomy, estimatedValue, ownerAnnualKm, technicalCarDetails, insurance, owner, comments);
                car.setActive(active);
                car.setPhotoId(photoId);
                return car;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create new car.", ex);
        }
    }

    private LazyStatement updateTechnicalCarDetailsStatement = new LazyStatement (
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

    private LazyStatement updateInsuranceStatement = new LazyStatement (
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

    private LazyStatement updateLocationStatement = new LazyStatement(
            "UPDATE addresses JOIN cars ON car_location=address_id " +
                    "SET address_city = ?, address_zipcode = ?, address_street = ?, address_number = ?, address_country=? " +
                    "WHERE car_id = ?"
    );

    private void updateLocation (int carId, Address location) {
        try {
            PreparedStatement ps = updateLocationStatement.value();
            ps.setString(1, location.getCity());
            ps.setString(2, location.getZip());
            ps.setString(3, location.getStreet());
            ps.setString(4, location.getNum());
            ps.setString(5, location.getCountry());

            ps.setInt(6, carId);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Address update affected 0 rows.");

        } catch(SQLException ex) {
            throw new DataAccessException("Failed to update car location.", ex);
        }
    }

    private LazyStatement updateCarStatement = new LazyStatement(
            "UPDATE cars SET car_name=?, car_type=? , car_brand=? ,  " +
                    "car_seats=? , car_doors=? , car_year=? , car_manual=?, car_gps=? , car_hook=? , car_fuel=? , " +
                    "car_fuel_economy=? , car_estimated_value=? , car_owner_annual_km=? , " +
                    "car_owner_user_id=? , car_comments=?, car_active=?, car_images_id=?, car_email=? WHERE car_id = ?");

    @Override
    public void updateCar(Car car) throws DataAccessException {
        try {
            PreparedStatement ps = updateCarStatement.value();
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

            int photoId = car.getPhotoId();
            if (photoId == 0) {
                ps.setNull (17, Types.INTEGER); // 0 not allowed because of foreign key constraint
            } else {
                ps.setInt(17, photoId);
            }

            ps.setString(18, car.getEmail());

            int carId = car.getId();

            ps.setInt(19, carId);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when updating car.");

            updateTechnicalCarDetails(carId, car.getTechnicalCarDetails());
            updateInsurance(carId, car.getInsurance());
            updateLocation(carId, car.getLocation());

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update car.", ex);
        }
    }

    private LazyStatement getCarStatement = new LazyStatement(
            "SELECT * FROM cars " +
            "LEFT JOIN addresses ON addresses.address_id=cars.car_location " +
            "LEFT JOIN users ON users.user_id=cars.car_owner_user_id " +
            "LEFT JOIN technicalcardetails ON technicalcardetails.details_id = cars.car_id " +
            "LEFT JOIN carinsurances ON carinsurances.insurance_id = cars.car_id " +
            "LEFT JOIN caravailabilities ON caravailabilities.car_availability_car_id = cars.car_id " +
            "WHERE car_id=?");

    @Override
    public Car getCar(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getCarStatement.value();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return populateCar(rs, true);
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch car by id.", ex);
        }
    }


    private LazyStatement countCarsStatement = new LazyStatement (
                    "SELECT COUNT(*) AS count FROM cars WHERE car_name LIKE ? AND car_brand LIKE ?"
    );

    /**
     * @param filter The filter to apply to
     * @return The amount of filtered cars
     * @throws DataAccessException
     */
    @Override
    public int countCars(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = countCarsStatement.value();
            ps.setString (1, filter.getValue(FilterField.CAR_NAME));
            ps.setString(2, filter.getValue(FilterField.CAR_BRAND));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of cars", ex);
        }
    }

    private static void appendCarFilter (StringBuilder builder, Filter filter) {

        FilterUtils.appendIdFilter(builder, "car_id", filter.getValue(FilterField.CAR_ID));

        // filter on car_seats
        String carSeats = filter.getValue(FilterField.CAR_SEATS);
        if (!carSeats.isEmpty()) {
            Integer.parseInt(carSeats); // check that this is an integer - avoid SQL injection
            builder.append (" AND car_seats >= ").append(carSeats);
        }

        // filter on car_fuel
        String carFuel =  filter.getValue(FilterField.CAR_FUEL);
        if (! carFuel.isEmpty() && ! carFuel.equals("ALL")) {
            CarFuel.valueOf(carFuel);  // protects against SQL injection
            builder.append(" AND car_fuel = '").append(carFuel).append('\'');
        }

        FilterUtils.appendWhenOneFilter(builder, "car_gps", filter.getValue(FilterField.CAR_GPS));
        FilterUtils.appendWhenOneFilter(builder, "car_hook", filter.getValue(FilterField.CAR_HOOK));
        FilterUtils.appendNotWhenOneFilter(builder, "car_manual", filter.getValue(FilterField.CAR_AUTOMATIC));



    }

    private static final String NEW_CAR_QUERY =
            "SELECT car_id, car_name, car_email, car_type, car_brand, car_seats, car_doors, " +
                    "car_manual, car_gps, car_hook, car_active, " +
                    "address_id, address_city, address_zipcode, address_street, " +
                    "address_number, address_country " +
            "FROM cars JOIN addresses ON address_id=car_location ";

    private static final String SELECT_NOT_OVERLAP =
            " AND car_id NOT IN (" +
                "SELECT reservation_car_id FROM reservations " +
                    "WHERE reservation_to >= ? AND reservation_from <= ? " +
                    "AND reservation_status > 3 " +  // [ENUM INDEX]
            ") ";

    /**
     * @param orderBy The field you want to order by
     * @param asc Ascending
     * @param page The page you want to see
     * @param pageSize The page size
     * @param filter The filter you want to apply
     * @return List of cars with custom ordering and filtering
     */
    @Override
    public Iterable<Car> getCarList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        // build query
        StringBuilder builder = new StringBuilder(NEW_CAR_QUERY);
        builder.append (" WHERE car_active ");

        appendCarFilter (builder, filter);

        builder.append (SELECT_NOT_OVERLAP);

        if (orderBy == FilterField.CAR_NAME) {
            builder.append (" ORDER BY car_name ");
            builder.append(asc ? "ASC" : "DESC");
        } else if (orderBy == FilterField.CAR_BRAND) {
            builder.append (" ORDER BY car_brand ");
            builder.append (asc ? "ASC" : "DESC");
        }

        builder.append ( " LIMIT ?, ?");

        //System.err.println("QUERY = " + builder.toString());

        try (PreparedStatement ps = prepareStatement(builder.toString())) {

            ps.setString (1, filter.getValue(FilterField.FROM));
            ps.setString (2, filter.getValue(FilterField.UNTIL));   // TODO: use time stamps instead of strings
            int first = (page-1)*pageSize;
            ps.setInt(3, first);
            ps.setInt(4, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                Collection<Car> cars = new ArrayList<>();
                while (rs.next()) {

                    Car result = new Car (
                            rs.getInt ("car_id"),
                            rs.getString ("car_name"),
                            rs.getString("car_email"),
                            rs.getString ("car_brand"),
                            rs.getString ("car_type"),
                            JDBCAddressDAO.populateAddress(rs),
                            (Integer)rs.getObject("car_seats"),
                            (Integer)rs.getObject("car_doors"),
                            null,
                            rs.getBoolean("car_manual"),
                            rs.getBoolean("car_gps"),
                            rs.getBoolean("car_hook"),
                            null, null, null, null, null, null,
                            null, null
                    );
                    result.setActive(rs.getBoolean("car_active"));
                    cars.add(result);
                }
                return cars;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars", ex);
        }
    }

    /**
     * @param filter The filter to apply to
     * @return The amount of filtered cars
     * @throws DataAccessException
     */
    @Override
    public int getAmountOfCars(Filter filter) throws DataAccessException {
        // build query
        StringBuilder builder = new StringBuilder(
                "SELECT count(*) AS amount_of_cars FROM cars "
        );
        builder.append(" WHERE car_active ");

        appendCarFilter(builder, filter);

        builder.append (SELECT_NOT_OVERLAP);

        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            ps.setString (1, filter.getValue(FilterField.FROM));
            ps.setString (2, filter.getValue(FilterField.UNTIL));   // TODO: use time stamps instead of strings
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("amount_of_cars");
                else
                    return 0;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of cars", ex);
        }
    }

    private LazyStatement listCarsPageByNameAscStatement = new LazyStatement (LIST_CAR_QUERY +  "ORDER BY car_name asc LIMIT ?, ?");
    private LazyStatement listCarsPageByNameDescStatement = new LazyStatement (LIST_CAR_QUERY + "ORDER BY car_name desc LIMIT ?, ?");
    private LazyStatement listCarsPageByBrandAscStatement = new LazyStatement (LIST_CAR_QUERY + "ORDER BY car_brand asc LIMIT ?, ?");
    private LazyStatement listCarsPageByBrandDescStatement = new LazyStatement (LIST_CAR_QUERY + "ORDER BY car_brand desc LIMIT ?, ?");

    /**
     * @param orderBy The field you want to order by
     * @param asc Ascending
     * @param page The page you want to see
     * @param pageSize The page size
     * @param filter The filter you want to apply
     * @return List of cars with custom ordering and filtering
     */
    @Override
    public Iterable<Car> listCars(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = null;
            switch(orderBy) {
                case CAR_NAME:
                    ps = asc ? listCarsPageByNameAscStatement.value() : listCarsPageByNameDescStatement.value();
                    break;
                case CAR_BRAND:
                    ps = asc ? listCarsPageByBrandAscStatement.value() : listCarsPageByBrandDescStatement.value();
                    break;
            }
            if(ps == null) {
                throw new DataAccessException("Could not create listCars statement");
            }

            ps.setString (1, filter.getValue(FilterField.CAR_NAME));
            ps.setString(2, filter.getValue(FilterField.CAR_BRAND));

            ps.setInt(3, (page-1)*pageSize);
            ps.setInt(4, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                Collection<Car> cars = new ArrayList<>();
                while (rs.next()) {
                    UserHeader owner = JDBCUserDAO.populateUserHeader(rs);
                    // only header is really needed
                    Car result = new Car (
                            rs.getInt ("car_id"),
                            rs.getString ("car_name"),
                            rs.getString ("car_email"),
                            rs.getString ("car_brand"),
                            null, null, null, null, null, false, false, false, null, null, null, null, null, null,
                            owner,
                            null
                    );
                    result.setActive(rs.getBoolean("car_active"));
                    cars.add(result);
                }
                return cars;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars", ex);
        }
    }

    public static final String LIST_ALL_CARS_QUERY =
            "SELECT car_id, car_name, car_email, car_brand, car_type," +
                "address_id, address_city, address_zipcode, address_street, " +
                "address_number, address_country, " +
                "car_seats, car_doors, car_year, car_manual, car_gps, car_hook," +
                "car_fuel, car_fuel_economy, car_estimated_value, car_owner_annual_km," +
                "car_comments, car_active, " +
                "insurance_id, insurance_name, insurance_expiration, " +
                "insurance_contract_id, insurance_bonus_malus, " +
                "details_id, details_car_license_plate, details_car_chassis_number, " +
                    USER_HEADER_FIELDS +
            "FROM cars " +
            "LEFT JOIN addresses ON address_id=car_location " +
            "LEFT JOIN users ON user_id=car_owner_user_id " +
            "LEFT JOIN technicalcardetails ON details_id = car_id " +
            "LEFT JOIN carinsurances ON insurance_id = car_id " +
            "ORDER BY car_name";

    @Override
    public Iterable<Car> listAllCars() throws DataAccessException {
        try (Statement stat = createStatement();
             ResultSet rs = stat.executeQuery(LIST_ALL_CARS_QUERY)) {
            Collection<Car> cars = new ArrayList<>();
            while (rs.next()) {
                Date insuranceExpiration = rs.getDate("insurance_expiration"); // can be null and must be converted
                Car result = new Car(
                        rs.getInt("car_id"),
                        rs.getString("car_name"),
                        rs.getString("car_email"),
                        rs.getString("car_brand"),
                        rs.getString("car_type"),
                        JDBCAddressDAO.populateAddress(rs),
                        (Integer)rs.getObject("car_seats"),
                        (Integer)rs.getObject("car_doors"),
                        (Integer)rs.getObject("car_year"),
                        rs.getBoolean ("car_manual"),
                        rs.getBoolean ("car_gps"),
                        rs.getBoolean ("car_hook"),
                        CarFuel.valueOf(rs.getString("car_fuel")),
                        (Integer)rs.getObject("car_fuel_economy"),
                        (Integer)rs.getObject("car_estimated_value"),
                        (Integer)rs.getObject("car_owner_annual_km"),
                        new TechnicalCarDetails(
                                rs.getString("details_car_license_plate"),
                                0,
                                rs.getString("details_car_chassis_number")
                        ),
                        new CarInsurance(
                                rs.getString("insurance_name"),
                                insuranceExpiration == null ? null : insuranceExpiration.toLocalDate(),
                                rs.getString("insurance_bonus_malus"),
                                rs.getString("insurance_contract_id")
                        ),
                        JDBCUserDAO.populateUserHeader(rs),
                        rs.getString ("car_comments")
                );
                result.setActive(rs.getBoolean("car_active"));
                cars.add(result);
            }
            return cars;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars", ex);
        }
    }


    private LazyStatement listCarsOfUserStatement = new LazyStatement (
            "SELECT car_id, car_name, car_email, car_brand, car_active " +
            "FROM cars WHERE car_owner_user_id = ?");

    @Override
    public Iterable<Car> listCarsOfUser(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = listCarsOfUserStatement.value();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                Collection<Car> cars = new ArrayList<>();
                while (rs.next()) {
                    Car result = new Car (
                            rs.getInt ("car_id"),
                            rs.getString ("car_name"),
                            rs.getString ("car_email"),
                            rs.getString ("car_brand"),
                            null, null, null, null, null, false, false, false, null, null, null, null, null, null,
                            null, null
                    );
                    result.setActive(rs.getBoolean("car_active"));
                    cars.add(result);
                }
                return cars;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars for user with id " + userId, ex);
        }
    }

    private LazyStatement isCarOfUserStatement = new LazyStatement(
            " SELECT 1 FROM cars WHERE car_id = ? AND car_owner_user_id = ?"
    );

    @Override
    public boolean isCarOfUser (int carId, int userId) throws DataAccessException {
        try {
            PreparedStatement ps = isCarOfUserStatement.value();
            ps.setInt (1, carId);
            ps.setInt (2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next ();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not determine car onwerschip");
        }
    }

}
