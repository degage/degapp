/* Workflow.java
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
import be.ugent.degage.db.dao.CarRideDAO;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.*;
import com.google.common.base.Strings;
import controllers.util.FileHelper;
import controllers.util.WorkflowAction;
import data.EurocentAmount;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.workflow.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller responsible for reservation/trip work flow
 */
public class Workflow extends WFCommon {
   

    /* ========================================================================
       CREATION
       ======================================================================== */

    /* ========================================================================
       CANCEL
       ======================================================================== */

    /**
     * Cancel a reservation for a trip in the past which did not actually take place
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result lateCancelReservation(int reservationId) {
        // TODO: show a form with two choices
        // TODO: lots of code in common with cancelReservation
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        ReservationHeader reservation = dao.getReservationHeader(reservationId);
        if (!(CurrentUser.hasRole(UserRole.RESERVATION_ADMIN))) {
            // extra checks when not reservation admin
            if (CurrentUser.is(reservation.getUserId())) {
                ReservationStatus status = reservation.getStatus();
                if (status != ReservationStatus.DETAILS_PROVIDED) {
                    flash("danger", "Deze (lopende) rit kan niet worden geannuleerd.");
                    return redirect(routes.Trips.index(0));
                }
            } else {
                flash("danger", "Alleen de ontlener kan een lopende rit annuleren!");
                return redirect(routes.Trips.index(0));
            }
        }
        dao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED_LATE);

        return redirectToDetails(reservationId);
    }



    /* ========================================================================
       APPROVE / REJECT
       ======================================================================== */

    public static class RemarksData {
        // String containing the reason for refusing a reservation (or refuel)
        public String status;
        public String remarks;

        public List<ValidationError> validate() {
            if ("REFUSED".equals(status) && Strings.isNullOrEmpty(remarks)) { // TODO: isNullOrBlank
                return Collections.singletonList(
                        new ValidationError("remarks", "Je moet een reden opgeven voor de weigering")
                );
            } else {
                return null;
            }
        }
    }

    /**
     * Show the page that allows approval or rejection of a reservation by the owner
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result approveReservation(int reservationId) {
        Reservation reservation = DataAccess.getInjectedContext().getReservationDAO().getReservationExtended(reservationId);
        return ok(approveorreject.render(Form.form(RemarksData.class), reservation));
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
        Form<RemarksData> form = Form.form(RemarksData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(approveorreject.render(form, reservation));
        } else {
            RemarksData data = form.get(); // the form does not contain errors
            ReservationStatus status = ReservationStatus.valueOf(data.status);
            String remarks = data.remarks;

            if (status == ReservationStatus.REFUSED || status == ReservationStatus.ACCEPTED) {
                if (!(CurrentUser.hasRole(UserRole.RESERVATION_ADMIN))) {
                    // extra checks when not reservation admin
                    if (CurrentUser.isNot(reservation.getOwnerId())
                            || reservation.getStatus() != ReservationStatus.REQUEST) {
                        flash("danger", "Alleen de eigenaar kan een reservatie goed- of afkeuren");
                        return redirectToDetails(reservationId);
                    }
                }
                dao.updateReservationStatus(reservationId, status, remarks);
                if (status == ReservationStatus.REFUSED) {
                    Notifier.sendReservationRefusedByOwnerMail(remarks, reservation);
                } else {
                    Notifier.sendReservationApprovedByOwnerMail(context, remarks, reservation);
                }
                return redirectToDetails(reservationId);

            } else { // other cases only happen when somebody is hacking
                return badRequest();
            }
        }
    }

    /* ========================================================================
       SHORTEN
       ======================================================================== */

    /* ========================================================================
       ENTER TRIP DATA
       ======================================================================== */


    public static class TripData {
        @Constraints.Required
        @Constraints.Min(value = 1, message = "Ongeldige kilometerstand")
        public int startKm;

        @Constraints.Required
        @Constraints.Min(value = 1, message = "Ongeldige kilometerstand")
        public int endKm;

        public boolean damaged = false;

