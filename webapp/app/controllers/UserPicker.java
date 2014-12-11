package controllers;

import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.jdbc.JDBCFilter;
import be.ugent.degage.db.models.User;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class UserPicker extends Controller {

    // TODO: code in common with CarPicker

    private static final int MAX_VISIBLE_RESULTS = 10;

    @AllowRoles
    @InjectContext
    public static Result getList(String search) {
        search = search.trim();
        if (!search.isEmpty()) {
            search = search.replaceAll("\\s+", " ");
            UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
            String users = "";
            Filter filter = new JDBCFilter();
            filter.putValue(FilterField.USER_NAME, search);
            List<User> results = dao.getUserList(FilterField.USER_NAME, true, 1, MAX_VISIBLE_RESULTS, filter);
            for (User user : results) {
                String value = user.getFullName();
                for (String part : search.split(" ")) {
                    value = value.replaceAll("(?i)\\b(" + part + ")", "<#>$1</#>");
                }

                users += "<li data-uid=\"" + user.getId() + "\"><a href=\"javascript:void(0)\"><span>" + value.replace("#", "strong") + "</span> (" + user.getId() + ")</a></li>";
            }
            return ok(users);
        } else {
            return ok(); // TODO: avoid this case
        }
    }
}
