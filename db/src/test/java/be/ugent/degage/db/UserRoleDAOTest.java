/* UserRoleDAOTest.java
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

package be.ugent.degage.db;

import be.ugent.degage.db.dao.UserRoleDAO;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.*;
/**
 * Test for {@link UserRoleDAO}
 */
public class UserRoleDAOTest extends DAOTest {

    private UserRoleDAO dao;

    private User[] users;

    @Before
    public void getDAOAndFixtures () {
        dao = context.getUserRoleDAO();
        users = Fixtures.createUsersPartial(context);
        Fixtures.createUserRoles(context, users);
    }

    @Test
    public void getUserRolesTest () {
        Set<UserRole> expected = EnumSet.of(
                UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.CAR_ADMIN, UserRole.USER
        );
        assertEquals (expected, dao.getUserRoles(users[2].getId()));
    }

    @Test
    public void getUsersByRoleTest () {
        Iterable<User> list = dao.getUsersByRole(UserRole.CAR_ADMIN);
        assertEquals(4, Iterables.size(list)); // includes default super user!

        assertNotNull(Iterables.tryFind(list, x-> x.getId() == users[0].getId()));
        assertNotNull(Iterables.tryFind(list, x-> x.getId() == users[3].getId()));
    }

    @Test
    public void addAndRemoveTest () {
        dao.addUserRole(users[1].getId(), UserRole.MAIL_ADMIN);
        assertTrue(dao.getUserRoles(users[1].getId()).contains(UserRole.MAIL_ADMIN));
        dao.removeUserRole(users[1].getId(), UserRole.MAIL_ADMIN);
        assertFalse(dao.getUserRoles(users[1].getId()).contains(UserRole.MAIL_ADMIN));
    }

    @Test
    public void addTwiceAndRemoveOnceTest () {
        dao.addUserRole(users[3].getId(), UserRole.MAIL_ADMIN);
        dao.addUserRole(users[3].getId(), UserRole.MAIL_ADMIN);
        assertTrue(dao.getUserRoles(users[3].getId()).contains(UserRole.MAIL_ADMIN));
        dao.removeUserRole(users[3].getId(), UserRole.MAIL_ADMIN);
        assertFalse(dao.getUserRoles(users[3].getId()).contains(UserRole.MAIL_ADMIN));
    }

}