        public List<ValidationError> validate() {
            if (startKm > endKm) {
                String message = "De kilometerstand na de rit moet groter zijn dan vóór de rit";
                return Arrays.asList(
                        new ValidationError("startKm", message), new ValidationError("endKm", message)
                );
            } else {
                return null;
            }
        }

        public TripData populate(CarRide ride) {
            this.startKm = ride.getStartKm();
            this.endKm = ride.getEndKm();
            this.damaged = ride.isDamaged();
            return this;
        }

    }


    public static class TripDataExtended extends TripData {

        public EurocentAmount amount;

        public String picture; // only used to enable field error messages...

        public String fuelAmount;

        public int km;

        public boolean someFilledIn() {
            return (amount != null && amount.getValue() != 0)
                    || !Strings.isNullOrEmpty(fuelAmount) || km != 0;
        }

        public List<ValidationError> listOfErrors() {
            List<ValidationError> result = new ArrayList<>();
            if (amount == null || amount.getValue() <= 0) {
                result.add(new ValidationError("amount", "Bedrag moet groter zijn dan 0 EURO"));
            }
            if (Strings.isNullOrEmpty(fuelAmount)) {
                result.add(new ValidationError("fuelAmount", "Veld mag niet leeg zijn"));
            }
            if (km <= 0) {
                result.add(new ValidationError("km", "Ongeldige kilometerstand"));
            }
            return result;
        }

    }

    private static boolean newTripInfoAllowed(ReservationHeader reservation) {
        return reservation.getStatus() == ReservationStatus.REQUEST_DETAILS &&
                isDriverOrOwnerOrAdmin(reservation);
    }

    /**
     * Allows first time input of journey info
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result newTripInfo(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Reservation reservation = context.getReservationDAO().getReservation(reservationId);
        if (newTripInfoAllowed(reservation)) {
            // prefill
            CarRideDAO dao = context.getCarRideDAO();
            int startKm = dao.getPrevEndKm(reservationId);
            int endKm = dao.getNextStartKm(reservationId);
            TripDataExtended data = new TripDataExtended();
            data.startKm = startKm;
            data.endKm = endKm;
            data.damaged = false;
            return ok(newtrip.render(
                    Form.form(TripDataExtended.class).fill(data),
                    reservation
            ));
        } else {
            // not allowed
            return badRequest(); // should not happen
        }
    }

    /**
     * Processes result from {@link #newTripInfo}
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result doNewTripInfo(int reservationId) {

        // complicated because page allows both new trips and refuels
        Form<TripDataExtended> form = Form.form(TripDataExtended.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        Reservation reservation = rdao.getReservation(reservationId);
        if (form.hasErrors()) {
            return badRequest(newtrip.render(form, reservation));
        } else {
            TripDataExtended data = form.get();
            Http.MultipartFormData.FilePart filePart = Controller.request().body().asMultipartFormData().getFile("picture");

            // process validation errors (delayed)
            if (data.someFilledIn() || filePart != null) {
                List<ValidationError> errors = data.listOfErrors();
                if (errors.isEmpty()) {
                    if (filePart == null) {
                        form.reject("picture", "Bestand met foto of scan van bonnetje is verplicht");
                        return badRequest(newtrip.render(form, reservation));
                    }
                } else {
                    if (filePart == null) {
                        form.reject("picture", "Bestand met foto of scan van bonnetje is verplicht");
                    } else {
                        form.reject("picture", "Bestand opnieuw selecteren");
                    }
                    for (ValidationError error : errors) {
                        form.reject(error);
                    }
                    return badRequest(newtrip.render(form, reservation));
                }
            }

            if (newTripInfoAllowed(reservation)) {

                // register/change ride in database
                CarRideDAO dao = context.getCarRideDAO();
                CarRide ride = dao.getCarRide(reservationId);
                if (ride == null) {
                    boolean damaged = data.damaged;
                    dao.createCarRide(reservation, data.startKm, data.endKm, damaged);
                    if (damaged) {
                        context.getDamageDAO().createDamage(reservation); // TODO: why is this? Delegate to database module?
                    }
                } else {//  make changes and approve
                    dao.updateCarRideKm(reservationId, data.startKm, data.endKm);
                }

                boolean isAdmin = isOwnerOrAdmin(reservation);


                // change ride status according to whether current user is owner or not
                UserHeader owner = null;
                if (isAdmin) {
                    // approve immediately
                    rdao.updateReservationStatus(reservationId, ReservationStatus.FINISHED);
                } else {
                    // register and send mail to owner
                    rdao.updateReservationStatus(reservationId, ReservationStatus.DETAILS_PROVIDED);
                    owner = context.getUserDAO().getUserHeader(reservation.getOwnerId());
                    if (ride == null) {
                        ride = dao.getCarRide(reservationId);
                    }
                    Notifier.sendReservationDetailsProvidedMail(owner, reservation, ride);
                }

                // add first refuel, if present


                if (filePart != null) {
                    File file = FileHelper.getFileFromFilePart(filePart, FileHelper.DOCUMENT_CONTENT_TYPES, "uploads.refuelproofs");
                    if (file == null || file.getContentType() == null) {
                        form.reject("picture", "Het bestand  is van het verkeerde type");
                        return badRequest(newtrip.render(form, reservation));
                    } else {
                        Refuels.newRefuel(reservation, owner, data.amount.getValue(),
                                file.getId(), data.km, data.fuelAmount
                        );
                    }
                }

                return redirectToDetails(reservationId);
            } else {
                // not allowed
                return badRequest(); // hacker?
            }
        }
    }
     
    /* ========================================================================
       EDIT TRIP DATA
       ======================================================================== */

