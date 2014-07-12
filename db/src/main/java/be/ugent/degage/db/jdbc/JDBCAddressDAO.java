package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Address;

import java.sql.*;

/**
 * JDBC implementation of {@link AddressDAO}
 */
class JDBCAddressDAO extends AbstractDAO implements AddressDAO {

    public JDBCAddressDAO(JDBCDataAccessContext context){
        super (context);
    }

    // TODO: avoid these
    public static Address populateAddress(ResultSet rs) throws SQLException {
        return populateAddress(rs, "addresses");
    }

    // TODO: avoid these
    public static Address populateAddress(ResultSet rs, String tableName) throws SQLException {
        if(rs.getObject(tableName + ".address_id") == null)
            return null;
        else
            return new Address(rs.getInt(tableName + ".address_id"), rs.getString(tableName + ".address_country"), rs.getString(tableName + ".address_zipcode"), rs.getString(tableName + ".address_city"), rs.getString(tableName + ".address_street"), rs.getString(tableName + ".address_street_number"), rs.getString(tableName + ".address_street_bus"));
    }


    private LazyStatement getAddressStatement = new LazyStatement(
            "SELECT address_id, address_city, address_zipcode, address_street, " +
                    "address_street_number, address_street_bus, address_country " +
                    "FROM addresses WHERE address_id = ?");

    @Override
    public Address getAddress(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getAddressStatement.value();   // reused so should not be auto-closed
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return populateAddress(rs);
                } else
                    return null;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch address by id.", ex);
        }
    }

    private LazyStatement createAddressStatement = new LazyStatement(
            "INSERT INTO addresses(address_city, address_zipcode, address_street, " +
                    "address_street_number, address_street_bus, address_country) " +
                    "VALUES (?,?,?,?,?,?)",
            "address_id"
    );

    @Override
    public Address createAddress(String country, String zip, String city, String street, String number, String bus) throws DataAccessException {
        try {
            PreparedStatement ps = createAddressStatement.value(); // reused so should not be auto-closed
            ps.setString(1, city);
            ps.setString(2, zip);
            ps.setString(3, street);
            ps.setString(4, number);
            ps.setString(5, bus);
            ps.setString(6, country);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating address.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway

                return new Address(keys.getInt(1), country, zip, city, street, number, bus);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create address.", ex);
        }
    }
	
    private LazyStatement deleteAddressStatement = new LazyStatement(
            "DELETE FROM addresses WHERE address_id = ?"
    );

    @Override
    public void deleteAddress(int addressId) throws DataAccessException {
        try {
            PreparedStatement ps = deleteAddressStatement.value(); // reused so should not be auto-closed
            ps.setInt(1, addressId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting address with ID=" + addressId);

        } catch(SQLException ex){
            throw new DataAccessException("Failed to execute address deletion query.", ex);
        }
    }


    private LazyStatement updateAddressStatement = new LazyStatement(
            "UPDATE addresses SET address_city = ?, address_zipcode = ?, address_street = ?, " +
                    "address_street_number = ?, address_street_bus = ?, address_country=? " +
                    "WHERE address_id = ?"
    );

    @Override
    public void updateAddress(Address address) throws DataAccessException {
        try {
            PreparedStatement ps = updateAddressStatement.value();   // reused so should not be auto-closed
            ps.setString(1, address.getCity());
            ps.setString(2, address.getZip());
            ps.setString(3, address.getStreet());
            ps.setString(4, address.getNumber());
            ps.setString(5, address.getBus());
            ps.setString(6, address.getCountry());

            ps.setInt(7, address.getId());
			
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Address update affected 0 rows.");

        } catch(SQLException ex) {
            throw new DataAccessException("Failed to update address.", ex);
        }

    }
}
