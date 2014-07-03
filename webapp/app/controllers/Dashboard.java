package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.User;
import controllers.Security.RoleSecured;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.dashboard;

import java.util.List;

public class Dashboard extends Controller {

    @RoleSecured.RoleAuthenticated()
    public static Result index() {
        return ok(dashboard());
    }

    public static Html dashboard() {
        User currentUser = DataProvider.getUserProvider().getUser();
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO dao = context.getReservationDAO();
            List<Reservation> reservations = dao.getReservationListForUser(currentUser.getId());
            return dashboard.render(currentUser, reservations, Form.form(Reserve.IndexModel.class),
                    Profile.getProfileCompleteness(currentUser), InfoSessions.didUserGoToInfoSession(),
                    InfoSessions.approvalRequestSent());
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

}
