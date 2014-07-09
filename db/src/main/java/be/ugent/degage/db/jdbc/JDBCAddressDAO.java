package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Address;

import java.sql.*;

/**
 * JDBC implementation of {@link AddressDAO}
 */
class JDBCAddressDAO implements AddressDAO {

    private final Connection connection;

    private PreparedStatement getAddressStatement;
    private PreparedStatement createAddressStatement;
    private PreparedStatement deleteAddressStatement;
    private PreparedStatement updateAddressStatement;

    private static final String[] AUTO_GENERATED_KEYS = {"address_id"};

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

    private PreparedStatement getGetAddressStatement() throws SQLException {
        if (getAddressStatement == null) {
            getAddressStatement = connection.prepareStatement("SELECT address_id, address_city, address_zipcode, address_street, address_street_number, address_street_bus, address_country FROM addresses WHERE address_id = ?");
        }
        return getAddressStatement;
    }

    private PreparedStatement getUpdateAddressStatement() throws SQLException {
        if(updateAddressStatement == null){
            updateAddressStatement = connection.prepareStatement("UPDATE addresses SET address_city = ?, address_zipcode = ?, address_street = ?, address_street_number = ?, address_street_bus = ?, address_country=? WHERE address_id = ?");
        }
        return updateAddressStatement;
    }
    private PreparedStatement getDeleteAddressStatement() throws SQLException {
        if(deleteAddressStatement == null){
            deleteAddressStatement = connection.prepareStatement("DELETE FROM addresses WHERE address_id = ?");
        }
        return deleteAddressStatement;
    }

    private PreparedStatement getCreateAddressStatement() throws SQLException {
        if (createAddressStatement == null) {
            createAddressStatement = connection.prepareStatement("INSERT INTO addresses(address_city, address_zipcode, address_street, address_street_number, address_street_bus, address_country) VALUES (?,?,?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createAddressStatement;
    }

    public JDBCAddressDAO(Connection connection){
        this.connection = connection;
    }

    @Override
    public Address getAddress(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAddressStatement();   // reused so should not be auto-closed
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

    @Override
    public Address createAddress(String country, String zip, String city, String street, String number, String bus) throws DataAccessException {
        try {
            PreparedStatement ps = getCreateAddressStatement(); // reused so should not be auto-closed
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
	
    @Override
    public void deleteAddress(int addressId) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteAddressStatement(); // reused so should not be auto-closed
            ps.setInt(1, addressId);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting address with ID=" + addressId);

        } catch(SQLException ex){
            throw new DataAccessException("Failed to execute address deletion query.", ex);
        }
    }

    @Override
    public void updateAddress(Address address) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateAddressStatement();   // reused so should not be auto-closed
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
