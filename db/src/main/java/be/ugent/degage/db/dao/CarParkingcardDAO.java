/* CarParkingcardDAO.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Emmanuel Isebaert
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

import java.util.Date;
import java.util.List;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.CarParkingcardExtended;
import be.ugent.degage.db.models.Page;

public interface CarParkingcardDAO {

	// public CarParkingcardExtended createCarParkingcard(String name, Date expiration, CarParkingcardType type, String contractNr, Car car) throws DataAccessException;
	// public void updateCarParkingcard(CarParkingcardExtended assistance) throws DataAccessException;
	// public void deleteCarParkingcard(CarParkingcardExtended assistance) throws DataAccessException;
	public Page<CarParkingcardExtended> getAllCarParkingcards(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
	// public void deleteAllCarParkingcards() throws DataAccessException;
}
