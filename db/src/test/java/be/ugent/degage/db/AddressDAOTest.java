package be.ugent.degage.db;

import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.models.Address;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for {@link AddressDAO}
 */
public class AddressDAOTest extends DAOTest {

    private AddressDAO dao;

    private Address[] addresses;

    @Before
    public void getDAOAndFixtures () {
        dao = context.getAddressDAO();
        addresses = Fixtures.createAddresses(context);
    }

    @Test
    public void getAddressTest () {
        Address address = dao.getAddress(addresses[0].getId());
        assertEquals ("België", address.getCountry());
        assertEquals ("Gent", address.getCity());
        assertEquals ("9000", address.getZip());
        assertEquals ("281", address.getNumber());
        assertEquals ("Krijgslaan", address.getStreet());
        assertEquals ("S9", address.getBus());
    }

    @Test
    public void updateAddressTest () {
        Address address = addresses[0];
        address.setZip ("9999");
        address.setBus (null);
        dao.updateAddress(address);

        address = dao.getAddress(addresses[0].getId());
        assertEquals ("9999", address.getZip());
        assertEquals ("Krijgslaan", address.getStreet());
        assertNull (address.getBus());
    }

    @Test
    public void deleteTest () {
        dao.deleteAddress(addresses[0].getId());
        assertNull (dao.getAddress(addresses[0].getId()));
    }
}
