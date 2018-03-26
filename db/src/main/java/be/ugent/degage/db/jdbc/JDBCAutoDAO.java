package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.AutoDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class JDBCAutoDAO extends AbstractDAO implements AutoDAO {

  public static final int SQL_DUPLICATE_ENTRY = 1062;

  public JDBCAutoDAO(JDBCDataAccessContext context) {
    super(context);
  }

  private static final String AUTO_FIELDS =
            "car_name, car_status, car_email, car_type, car_brand, car_location, car_seats, car_doors, car_year," +
            "car_manual, car_gps, car_hook, car_fuel, car_fuel_economy, car_estimated_value, car_owner_annual_km, car_owner_user_id," +
            "car_comments, car_active, car_images_id, car_deprec, car_deprec_limit, car_deprec_last," +
            "car_start_sharing, car_end_sharing";

  @Override
  public int createAuto(Auto auto) throws DataAccessException {
    try(PreparedStatement ps = prepareStatement(
      "INSERT INTO cars("+ AUTO_FIELDS + ") " +
      "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
      "car_id"
    )){
      int counter = 1;
      // ps.setInt(1, auto.getCarId());
      ps.setString(counter++, auto.getCarName());
      ps.setString(counter++, auto.getCarStatus().name());
      ps.setString(counter++, auto.getCarEmail());
      ps.setString(counter++, auto.getCarType());
      ps.setString(counter++, auto.getCarBrand());
      ps.setInt(counter++, auto.getCarLocationId());
      ps.setInt(counter++, auto.getCarSeats());
      ps.setInt(counter++, auto.getCarDoors());
      ps.setInt(counter++, auto.getCarYear());
      ps.setBoolean(counter++, auto.getCarManual());
      ps.setBoolean(counter++, auto.getCarGps());
      ps.setBoolean(counter++, auto.getCarHook());
      ps.setString(counter++, auto.getCarFuel() == null ? "DIESEL" : auto.getCarFuel().name());
      ps.setInt(counter++, auto.getCarFuelEconomy());
      ps.setInt(counter++, auto.getCarEstimatedValue());
      ps.setInt(counter++, auto.getCarOwnerAnnualKm());
      ps.setInt(counter++, auto.getCarOwnerUserId());
      ps.setString(counter++, auto.getCarComments());
      ps.setBoolean(counter++, auto.getCarActive());
      if (auto.getCarImagesId() > -1) {
        ps.setInt(counter++, auto.getCarImagesId());
      } else {
        ps.setNull(counter++, java.sql.Types.INTEGER);
      }
      ps.setInt(counter++, auto.getCarDeprec());
      ps.setInt(counter++, auto.getCarDeprecLimit());
      ps.setInt(counter++, auto.getCarDeprecLast());
      ps.setDate(counter++, auto.getCarStartSharing() == null ? null : Date.valueOf(auto.getCarStartSharing()));
      ps.setDate(counter++, auto.getCarEndSharing() == null ? null : Date.valueOf(auto.getCarEndSharing()));
      ps.executeUpdate();

      try(ResultSet keys = ps.getGeneratedKeys()){
        keys.next();
        int carId = keys.getInt(1);
        return carId;
      }
    } catch (SQLException ex) {
      if (ex.getErrorCode() == SQL_DUPLICATE_ENTRY){
        throw new DataAccessException("Could not create auto because it already exists.", ex);
      } else {
        throw new DataAccessException("Could not create auto", ex);
      }
    }
  }

  @Override
  public void updateAuto(Auto auto) throws DataAccessException {
    try(PreparedStatement ps = prepareStatement(
      "UPDATE cars SET car_name = ?, car_email = ?, car_type = ?, car_brand = ?, car_location = ?, car_seats = ?, car_doors = ?, car_year = ?," +
      "car_manual = ?, car_gps = ?, car_hook= ?, car_fuel = ?, car_fuel_economy = ?, car_estimated_value = ?, car_owner_annual_km = ?, car_owner_user_id = ?," +
      "car_comments = ?, car_active = ?, car_images_id = ?, car_deprec = ?, car_deprec_limit = ?, car_deprec_last = ?," +
      "car_start_sharing = ?, car_end_sharing = ?, car_status = ?, car_contract = ?, car_agreed_value = ? " +
      "WHERE car_id = ?"
    )){
      ps.setString(1, auto.getCarName());
      ps.setString(2, auto.getCarEmail());
      ps.setString(3, auto.getCarType());
      ps.setString(4, auto.getCarBrand());
      ps.setInt(5, auto.getCarLocationId());
      ps.setInt(6, auto.getCarSeats());
      ps.setInt(7, auto.getCarDoors());
      ps.setInt(8, auto.getCarYear());
      ps.setBoolean(9, auto.getCarManual());
      ps.setBoolean(10, auto.getCarGps());
      ps.setBoolean(11, auto.getCarHook());
      ps.setString(12, auto.getCarFuel() == null ? "UNKNOWN" : auto.getCarFuel().name());
      ps.setInt(13, auto.getCarFuelEconomy());
      ps.setInt(14, auto.getCarEstimatedValue());
      ps.setInt(15, auto.getCarOwnerAnnualKm());
      ps.setInt(16, auto.getCarOwnerUserId());
      ps.setString(17, auto.getCarComments());
      ps.setBoolean(18, auto.getCarActive());
      if (auto.getCarImagesId() == 0) {
        ps.setNull(19, java.sql.Types.INTEGER);
      } else {
        ps.setInt(19, auto.getCarImagesId());
      }
      if (auto.getCarDeprec() == 0) {
        ps.setNull(20, java.sql.Types.INTEGER);
      } else {
        ps.setInt(20, auto.getCarDeprec());
      }
      if (auto.getCarDeprecLimit() == 0) {
        ps.setNull(21, java.sql.Types.INTEGER);
      } else {
        ps.setInt(21, auto.getCarDeprecLimit());
      }
      if (auto.getCarDeprecLast() == 0) {
        ps.setNull(22, java.sql.Types.INTEGER);
      } else {
        ps.setInt(22, auto.getCarDeprecLast());
      }
      ps.setDate(23, auto.getCarStartSharing() == null ? null : Date.valueOf(auto.getCarStartSharing()));
      ps.setDate(24, auto.getCarEndSharing() == null ? null : Date.valueOf(auto.getCarEndSharing()));
      ps.setString(25, auto.getCarStatus().name());
      ps.setDate(26, auto.getContract() == null ? null : Date.valueOf(auto.getContract()));
      if (auto.getCarAgreedValue() == 0) {
        ps.setNull(27, java.sql.Types.INTEGER);
      } else {
        ps.setInt(27, auto.getCarAgreedValue());
      }
      ps.setInt(28, auto.getCarId());

      if (ps.executeUpdate() == 0) {
        throw new DataAccessException("No rows were affected when updating auto.");
      }

    } catch(SQLException ex){
      throw new DataAccessException("Failed to update auto.", ex);
    }
  }

  @Override
  public Auto getAuto(int autoId) throws DataAccessException {
    try (PreparedStatement ps = prepareStatement(
            "SELECT * FROM cars " +
            "LEFT JOIN addresses on address_id = car_location " +
            "LEFT JOIN carinsurances on insurance_id = car_id " +
            "LEFT JOIN carassistances ON carassistances.assistance_id = cars.car_id " +
            "LEFT JOIN carparkingcards ON carparkingcards.parkingcard_id = cars.car_id " +
            "WHERE car_id = ? "
    )) {
        ps.setInt(1, autoId);
        return toSingleObject(ps, JDBCAutoDAO::populateAuto);
    } catch (SQLException e) {
        throw new DataAccessException("Unable to get auto", e);
    }
  }

  @Override
  public Auto getAutoByUserId(int userId) throws DataAccessException {
    try (PreparedStatement ps = prepareStatement(
            "SELECT * FROM cars " +
            "LEFT JOIN addresses on address_id = car_location " +
            "LEFT JOIN carinsurances on insurance_id = car_id " +
            "LEFT JOIN carassistances ON carassistances.assistance_id = cars.car_id " +
            "LEFT JOIN carparkingcards ON carparkingcards.parkingcard_id = cars.car_id " +
            "WHERE car_owner_user_id = ? "
    )) {
        ps.setInt(1, userId);
        return toSingleObject(ps, JDBCAutoDAO::populateAuto);
    } catch (SQLException e) {
        throw new DataAccessException("Unable to get auto", e);
    }
  }

  @Override
  public Page<AutoAndUser> listAutosAndOwners(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException {
      StringBuilder builder = new StringBuilder(
        "SELECT SQL_CALC_FOUND_ROWS * " +
                "FROM cars " +
                "LEFT JOIN addresses on address_id = car_location " +
                "LEFT JOIN carassistances ON carassistances.assistance_id = cars.car_id " +
                "LEFT JOIN carinsurances on insurance_id = car_id " +
                "LEFT JOIN carparkingcards ON carparkingcards.parkingcard_id = cars.car_id " +
                "JOIN users ON car_owner_user_id = user_id " +
                "LEFT JOIN technicalcardetails ON details_id = car_id "
      );

      // add filters
      if (filter != null && filter.length() > 0) {
        builder.append(" WHERE ");
        String[] searchStrings = filter.trim().split(" ");
        for (int i = 0; i < searchStrings.length; i++) {
          if (i > 0) {
            builder.append(" AND ");
          }
          builder.append("(");
          StringBuilder filterBuilder = new StringBuilder();
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_name", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_status", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_brand", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "details_car_license_plate", searchStrings[i]);
          builder.append(filterBuilder).append(")");
        }
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
          case LICENSE_PLATE:
              builder.append(" ORDER BY details_car_license_plate ");
              builder.append(asc ? "ASC" : "DESC");
              break;
      }

      builder.append (" LIMIT ").append (pageSize).append(" OFFSET ").append((page-1)*pageSize);

      try (PreparedStatement ps = prepareStatement(builder.toString())) {
          return toPage(ps, pageSize, JDBCAutoDAO::populateAutoAndUser);
      } catch (SQLException ex) {
          throw new DataAccessException(ex);
      }
  }

  @Override
  public Page<AutoAndUserAndEnrollee> listAutosAndOwnersAndEnrollees(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException {
      StringBuilder builder = new StringBuilder(
        "SELECT SQL_CALC_FOUND_ROWS * " +
                "FROM cars " +
                "LEFT JOIN addresses on address_id = car_location " +
                "JOIN users ON car_owner_user_id = user_id " +
                "LEFT JOIN " + 
                  "(SELECT max(infosession_id) as infosession_id, infosession_enrollment_status, infosession_enrollee_id " +
                    "FROM infosessionenrollees " +
                    "GROUP BY infosession_enrollee_id, infosession_enrollment_status " +
                ") AS lastsession " +
                "ON lastsession.infosession_enrollee_id = user_id " +
                "LEFT JOIN technicalcardetails ON details_id = car_id " +
                "LEFT JOIN carinsurances on insurance_id = car_id " +
                "LEFT JOIN carassistances ON carassistances.assistance_id = cars.car_id " +
                "LEFT JOIN carparkingcards ON carparkingcards.parkingcard_id = cars.car_id "
      );

      // add filters
      if (filter != null && filter.length() > 0) {
        builder.append(" WHERE (infosession_enrollment_status = 'PRESENT' or infosession_enrollment_status = 'ENROLLED') ");
        String[] searchStrings = filter.trim().split(" ");
        for (int i = 0; i < searchStrings.length; i++) {
          builder.append(" AND ").append("(");
          StringBuilder filterBuilder = new StringBuilder();
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_name", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_status", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_brand", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_type", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_year", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_fuel", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "details_car_license_plate", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "user_lastname", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "user_firstname", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "address_city", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "address_street", searchStrings[i]);
          builder.append(filterBuilder).append(")");
        }
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
          case LICENSE_PLATE:
              builder.append(" ORDER BY details_car_license_plate ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case CAR_CREATION_DATE:
              builder.append(" ORDER BY car_created_at ");
              builder.append(asc ? "ASC" : "DESC");
              break;
      }

      builder.append (" LIMIT ").append (pageSize).append(" OFFSET ").append((page-1)*pageSize);
      System.out.println(builder.toString());

      try (PreparedStatement ps = prepareStatement(builder.toString())) {
          return toPage(ps, pageSize, JDBCAutoDAO::populateAutoAndUserAndEnrollee);
      } catch (SQLException ex) {
          throw new DataAccessException(ex);
      }
  }

  @Override
  public void updateAutoDocument(int autoId, int fileId) {
    try (PreparedStatement ps = prepareStatement(
      "UPDATE cars SET car_contract_file_id = ? WHERE car_id = ?"
    )) {
      ps.setInt(1, fileId);
      ps.setInt(2, autoId);
      ps.executeUpdate();
    } catch (SQLException ex) {
      throw new DataAccessException("Failed to update car document", ex);
    }
  }

  private static AutoAndUserAndEnrollee populateAutoAndUserAndEnrollee(ResultSet rs) throws SQLException {
    return new AutoAndUserAndEnrollee.Builder()
      .auto(populateAuto(rs))
      .user(rs.getObject("user_id") == null ? null : populateUser(rs, "users"))
      .infosessionId(rs.getInt("infosession_id"))
      .enrollmentStatus(EnrollmentStatus.valueOf(rs.getString("infosession_enrollment_status")))
      .build();
  }
  
  public static User populateUser(ResultSet rs, String tableName) throws SQLException {
    return new User(
      rs.getInt(tableName + ".user_id"),
      rs.getString(tableName + ".user_email"),
      rs.getString(tableName + ".user_firstname"),
      rs.getString(tableName + ".user_lastname"),
      UserStatus.valueOf(rs.getString(tableName + ".user_status")),
      rs.getString(tableName + ".user_phone"),
      rs.getString(tableName + ".user_cellphone"),
      (Integer) rs.getObject(tableName + ".user_degage_id"))
      .setPaymentInfoBuilder(rs.getString(tableName + ".user_payment_info"));
  }

  private static AutoAndUser populateAutoAndUser(ResultSet rs) throws SQLException {
    return new AutoAndUser(
      populateAuto(rs),
      rs.getObject("user_id") == null ? null : new UserHeaderShort(
        rs.getInt("user_id"),
        rs.getString("user_firstname"),
        rs.getString("user_lastname")
      )
    );
  }

  public static Auto populateAuto(ResultSet rs) throws SQLException {

    Address address = JDBCAddressDAO.populateAddress(rs);
    CarInsurance insurance = JDBCCarInsuranceDAO.populateCarInsurance(rs);
    CarAssistance assistance = JDBCCarAssistanceDAO.populateCarAssistance(rs);
    CarParkingcard parkingcard = JDBCCarParkingcardDAO.populateCarParkingcard(rs);

    Auto newAuto = new Auto.Builder(rs.getInt("car_id"), rs.getInt("car_owner_user_id"))
      .name(rs.getString("car_name"))
      .status(CarStatus.valueOf(rs.getString("car_status")))
      .email(rs.getString("car_email"))
      .type(rs.getString("car_type"))
      .brand(rs.getString("car_brand"))
      .locationId(rs.getInt("car_location"))
      .location(address)
      .seats(rs.getInt("car_seats"))
      .doors(rs.getInt("car_doors"))
      .year(rs.getInt("car_year"))
      .manual(rs.getBoolean("car_manual"))
      .gps(rs.getBoolean("car_gps"))
      .hook(rs.getBoolean("car_hook"))
      .fuel(CarFuel.valueOf(rs.getString("car_fuel")))
      .fuelEconomy(rs.getInt("car_fuel_economy"))
      .estimatedValue(rs.getInt("car_estimated_value"))
      .ownerAnnualKm(rs.getInt("car_owner_annual_km"))
      .comments(rs.getString("car_comments"))
      .active(rs.getBoolean("car_active"))
      .imagesId(rs.getInt("car_images_id"))
      .deprec(rs.getInt("car_deprec"))
      .deprecLimit(rs.getInt("car_deprec_limit"))
      .deprecLast(rs.getInt("car_deprec_last"))
      .startSharing(rs.getDate("car_start_sharing") == null ? null : rs.getDate("car_start_sharing").toLocalDate())
      .endSharing(rs.getDate("car_end_sharing") == null ? null : rs.getDate("car_end_sharing").toLocalDate())
      .createdAt(rs.getDate("car_created_at") == null ? null : rs.getDate("car_created_at").toLocalDate())
      .updatedAt(rs.getDate("car_updated_at") == null ? null : rs.getDate("car_updated_at").toLocalDate())
      .contractFileId(rs.getInt("car_contract_file_id"))
      .contract(rs.getDate("car_contract") == null ? null : rs.getDate("car_contract").toLocalDate())
      .carAgreedValue(rs.getInt("car_agreed_value"))
      .insurance(insurance)
      .assistance(assistance)
      .parkingcard(parkingcard)
      .build();

      return newAuto;
  }
}
