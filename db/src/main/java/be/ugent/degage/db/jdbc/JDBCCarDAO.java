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
import org.joda.time.LocalTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Laurent
 */
class JDBCCarDAO implements CarDAO{

    private static final String[] AUTO_GENERATED_KEYS = {"car_id"};

            // TODO: replace * by actual fields
    public static final String CAR_QUERY = "SELECT * FROM cars " +
            "LEFT JOIN addresses ON addresses.address_id=cars.car_location " +
            "LEFT JOIN users ON users.user_id=cars.car_owner_user_id " +
            "LEFT JOIN technicalcardetails ON technicalcardetails.details_id = cars.car_id " +
            "LEFT JOIN files ON files.file_id = technicalcardetails.details_car_registration " +
            "LEFT JOIN files AS pictures ON pictures.file_id = cars.car_images_id " +
            "LEFT JOIN carinsurances ON carinsurances.insurance_id = cars.car_id " +
            "LEFT JOIN caravailabilities ON caravailabilities.car_availability_car_id = cars.car_id"; // TODO: multiple records in caravailabilities

    public static final String LIST_CAR_QUERY =
            "SELECT car_id, car_name, car_brand, car_active, " +
            "       user_id, user_firstname, user_lastname, user_phone, user_email, user_status " +
            "FROM cars JOIN users ON car_owner_user_id = user_id " +
            "WHERE car_name LIKE ? AND car_brand LIKE ? ";

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

    private Connection connection;
    private PreparedStatement createCarStatement;
    private PreparedStatement updateCarStatement;
    private PreparedStatement getCarStatement;
    private PreparedStatement getCarsOfUserStatement;
    private PreparedStatement getGetCarListPageByNameAscStatement;
    private PreparedStatement getGetCarListPageByNameDescStatement;
    private PreparedStatement getGetCarListPageByBrandAscStatement;
    private PreparedStatement getGetCarListPageByBrandDescStatement;
    private PreparedStatement getGetAmountOfCarsStatement;
    private PreparedStatement updateTechnicalCarDetailsStatement;
    private PreparedStatement createTechnicalCarDetailsStatement;
    private PreparedStatement updateInsuranceStatement;
    private PreparedStatement createInsuranceStatement;
    private PreparedStatement getAvailabilitiesStatement;
    private PreparedStatement createAvailabilityStatement;
    private PreparedStatement updateAvailabilityStatement;
    private PreparedStatement deleteAvailabilityStatement;
    private PreparedStatement getPrivilegedStatement;
    private PreparedStatement createPrivilegedStatement;
    private PreparedStatement deletePrivilegedStatement;

