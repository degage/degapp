/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

/**
 *
 * @author Laurent
 */
class JDBCCarDAO extends AbstractDAO implements CarDAO{

    public static final String LIST_CAR_QUERY =
            "SELECT car_id, car_name, car_brand, car_active, " +
            "       user_id, user_firstname, user_lastname, user_phone, user_email, user_status " +
            "FROM cars JOIN users ON car_owner_user_id = user_id " +
            "WHERE car_name LIKE ? AND car_brand LIKE ? ";

/*
    public static final String FILTER_FRAGMENT = " WHERE cars.car_name LIKE ? AND cars.car_id LIKE ? AND cars.car_brand LIKE ? " +
            "AND ( cars.car_manual = ? OR cars.car_manual LIKE ? ) " +
            "AND cars.car_gps >= ? AND cars.car_hook >= ? AND cars.car_seats >= ? AND addresses.address_zipcode LIKE ? AND cars.car_fuel LIKE ? " +
            "AND cars.car_id NOT IN (SELECT DISTINCT(car_id) FROM cars INNER JOIN carreservations " +
            "ON carreservations.reservation_car_id = cars.car_id " +
            "WHERE ? < carreservations.reservation_to AND ? > carreservations.reservation_from) " +
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

        String manual = filter.getValue(FilterField.CAR_MANUAL);
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

    public static Car populateCar(ResultSet rs, boolean withRest) throws SQLException {
        // Extra check if car actually exists
        if(rs.getObject("car_id") != null) {
            Car car = new Car();
            int id = rs.getInt("car_id");
            car.setId(id);
            car.setName(rs.getString("car_name"));
            car.setBrand(rs.getString("car_brand"));
            car.setType(rs.getString("car_type"));
            Integer seats = rs.getInt("car_seats");
            if(!rs.wasNull())
                car.setSeats(seats);
            Integer doors = rs.getInt("car_doors");
            if(!rs.wasNull())
                car.setDoors(doors);
            car.setManual(rs.getBoolean("car_manual"));
            car.setGps(rs.getBoolean("car_gps"));
            car.setHook(rs.getBoolean("car_hook"));
            Integer year = rs.getInt("car_year");
            if(!rs.wasNull())
                car.setYear(year);
            Integer estimatedValue = rs.getInt("car_estimated_value");
            if(!rs.wasNull())
                car.setEstimatedValue(estimatedValue);
            Integer fuelEconomy = rs.getInt("car_fuel_economy");
            if(!rs.wasNull())
                car.setFuelEconomy(fuelEconomy);
            Integer ownerAnnualKm = rs.getInt("car_owner_annual_km");
            if(!rs.wasNull())
                car.setOwnerAnnualKm(ownerAnnualKm);
            car.setComments(rs.getString("car_comments"));
            car.setActive(rs.getBoolean("car_active"));
            File photo = null;
            Address location = null;
            User user = null;
            TechnicalCarDetails technicalCarDetails = null;
            CarInsurance insurance = null;
            if(withRest) {
                photo = JDBCFileDAO.populateFile(rs, "pictures");
                location = JDBCAddressDAO.populateAddress(rs);
                user = JDBCUserDAO.populateUserPartial(rs);

                File registration = null;
                rs.getInt("details_car_registration");
                if (!rs.wasNull()) {
                    registration = JDBCFileDAO.populateFile(rs, "files");
                }

                String chassisNr = rs.getString("details_car_chassis_number");
                if (rs.wasNull()) {
                    chassisNr = null;
                }
                technicalCarDetails = new TechnicalCarDetails(rs.getString("details_car_license_plate"), registration, chassisNr);

                Integer bonusMalus = rs.getInt("insurance_bonus_malus");
                if (rs.wasNull()) {
                    bonusMalus = null;
                }
                Integer contractId = rs.getInt("insurance_contract_id");
                if (rs.wasNull()) {
                    contractId = null;
                }
                insurance = new CarInsurance(rs.getString("insurance_name"), rs.getDate("insurance_expiration"), bonusMalus, contractId);
            }
            car.setPhoto(photo);
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
            "INSERT INTO cars(car_name, car_type, car_brand, car_location, " +
                    "car_seats, car_doors, car_year, car_manual, car_gps, car_hook, car_fuel, " +
                    "car_fuel_economy, car_estimated_value, car_owner_annual_km, " +
                    "car_owner_user_id, car_comments, car_active, car_images_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            "car_id");

    @Override
    public Car createCar(String name, String brand, String type, Address location, Integer seats, Integer doors, Integer year, boolean manual,
                         boolean gps, boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm,
                         TechnicalCarDetails technicalCarDetails, CarInsurance insurance, User owner, String comments, boolean active, File photo) throws DataAccessException {
        try {
            PreparedStatement ps = createCarStatement.value();
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, brand);
            // TODO: make sure location is never null

            if(location != null) {
                ps.setInt(4, location.getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if(seats != null) {
                ps.setInt(5, seats);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if(doors != null) {
                ps.setInt(6, doors);
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            if(year != null) {
                ps.setInt(7, year);
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, manual);
            ps.setBoolean(9, gps);
            ps.setBoolean(10, hook);
            ps.setString(11, fuel.toString());
            if(fuelEconomy != null) {
                ps.setInt(12, fuelEconomy);
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            if(estimatedValue!= null) {
                ps.setInt(13, estimatedValue);
            } else {
                ps.setNull(13, Types.INTEGER);
            }
            if(ownerAnnualKm != null) {
                ps.setInt(14, ownerAnnualKm);
            } else {
                ps.setNull(14, Types.INTEGER);
            }

            // Owner cannot be null according to SQL script so this will throw an Exception
            if(owner != null) {
                ps.setInt(15, owner.getId());
            } else {
                ps.setNull(15, Types.INTEGER);
            }
            ps.setString(16, comments);
            ps.setBoolean(17, active);

            if (photo != null) {
                ps.setInt(18, photo.getId());
            } else {
                ps.setNull(18, Types.INTEGER);
            }

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating car.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);

                // records have been automatically created by db trigger
                updateTechnicalCarDetails(id, technicalCarDetails);
                updateInsurance(id, insurance);

                Car car = new Car(id, name, brand, type, location, seats, doors, year, manual, gps, hook, fuel,
                        fuelEconomy, estimatedValue, ownerAnnualKm, technicalCarDetails, insurance, owner, comments);
                car.setActive(active);
                car.setPhoto(photo);
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
        if (technicalCarDetails.getRegistration() == null) {
            ps.setNull(2, Types.INTEGER);
        } else {
            ps.setInt(2, technicalCarDetails.getRegistration().getId());
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
            ps.setDate (2, null);
        } else {
            ps.setDate(2, new Date(insurance.getExpiration().getTime()));
        }
        if (insurance.getPolisNr() != null) {
            ps.setInt(3, insurance.getPolisNr());
        } else {
            ps.setNull(3, Types.INTEGER);
        }
        if (insurance.getBonusMalus() != null) {
            ps.setInt(4, insurance.getBonusMalus());
        } else {
            ps.setNull(4, Types.INTEGER);
        }
        ps.setInt(5, id);

        if (ps.executeUpdate() == 0) {
            throw new DataAccessException("No rows were affected when updating carInsurance.");
        }
    }

    private LazyStatement updateCarStatement = new LazyStatement(
            "UPDATE cars SET car_name=?, car_type=? , car_brand=? , car_location=? , " +
                    "car_seats=? , car_doors=? , car_year=? , car_manual=?, car_gps=? , car_hook=? , car_fuel=? , " +
                    "car_fuel_economy=? , car_estimated_value=? , car_owner_annual_km=? , " +
                    "car_owner_user_id=? , car_comments=?, car_active=?, car_images_id=? WHERE car_id = ?");

    @Override
    public void updateCar(Car car) throws DataAccessException {
        try {
            PreparedStatement ps = updateCarStatement.value();
            ps.setString(1, car.getName());
            ps.setString(2, car.getType());
            ps.setString(3, car.getBrand());
            if(car.getLocation() != null) {
                ps.setInt(4, car.getLocation().getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if(car.getSeats() != null) {
                ps.setInt(5, car.getSeats());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if(car.getDoors() != null) {
                ps.setInt(6, car.getDoors());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            if(car.getYear() != null) {
                ps.setInt(7, car.getYear());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, car.isManual());
            ps.setBoolean(9, car.isGps());
            ps.setBoolean(10, car.isHook());
            ps.setString(11, car.getFuel().toString());
            if(car.getFuelEconomy() != null) {
                ps.setInt(12, car.getFuelEconomy());
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            if(car.getEstimatedValue()!= null) {
                ps.setInt(13, car.getEstimatedValue());
            } else {
                ps.setNull(13, Types.INTEGER);
            }
            if(car.getOwnerAnnualKm() != null) {
                ps.setInt(14, car.getOwnerAnnualKm());
            } else {
                ps.setNull(14, Types.INTEGER);
            }

            updateTechnicalCarDetails(car.getId(), car.getTechnicalCarDetails());
            updateInsurance(car.getId(), car.getInsurance());

            // If Owner == null, this should throw an error on execution
            if(car.getOwner() != null) {
                ps.setInt(15,car.getOwner().getId());
            } else {
                ps.setNull(15, Types.INTEGER);
            }
            ps.setString(16, car.getComments());

            ps.setBoolean(17, car.isActive());

            if(car.getPhoto() != null && car.getPhoto().getId() != 0){
                ps.setInt(18, car.getPhoto().getId());
            }else{
                ps.setNull(18, Types.INTEGER);
            }

            ps.setInt(19, car.getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when updating car.");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update car.", ex);
        }
    }

    private LazyStatement getCarStatement = new LazyStatement(
            "SELECT * FROM cars " +
            "LEFT JOIN addresses ON addresses.address_id=cars.car_location " +
            "LEFT JOIN users ON users.user_id=cars.car_owner_user_id " +
            "LEFT JOIN technicalcardetails ON technicalcardetails.details_id = cars.car_id " +
            "LEFT JOIN files ON files.file_id = technicalcardetails.details_car_registration " +
            "LEFT JOIN files AS pictures ON pictures.file_id = cars.car_images_id " +
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
                } else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading car resultset", ex);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch car by id.", ex);
        }
    }

    // TODO: split off into separate DAO

    // TODO: replace * by actual fields
    private LazyStatement getPrivilegedStatement = new LazyStatement (
            "SELECT * FROM carprivileges " +
                "INNER JOIN users ON users.user_id = carprivileges.car_privilege_user_id WHERE car_privilege_car_id=?"
    );

    @Override
    public Iterable<User> getPrivileged(int carId) throws DataAccessException {
        try {
            PreparedStatement ps = getPrivilegedStatement.value();
            ps.setInt(1, carId);
            Collection<User> users = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(JDBCUserDAO.populateUserPartial(rs));
                }
                return users;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading privileged resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of privileged", ex);
        }
    }

    private LazyStatement createPrivilegedStatement = new LazyStatement (
            "INSERT INTO carprivileges(car_privilege_user_id, car_privilege_car_id) VALUES (?,?)"
    );

    @Override
    public void addPrivileged(int carId, Iterable<User> users) throws DataAccessException {
        try {
            for(User user : users) {
                PreparedStatement ps = createPrivilegedStatement.value();
                ps.setInt(1, user.getId());
                ps.setInt(2, carId);

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when creating privileged.");
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create new privileged");
        }
    }

    private LazyStatement deletePrivilegedStatement = new LazyStatement (
        "DELETE FROM carprivileges WHERE car_privilege_user_id = ? AND car_privilege_car_id=?"
    );

    @Override
    public void deletePrivileged(int carId, Iterable<User> users) throws DataAccessException {
        try {
            for(User user : users) {
                PreparedStatement ps = deletePrivilegedStatement.value();

                ps.setInt(1, user.getId());
                ps.setInt(2, carId);

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when deleting privileged.");

            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to delete privileged");
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
        // filter on car id
        String carId = filter.getValue(FilterField.CAR_ID);
        if(! carId.isEmpty()) {
            Integer.parseInt(carId); // check that this is an integer - avoid SQL injection
            builder.append (" AND car_id = ").append(carId);
        }

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

        // filter on gps
        if (filter.getValue(FilterField.CAR_GPS).equals("1")) {
            builder.append (" AND car_gps ");
        }

        // filter on hook
        if (filter.getValue(FilterField.CAR_HOOK).equals("1")) {
            builder.append (" AND car_hook ");
        }

        // filter on manual
        String manual = filter.getValue(FilterField.CAR_MANUAL);
        if (! manual.isEmpty()) {
            int man = Integer.parseInt(manual);
            if (man > 0) {
                builder.append (" AND car_manual ");
            }
            else if (man == 0) {
                builder.append (" AND NOT car_manual ");
            }
        }

    }

    private String NEW_CAR_QUERY =
            "SELECT car_id, car_name, car_type, car_brand, car_seats, car_doors, " +
                    "car_manual, car_gps, car_hook, car_active, " +
                    "address_id, address_city, address_zipcode, address_street, " +
                    "address_street_number, address_street_bus, address_country " +
            "FROM cars JOIN addresses ON address_id=car_location ";

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

            int first = (page-1)*pageSize;
            ps.setInt(1, first);
            ps.setInt(2, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                Collection<Car> cars = new ArrayList<>();
                while (rs.next()) {

                    Car result = new Car (
                            rs.getInt ("car_id"),
                            rs.getString ("car_name"),
                            rs.getString ("car_brand"),
                            rs.getString ("car_type"),
                            JDBCAddressDAO.populateAddress(rs),
                            rs.getObject("car_seats", Integer.class),
                            rs.getObject("car_doors", Integer.class),
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

        try (Statement stat = createStatement();
             ResultSet rs = stat.executeQuery(builder.toString())) {
            if (rs.next())
                return rs.getInt("amount_of_cars");
            else
                return 0;
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
                    User owner = JDBCUserDAO.populateUserPartial(rs);
                    // only header is really needed
                    Car result = new Car (
                            rs.getInt ("car_id"),
                            rs.getString ("car_name"),
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
            "SELECT car_id, car_name, car_brand, car_type," +
                "address_id, address_city, address_zipcode, address_street, " +
                "address_street_number, address_street_bus, address_country, " +
                "car_seats, car_doors, car_year, car_manual, car_gps, car_hook," +
                "car_fuel, car_fuel_economy, car_estimated_value, car_owner_annual_km," +
                "car_comments, car_active, " +
                "insurance_id, insurance_name, insurance_expiration, " +
                "insurance_contract_id, insurance_bonus_malus, " +
                "details_id, details_car_license_plate, details_car_chassis_number, " +
                "user_id, user_firstname, user_lastname, user_phone, user_email, user_status " +
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
                Car result = new Car(
                        rs.getInt("car_id"),
                        rs.getString("car_name"),
                        rs.getString("car_brand"),
                        rs.getString("car_type"),
                        JDBCAddressDAO.populateAddress(rs),
                        rs.getObject("car_seats", Integer.class),
                        rs.getObject("car_doors", Integer.class),
                        rs.getObject("car_year", Integer.class),
                        rs.getBoolean ("car_manual"),
                        rs.getBoolean ("car_gps"),
                        rs.getBoolean ("car_hook"),
                        CarFuel.valueOf(rs.getString("car_fuel")),
                        rs.getObject("car_fuel_economy", Integer.class),
                        rs.getObject("car_estimated_value", Integer.class),
                        rs.getObject("car_owner_annual_km", Integer.class),
                        new TechnicalCarDetails(
                                rs.getString("details_car_license_plate"),
                                null,
                                rs.getString("details_car_chassis_number")
                        ),
                        new CarInsurance(
                                rs.getString("insurance_name"),
                                rs.getDate("insurance_expiration"),
                                rs.getObject("insurance_bonus_malus", Integer.class),
                                rs.getObject("insurance_contract_id", Integer.class)
                        ),
                        JDBCUserDAO.populateUserPartial(rs),
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
            "SELECT car_id, car_name, car_brand, car_active " +
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
