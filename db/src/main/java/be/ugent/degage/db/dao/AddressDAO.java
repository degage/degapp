package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Address;

/**
 * Created by Cedric on 2/21/14.
 */
public interface AddressDAO {

    /**
     * Return the address record with the given id. Returns null when not found.
     */
    public Address getAddress(int id) throws DataAccessException;

    /**
     * Create a new address record
     */
    public Address createAddress(String country, String zip, String city, String street, String number, String bus) throws DataAccessException;

    /**
     * Update the address record with the given id.
     */
    public void updateAddress(Address address) throws DataAccessException;

    /**
     * Delete the address record with the given id.
     */
    public void deleteAddress(int addressId) throws DataAccessException;
}
