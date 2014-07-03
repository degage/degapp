package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Address;

/**
 * Created by Cedric on 2/21/14.
 */
public interface AddressDAO {
    public Address getAddress(int id) throws DataAccessException;
    public Address createAddress(String country, String zip, String city, String street, String number, String bus) throws DataAccessException;
    public int existsAddress(Address address) throws DataAccessException;
    public void updateAddress(Address address) throws DataAccessException;
    public void deleteAddress(Address address) throws DataAccessException;
}
