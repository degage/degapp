/* Drives.java
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
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import com.google.common.base.Strings;
import controllers.Reserve.ReservationData;
import controllers.util.FileHelper;
import controllers.util.Pagination;
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
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.drives.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller responsible for the display of (pending) reservations and the processing
 * of pending reservations (approval or refusal of a reservation).
 * <p>
 * A reservation becomes a drive when this reservation is approved by the owner of the
 * reserved car.
 * There is no difference between a drive and reservation apart from the reservation
 * status (a drive has status approved or request_new).
 * If a reservation is approved, in which case it is a drive, extra information will
 * be associated with the reservation.
 */
public class Drives extends Controller {

    /**
     * Method: GET
     *
     * @param tab The number of the tab that should be shown 0,1,2 or 3
     * @return the drives index page containing all (pending) reservations of the user or for his car
     * starting with the tab containing the reservations with the specified status active.
     */
    @AllowRoles
    @InjectContext
    public static Result index(int tab) {
        if (CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
            return ok(drivesAdmin.render());
        } else {
            return ok(drives.render(tab));
        }
    }

    /**
     * Method: GET
     * <p>
     * Render the detailpage of a drive/reservation.
     *
     * @param reservationId the id of the reservation of which the details are requested
     * @return the detail page of specific drive/reservation
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result details(int reservationId) {
        Html result = detailsPage(DataAccess.getInjectedContext().getReservationDAO().getReservation(reservationId));
        if (result != null) {
            return ok(result);
        } else {
            return redirect(routes.Drives.index(0));
        }
    }

    /**
     * Create a details page for the given reservation. Form fields
     * are filled in with details from the corresponding reservation.
     */
    private static Html detailsPage(Reservation reservation) {
        int reservationId = reservation.getId();

        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        UserDAO udao = context.getUserDAO();
        CarRideDAO ddao = context.getCarRideDAO();
        User loaner = udao.getUser(reservation.getUserId());
        User owner = udao.getUser(reservation.getOwnerId());
        if ( ! isDriverOrOwnerOrAdmin(reservation)) {
            flash("danger", "Je bent niet gemachtigd om deze informatie op te vragen");
            return null;
        }
        CarRide driveInfo = null;
        if (reservation.getStatus() == ReservationStatus.DETAILS_PROVIDED || reservation.getStatus() == ReservationStatus.FINISHED)
            driveInfo = ddao.getCarRide(reservationId);

        User previousLoaner = null;
        User nextLoaner = null;
        if (reservation.getStatus() == ReservationStatus.ACCEPTED) {
            Reservation nextReservation = rdao.getNextReservation(reservationId);
            if (nextReservation != null) {
                nextLoaner = udao.getUser(nextReservation.getUser().getId()); // TODO: only phones needed
            }
            Reservation previousReservation = rdao.getPreviousReservation(reservationId);
            if (previousReservation != null) {
                previousLoaner = udao.getUser(previousReservation.getUser().getId()); // TODO: only phones needed
            }
        }

        Car car = context.getCarDAO().getCar(reservation.getCar().getId()); // TOOD: check whether this is necessary
        if (CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
            return driveDetailsAsAdmin.render(
                    reservation, driveInfo, car, owner, loaner,
                    previousLoaner, nextLoaner
            );
        } else if (CurrentUser.is(owner.getId())) {
            return driveDetailsAsOwner.render(
                    reservation, driveInfo, car, loaner,
                    previousLoaner, nextLoaner
            );
        } else {
            return driveDetailsAsLoaner.render(
                    reservation, driveInfo, car, owner,
                    previousLoaner, nextLoaner
            );
        }
    }

    public static class RemarksData {
        // String containing the reason for refusing a reservation or refuel
        public String status;
        public String remarks;

