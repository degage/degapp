/* CarDAO.java
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

package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

/**
 *
 */
public interface CarDAO {

    public Car createCar(String name, String email, String brand, String type, Address location, Integer seats, Integer doors, Integer year, boolean manual,
                         boolean gps, boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm,
                         TechnicalCarDetails technicalCarDetails, CarInsurance insurance, UserHeader owner, String comments, boolean active) throws DataAccessException;
    public void updateCar(Car car) throws DataAccessException;

    public int getCarPicture (int carId);

    public void updateCarPicture (int carId, int fileId);

    public CarHeaderShort getCarHeaderShort(int carId) throws DataAccessException;

    public Car getCar(int id) throws DataAccessException;

    /**
     * Returns a filtered list of cars. Only active cars are shown. Always contains a location. Owner is
     * left null.
     * @param uid User whose preferences will be used in sorting the cars
     */
    public Page<CarHeaderLong> listActiveCars(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter, int uid) throws DataAccessException;

    /**
     * Returns a list of all active cars. Similar to {@link #listActiveCars} but
     * unfiltered and without pagination
     */
    public Iterable<CarHeaderLong> listAllActiveCars(int uid);

    /**
     * Return a filtered list of cars. Does not contain location information.
     * @param onlyActive Only include cars that are active
     */
    public Iterable<CarHeaderAndOwner> listCarsAndOwners(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter, boolean onlyActive) throws DataAccessException;

    /**
     * Number of cars that can be returned by an equivalent call to {@link #listCarsAndOwners}
     */
    public int countCars(Filter filter) throws DataAccessException;

    /**
     * Lists all cars of the given user, also those that are not active.
     * Does not contain location information
     */
    public Iterable<CarHeader> listCarsOfUser (int userId)  throws DataAccessException;

    public boolean isCarOfUser (int carId, int userId) throws DataAccessException;

    public UserHeader getOwnerOfCar(int carId)  throws DataAccessException;

    public CarDepreciation getDepreciation(int carId) throws DataAccessException;

    public void updateDepreciation (int carId, int cents, int limit, int last) throws DataAccessException;



    /**
     * Return a car list for cars with name containing the given string. Used in pickers.
     */
    public Iterable<CarHeaderShort> listCarByName(String str, int limit);

}
