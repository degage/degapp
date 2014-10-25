package controllers;

import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.User;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.dashboard;

import java.util.List;

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
