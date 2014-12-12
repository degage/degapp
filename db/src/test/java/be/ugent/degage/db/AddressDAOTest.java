/* AddressDAOTest.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

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
