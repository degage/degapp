/* TestAddressDAO.java
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
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package database.mocking;

import java.util.ArrayList;
import java.util.List;

import be.ugent.degage.db.models.Address;
import be.ugent.degage.db.AddressDAO;
import be.ugent.degage.db.DataAccessException;

public class TestAddressDAO implements AddressDAO{
	
	private List<Address> list;
	private int idCounter;
	
	public TestAddressDAO(){
		list = new ArrayList<>();
		idCounter=0;
	}

	@Override
	public Address getAddress(int id) throws DataAccessException {
		for(Address address : list){
			if(address.getId()==id){
				return new Address(address.getCountry(),address.getZip(), address.getCity(), address.getStreet(), address.getNumber(), address.getBus());
			}
		}
		return null;
	}

	@Override
	public int existsAddress(Address address) throws DataAccessException {
		for(Address a : list){
			if(a.getId()==address.getId()){
				return address.getId();
			}
		}
		return -1;
	}

	@Override
	public void updateAddress(Address address) throws DataAccessException {
		// ok
	}

	@Override
	public void deleteAddress(Address address) throws DataAccessException {
		if (list.contains(address)) {
			list.remove(address);
		}
	}

	@Override
	public Address createAddress(String country, String zip, String city, String street, String number, String bus)	throws DataAccessException {
		Address address = new Address(idCounter++, country, zip, city, street, number, bus);
		list.add(address);
		return address;
	}

}
