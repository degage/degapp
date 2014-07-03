package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.jdbc.JDBCFilter;
import be.ugent.degage.db.models.User;
import controllers.Security.RoleSecured;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;

import java.util.List;

public class UserPicker extends Controller {

    private static final int MAX_VISIBLE_RESULTS = 10;

    @RoleSecured.RoleAuthenticated()
    public static Result getList(String search) {
        search = search.trim();
        if (search != "") {
            search = search.replaceAll("\\s+", " ");
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                UserDAO dao = context.getUserDAO();
                String users = "";
                Filter filter = new JDBCFilter();
                filter.putValue(FilterField.USER_NAME, search);
                List<User> results = dao.getUserList(FilterField.USER_NAME, true, 1, MAX_VISIBLE_RESULTS, filter);
                for (User user : results) {
                    String value = user.getFirstName() + " " + user.getLastName();
                    for (String part : search.split(" ")) {
                        value = value.replaceAll("(?i)\\b(" + part + ")", "<#>$1</#>");
                    }

                    users += "<li data-uid=\"" + user.getId() + "\"><a href=\"javascript:void(0)\"><span>" + value.replace("#", "strong") + "</span> (" + user.getId() + ")</a></li>";
                }
                return ok(users);
            } catch (DataAccessException ex) {
                throw ex;//TODO?
            }
        } else {
            return ok();
        }
    }
}
