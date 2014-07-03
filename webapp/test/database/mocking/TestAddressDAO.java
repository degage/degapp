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
