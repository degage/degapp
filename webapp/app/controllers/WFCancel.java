/* WFCancel.java
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

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.*;
import com.google.common.base.Strings;
import controllers.util.WorkflowAction;
import controllers.util.WorkflowRole;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Result;
import views.html.workflow.cancelaccepted;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Controller that groups all cancelation actions in the reservation workflow
 */
public class WFCancel extends WFCommon {
    /**
     * Try to cancel a reservation.
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result cancelReservation(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        ReservationHeader reservation = dao.getReservationHeader(reservationId);

        if (WorkflowAction.CANCEL.isForbiddenForCurrentUser(reservation)) {
            flash("danger", "U kan deze reservatie niet (meer) annuleren");
            return redirectToDetails(reservationId);
        }

        // one special case: already accepted and in the past (and not owner or admin)
        if (reservation.getStatus() == ReservationStatus.ACCEPTED
                && reservation.getFrom().isAfter(LocalDateTime.now())
                && !WorkflowRole.OWNER.isCurrentRoleFor(reservation)
                && !WorkflowRole.ADMIN.isCurrentRoleFor(reservation)
                ) {
            flash("danger",
                    "Deze reservatie was reeds goedgekeurd! " +
                            "Je moet daarom verplicht een reden opgeven voor de annulatie");
            return redirect(routes.WFCancel.cancelAccepted(reservationId));
        } else {
            dao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED);
            return redirectToDetails(reservationId);
        }
    }

    /**
     * Show a form for cancelling a reservation which was already accepted
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result cancelAccepted(int reservationId) {
        Reservation reservation = DataAccess.getInjectedContext().getReservationDAO().getReservationExtended(reservationId);
        return ok(cancelaccepted.render(Form.form(CancelData.class), reservation));
    }

    /**
     * Process the results of {@link #cancelAccepted}
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result doCancelAccepted(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        Reservation reservation = dao.getReservation(reservationId);

        if (CurrentUser.isNot(reservation.getUserId()) ||
                reservation.getStatus() != ReservationStatus.ACCEPTED) {
            return badRequest(); // should not happen
        }

        Form<CancelData> form = Form.form(CancelData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(cancelaccepted.render(form, reservation));
        } else {
            String comments = form.get().remarks;
            dao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED, comments);
            Car car = context.getCarDAO().getCar(reservation.getCarId());
            reservation.setMessage(comments);
            Notifier.sendReservationCancelled(car.getOwner(), reservation, car.getName());
            return redirectToDetails(reservationId);
        }
    }

    public static class CancelData {
        public String remarks;

        public List<ValidationError> validate() {
            if (Strings.isNullOrEmpty(remarks)) { // TODO: isNullOrBlank
                return Collections.singletonList(
                        new ValidationError("remarks", "Je moet een reden opgeven voor de annulatie")
                );
            } else {
                return null;
            }
        }
    }
}
