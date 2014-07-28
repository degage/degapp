package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.dao.UserRoleDAO;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
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
        FilterField carField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        return ok(userList(page, pageSize, carField, asc, filter)); // TODO: inline userList
    }

    public static class UserWithRoles {
        public Iterable<UserRole> roleSet;
        public int id;
        public String fullName;
    }

    // called within an injected context
    private static Html userList(int page, int pageSize, FilterField orderBy, boolean asc, Filter filter) {
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO dao = context.getUserDAO();
        UserRoleDAO urdao = context.getUserRoleDAO();

        if (orderBy == null) {
            orderBy = FilterField.USER_NAME;
        }
        List<User> listOfUsers = dao.getUserList(orderBy, asc, page, pageSize, filter);

        List<UserWithRoles> list = new ArrayList<>();
        for (User user : listOfUsers) {
            UserWithRoles uwr = new UserWithRoles();
            uwr.roleSet =  urdao.getUserRoles(user.getId());
            uwr.id = user.getId();
            uwr.fullName = user.getFullName();
            list.add (uwr);
        }

        int amountOfResults = dao.getAmountOfUsers(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return userspage.render(list, page, amountOfResults, amountOfPages);
    }

    /**
     * Method: GET
     * Returns a form to edit a users roles
     *
     * @param userId
     * @return
     */
    @AllowRoles({UserRole.SUPER_USER})
    @InjectContext
    public static Result edit(int userId) {

        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO udao = context.getUserDAO();
        User user = udao.getUser(userId);
        if (user == null) {
            flash("danger", "GebruikersID " + userId + " bestaat niet.");
            return badRequest(overview.render());
        } else {
            UserRoleDAO dao = context.getUserRoleDAO();
            Set<UserRole> roles = dao.getUserRoles(userId);

            return ok(editroles.render(getUserRolesStatus(roles), user));
        }
    }

    /**
     * Creates a mapping between all roles and a boolean whether the rule is set in the provided role set
     *
     * @param assignedRoles The roles which the user has enabled
     * @return An array of all roles and their corresponding status (enabled | disabled)
     */
    @SuppressWarnings("unchecked")
    public static Tuple2<UserRole, Boolean>[] getUserRolesStatus(Set<UserRole> assignedRoles) {
        UserRole[] allRoles = UserRole.values();
        Tuple2<UserRole, Boolean>[] filtered = new Tuple2[allRoles.length - 1];
        int k = 0;
        for (UserRole allRole : allRoles) {
            if (allRole != UserRole.USER) { //TODO: review whole USER role, this thing is a hack and can be left out
                filtered[k++] = new Tuple2<>(allRole, assignedRoles.contains(allRole));
            }
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
        User user = udao.getUser(userId);
        if (user == null) {
            flash("danger", "GebruikersID " + userId + " bestaat niet.");
            return badRequest(overview.render());
        } else {
            UserRoleDAO dao = context.getUserRoleDAO();
            Set<UserRole> oldRoles = dao.getUserRoles(userId);

            Map<String, String[]> map = request().body().asFormUrlEncoded();
            String[] checkedVal = map.get("role"); // get selected topics

            // Parse the POST values whether they contain the roles (only checked get posted)
            Set<UserRole> newRoles = EnumSet.of(UserRole.USER);
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
            if (user.getId() == CurrentUser.getId() && removedRoles.contains(UserRole.SUPER_USER)) {
                flash("danger", "Als superuser kan je je eigen superuser rechten niet verwijderen.");
                return badRequest(editroles.render(getUserRolesStatus(oldRoles), user));
            } else {
                try {
                    for (UserRole removedRole : removedRoles) {
                        dao.removeUserRole(userId, removedRole);
                    }

                    for (UserRole addedRole : addedRoles) {
                        dao.addUserRole(userId, addedRole);
                    }

                    flash("success", "Er werden " + addedRoles.size() + " recht(en) toegevoegd en " + removedRoles.size() + " recht(en) verwijderd.");
                    return ok(editroles.render(getUserRolesStatus(newRoles), user));
                } catch (DataAccessException ex) {
                    context.rollback();
                    throw ex;
                }
            }
        }
    }

}
