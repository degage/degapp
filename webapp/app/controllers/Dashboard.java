package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.User;
import controllers.Security.RoleSecured;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.dashboard;

import java.util.List;

public class Dashboard extends Controller {

    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result index() {
        User currentUser = DataProvider.getUserProvider().getUser();
        ReservationDAO dao = DataAccess.getInjectedContext().getReservationDAO();
        List<Reservation> reservations = dao.getReservationListForUser(currentUser.getId());
        return ok (
            dashboard.render(currentUser, reservations, Form.form(Reserve.IndexModel.class),
                Profile.getProfileCompleteness(currentUser), InfoSessions.didUserGoToInfoSession(),
                InfoSessions.approvalRequestSent())
            );
    }

}
