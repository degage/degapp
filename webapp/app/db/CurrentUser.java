/* CurrentUser.java
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

package db;

import be.ugent.degage.db.models.UserHeader;
import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.models.UserStatus;
import play.mvc.Controller;

import java.util.Set;

/**
 * Manages information about the current user, as stored in the HTTP session
 */
public class CurrentUser {

    /**
     * Is there a valid user?
     */
    public static boolean isValid () {
        return Controller.session().get("id") != null;
    }

    /**
     * Register the given user as the current user. (As part of logging in or switching to another user.)
     * @param user partially filled in user object
     */
    public static void set (UserHeader user, Set<UserRole> roleSet) {
        Controller.session("id", Integer.toString(user.getId()));
        Controller.session("email", user.getEmail());
        Controller.session("fullName", user.getFullName());

        Controller.session("roles", UserRole.toString(roleSet));
        Controller.session("status", user.getStatus().name());
    }

    /**
     * Clear the current user. (As part of logging out.)
     */
    public static void clear () {
        Controller.session().clear();
    }

    /**
     * Retrieve the id of the current user, or null if there is no current user.
     *
     * Important, do not compare this result with ==, use {@link #is} or {2link #isNot} instead
     */
    public static Integer getId () {
        String idString = Controller.session("id");
        return idString == null ? null : Integer.parseInt(idString);
    }

    /**
     * Check whether the given user correspond to the current user
     */
    public static boolean is(int userId) {
        String idString = Controller.session("id");
        return idString != null && Integer.parseInt(idString) == userId;
    }

    /**
     * Short for <code>! is(userId)</code>
     */
    public static boolean isNot(int userId) {
        String idString = Controller.session("id");
        return idString == null || Integer.parseInt(idString) != userId;
    }

    /**
     * Retrieve the full name of the current user
     */
    public static String getFullName () {
        return Controller.session("fullName");
    }

    /**
     * Check whether the current user has status 'FULL'.
     */
    public static boolean hasFullStatus() {
        return Controller.session("status").equals(UserStatus.FULL.name());
    }


    /**
     * Retrieve the roles of the current user
     */
    private static Set<UserRole> getRoles () {
        return UserRole.fromString(Controller.session("roles"));
    }

    /**
     * Does the current user have the indicated role, or is the current user a super user? (Super users automatically
     * acquire all roles.)
     */
    public static boolean hasRole (UserRole role) {
        Set<UserRole> roleSet = getRoles();
        return roleSet.contains (UserRole.SUPER_USER) ||
                roleSet.contains (role);
    }

    /**
     * Does the current user have one of the indicated roles (or is he a super user)?
     */
    public static boolean hasSomeRole (UserRole role1, UserRole role2) {
        Set<UserRole> roleSet = getRoles();
        return roleSet.contains (UserRole.SUPER_USER) ||
                roleSet.contains (role1) ||
                roleSet.contains (role2);
    }

    /**
     * Does the current user have one of the administrative roles?
     */
    public static boolean isAdmin () {
        Set<UserRole> roleSet = getRoles();
        return roleSet.contains (UserRole.SUPER_USER) ||
                roleSet.contains (UserRole.CAR_ADMIN) ||
                roleSet.contains (UserRole.INFOSESSION_ADMIN) ||
                roleSet.contains (UserRole.MAIL_ADMIN) ||
                roleSet.contains (UserRole.RESERVATION_ADMIN);
    }

}
