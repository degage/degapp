/* UserRoleTest.java
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

import be.ugent.degage.db.models.UserRole;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link UserRole} class.
 */
public class UserRoleTest {

    @Test
    public void conversion () {
        Set<UserRole> originalSet = EnumSet.of(UserRole.CAR_ADMIN, UserRole.INFOSESSION_ADMIN, UserRole.PROFILE_ADMIN);
        String string = UserRole.toString(originalSet);
        Set<UserRole> newSet = UserRole.fromString(string);
        assertEquals(originalSet, newSet);
        String newString = UserRole.toString(newSet);
        assertEquals(string, newString);
    }

    @Test
    public void conversionSimple () {
        assertEquals ("", UserRole.toString(EnumSet.noneOf(UserRole.class)));
        assertEquals ("0", UserRole.toString(EnumSet.of(UserRole.USER)));
    }
}
