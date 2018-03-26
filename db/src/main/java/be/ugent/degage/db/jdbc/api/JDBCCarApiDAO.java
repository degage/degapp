package be.ugent.degage.db.jdbc.api;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.jdbc.JDBCDataAccessContext;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.api.CarApiDAO;
import be.ugent.degage.db.models.api.*;
import be.ugent.degage.db.jdbc.AbstractDAO;

import java.sql.*;

/**
 * @author Dries
 */
public class JDBCCarApiDAO extends AbstractDAO implements CarApiDAO {

    public JDBCCarApiDAO(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public Iterable<CarStand> listCarStands() throws DataAccessException {
      StringBuilder builder = new StringBuilder();
      builder.append("SELECT c.car_id, c.car_name, c.car_fuel, a.address_longitude, a.address_latitude from cars c");
      builder.append(" JOIN addresses a ON  c.car_location = a.address_id");
      builder.append(" WHERE car_active");
       try (PreparedStatement ps = prepareStatement(builder.toString())) {
            return toList(ps, JDBCCarApiDAO::populateCarStand);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of car stands", ex);
        }
    }

    @Override
    public int createCar(String name, String email, String brand, String type, int carOwnerUserId, String fuel) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO cars(car_name, car_email, car_type, car_brand, car_owner_user_id, car_fuel) VALUES (?,?,?,?,?,?)",
                "car_id"
        )) {

          ps.setString(1, name);
          ps.setString(2, email);
          ps.setString(3, type);
          ps.setString(4, brand);
          ps.setInt(5, carOwnerUserId);
          ps.setString(6, fuel);

          ps.executeUpdate();

          try (ResultSet keys = ps.getGeneratedKeys()) {
              keys.next();
              int id = keys.getInt(1);
              return id;
          }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create new car.", ex);
        }
    }


    private static CarStand populateCarStand(ResultSet rs) throws SQLException {
      return new CarStand.Builder(rs.getInt("car_id"))
        .name(rs.getString("car_name"))
        .latitude(rs.getFloat("address_latitude"))
        .longitude(rs.getFloat("address_longitude"))
        .fuelType(getFuelType(rs.getString("car_fuel")))
        .build();
    }

    private static String getFuelType(String carFuel) {
      switch (carFuel) {
        case "PETROL": return "gasoline";
        case "DIESEL": return "diesel";
        case "BIODIESEL": return "diesel";
        case "LPG": return "lpg";
        case "CNG": return "cng";
        case "HYBRID": return "hybrid";
        case "ELECTRIC": return "electric";
      }
      return "unknown";
    }

}
