/* DamageDAO.java
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
import be.ugent.degage.db.models.Damage;
import be.ugent.degage.db.models.ReservationHeader;

import java.time.LocalDate;

/**
 * Created by Stefaan Vermassen on 02/05/14.
 */
public interface DamageDAO {

    public void createDamage(ReservationHeader reservation) throws DataAccessException;

    public Damage getDamage(int damageId) throws DataAccessException;

    public void updateDamageFinished(int damageId, boolean finished) throws DataAccessException;
    public void updateDamageDetails (int damageId, String description, LocalDate date) throws DataAccessException;

    public int getAmountOfOpenDamages(int userId) throws DataAccessException;

    /**
     * Retrieve a list of damages for the given driver
     */
    public Iterable<Damage> listDamagesForDriver (int driverId) throws DataAccessException;

    /**
     * Retrieve a list of damages for the given owner
     */
    public Iterable<Damage> listDamagesForOwner (int driverId) throws DataAccessException;

    /**
     * Retrieve a filtered list of damages
     */
    public Iterable<Damage> getDamages(int page, int pageSize, Filter filter) throws DataAccessException;

    /**
     * Size of the filtered list as produced by {@link #getDamages}.
     */
    public int getAmountOfDamages(Filter filter) throws DataAccessException;


}

