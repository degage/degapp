/* PrivilegedDAO.java
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
import be.ugent.degage.db.models.UserHeader;

/**
 * Handles privileged status for cars
 */
public interface PrivilegedDAO {

    /**
     * List all users with privileges for a certain car
     */
    public Iterable<UserHeader> getPrivileged(int carId) throws DataAccessException;

    /**
     * Add new privileged users for the given car
     */
    public void addPrivileged(int carId, Iterable<Integer> userIds) throws DataAccessException;

    /**
     * Remove a number of privileges for the given car
     */
    public void deletePrivileged(int carId, Iterable<Integer> userIds) throws DataAccessException;

    /**
     * Is the given user privileged to use the given car (or owner of that car)
     */
    public boolean isOwnerOrPrivileged (int carId, int userId);

}
