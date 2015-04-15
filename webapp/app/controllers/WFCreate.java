/* WFCreate.java
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
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Result;
import views.html.workflow.create;
import views.html.workflow.createold;
import views.html.workflow.shorten;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller for creation and shortening of reservations
 */
public class WFCreate extends WFCommon {

    /**
     * Show the page to create a reservation
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result create(int carId, String fromString, String untilString) {
        LocalDateTime fromDateTime = Utils.toLocalDateTime(fromString);
        return ok(create.render(
                // query string binders are quite complicated to write :-(
                new Form<>(ReservationData.class).fill(
                        new ReservationData().populate(
                                fromDateTime,
                                untilString.isEmpty() ? fromDateTime : Utils.toLocalDateTime(untilString)
                        )
                ),
                DataAccess.getInjectedContext().getCarDAO().getCar(carId)
        ));
    }

    /**
     * Process the reservation made in {@link #create}
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result doCreate(int carId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Car car = context.getCarDAO().getCar(carId);
        Form<ReservationData> form = new Form<>(ReservationData.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(create.render(form, car));
        }

        ReservationData data = form.get();
        LocalDateTime from = data.from;
        LocalDateTime until = data.until;

        if (from.isBefore(LocalDateTime.now()) && CurrentUser.isNot(car.getOwner().getId())) {
            form.reject("from",
                    "Een reservatie uit het verleden kan enkel door de eigenaar worden ingebracht");
            return ok(create.render(form, car));
        }

        // Test whether the reservation is valid
        ReservationDAO rdao = context.getReservationDAO();
        if (rdao.hasOverlap(carId, from, until)) {
            String errorMessage = "De reservatie overlapt met een bestaande reservatie";
            form.reject("from", errorMessage);
            form.reject("until", errorMessage);
            return ok(create.render(form, car));
        }

        ReservationHeader reservation = rdao.createReservation(from, until, carId, CurrentUser.getId());
        if (reservation.getStatus() == ReservationStatus.REQUEST) {
            // No mails need to be sent when ACCEPTED or REQUEST DETAILS

            // note: user contained in this record was null
            // TODO: avoid having to retrieve the whole record
            Reservation res = rdao.getReservation(reservation.getId());
            res.setMessage(data.message);
            Notifier.sendReservationApproveRequestMail(car.getOwner(), res, car.getName()
            );
        }
        return redirect(routes.Trips.index(0));
    }

    /**
     * Show the page that allows shortening of reservations
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result shortenReservation(int reservationId) {
        Reservation reservation = DataAccess.getInjectedContext().getReservationDAO().getReservationExtended(reservationId);

        if (WorkflowAction.SHORTEN.isForbiddenForCurrentUser(reservation)) {
            flash("danger", "U kan deze reservatie niet inkorten");
            return redirectToDetails(reservationId);
        }


        return ok(shorten.render(
                Form.form(ReservationData.class).fill(
                        new ReservationData().populate(reservation.getFrom(), reservation.getUntil())
                ),
                reservation
        ));
    }

    /**
     * Process the page that allows shortening of reservations
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result doShortenReservation(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        Reservation reservation = dao.getReservationExtended(reservationId);
        if (WorkflowAction.SHORTEN.isForbiddenForCurrentUser(reservation)) {
            return badRequest();
        }

        Form<ReservationData> form = Form.form(ReservationData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(shorten.render(form, reservation));
        }

        ReservationData data = form.get();
        if (data.from.isBefore(reservation.getFrom())) {
            form.reject("from", "Periode mag alleen ingekort worden");
        }
        if (data.until.isAfter(reservation.getUntil())) {
            form.reject("until", "Periode mag alleen ingekort worden");
        }
        if (form.hasErrors()) {
            return badRequest(shorten.render(form, reservation));
        }
        dao.updateReservationTime(reservationId, data.from, data.until);

        return redirectToDetails(reservationId);
    }

    public static class ReservationData {
        @Constraints.Required
        public LocalDateTime from;

        @Constraints.Required
        public LocalDateTime until;

        public String message;

        public List<ValidationError> validate() {
            if (from.isBefore(until)) {
                return null;
            } else {
                return Collections.singletonList(
                        new ValidationError("until", "Het einde van de periode moet na het begin van de periode liggen")
                );
            }
        }

        public ReservationData populate(LocalDateTime from, LocalDateTime until) {
            this.from = from;
            this.until = until;
            return this;
        }

    }

    /**
     * Show the page to create a reservation in the past
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result createOld(int carId) {
        // check whether allowed to do this
        DataAccessContext context = DataAccess.getInjectedContext();
        Car car = context.getCarDAO().getCar(carId);
        if (CurrentUser.hasRole(UserRole.RESERVATION_ADMIN) ||
                CurrentUser.is(car.getOwner().getId())) {
            return ok (createold.render(
                    Form.form(OldReservationData.class).fill(
                            OldReservationData.forCurrentUser()
                    ),
                    car
            ));
        } else {
            return badRequest(); // hack?
        }
    }

     /**
     * Process the reservation made in {@link #createOld}
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result doCreateOld(int carId) {
        // TODO: factor out common code with #doCreate
        DataAccessContext context = DataAccess.getInjectedContext();
        Car car = context.getCarDAO().getCar(carId);

        if (!CurrentUser.hasRole(UserRole.RESERVATION_ADMIN) &&
                CurrentUser.isNot(car.getOwner().getId())) {
            // not allowed
            return badRequest();  // hack?
        }

        Form<OldReservationData> form = Form.form(OldReservationData.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(createold.render(form, car));
        }

        OldReservationData data = form.get();
        LocalDateTime from = data.from;
        LocalDateTime until = data.until;

        // Test whether the reservation is valid
        ReservationDAO rdao = context.getReservationDAO();
        if (rdao.hasOverlap(carId, from, until)) {
            String errorMessage = "De reservatie overlapt met een bestaande reservatie";
            form.reject("from", errorMessage);
            form.reject("until", errorMessage);
            return ok(createold.render(form, car));
        }

        rdao.createReservation(from, until, carId, data.userId);
        // status will be set automatically to REQUEST_DETAILS - no mails will be sent
        return redirect(routes.Trips.index(0));
    }

    public static class OldReservationData {
        @Constraints.Required
        public LocalDateTime from;

        @Constraints.Required
        public LocalDateTime until;

        // TODO: reuse e.g. InfoSessions.UserPickerData
        public Integer userId;

        @Constraints.Required
        public String userIdAsString;

        public List<ValidationError> validate() {
            List<ValidationError> errors = new ArrayList<>();
            if (userId == null || userId <= 0) {
                // needed for those cases where a string is input which does not correspond with a real person
                errors.add(new ValidationError("userId", "Gelieve een bestaande persoon te selecteren"));
            }
            if (!from.isBefore(until)) {
                errors.add(new ValidationError("until", "Het einde van de periode moet na het begin van de periode liggen"));
            }
            if (!from.isBefore(LocalDateTime.now())) {
                errors.add(new ValidationError("from", "Reservatie moet in het verleden liggen"));
            }
            if (errors.isEmpty()) {
                return null;
            } else {
                return errors;
            }
        }

        public static OldReservationData forCurrentUser() {
            OldReservationData data = new OldReservationData();
            data.userId = CurrentUser.getId();
            data.userIdAsString = CurrentUser.getFullName();
            return data;
        }

    }

}
