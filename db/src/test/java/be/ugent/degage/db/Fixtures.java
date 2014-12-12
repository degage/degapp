/* Fixtures.java
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

import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.dao.SettingDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.dao.UserRoleDAO;
import be.ugent.degage.db.models.Address;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;

import java.time.Instant;

/**
 * Provides data for several of the tests.
 */
public final class Fixtures {

    public static Address[] createAddresses(DataAccessContext context) {
        AddressDAO dao = context.getAddressDAO();

        return new Address[]{
                dao.createAddress("België", "9000", "Gent", "Krijgslaan", "281", "S9"),
                dao.createAddress("België", "8500", "Kortrijk", "Lange steenstraat", "11", null)
        };
    }

    public static void createSettings(DataAccessContext context) {
        SettingDAO dao = context.getSettingDAO();

        dao.createSettingAfterDate("setting_1", "value_1", Instant.now().minusSeconds(1));
        dao.createSettingAfterDate("setting_2", "value_new", Instant.now().plusSeconds(60));
        dao.createSettingAfterDate("setting_2", "value_old", Instant.now().minusSeconds(60));
        dao.createSettingAfterDate("setting_2", "value_recent", Instant.now().minusSeconds(20));

    }

    public static void createCostSettings(DataAccessContext context) {
        SettingDAO dao = context.getSettingDAO();

        for (int i = 0; i < 4; i++) {
            dao.createSettingAfterDate("cost_" + i, "0.2" + i, Instant.now().minusSeconds(1));
            dao.createSettingAfterDate("cost_" + i, "0.3" + i, Instant.now().plusSeconds(20));
            dao.createSettingAfterDate("cost_" + i, "0.4" + i, Instant.now().plusSeconds(60));
        }

        for (int i = 0; i < 3; i++) {
            dao.createSettingAfterDate("cost_limit_" + i, "12" + i, Instant.now().minusSeconds(1));
            dao.createSettingAfterDate("cost_limit_" + i, "13" + i, Instant.now().plusSeconds(20));
            dao.createSettingAfterDate("cost_limit_" + i, "14" + i, Instant.now().plusSeconds(60));
        }

        dao.createSettingAfterDate("deprecation_cost", "0.75", Instant.now().minusSeconds(1));
        dao.createSettingAfterDate("deprecation_cost", "0.85", Instant.now().plusSeconds(20));
        dao.createSettingAfterDate("deprecation_cost", "0.95", Instant.now().plusSeconds(60));
    }

    public static User[] createUsersPartial (DataAccessContext context) {

        UserDAO dao = context.getUserDAO();

        return new User[] {
                dao.createUser("admin@gmail.com", "adminpass", "Sam", "Vimes"),
                dao.createUser("normal@gmail.com", "normalpass", "Nobby", "Nobbs"),
                dao.createUser ("user1@gmail.com", "user1pass", "John", "Jones"),
                dao.createUser ("user2@gmail.com", "user2pass", "Jane", "Doe")
        };

    }

    public static void createUserRoles (DataAccessContext context, User[] users) {
        UserRoleDAO dao = context.getUserRoleDAO();

        dao.addUserRole(users[0].getId(), UserRole.SUPER_USER);
        dao.addUserRole(users[2].getId(), UserRole.CAR_ADMIN);
        dao.addUserRole(users[2].getId(), UserRole.CAR_OWNER);
        dao.addUserRole(users[3].getId(), UserRole.CAR_ADMIN);
        for (User user : users) {
            dao.addUserRole (user.getId(), UserRole.CAR_USER);
        }
    }

}
