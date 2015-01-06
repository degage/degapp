/* Application.java
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
import be.ugent.degage.db.models.UserRole;
import controllers.routes.javascript;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.dashboardFullUser;
import views.html.dashboardRegistered;

public class Application extends Controller {

    /**
     * Redirects to a specific page depending on user status (logged in? full user? ...)
     */
    @InjectContext
    public static Result index() {

        if (!CurrentUser.isValid()) {
            // login page if not logged in
            return redirect(routes.Login.login(null));
        } else {
            User currentUser = DataProvider.getUserProvider().getUser();
            int completeness = Profile.getProfileCompleteness(currentUser);
            if (CurrentUser.hasFullStatus() || CurrentUser.hasRole(UserRole.SUPER_USER)) {
                // normal dashboard if user has full status
                return ok(
                    dashboardFullUser.render(
                        currentUser,
                        completeness
                    )
                );

            } else {
                // reduced dashboard
                return ok(
                    dashboardRegistered.render(
                        currentUser,
                        completeness,
                        InfoSessions.didUserGoToInfoSession(),
                        DataAccess.getInjectedContext().getApprovalDAO().hasApprovalPending(currentUser.getId())
                    )
                );
            }
        }
    }

    /**
     * Javascript controllers.routes allowing the calling of actions on the server from
     * Javascript as if they were invoked directly in the script.
     */
    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("myJsRoutes",
                        // Routes
                        javascript.Cars.getCarCostModal(),
                        javascript.Cars.updatePrivileged(),
                        javascript.Refuels.provideRefuelInfo(),
                        javascript.Damages.editDamage(),
                        javascript.Damages.addStatus(),
                        javascript.Damages.addProof(),
                        javascript.EmailTemplates.editTemplate(),
                        javascript.InfoSessions.enrollSession(),
                        javascript.Maps.getMap()
                )
        );
    }

    public static Result paginationRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("paginationJsRoutes",
                        // Routes
                        javascript.Approvals.pendingApprovalListPaged(),
                        javascript.Cars.showCarCostsPage(),
                        javascript.Cars.showCarsPage(),
                        javascript.Damages.showDamagesPage(),
                        javascript.Damages.showDamagesPageOwner(),
                        javascript.Damages.showDamagesPageAdmin(),
                        javascript.Drives.showDrivesPage(),
                        javascript.Drives.showDrivesAdminPage(),
                        javascript.EmailTemplates.showExistingTemplatesPage(),
                        javascript.Messages.showReceivedMessagesPage(),
                        javascript.Messages.showSentMessagesPage(),
                        javascript.Notifications.showNotificationsPage(),
                        javascript.Receipts.showReceiptsPage(),
                        javascript.Refuels.showUserRefuelsPage(),
                        javascript.Refuels.showOwnerRefuelsPage(),
                        javascript.Refuels.showAllRefuelsPage(),
                        javascript.Reserve.listAvailableCarsPage(),
                        javascript.UserRoles.showUsersPage(),
                        javascript.Users.showUsersPage()
                )
        );
    }
}
