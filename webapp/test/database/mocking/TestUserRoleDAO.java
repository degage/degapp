/* TestUserRoleDAO.java
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

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.UserDAO;
import be.ugent.degage.db.UserRoleDAO;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cedric on 3/8/14.
 */
public class TestUserRoleDAO implements UserRoleDAO {


    private Map<Integer, EnumSet<UserRole>> roles;
    private UserDAO userDao;

    public TestUserRoleDAO(UserDAO userDao) {
        roles = new HashMap<>();
        this.userDao = userDao;
    }

    @Override
    public EnumSet<UserRole> getUserRoles(int userId) throws DataAccessException {
        EnumSet<UserRole> r = roles.get(userId);
        if (r == null) {
            if (userDao.getUser(userId, false) != null) {
                return EnumSet.of(UserRole.USER);
            } else {
                return EnumSet.noneOf(UserRole.class);
            }
        } else {
            r = EnumSet.copyOf(r);
            r.add(UserRole.USER);
            return r;
        }
    }

    @Override
    public List<User> getUsersByRole(UserRole userRole) throws DataAccessException {
        return null;
    }

    @Override
    public void addUserRole(int userId, UserRole role) throws DataAccessException {
        EnumSet<UserRole> r = roles.get(userId);
        if (r == null) {
            roles.put(userId, EnumSet.of(role));
        } else {
            r.add(role);
        }
    }

    @Override
    public void removeUserRole(int userId, UserRole role) throws DataAccessException {
        EnumSet<UserRole> r = roles.get(userId);
        if (r == null) {
            throw new DataAccessException("User did not have this role");
        } else {
            r.remove(role);
        }
    }
}
