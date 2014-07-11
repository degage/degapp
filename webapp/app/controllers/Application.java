package controllers;

import controllers.routes.javascript;
import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

    // does not need context
    public static Result index() {
        return ok(index.render());
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
                        javascript.Cars.showCarsPage(),
                        javascript.Cars.showCarCostsPage(),
                        javascript.Cars.getCarCostModal(),
                        javascript.Cars.updateAvailabilities(),
                        javascript.Cars.updatePrivileged(),
                        javascript.Refuels.provideRefuelInfo(),
                        javascript.Refuels.showUserRefuelsPage(),
                        javascript.Refuels.showOwnerRefuelsPage(),
                        javascript.Refuels.showAllRefuelsPage(),
                        javascript.Damages.showDamagesPage(),
                        javascript.Damages.showDamagesPageOwner(),
                        javascript.Damages.showDamagesPageAdmin(),
                        javascript.Damages.editDamage(),
                        javascript.Damages.addStatus(),
                        javascript.Damages.addProof(),
                        javascript.InfoSessions.showUpcomingSessionsPage(),
                        javascript.InfoSessions.showSessionsPage(),
                        javascript.Reserve.showCarsPage(),
                        javascript.Users.showUsersPage(),
                        javascript.UserRoles.showUsersPage(),
                        javascript.EmailTemplates.showExistingTemplatesPage(),
                        javascript.EmailTemplates.editTemplate(),
                        javascript.Notifications.showNotificationsPage(),
                        javascript.Messages.showReceivedMessagesPage(),
                        javascript.Messages.showSentMessagesPage(),
                        javascript.InfoSessions.enrollSession(),
                        javascript.Receipts.showReceiptsPage(),
                        javascript.Drives.showDrivesPage(),
                        javascript.Drives.showDrivesAdminPage(),
                        javascript.Maps.getMap(),
                        javascript.InfoSessions.pendingApprovalListPaged(),
                        javascript.Reserve.reserve()
                )
        );
    }

}
