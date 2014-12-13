/* Dashboard.java
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

import be.ugent.degage.db.models.User;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.dashboard;

public class Dashboard extends Controller {

    @AllowRoles
    @InjectContext
    public static Result index() {
        User currentUser = DataProvider.getUserProvider().getUser();
        return ok(
                dashboard.render(
                        currentUser,
                        DataAccess.getInjectedContext().getReservationDAO().getReservationListForUser(currentUser.getId()),
                        Form.form(Reserve.IndexModel.class),
                        Profile.getProfileCompleteness(currentUser),
                        InfoSessions.didUserGoToInfoSession(),
                        DataAccess.getInjectedContext().getApprovalDAO().hasApprovalPending(currentUser.getId())
                )
        );
    }

}
