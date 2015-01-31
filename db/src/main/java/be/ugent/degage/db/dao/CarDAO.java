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
                         TechnicalCarDetails technicalCarDetails, CarInsurance insurance, UserHeader owner, String comments, boolean active, int photoId) throws DataAccessException;
    public void updateCar(Car car) throws DataAccessException;
    public Car getCar(int id) throws DataAccessException;

    /**
     * Return a list of all cars, with full information (in order to export it to a spread sheet)
     */
    public Iterable<Car> listAllCars () throws DataAccessException;


    /**
     * Returns a filtered list of cars. Only <i>active</i> cars are shown.
     */
    public Iterable<Car> getCarList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    /**
     * Number of cars that can be returned by equivalent call to {@link #getCarList}
     */
    public int getAmountOfCars(Filter filter) throws DataAccessException;

    // newer version - uses shorter SQL
    public Iterable<Car> listCars (FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public int countCars(Filter filter) throws DataAccessException;

    public Iterable<Car> listCarsOfUser (int userId)  throws DataAccessException;

    public boolean isCarOfUser (int carId, int userId) throws DataAccessException;

}