        public List<ValidationError> validate() {
            if ("REFUSED".equals(status) && Strings.isNullOrEmpty(remarks)) { // TODO: isNullOrBlank
                return Arrays.asList(new ValidationError("remarks", "Je moet een reden opgeven voor de weigering"));
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
    public static Result approveOrReject(int reservationId) {
        Reservation reservation = DataAccess.getInjectedContext().getReservationDAO().getReservationExtended(reservationId);
        return ok(approveorreject.render(Form.form(RemarksData.class), reservation));
    }

    /**
     * Method: POST
     * <p>
     * Called when a reservation of a car is refused/accepted by the owner.
     *
     * @param reservationId the id of the reservation being refused/accepted
     * @return the drives index page
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result approveOrRejectPost(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        Reservation reservation = dao.getReservationExtended(reservationId);
        Form<RemarksData> form =  Form.form(RemarksData.class).bindFromRequest();
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
                        return redirect(routes.Drives.details(reservationId));
                    }
                }
                dao.updateReservationStatus(reservationId, status, remarks);

                // Unschedule the job for auto accept
                context.getJobDAO().deleteJob(JobType.RESERVE_ACCEPT, reservationId);

                if (status == ReservationStatus.REFUSED) {
                    Notifier.sendReservationRefusedByOwnerMail(remarks, reservation);
                } else {
                    Notifier.sendReservationApprovedByOwnerMail(context, remarks, reservation);
                }
                return redirect(routes.Drives.details(reservationId));

            } else { // other cases only happen when somebody is hacking
                return badRequest();
            }
        }
    }

    /**
     * Method: GET
     * <p>
     * Called when a reservation of a car is cancelled by the loaner.
     *
     * @param reservationId the id of the reservation being cancelled
     * @return the drives index page
     */
    @AllowRoles({UserRole.CAR_USER,UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result cancelReservation(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        ReservationHeader reservation = dao.getReservationHeader(reservationId);
        if (!(CurrentUser.hasRole(UserRole.RESERVATION_ADMIN))) {
            // extra checks when not reservation admin
            if (CurrentUser.is(reservation.getUserId())) {
                ReservationStatus status = reservation.getStatus();
                if (status != ReservationStatus.REQUEST && status != ReservationStatus.ACCEPTED) {
                    flash("danger", "Deze reservatie kan niet meer geannuleerd worden.");
                    return redirect(routes.Drives.index(0));
                }
            } else {
                flash("danger", "Alleen de ontlener mag een reservatie annuleren!");
                return redirect(routes.Drives.index(0));
            }
        }
        dao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED);

        // Unschedule the job for auto accept
        context.getJobDAO().deleteJob(JobType.RESERVE_ACCEPT, reservationId);

        return redirect(routes.Drives.details(reservationId));
    }

    /**
     * Cancel a reservation for a ride in the past which did not actually take place
     */

    @AllowRoles({UserRole.CAR_USER,UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result lateCancelReservation(int reservationId) {
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
                    return redirect(routes.Drives.index(0));
                }
            } else {
                flash("danger", "Alleen de ontlener kan een lopende rit annuleren!");
                return redirect(routes.Drives.index(0));
            }
        }
        dao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED_LATE);

        return redirect(routes.Drives.details(reservationId));
    }

    private static boolean editJourneyInfoAllowed(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.DETAILS_PROVIDED) {
            return isOwnerOrAdmin(reservation);
        } else
            return reservation.getStatus() == ReservationStatus.FINISHED && CurrentUser.hasRole(UserRole.RESERVATION_ADMIN);
    }

    /**
     * Show the page that allows editing of the journey info
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result editJourneyInfo (int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Reservation reservation = context.getReservationDAO().getReservationExtended(reservationId);
        if (editJourneyInfoAllowed(reservation)) {
            CarRide ride = context.getCarRideDAO().getCarRide(reservationId);
            return ok(editjourney.render(
                    Form.form(JourneyData.class).fill(new JourneyData().populate(ride)),
                    reservation
            ));
        }  else {
            // not allowed
            return badRequest(); // should not happen - hacker?
        }
    }

    /**
     * Process the results of {@link #editJourneyInfo}
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result editJourneyInfoPost (int reservationId) {
        Form<JourneyData> form = Form.form(JourneyData.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        Reservation reservation = rdao.getReservation(reservationId);
        if (form.hasErrors()) {
            return badRequest(editjourney.render(form,reservation));
        } else if (editJourneyInfoAllowed(reservation)) {
            JourneyData data = form.get();
            CarRideDAO dao = context.getCarRideDAO();
            dao.updateCarRideKm(reservationId, data.startKm, data.endKm);
            dao.approveInfo(reservationId);
            rdao.updateReservationStatus(reservationId, ReservationStatus.FINISHED);
            return redirect(routes.Drives.details(reservationId));
        } else {
            // not allowed
            return badRequest(); // hacker?
        }
    }

    /**
     * Keur de kilometerstanden goed.
     */

