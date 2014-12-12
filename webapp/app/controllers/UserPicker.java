/* UserPicker.java
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
