/* RefuelDAO.java
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

import java.time.LocalDate;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public interface RefuelDAO {

    public Refuel createRefuel(CarRide carRide) throws DataAccessException;
    public void acceptRefuel(int refuelId) throws DataAccessException;
    public void rejectRefuel(int refuelId) throws DataAccessException;
    public void deleteRefuel(int refuelId) throws DataAccessException;
    public Refuel getRefuel(int refuelId) throws DataAccessException;
    public void updateRefuel(Refuel refuel) throws DataAccessException;

    public Iterable<Refuel> getRefuels(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public int getAmountOfRefuels(Filter filter) throws DataAccessException;
    public Iterable<Refuel> getRefuelsForCarRide(int reservationId) throws DataAccessException;
    public Iterable<Refuel> getRefuelsForUser(int userId) throws DataAccessException;
    public Iterable<Refuel> getRefuelsForOwner(int userId) throws DataAccessException;
    public int getAmountOfRefuelsWithStatus(RefuelStatus status, int userId) throws DataAccessException;
    public void endPeriod() throws DataAccessException;
    public Iterable<Refuel> getBillRefuelsForLoaner(LocalDate date, int user) throws DataAccessException;


    /**
     * Return the total amount spent (in eurocent) for refuels for the given car billed at the given date
     * @return two integers, first is for privileged drivers, second is for others
     */
    public int[] eurocentsSpentOnFuel (LocalDate date, int carId) throws DataAccessException;


}