    public JDBCCarDAO(Connection connection) {
        this.connection = connection;
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

    private PreparedStatement createCarStatement() throws SQLException {
        if (createCarStatement == null) {
            createCarStatement = connection.prepareStatement("INSERT INTO cars(car_name, car_type, car_brand, car_location, " +
                    "car_seats, car_doors, car_year, car_manual, car_gps, car_hook, car_fuel, " +
                    "car_fuel_economy, car_estimated_value, car_owner_annual_km, " +
                    "car_owner_user_id, car_comments, car_active, car_images_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createCarStatement;
    }
    
    private PreparedStatement updateCarStatement() throws SQLException {
        if (updateCarStatement == null) {
            updateCarStatement = connection.prepareStatement("UPDATE cars SET car_name=?, car_type=? , car_brand=? , car_location=? , " +
                    "car_seats=? , car_doors=? , car_year=? , car_manual=?, car_gps=? , car_hook=? , car_fuel=? , " +
                    "car_fuel_economy=? , car_estimated_value=? , car_owner_annual_km=? , " +
                    "car_owner_user_id=? , car_comments=?, car_active=?, car_images_id=? WHERE car_id = ?");
        }
        return updateCarStatement;
    }
    
    private PreparedStatement getCarStatement() throws SQLException {
        if (getCarStatement == null) {
            getCarStatement = connection.prepareStatement(CAR_QUERY + " WHERE car_id=?");
        }
        return getCarStatement;
    }

    private PreparedStatement getGetCarsOfUserStatement() throws SQLException {
        if (getCarsOfUserStatement == null) {
            getCarsOfUserStatement = connection.prepareStatement(CAR_QUERY + " WHERE user_id=?");
        }
        return getCarsOfUserStatement;
    }


    private PreparedStatement getGetCarListPageByNameAscStatement() throws SQLException {
        if(getGetCarListPageByNameAscStatement == null) {
            getGetCarListPageByNameAscStatement = connection.prepareStatement(CAR_QUERY + FILTER_FRAGMENT + "ORDER BY car_name asc LIMIT ?, ?");
        }
        return getGetCarListPageByNameAscStatement;
    }

    private PreparedStatement getGetCarListPageByNameDescStatement() throws SQLException {
        if(getGetCarListPageByNameDescStatement == null) {
            getGetCarListPageByNameDescStatement = connection.prepareStatement(CAR_QUERY + FILTER_FRAGMENT +"ORDER BY car_name desc LIMIT ?, ?");
        }
        return getGetCarListPageByNameDescStatement;
    }
    private PreparedStatement getGetCarListPageByBrandAscStatement() throws SQLException {
        if(getGetCarListPageByBrandAscStatement == null) {
            getGetCarListPageByBrandAscStatement = connection.prepareStatement(CAR_QUERY + FILTER_FRAGMENT + "ORDER BY car_brand asc LIMIT ?, ?");
        }
        return getGetCarListPageByBrandAscStatement;
    }

    private PreparedStatement getGetCarListPageByBrandDescStatement() throws SQLException {
        if(getGetCarListPageByBrandDescStatement == null) {
            getGetCarListPageByBrandDescStatement = connection.prepareStatement(CAR_QUERY + FILTER_FRAGMENT + "ORDER BY car_brand desc LIMIT ?, ?");
        }
        return getGetCarListPageByBrandDescStatement;
    }

    private  PreparedStatement getListCarsPageByBrandAscStatement;
    private  PreparedStatement getListCarsPageByBrandDescStatement;
    private  PreparedStatement getListCarsPageByNameAscStatement;
    private  PreparedStatement getListCarsPageByNameDescStatement;

    private PreparedStatement getListCarsPageByNameAscStatement() throws SQLException {
        if(getListCarsPageByNameAscStatement == null) {
            getListCarsPageByNameAscStatement = connection.prepareStatement(LIST_CAR_QUERY +  "ORDER BY car_name asc LIMIT ?, ?");
        }
        return getListCarsPageByNameAscStatement;
    }

    private PreparedStatement getListCarsPageByNameDescStatement() throws SQLException {
        if(getListCarsPageByNameDescStatement == null) {
            getListCarsPageByNameDescStatement = connection.prepareStatement(LIST_CAR_QUERY + "ORDER BY car_name desc LIMIT ?, ?");
        }
        return getListCarsPageByNameDescStatement;
    }
    private PreparedStatement getListCarsPageByBrandAscStatement() throws SQLException {
        if(getListCarsPageByBrandAscStatement == null) {
            getListCarsPageByBrandAscStatement = connection.prepareStatement(LIST_CAR_QUERY + "ORDER BY car_brand asc LIMIT ?, ?");
        }
        return getListCarsPageByBrandAscStatement;
    }

    private PreparedStatement getListCarsPageByBrandDescStatement() throws SQLException {
        if (getListCarsPageByBrandDescStatement == null) {
            getListCarsPageByBrandDescStatement = connection.prepareStatement(LIST_CAR_QUERY + "ORDER BY car_brand desc LIMIT ?, ?");
        }
        return getListCarsPageByBrandDescStatement;
    }

    private PreparedStatement getGetAmountOfCarsStatement() throws SQLException {
        if (getGetAmountOfCarsStatement == null) {
            // TODO: only join with tables tht can be filtered upon
            getGetAmountOfCarsStatement = connection.prepareStatement("SELECT COUNT(car_id) AS amount_of_cars FROM cars " +
                    "LEFT JOIN addresses ON addresses.address_id=cars.car_location " +
                    "LEFT JOIN users ON users.user_id=cars.car_owner_user_id " +
                    "LEFT JOIN technicalcardetails ON technicalcardetails.details_id = cars.car_id " +
                    "LEFT JOIN carinsurances ON carinsurances.insurance_id = cars.car_id " +
                    "LEFT JOIN caravailabilities ON caravailabilities.car_availability_car_id = cars.car_id" + FILTER_FRAGMENT);
        }
        return getGetAmountOfCarsStatement;
    }

    private PreparedStatement getCountCarsStatement;

    private PreparedStatement getCountCarsStatement() throws SQLException {
        if (getCountCarsStatement == null) {
            getCountCarsStatement = connection.prepareStatement(
                    "SELECT COUNT(*) AS count FROM cars WHERE car_name LIKE ? AND car_brand LIKE ?");
        }
        return getCountCarsStatement;
    }

    private PreparedStatement updateInsuranceStatement() throws SQLException {
        if (updateInsuranceStatement == null) {
            updateInsuranceStatement = connection.prepareStatement("UPDATE carinsurances SET insurance_name=?, insurance_expiration=?, " +
                    "insurance_contract_id=?, insurance_bonus_malus=? WHERE insurance_id = ?");
        }
        return updateInsuranceStatement;
    }

    private PreparedStatement updateTechnicalCarDetailsStatement() throws SQLException {
        if (updateTechnicalCarDetailsStatement == null) {
            updateTechnicalCarDetailsStatement = connection.prepareStatement("UPDATE technicalcardetails SET details_car_license_plate=?, " +
                    "details_car_registration=?, details_car_chassis_number=? WHERE details_id = ?");
        }
        return updateTechnicalCarDetailsStatement;
    }

    private PreparedStatement getAvailabilitiesStatement() throws SQLException {
        if (getAvailabilitiesStatement == null) {
            getAvailabilitiesStatement = connection.prepareStatement("SELECT * FROM caravailabilities WHERE car_availability_car_id=?");
        }
        return getAvailabilitiesStatement;
    }

    private PreparedStatement createAvailabilityStatement() throws SQLException {
        if (createAvailabilityStatement == null) {
            createAvailabilityStatement = connection.prepareStatement("INSERT INTO caravailabilities(car_availability_car_id, " +
                    "car_availability_begin_day_of_week, car_availability_begin_time, car_availability_end_day_of_week, car_availability_end_time) " +
                    "VALUES (?,?,?,?,?)", new String[] {"car_availability_id"});
        }
        return createAvailabilityStatement;
    }

    private PreparedStatement updateAvailabilityStatement() throws SQLException {
        if (updateAvailabilityStatement == null) {
            updateAvailabilityStatement = connection.prepareStatement("UPDATE caravailabilities SET car_availability_car_id=?, " +
                    "car_availability_begin_day_of_week=?, car_availability_begin_time=?, car_availability_end_day_of_week=?, car_availability_end_time=? " +
                    "WHERE car_availability_id = ?");
        }
        return updateAvailabilityStatement;
    }

    private PreparedStatement deleteAvailabilityStatement() throws SQLException {
        if (deleteAvailabilityStatement == null) {
            deleteAvailabilityStatement = connection.prepareStatement("DELETE FROM caravailabilities WHERE car_availability_id = ?");
        }
        return deleteAvailabilityStatement;
    }

    private PreparedStatement getPrivilegedStatement() throws SQLException {
        if (getPrivilegedStatement == null) {
            // TODO: replace * by actual fields
            getPrivilegedStatement = connection.prepareStatement("SELECT * FROM carprivileges " +
                    "INNER JOIN users ON users.user_id = carprivileges.car_privilege_user_id WHERE car_privilege_car_id=?");
        }
        return getPrivilegedStatement;
    }

    private PreparedStatement createPrivilegedStatement() throws SQLException {
        if (createPrivilegedStatement == null) {
            createPrivilegedStatement = connection.prepareStatement("INSERT INTO carprivileges(car_privilege_user_id, " +
                    "car_privilege_car_id) " +
                    "VALUES (?,?)");
        }
        return createPrivilegedStatement;
    }

    private PreparedStatement deletePrivilegedStatement() throws SQLException {
        if (deletePrivilegedStatement == null) {
            deletePrivilegedStatement = connection.prepareStatement("DELETE FROM carprivileges WHERE car_privilege_user_id = ? AND car_privilege_car_id=?");
        }
        return deletePrivilegedStatement;
    }
    
    @Override
    public Car createCar(String name, String brand, String type, Address location, Integer seats, Integer doors, Integer year, boolean manual,
                         boolean gps, boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm,
                         TechnicalCarDetails technicalCarDetails, CarInsurance insurance, User owner, String comments, boolean active, File photo) throws DataAccessException {
        try {
            PreparedStatement ps = createCarStatement();
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, brand);
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

    private void updateTechnicalCarDetails(int id, TechnicalCarDetails technicalCarDetails) throws SQLException {

        PreparedStatement ps = updateTechnicalCarDetailsStatement();

        ps.setString(1, technicalCarDetails.getLicensePlate());
        ps.setInt(2, technicalCarDetails.getRegistration().getId());
        ps.setString(3, technicalCarDetails.getChassisNumber());
        ps.setInt(4, id);

        if (ps.executeUpdate() == 0) {
            throw new DataAccessException("No rows were affected when updating technicalCarDetails.");
        }
    }

    private void updateInsurance(int id, CarInsurance insurance) throws SQLException {
        PreparedStatement ps = updateInsuranceStatement();
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

    @Override
    public void updateCar(Car car) throws DataAccessException {
        try {
            PreparedStatement ps = updateCarStatement();
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

    @Override
    public Car getCar(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getCarStatement();
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

    @Override
    public List<CarAvailabilityInterval> getAvailabilities(int carId) throws DataAccessException {
        try {
            PreparedStatement ps = getAvailabilitiesStatement();
            ps.setInt(1, carId);
            List<CarAvailabilityInterval> availabilities = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time beginTime = rs.getTime("car_availability_begin_time");
                    LocalTime beginLocalTime = LocalTime.fromDateFields(beginTime);

                    Time endTime = rs.getTime("car_availability_end_time");
                    LocalTime endLocalTime = LocalTime.fromDateFields(endTime);

                    availabilities.add(new CarAvailabilityInterval(rs.getInt("car_availability_id"), DayOfWeek.getDayFromInt(rs.getInt("car_availability_begin_day_of_week")),
                            beginLocalTime, DayOfWeek.getDayFromInt(rs.getInt("car_availability_end_day_of_week")), endLocalTime));
                }
                return availabilities;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading availabilities resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of availabilities", ex);
        }
    }

    private void setAvailabilityVariables(PreparedStatement ps, int carId, CarAvailabilityInterval availability) throws SQLException {
        ps.setInt(1, carId);
        ps.setInt(2, availability.getBeginDayOfWeek().getI());
        ps.setTime(3, new Time(availability.getBeginTime().toDateTimeToday().getMillis()));
        ps.setInt(4, availability.getEndDayOfWeek().getI());
        ps.setTime(5, new Time(availability.getEndTime().toDateTimeToday().getMillis()));
    }

    @Override
    public void addOrUpdateAvailabilities(Car car, Iterable<CarAvailabilityInterval> availabilities) throws DataAccessException {
        try {
            for(CarAvailabilityInterval availability : availabilities) {
                if(availability.getId() == null) { // create
                    PreparedStatement ps = createAvailabilityStatement();
                    setAvailabilityVariables(ps, car.getId(), availability);

                    if(ps.executeUpdate() == 0)
                        throw new DataAccessException("No rows were affected when creating availability.");
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        int id = keys.getInt(1);
                        availability.setId(id);
                    } catch (SQLException ex) {
                        throw new DataAccessException("Failed to get primary key for new availability.", ex);
                    }
                } else { // update
                    PreparedStatement ps = updateAvailabilityStatement();
                    setAvailabilityVariables(ps, car.getId(), availability);

                    ps.setInt(6, availability.getId());

                    if(ps.executeUpdate() == 0)
                        throw new DataAccessException("No rows were affected when updating availability.");

                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create/update new availabilitiy");
        }
    }

    @Override
    public void deleteAvailabilties(Iterable<CarAvailabilityInterval> availabilities) throws DataAccessException {
        try {
            for(CarAvailabilityInterval availability : availabilities) {
                if(availability.getId() == null) {
                    throw new DataAccessException("No id is available to availability.");
                } else {
                    PreparedStatement ps = deleteAvailabilityStatement();

                    ps.setInt(1, availability.getId());

                    if(ps.executeUpdate() == 0)
                        throw new DataAccessException("No rows were affected when deleting availability.");
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to delete new availabilitiy");
        }
    }

    @Override
    public Iterable<User> getPrivileged(int carId) throws DataAccessException {
        try {
            PreparedStatement ps = getPrivilegedStatement();
            ps.setInt(1, carId);
            List<User> users = new ArrayList<>();
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

    @Override
    public void addPrivileged(int carId, Iterable<User> users) throws DataAccessException {
        try {
            for(User user : users) {
                PreparedStatement ps = createPrivilegedStatement();
                ps.setInt(1, user.getId());
                ps.setInt(2, carId);

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when creating privileged.");
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create new privileged");
        }
    }

    @Override
    public void deletePrivileged(int carId, Iterable<User> users) throws DataAccessException {
        try {
            for(User user : users) {
                PreparedStatement ps = deletePrivilegedStatement();

                ps.setInt(1, user.getId());
                ps.setInt(2, carId);

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when deleting privileged.");

            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to delete privileged");
        }
    }

    /**
     * @param filter The filter to apply to
     * @return The amount of filtered cars
     * @throws DataAccessException
     */
    @Override
    public int getAmountOfCars(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfCarsStatement();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_cars");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of cars", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of cars", ex);
        }
    }

    /**
     * @param filter The filter to apply to
     * @return The amount of filtered cars
     * @throws DataAccessException
     */
    @Override
    public int countCars(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getCountCarsStatement();
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

    /**
     * Get a carlist, with the default ordering and without filtering
     * @param page The page you want to see
     * @param pageSize The page size
     * @return The page of list of cars
     */
    @Override
    public List<Car> getCarList(int page, int pageSize) throws DataAccessException {
        return getCarList(FilterField.CAR_NAME, true, page, pageSize, null);
    }

    /**
     * @param orderBy The field you want to order by
     * @param asc Ascending
     * @param page The page you want to see
     * @param pageSize The page size
     * @param filter The filter you want to apply
     * @return List of cars with custom ordering and filtering
     */
    @Override
    public List<Car> getCarList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = null;
            switch(orderBy) {
                case CAR_NAME:
                    ps = asc ? getGetCarListPageByNameAscStatement() : getGetCarListPageByNameDescStatement();
                    break;
                case CAR_BRAND:
                    ps = asc ? getGetCarListPageByBrandAscStatement() : getGetCarListPageByBrandDescStatement();
                    break;
            }
            if(ps == null) {
                throw new DataAccessException("Could not create getCarList statement");
            }

            fillFragment(ps, filter, 1);
            int first = (page-1)*pageSize;
            ps.setInt(30, first);
            ps.setInt(31, pageSize);
            return getCars(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars", ex);
        }
    }

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
                    ps = asc ? getListCarsPageByNameAscStatement() : getListCarsPageByNameDescStatement();
                    break;
                case CAR_BRAND:
                    ps = asc ? getListCarsPageByBrandAscStatement() : getListCarsPageByBrandDescStatement();
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
                    cars.add(result);
                }
                return cars;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars", ex);
        }
    }
    /**
     *
     * @param user_id The id of the user
     * @return The cars of the user (without pagination)
     * @throws DataAccessException
     */
    @Override
    public List<Car> getCarsOfUser(int user_id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetCarsOfUserStatement();
            ps.setInt(1, user_id);
            return getCars(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of cars for user with id " + user_id, ex);
        }
    }

    private List<Car> getCars(PreparedStatement ps) {
        List<Car> cars = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cars.add(populateCar(rs, true));
            }
            return cars;
        } catch (SQLException ex) {
            throw new DataAccessException("Error reading cars resultset", ex);
        }
    }
}
