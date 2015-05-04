/* CarCostDAO.java
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
import be.ugent.degage.db.models.CarCost;
import be.ugent.degage.db.models.CarCostCategory;

import java.time.LocalDate;

/**
 * Data access object for manipulating car costs
 */
public interface CarCostDAO {

    public void createCarCost(int carId, String carName, int amount, int km, String description, LocalDate date, int fileId, int categoryId) throws DataAccessException;

    public int getAmountOfCarCosts(Filter filter) throws DataAccessException;

    public Iterable<CarCost> getCarCostList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    public void updateCarCost(CarCost carCost) throws DataAccessException;

    public CarCost getCarCost(int id) throws DataAccessException;

    //public List<CarCost> getBillCarCosts(LocalDate date, int car) throws DataAccessException;

    /**
     * List all costs for a given car
     */
    public Iterable<CarCost> listCostsOfCar (int carId)   throws DataAccessException;

    /**
     * Return cost category information for the given id
     */
    public CarCostCategory getCategory (int id);

    /**
     * Return a list of all car cost categories
     */
    public Iterable<CarCostCategory> listCategories ();

}
