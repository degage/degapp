/* WFApprove.java
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
import controllers.util.WorkflowAction;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.mvc.Result;
import views.html.workflow.aortripinfo;
import views.html.workflow.approveonly;
import views.html.workflow.approveorreject;
import views.html.workflow.reminder;

import java.time.LocalDateTime;

/**
 * Controller for approval and rejection of reservations / trip data
 */
public class WFApprove extends WFCommon {
    /**
     * Show the page that allows approval or rejection of a reservation by the owner
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result approveReservation(int reservationId) {
        Reservation reservation = DataAccess.getInjectedContext().getReservationDAO().getReservationExtended(reservationId);
        // special case: already cancelled by user
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            flash("warning", "De reservatie is reeds geannulleerd");
            return redirectToDetails(reservationId);
        }

        if (WorkflowAction.AOR_RESERVATION.isForbiddenForCurrentUser(reservation)) {
            flash("warning", "De reservatie kan niet (meer) worden goed- of afgekeurd");
            return redirectToDetails(reservationId);
        } else if (reservation.getFrom().isBefore(LocalDateTime.now())) {
            // a reservation in the past can only be accepted
            return ok(approveonly.render(reservation));
        } else {
            // can be accepted or rejected
            return ok(approveorreject.render(Form.form(RemarksData.class), reservation));
        }
    }

    /**
     * Method: POST
     * <p>
     * Called when a reservation of a car is refused/accepted by the owner.
     *
     * @param reservationId the id of the reservation being refused/accepted
     * @return the trips index page
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result doApproveReservation(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        Reservation reservation = dao.getReservationExtended(reservationId);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            flash("warning", "De reservatie is (ondertussen) reeds geannuleerd");
            return redirectToDetails(reservationId);
        }
        if (WorkflowAction.AOR_RESERVATION.isForbiddenForCurrentUser(reservation)) {
            return badRequest(); // should not happen
        }

        Form<RemarksData> form = Form.form(RemarksData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(approveorreject.render(form, reservation));
        } else {
            RemarksData data = form.get(); // the form does not contain errors
            ReservationStatus status = ReservationStatus.valueOf(data.status);
            String remarks = data.remarks;

            if (status == ReservationStatus.REFUSED || status == ReservationStatus.ACCEPTED) {
                // authorization already checked
                if (status == ReservationStatus.REFUSED) {
                    dao.updateReservationStatus(reservationId, status, remarks);
                    Notifier.sendReservationRefusedByOwnerMail(remarks, reservation);
                } else {
                    dao.updateReservationStatus(reservationId, status);
                    UserHeader owner = context.getUserDAO().getUserHeader(reservation.getOwnerId());
                    Notifier.sendReservationApprovedByOwnerMail(owner, remarks, reservation);
                }
                return redirectToDetails(reservationId);
            } else {
                return badRequest(); // should not happen
            }
        }
    }

    /**
     * Approve a reservation which was already in the past. Changes it status immediately to
     * REQUEST_DETAILS
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result doApproveOldReservation(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        Reservation reservation = dao.getReservationExtended(reservationId);
        if (WorkflowAction.AOR_RESERVATION.isForbiddenForCurrentUser(reservation)) {
            return badRequest(); // should not happen
        }
        dao.updateReservationStatus(reservationId, ReservationStatus.REQUEST_DETAILS);
        Notifier.sendOldReservationApproved(reservation);
        return redirectToDetails(reservationId);
    }


    /**
     * Show page which allows a driver to send a reminder that a reservation has
     * not been approved
     */
    @AllowRoles
    @InjectContext
    public static Result sendReminder(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        Reservation reservation = dao.getReservation(reservationId);
        if (WorkflowAction.SEND_REMINDER.isForbiddenForCurrentUser(reservation)) {
            flash ("danger", "Je kan geen herinnering sturen voor deze reservatie");
            return redirectToDetails(reservationId);
        }
        return ok(reminder.render(reservation));
    }

    /**
     * Send a mail with a reminder to approve a certain reservation.
     */
    @AllowRoles
    @InjectContext
    public static Result doSendReminder(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        Reservation reservation = dao.getReservation(reservationId);
        if (WorkflowAction.SEND_REMINDER.isForbiddenForCurrentUser(reservation)) {
            return badRequest();
        }
        UserHeader owner = context.getUserDAO().getUserHeader(reservation.getOwnerId());
        Notifier.sendRemindOwner(owner, reservation, reservation.getCar().getName());
        flash("warning", "De herinneringse-mail wordt zo snel mogelijk verstuurd");
        return redirectToDetails(reservationId);
    }


    /**
     * Show form where trip info can be approved or rejected.
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result approveTripInfo(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Reservation reservation = context.getReservationDAO().getReservation(reservationId);
        if (WorkflowAction.AOR_TRIP.isForbiddenForCurrentUser(reservation)) {
            flash("danger", "Deze trip kan niet (meer) goed- of afgekeurd worden");
            redirectToDetails(reservationId);
        }
        return ok(aortripinfo.render(
                Form.form(RemarksData.class),
                reservation,
                context.getCarRideDAO().getCarRide(reservationId)
        ));
    }

    /**
     * Process the results of {@link #approveTripInfo(int)}
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result doApproveTripInfo(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        Reservation reservation = rdao.getReservation(reservationId);

        if (WorkflowAction.AOR_TRIP.isForbiddenForCurrentUser(reservation)) {
            return badRequest(); // should not happen
        }
        CarRide ride = context.getCarRideDAO().getCarRide(reservationId);
        Form<RemarksData> form = Form.form(RemarksData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(aortripinfo.render( form, reservation, ride ));
        } else {
            RemarksData data = form.get();
            if ("REFUSED".equals(data.status)) {
                rdao.updateReservationStatus(reservationId, ReservationStatus.DETAILS_REJECTED);
                Notifier.sendDetailsRejected(reservation, ride, data.remarks);
                flash("success", "Uw opmerking wordt gemaild naar de bestuurder.");
            } else {
                rdao.updateReservationStatus(reservationId, ReservationStatus.FINISHED);
                flash("success", "De ritgegevens werden goedgekeurd.");
            }
            return redirectToDetails(reservationId);
        }
    }
}