    @AllowRoles({UserRole.CAR_OWNER,UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result approveDriveInfo(int reservationId) {

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
        return redirect(routes.Drives.details(reservationId));
    }

    /* KEPT HERE FOR FUTURE REFERENCE
    private static BigDecimal calculateDriveCost(int distance, boolean privileged, Costs costInfo) {
        if (privileged) {
            return BigDecimal.ZERO;
        } else {
            double cost = 0;
            int levels = costInfo.getLevels();
            int lower = 0;

            for (int level = 0; level < levels; level++) {
                int limit;

                // TODO: refactor this
                if (level == levels - 1 || distance <= (limit = costInfo.getLimit(level))) {
                    cost += distance * costInfo.getCost(level);
                    break;
                } else {
                    cost += (limit - lower) * costInfo.getCost(level);
                    distance -= (limit - lower);
                    lower = limit;
                }
            }

            return new BigDecimal(cost);
        }
    }
    */

    /**
     * Get the number of reservations having the provided status
     *
     * @param status       The status
     * @param userIsLoaner Extra filtering specifying the user has to be loaner
     * @return The number of reservations
     */
    // must be used with injected context
    public static int reservationsWithStatus(ReservationStatus status, boolean userIsLoaner) {
        return DataAccess.getInjectedContext().getReservationDAO().numberOfReservationsWithStatus(status, CurrentUser.getId(), userIsLoaner);
    }

    // RENDERING THE PARTIAL

    /**
     * @param page         The page in the drivelists
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of cars of the corresponding page (only available to car_user+)
     */
    @AllowRoles
    @InjectContext
    public static Result showDrivesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        User user = DataProvider.getUserProvider().getUser();
        ReservationDAO dao = DataAccess.getInjectedContext().getReservationDAO();

        if (field == null) {
            field = FilterField.FROM;
        }

        // We only want reservations from the current user (or his car(s))
        filter.putValue(FilterField.RESERVATION_USER_OR_OWNER_ID, "" + user.getId());

        Iterable<Reservation> listOfReservations = dao.getReservationListPage(field, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfReservations(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return ok(drivespage.render(user.getId(), listOfReservations, page, amountOfResults, amountOfPages, ascInt, orderBy, searchString));
    }

    /**
     * @param page         The page in the drivelists
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of cars of the corresponding page (only available to car_user+)
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result showDrivesAdminPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        User user = DataProvider.getUserProvider().getUser();
        ReservationDAO dao = DataAccess.getInjectedContext().getReservationDAO();

        if (field == null) {
            field = FilterField.FROM;
        }

        Iterable<Reservation> listOfReservations = dao.getReservationListPage(field, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfReservations(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return ok(drivespage.render(user.getId(), listOfReservations, page, amountOfResults, amountOfPages, ascInt, orderBy, searchString));
    }

    /**
     * Show the page that allows shortening of reservations
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result shortenReservation (int reservationId) {
        Reservation reservation = DataAccess.getInjectedContext().getReservationDAO().getReservationExtended(reservationId);
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
    public static Result shortenReservationPost (int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        Reservation reservation = dao.getReservationExtended(reservationId);
        ReservationStatus status = reservation.getStatus();

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

        if ( (CurrentUser.is(reservation.getUserId()) || CurrentUser.hasRole(UserRole.RESERVATION_ADMIN))
                        && (status ==  ReservationStatus.ACCEPTED || status == ReservationStatus.REQUEST)
        ) {
            dao.updateReservationTime(reservationId, data.from, data.until);
            if (status == ReservationStatus.REQUEST) {
                // Remove old reservation auto accept and add new
                JobDAO jdao = context.getJobDAO();
                // TODO: do the following in a single call
                jdao.deleteJob(JobType.RESERVE_ACCEPT, reservationId); //remove the old job
                /* TODO: reintroduce statement below, or similar
                jdao.createJob(
                        JobType.RESERVE_ACCEPT,
                        reservationId,
                        Instant.now().plusSeconds(60 * Integer.parseInt(context.getSettingDAO().getSettingForNow("reservation_auto_accept")))
                );
                */
            }
            return redirect(routes.Drives.details(reservationId));
        } else {
            // this means that somebody is hacking?
            return badRequest();
        }
    }

