/* TestCarDAO.java
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

package database.mocking;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import be.ugent.degage.db.models.*;
import org.joda.time.DateTime;

import be.ugent.degage.db.CarDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.jdbc.JDBCFilter;

public class TestCarDAO implements CarDAO{
	
	private List<Car> cars;
	private int idCounter;
	
	
	public TestCarDAO(){
		cars = new ArrayList<>();
		idCounter=0;
	}

	@Override
	public void updateCar(Car car) throws DataAccessException {
		// ok		
	}

	@Override
	public Car getCar(int id) throws DataAccessException {
		for(Car car : cars){
			if(car.getId()==id){
				return new Car(car.getId(),car.getName(), car.getBrand(),car.getType(), car.getLocation(),car.getSeats(),
						car.getDoors(),car.getYear(), car.isManual(), car.isGps(),car.isHook(),car.getFuel(),
						car.getFuelEconomy(),car.getEstimatedValue(),car.getOwnerAnnualKm(),
						car.getTechnicalCarDetails(), car.getInsurance(), car.getOwner(),car.getComments());
				}
		}
		return null;
	}

    @Override
    public List<CarAvailabilityInterval> getAvailabilities(Car car) throws DataAccessException {
        return null;
    }

    @Override
    public void addOrUpdateAvailabilities(Car car, List<CarAvailabilityInterval> carAvailabilityIntervals) throws DataAccessException {

    }

    @Override
    public void deleteAvailabilties(List<CarAvailabilityInterval> carAvailabilityIntervals) throws DataAccessException {

    }

    @Override
    public List<User> getPrivileged(Car car) throws DataAccessException {
        return null;
    }

    @Override
    public void addPrivileged(Car car, List<User> users) throws DataAccessException {

    }

    @Override
    public void deletePrivileged(Car car, List<User> users) throws DataAccessException {

    }

    @Override
	public Car createCar(String name, String brand, String type,
			Address location, Integer seats, Integer doors, Integer year, boolean manual, boolean gps,
			boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue,
            Integer ownerAnnualKm, TechnicalCarDetails technicalCarDetails, CarInsurance insurance, User owner, String comments, boolean active)
			throws DataAccessException {
		Car car = new Car(idCounter++,name, brand, type, location, seats, doors, year, manual, gps, hook, fuel, fuelEconomy, estimatedValue, ownerAnnualKm, technicalCarDetails, insurance, owner, comments);
		car.setActive(active);
        cars.add(car);
		return car;
	}

	@Override
	public int getAmountOfCars(Filter filter) throws DataAccessException {
		return getCarList().size(); // TODO: implement Filter methods
	}

	public List<Car> getCarList() throws DataAccessException {
		return cars;
	}

	@Override
	public List<Car> getCarList(int page, int pageSize) throws DataAccessException {
		return cars.subList((page-1)*pageSize, page*pageSize > cars.size() ? cars.size() : page*pageSize );
	}

	@Override
	public List<Car> getCarList(FilterField orderBy, boolean asc, int page,	int pageSize, Filter filter) throws DataAccessException {
        return getCarList(page, pageSize); // TODO: implement Filter methods
	}

	@Override
	public List<Car> getCarsOfUser(int user_id) throws DataAccessException {
		List<Car> list = new ArrayList<>();
		for(Car car : cars){
			if(car.getOwner().getId()==user_id){
				list.add(car);
			}
		}
		return list;
	}

}