    private static boolean editTripInfoAllowed(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.DETAILS_PROVIDED) {
            return isOwnerOrAdmin(reservation);
        } else {
            return reservation.getStatus() == ReservationStatus.FINISHED && CurrentUser.hasRole(UserRole.RESERVATION_ADMIN);
        }
    }

    /**
     * Show the page that allows editing of the journey info
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result editTripInfo(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Reservation reservation = context.getReservationDAO().getReservationExtended(reservationId);
        if (editTripInfoAllowed(reservation)) {
            CarRide ride = context.getCarRideDAO().getCarRide(reservationId);
            return ok(edittrip.render(
                    Form.form(TripData.class).fill(new TripData().populate(ride)),
                    reservation
            ));
        } else {
            // not allowed
            return badRequest(); // should not happen - hacker?
        }
    }

    /**
     * Process the results of {@link #editTripInfo}
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result doEditTripInfo(int reservationId) {
        Form<TripData> form = Form.form(TripData.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        Reservation reservation = rdao.getReservation(reservationId);
        if (form.hasErrors()) {
            return badRequest(edittrip.render(form, reservation));
        } else if (editTripInfoAllowed(reservation)) {
            TripData data = form.get();
            CarRideDAO dao = context.getCarRideDAO();
            dao.updateCarRideKm(reservationId, data.startKm, data.endKm);
            dao.approveInfo(reservationId);
            rdao.updateReservationStatus(reservationId, ReservationStatus.FINISHED);
            return redirectToDetails(reservationId);
        } else {
            // not allowed
            return badRequest(); // hacker?
        }
    }

    /* ========================================================================
       TRIP DATA APPROVAL
       ======================================================================== */

    /**
     * Approve trip info
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result approveTripInfo(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarRideDAO dao = context.getCarRideDAO();
        ReservationDAO rdao = context.getReservationDAO();
        Reservation reservation = rdao.getReservation(reservationId);
        if (isOwnerOrAdmin(reservation)) {
            dao.approveInfo(reservationId);
            rdao.updateReservationStatus(reservationId, ReservationStatus.FINISHED);
            flash("success", "De ritgegevens werden goedgekeurd.");
        } else {
            flash("danger", "Je bent niet gemachtigd om deze actie uit te voeren.");
        }
        return redirectToDetails(reservationId);
    }

    /**
     * SEND REMINDER
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result sendReminder(int reservationId) {
        // TODO
        return ok();
    }
}