    public static class JourneyData {
        @Constraints.Required
        @Constraints.Min(value=1, message="Ongeldige kilometerstand")
        public int startKm;

        @Constraints.Required
        @Constraints.Min(value=1, message="Ongeldige kilometerstand")
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

        public JourneyData populate(CarRide ride) {
            this.startKm = ride.getStartKm();
            this.endKm = ride.getEndKm();
            this.damaged = ride.isDamaged();
            return this;
        }

    }

    public static boolean isOwnerOrAdmin (ReservationHeader reservation) {
        return  CurrentUser.hasRole(UserRole.RESERVATION_ADMIN) ||
                        CurrentUser.is(reservation.getOwnerId());
    }

    public static boolean isDriverOrOwnerOrAdmin (ReservationHeader reservation) {
        return  isOwnerOrAdmin(reservation) || CurrentUser.is(reservation.getUserId());
    }

    private static boolean newJourneyInfoAllowed(ReservationHeader reservation) {
        return reservation.getStatus() == ReservationStatus.REQUEST_DETAILS &&
                isDriverOrOwnerOrAdmin(reservation);
    }

    public static class JourneyDataExtended extends JourneyData {

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
                result.add(new ValidationError("amount", "Bedrag moet groter zijn dan 0 EURO")) ;
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
     * Allows first time input of journey info
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result newJourneyInfo(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Reservation reservation = context.getReservationDAO().getReservation(reservationId);
        if (newJourneyInfoAllowed(reservation)) {
            // prefill
            CarRideDAO dao = context.getCarRideDAO();
            int startKm = dao.getPrevEndKm(reservationId);
            int endKm = dao.getNextStartKm(reservationId);
            JourneyDataExtended data = new JourneyDataExtended();
            data.startKm = startKm;
            data.endKm = endKm;
            data.damaged = false;
            return ok(newjourney.render(
                    Form.form(JourneyDataExtended.class).fill(data),
                    reservation
            ));
        } else {
            // not allowed
            return badRequest(); // should not happen
        }
    }

    /**
     * Processes result from {@Link #newJourneyInfo}
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result newJourneyInfoPost(int reservationId) {

        // complicated because page allows both new drives and refuels
        Form<JourneyDataExtended> form = Form.form(JourneyDataExtended.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        Reservation reservation = rdao.getReservation(reservationId);
        if (form.hasErrors()) {
            return badRequest(newjourney.render(form,reservation));
        } else {
            JourneyDataExtended data = form.get();
            Http.MultipartFormData.FilePart filePart = Controller.request().body().asMultipartFormData().getFile("picture");

            // process validation errors (delayed)
            if (data.someFilledIn() || filePart!= null) {
                List<ValidationError> errors = data.listOfErrors();
                if (errors.isEmpty()) {
                   if (filePart == null) {
                       form.reject("picture", "Bestand met foto of scan van bonnetje is verplicht");
                       return badRequest(newjourney.render(form, reservation));
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
                   return badRequest(newjourney.render(form, reservation));
                }
            }

            if (newJourneyInfoAllowed(reservation)) {

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
                    if (file.getContentType() == null) {
                        form.reject("picture", "Het bestand  is van het verkeerde type");
                        return badRequest(newjourney.render(form, reservation));
                    } else {
                        Refuels.newRefuel(reservation, owner, data.amount.getValue(),
                                file.getId(), data.km, data.fuelAmount
                                );
                    }
                }

                return redirect(routes.Drives.details(reservationId));
            } else {
                // not allowed
                return badRequest(); // hacker?
            }
        }
    }


}
