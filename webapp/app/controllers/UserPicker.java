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
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package controllers;

import be.ugent.degage.db.models.UserHeaderShort;
import controllers.util.PickerLine;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;

public class UserPicker extends Controller {

    private static final int MAX_VISIBLE_RESULTS = 10;

    @AllowRoles
    @InjectContext
    public static Result getList(String search, String status) {
        if (search.isEmpty()) {
            return ok(); // normally does not occur
        } else {
            Collection<PickerLine> lines = new ArrayList<>();
            for (UserHeaderShort user : DataAccess.getInjectedContext().getUserDAO().listUserByName(search, Arrays.asList(status.split(",")), MAX_VISIBLE_RESULTS)) {
                lines.add (new PickerLine(user.getFullName(),search, user.getId()));
            }
            return ok(views.html.picker.pickerlines.render(lines));
        }
    }
}
