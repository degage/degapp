/* UserRoleDAO.java
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

import java.util.Set;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.UserHeader;
import be.ugent.degage.db.models.UserRole;

/**
 * Data access object for user roles.
 */
public interface UserRoleDAO {

    /**
     * Return the set of roles for a given user. (Every registered user gets role 'USER' by default.)
     */
	public Set<UserRole> getUserRoles(int userId) throws DataAccessException;

    /**
     * All users that have a certain role, including all super users.
     */
    public Iterable<UserHeader> getUsersByRole(UserRole userRole) throws DataAccessException;

    /**
     * Add a role to a user (unless that user already has that role)
     */
	public void addUserRole(int userId, UserRole role) throws DataAccessException;

    /**
     * Remove a role from a user (unless the user did not have that role)
     */
	public void removeUserRole(int userId, UserRole role) throws DataAccessException;
}
