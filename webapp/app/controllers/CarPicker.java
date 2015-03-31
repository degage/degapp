/* CarPicker.java
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

import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.jdbc.JDBCFilter;
import be.ugent.degage.db.models.Car;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;

public class CarPicker extends Controller {

    private static final int MAX_VISIBLE_RESULTS = 10;

    @AllowRoles
    @InjectContext
    public static Result getList(String search) {
        search = search.trim();
        if (!search.isEmpty()) {
            search = search.replaceAll("\\s+", " ");
            CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
            String cars = "";
            Filter filter = new JDBCFilter();
            filter.putValue(FilterField.CAR_NAME, search);
            Iterable<Car> results = dao.listCars(FilterField.CAR_NAME, true, 1, MAX_VISIBLE_RESULTS, filter, true);
            for (Car car : results) {
                String value = car.getName();
                for (String part : search.split(" ")) {
                    value = value.replaceAll("(?i)\\b(" + part + ")", "<#>$1</#>");
                }

                cars += "<li data-uid=\"" + car.getId() + "\"><a href=\"javascript:void(0)\"><span>" + value.replace("#", "strong") + "</span> (" + car.getId() + ")</a></li>";
            }
            return ok(cars);
        } else {
            return ok(); // TODO: avoid this case
        }
    }
}
