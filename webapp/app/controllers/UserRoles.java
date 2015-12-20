/* UserRoles.java
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
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.dao.UserRoleDAO;
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserHeader;
import be.ugent.degage.db.models.UserRole;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Tuple2;
import views.html.userroles.editroles;
import views.html.userroles.overview;
import views.html.userroles.userspage;

import java.util.*;

public class UserRoles extends Controller {

    /**
     * Method: GET
     * Shows a list of all users and their roles
     *
     * @return A table with all users and their userroles
     */
    @AllowRoles({UserRole.SUPER_USER})
    @InjectContext
    public static Result index() {
        return ok(overview.render());
    }

    public static class UserWithRoles {
        public Iterable<UserRole> roleSet;
        public int id;
        public String fullName;
    }

    /**
     * @param page         The page in the userlists
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of users of the corresponding page
     */
    @AllowRoles
    @InjectContext
    public static Result showUsersPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField carField = FilterField.stringToField(orderBy, FilterField.USER_NAME);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO dao = context.getUserDAO();
        UserRoleDAO urdao = context.getUserRoleDAO();

        // TODO: obtain this information with a single database call...
        Page<User> listOfUsers = dao.getUserList(carField, asc, page, pageSize, filter);

        List<UserWithRoles> list = new ArrayList<>();
        for (User user : listOfUsers) {
            UserWithRoles uwr = new UserWithRoles();
            uwr.roleSet =  urdao.getUserRoles(user.getId());
            uwr.id = user.getId();
            uwr.fullName = user.getFullName();
            list.add (uwr);
        }
        return ok(userspage.render(new Page<>(list,pageSize,listOfUsers.getFullSize())));
    }

    /**
     * Method: GET
     * Returns a form to edit a users roles
     */
    @AllowRoles({UserRole.SUPER_USER})
    @InjectContext
    public static Result edit(int userId) {

        DataAccessContext context = DataAccess.getInjectedContext();
        UserHeader user = context.getUserDAO().getUserHeader(userId);
        return ok(editroles.render(
                getEditableRolesStatus(context.getUserRoleDAO().getUserRoles(userId)),
                user)
        );
    }

    // Editable roles
    public static final Set<UserRole> EDITABLE_ROLES = EnumSet.of(
            UserRole.CAR_OWNER, UserRole.INFOSESSION_ADMIN, UserRole.CONTRACT_ADMIN, UserRole.PROFILE_ADMIN,
            UserRole.RESERVATION_ADMIN, UserRole.CAR_ADMIN, UserRole.SUPER_USER
    );

    /**
     * Creates a mapping between all roles and a boolean whether the rule is set in the provided role set
     *
     * @param assignedRoles The roles which the user has enabled
     * @return An array of all roles and their corresponding status (enabled | disabled)
     */
    @SuppressWarnings("unchecked")
    public static Tuple2<UserRole, Boolean>[] getEditableRolesStatus(Set<UserRole> assignedRoles) {
        Tuple2<UserRole, Boolean>[] filtered = new Tuple2[EDITABLE_ROLES.size()];
        int k = 0;
        for (UserRole role : EDITABLE_ROLES) {
            filtered[k++] = new Tuple2<>(role, assignedRoles.contains(role));
        }
        return filtered;
    }


    /**
     * Method: POST
     * Finilizes a userrole edit submission form and saves the new roles to the database
     *
     * @param userId The user ID which has been edited
     * @return The user edit page with the newly assigned roles if successfull, error message otherwise
     */
    @SuppressWarnings("unchecked")
    @AllowRoles({UserRole.SUPER_USER})
    @InjectContext
    public static Result editPost(int userId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO udao = context.getUserDAO();
        UserHeader user = udao.getUserHeader(userId);
        UserRoleDAO dao = context.getUserRoleDAO();
        Set<UserRole> oldRoles = dao.getUserRoles(userId);

        Map<String, String[]> map = request().body().asFormUrlEncoded();
        String[] checkedVal = map.get("role"); // get selected topics

        // Parse the POST values whether they contain the roles (only checked get posted)
        Set<UserRole> newRoles = EnumSet.of(UserRole.CAR_USER);
        if (checkedVal != null) {
            for (String strRole : checkedVal) {
                newRoles.add(Enum.valueOf(UserRole.class, strRole));
            }
        }

        // Get all newly assigned roles
        Set<UserRole> addedRoles = EnumSet.copyOf(newRoles);
        addedRoles.removeAll(oldRoles);

        // Get all removed roles
        Set<UserRole> removedRoles = EnumSet.copyOf(oldRoles);
        removedRoles.removeAll(newRoles);

        // Check if a superuser did delete his role by accident (SU roles can only be removed by other SU users)
        if (CurrentUser.is(user.getId()) && removedRoles.contains(UserRole.SUPER_USER)) {
            flash("danger", "Als superuser kan je je eigen superuser rechten niet verwijderen.");
            return badRequest(editroles.render(getEditableRolesStatus(oldRoles), user));
        } else {
            try {
                for (UserRole removedRole : removedRoles) {
                    dao.removeUserRole(userId, removedRole);
                }

                for (UserRole addedRole : addedRoles) {
                    dao.addUserRole(userId, addedRole);
                }

                flash("success", "Er werden " + addedRoles.size() + " recht(en) toegevoegd en " + removedRoles.size() + " recht(en) verwijderd.");
                return ok(editroles.render(getEditableRolesStatus(newRoles), user));
            } catch (DataAccessException ex) {
                context.rollback();
                throw ex;
            }
        }
    }

}
