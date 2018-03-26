package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarApprovalDAO;
import be.ugent.degage.db.models.*;

import java.sql.*;
import java.time.LocalDate;

class JDBCCarApprovalDAO extends AbstractDAO implements CarApprovalDAO {

    public JDBCCarApprovalDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static CarApproval populateCarApproval(ResultSet rs) throws SQLException {      
        
        return new CarApproval.Builder().id(rs.getInt("carapproval_id"))
          .carId(rs.getInt("carapproval_car_id"))
        //   .adminId(rs.getInt("carapproval_admin_id"))
          .userMessage(rs.getString("carapproval_user_message"))
          .adminMessage(rs.getString("carapproval_admin_message"))
          .date(rs.getDate("carapproval_date") == null ? null : rs.getDate("carapproval_date").toLocalDate())
          .submissionDate(rs.getDate("carapproval_submission_date") == null ? null : rs.getDate("carapproval_submission_date").toLocalDate())
          .status(ApprovalStatus.valueOf(rs.getString("carapproval_status")))
          .car(JDBCAutoDAO.populateAuto(rs))
          .user(rs.getObject("users.user_id") == null ? null : JDBCAutoDAO.populateUser(rs, "users"))
          .admin(rs.getObject("admins.user_id") == null ? null : JDBCAutoDAO.populateUser(rs, "admins"))
          .infoSessionId(rs.getInt("infosession_id"))
          .enrollmentStatus(EnrollmentStatus.valueOf(rs.getString("infosession_enrollment_status")))
          .build();
    }
  
    @Override
    public CarApproval getCarApproval(int carApprovalId) throws DataAccessException {
      try (PreparedStatement ps = prepareStatement(
        "SELECT SQL_CALC_FOUND_ROWS * " +
        "FROM carapprovals appr " +
        "LEFT JOIN cars on appr.carapproval_car_id = cars.car_id " +
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
        "LEFT JOIN carparkingcards ON carparkingcards.parkingcard_id = cars.car_id " +
        "LEFT JOIN users as admins ON carapproval_admin_id = admins.user_id " +
        "WHERE carapproval_id = ? "
      )) {
          ps.setInt(1, carApprovalId);
          return toSingleObject(ps, JDBCCarApprovalDAO::populateCarApproval);
      } catch (SQLException e) {
          throw new DataAccessException("Unable to get car approval", e);
      }
    }

    @Override
  public Page<CarApproval> listCarApprovals(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException {
      StringBuilder builder = new StringBuilder(
        "SELECT SQL_CALC_FOUND_ROWS * " +
                "FROM carapprovals appr " +
                "LEFT JOIN cars on appr.carapproval_car_id = cars.car_id " +
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
                "LEFT JOIN carparkingcards ON carparkingcards.parkingcard_id = cars.car_id " +
                "LEFT JOIN users as admins ON carapproval_admin_id = admins.user_id "
      );

      // add filters
      if (filter != null && filter.length() > 0) {
        builder.append(" WHERE (infosession_enrollment_status = 'PRESENT' or infosession_enrollment_status = 'ENROLLED') ");
        String[] searchStrings = filter.trim().split(" ");
        for (int i = 0; i < searchStrings.length; i++) {
          builder.append(" AND ").append("(");
          StringBuilder filterBuilder = new StringBuilder();
          FilterUtils.appendOrContainsFilter(filterBuilder, "carapproval_status", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_name", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_brand", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_type", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_year", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "car_fuel", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "details_car_license_plate", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "users.user_lastname", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "users.user_firstname", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "admins.user_lastname", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "admins.user_firstname", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "address_city", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "address_street", searchStrings[i]);
          builder.append(filterBuilder).append(")");
        }
      }

      // add order
      switch (orderBy) {
          case CAR_ID:
            builder.append(" ORDER BY car_id ");
            builder.append(asc ? "ASC" : "DESC");
            break;
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
              builder.append(" ORDER BY users.user_lastname ");
              builder.append(asc ? "ASC" : "DESC");
              builder.append(" , users.user_firstname ");
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
              builder.append(" ORDER BY GREATEST(coalesce(car_updated_at,0), coalesce(carapproval_submission_date,0)) ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case CAR_ADMIN:
              builder.append(" ORDER BY admins.user_lastname ");
              builder.append(asc ? "ASC" : "DESC");
              builder.append(" , admins.user_firstname ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case STATUS:
              builder.append(" ORDER BY infosession_enrollment_status ");
              builder.append(asc ? "ASC" : "DESC");
              break;
      }

      builder.append (" LIMIT ").append (pageSize).append(" OFFSET ").append((page-1)*pageSize);
    //   System.out.println(builder.toString());

      try (PreparedStatement ps = prepareStatement(builder.toString())) {
          return toPage(ps, pageSize, JDBCCarApprovalDAO::populateCarApproval);
      } catch (SQLException ex) {
          throw new DataAccessException(ex);
      }
  }

  @Override
  public void updateCarApproval(CarApproval carApproval) throws DataAccessException {
    try(PreparedStatement ps = prepareStatement(
      "UPDATE carapprovals SET carapproval_car_id = ?, carapproval_admin_id = ?, carapproval_date = NOW(), carapproval_submission_date = ?, carapproval_status = ?, " +
      "carapproval_user_message = ?, carapproval_admin_message = ? " +
      "WHERE carapproval_id = ?"
    )){
      ps.setInt(1, carApproval.getCarId());
      ps.setInt(2, carApproval.getAdmin().getId());
      ps.setDate(3, carApproval.getDate() == null ? null : Date.valueOf(carApproval.getDate()));
      ps.setString(4, carApproval.getStatus().name());
      ps.setString(5, carApproval.getUserMessage());
      ps.setString(6, carApproval.getAdminMessage());
      ps.setInt(7, carApproval.getCarApprovalId());

      if (ps.executeUpdate() == 0) {
        throw new DataAccessException("No rows were affected when updating carApproval.");
      }

    } catch(SQLException ex){
      throw new DataAccessException("Failed to update carApproval.", ex);
    }
  }

  @Override
  public int getNrOfPendingCarApprovals() throws DataAccessException {
    try (PreparedStatement ps = prepareStatement(
        "SELECT count(*) FROM carapprovals WHERE carapproval_status = 'REQUEST'"
    )) {
        return toSingleInt(ps);
    } catch (SQLException ex) {
        throw new DataAccessException(ex);
    }
  }

}
