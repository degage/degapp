package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.PropertyDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.regex.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

/**
 * JDBC implementation of {@link PropertyDAO}
 */
public class JDBCPropertyDAO extends AbstractDAO implements PropertyDAO {


    private static final String PROPERTY_FIELDS =
            "property_id, property_key, property_value ";


    public JDBCPropertyDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static Property populateProperty(ResultSet rs) throws SQLException {

        Property r = new Property.Builder(rs.getString("property_key"))
                .id(rs.getInt("property_id"))
                .value(rs.getString("property_value"))
                .build();

        return r;
    }

    /*
     * Creates a property in the db
     * Returns -1 when no property was created
     */
    private int createProperty(String key, String value) {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO properties(property_key, property_value) " +
                        "VALUES (?,?)",
                "property_id"
        )) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                int propertyId = keys.getInt(1);
                return propertyId;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create property.", ex);
        }
    }

    @Override
    public void updateProperty(Property property) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE properties SET property_key = ?, property_value = ? WHERE property_id = ?"
        )) {
            ps.setString(1, property.getKey());
            ps.setString(2, property.getValue());
            ps.setInt(3, property.getId());

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("No rows were affected when updating property.");
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update property.", ex);
        }
    }

    @Override
    public Property getProperty(int propertyId) throws DataAccessException {
      StringBuilder builder = new StringBuilder(
          "SELECT * FROM properties WHERE property_id = ?"
      );

      try (PreparedStatement ps = prepareStatement(builder.toString())) {
        ps.setInt(1, propertyId);
        return toSingleObject(ps, JDBCPropertyDAO::populateProperty);
      } catch (SQLException ex) {
          throw new DataAccessException(ex);
      }

    }

    @Override
    public Property getPropertyByKey(String key) throws DataAccessException {
      StringBuilder builder = new StringBuilder(
          "SELECT * FROM properties WHERE property_key = ?"
      );

      try (PreparedStatement ps = prepareStatement(builder.toString())) {
        ps.setString(1, key);
        return toSingleObject(ps, JDBCPropertyDAO::populateProperty);
      } catch (SQLException ex) {
          throw new DataAccessException(ex);
      }

    }

}
