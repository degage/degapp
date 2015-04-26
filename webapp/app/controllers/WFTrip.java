/* WFTrip.java
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
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.workflow.edittrip;
import views.html.workflow.newtrip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller responsible for reservation/trip work flow
 */
public class WFTrip extends WFCommon {

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

    /**
     * Dispatch to the proper page for editing trip information
     */
    @AllowRoles
    @InjectContext
    public static Result tripInfo(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TripAndCar trip = context.getTripDAO().getTripAndCar(reservationId, false);
        if (WorkflowAction.EDIT_TRIP.isForbiddenForCurrentUser(trip)) {
            flash("danger", "Je kan geen ritdetails (meer) ingegeven voor deze rit.");
            return redirectToDetails(reservationId);
        }

        ReservationStatus status = trip.getStatus();
        if (status == ReservationStatus.REQUEST_DETAILS) {
            // first time information is entered
            // mail sent to owner (if owner is not driver)
            CarRideDAO dao = context.getCarRideDAO();
            TripDataExtended data = new TripDataExtended();
            data.startKm = dao.getPrevEndKm(reservationId);
            data.endKm = dao.getNextStartKm(reservationId);
            data.damaged = false;
            return ok(newtrip.render(
                    Form.form(TripDataExtended.class).fill(data),
                    trip
            ));
        } else {
            // information is edited
            CarRide ride = context.getCarRideDAO().getCarRide(reservationId);
            return ok(edittrip.render(
                    Form.form(TripData.class).fill(new TripData().populate(ride)),
                    trip
            ));
        }
    }

    // must be used with injected context
    private static void updateToDetailsProvided(TripAndCar trip, UserHeader owner) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        rdao.updateReservationStatus(trip.getId(), ReservationStatus.DETAILS_PROVIDED);
        Notifier.sendReservationDetailsProvidedMail(owner, trip);
    }

    /**
     * Processes result from {@link #tripInfo} for a new request
     */
    @AllowRoles
    @InjectContext
    public static Result doNewTripInfo(int reservationId) {

        // complicated because page allows both new trips and refuels
        Form<TripDataExtended> form = Form.form(TripDataExtended.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        TripAndCar trip = context.getTripDAO().getTripAndCar(reservationId, false);

        if (WorkflowAction.EDIT_TRIP.isForbiddenForCurrentUser(trip)
                || trip.getStatus() != ReservationStatus.REQUEST_DETAILS) {
            return badRequest(); // this should not happen
        }

        if (form.hasErrors()) {
            return badRequest(newtrip.render(form, trip));
        } else {
            TripDataExtended data = form.get();
            Http.MultipartFormData.FilePart filePart = Controller.request().body().asMultipartFormData().getFile("picture");

            // process validation errors (delayed)
            if (data.someFilledIn() || filePart != null) {
                List<ValidationError> errors = data.listOfErrors();
                if (errors.isEmpty()) {
                    if (filePart == null) {
                        form.reject("picture", "Bestand met foto of scan van bonnetje is verplicht");
                        return badRequest(newtrip.render(form, trip));
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
                    return badRequest(newtrip.render(form, trip));
                }
            }

            // register/change ride in database
            CarRideDAO dao = context.getCarRideDAO();
            if (dao.getCarRide(reservationId) == null) {
                boolean damaged = data.damaged;
                dao.createCarRide(trip, data.startKm, data.endKm, damaged);
                if (damaged) {
                    context.getDamageDAO().createDamage(trip); // TODO: why is this? Delegate to database module?
                }
            } else {//  make changes and approve
                dao.updateCarRideKm(reservationId, data.startKm, data.endKm);
            }

            trip.setStartKm(data.startKm);
            trip.setStartKm(data.endKm); // for use in message sent later

            // change ride status according to whether current user is owner or not
            UserHeader owner = null;
            if (isOwnerOrAdmin(trip)) {
                // approve immediately
                rdao.updateReservationStatus(reservationId, ReservationStatus.FINISHED);
            } else {
                // register and send mail to owner
                owner = context.getUserDAO().getUserHeader(trip.getOwnerId());
                updateToDetailsProvided(trip, owner);
            }

            // add first refuel, if present
            if (filePart != null) {
                File file = FileHelper.getFileFromFilePart(filePart, FileHelper.DOCUMENT_CONTENT_TYPES, "uploads.refuelproofs");
                if (file == null || file.getContentType() == null) {
                    form.reject("picture", "Het bestand  is van het verkeerde type");
                    return badRequest(newtrip.render(form, trip));
                } else {
                    RefuelCreate.newRefuel(trip, owner, data.amount.getValue(),
                            file.getId(), data.km, data.fuelAmount
                    );
                }
            }

            return redirectToDetails(reservationId);
        }
    }

    /**
     * Process the results of {@link #tripInfo} when not the first time
     */
    @AllowRoles
    @InjectContext
    public static Result doEditTripInfo(int reservationId) {
        Form<TripData> form = Form.form(TripData.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        TripAndCar trip = context.getTripDAO().getTripAndCar(reservationId, false);
        if (WorkflowAction.EDIT_TRIP.isForbiddenForCurrentUser(trip) ||
                trip.getStatus() == ReservationStatus.REQUEST_DETAILS) {
            return badRequest();
        }

        if (form.hasErrors()) {
            return badRequest(edittrip.render(form, trip));
        } else {
            TripData data = form.get();
            CarRideDAO dao = context.getCarRideDAO();
            dao.updateCarRideKm(reservationId, data.startKm, data.endKm);
            trip.setStartKm(data.startKm);
            trip.setStartKm(data.endKm); // for use in message sent later
            if (isOwnerOrAdmin(trip)) {
                // approve immediately
                rdao.updateReservationStatus(reservationId, ReservationStatus.FINISHED);
            } else if (trip.getStatus() == ReservationStatus.DETAILS_REJECTED) {
                // register and send mail to owner
                flash("success", "De bestuurder wordt via mail op de hoogte gebracht van uw correcties");
                updateToDetailsProvided(
                        trip,
                        context.getUserDAO().getUserHeader(trip.getOwnerId())
                );
            }
            return redirectToDetails(reservationId);
        }
    }

}
