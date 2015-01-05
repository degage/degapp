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
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.drives.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
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
     * Class implementing a model wrapped in a form.
     * This model is used during the form submission when an loaner provides information about
     * the drive.
     */
    public static class InfoModel {

        @Constraints.Required
        public int startKm;

        @Constraints.Required
        public int endKm;

        public boolean damaged;

        @Constraints.Required
        public int numberOfRefuels;

        /**
         * Validates the form:
         * - startKm must be smaller then the endKm;
         *
         * @return an error string or null
         */
        public List<ValidationError> validate() {
            List<ValidationError> result = new ArrayList<>();
            // TODO: use field constraints for most of these
            if (startKm <= 0) {
                result.add(new ValidationError("startKm", "Kilometerstand moet groter zijn dan 0"));
            }
            if (endKm <= 0) {
                result.add(new ValidationError("endKm", "Kilometerstand moet groter zijn dan 0"));
            }
            if (result.isEmpty()) {
                // further requirements
                if (startKm > endKm) {
                    result.add(new ValidationError("endKm", "De kilometerstand na de rit moet groter zijn dan vóór de rit"));
                }
            }
            if (numberOfRefuels < 0) {
                result.add(new ValidationError("numberOfRefuels", "Ongeldig aantal"));
            }
            return result.isEmpty() ? null : result ;
        }

        public void populate(CarRide ride) {
            startKm = ride.getStartKm();
            endKm = ride.getEndKm();
            damaged = ride.isDamaged();
            numberOfRefuels = ride.getNumberOfRefuels();
        }

    }

    /**
     * Method: GET
     *
     * @param status The status identifying the tab that should be shown
     * @return the drives index page containing all (pending) reservations of the user or for his car
     * starting with the tab containing the reservations with the specified status active.
     */
    @AllowRoles
    @InjectContext
    public static Result index(String status) {
        return ok(drives.render(ReservationStatus.valueOf(status)));
    }

    /**
     * Method: GET
     *
     * @return the html page of the drives page only visible for admins
     */
    @AllowRoles({UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result drivesAdmin() {
        // TODO: join with drives and keep history to redirect to the correct page and use the correct breadcrumbs
        return ok(drivesAdmin.render());
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
            return redirect(routes.Drives.index("ACCEPTED"));
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
        CarDAO cdao = context.getCarDAO();
        CarRideDAO ddao = context.getCarRideDAO();
        User loaner = udao.getUser(reservation.getUser().getId());
        Car car = cdao.getCar(reservation.getCar().getId());
        if (car == null || loaner == null) {
            flash("danger", "De reservatie bevat ongeldige gegevens");
            return null;
        }
        User owner = udao.getUser(car.getOwner().getId());
        if (CurrentUser.isNot(reservation.getUser().getId())
                && CurrentUser.isNot(owner.getId())
                && !CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
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

        InfoModel model = new InfoModel();
        if (driveInfo != null) {
            model.populate(driveInfo);
        }


        if (CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
            return driveDetailsAsAdmin.render(
                    new Form<>(InfoModel.class).fill(model),
                    reservation, driveInfo, car, owner, loaner,
                    previousLoaner, nextLoaner, Form.form(RemarksData.class)
            );
        } else if (CurrentUser.is(owner.getId())) {
            return driveDetailsAsOwner.render(
                    new Form<>(InfoModel.class).fill(model),
                    reservation, driveInfo, car, loaner,
                    previousLoaner, nextLoaner, Form.form(RemarksData.class)
            );
        } else {
            return driveDetailsAsLoaner.render(
                    new Form<>(InfoModel.class).fill(model),
                    reservation, driveInfo, car, owner,
                    previousLoaner, nextLoaner, Form.form(RemarksData.class)
            );
        }
    }

    public static class RemarksData {
        // String containing the reason for refusing a reservation
        public String status;
        public String remarks;

        public List<ValidationError> validate() {
            if ("REFUSED".equals(status) && Strings.isNullOrEmpty(remarks)) { // TODO: isNullOrBlank
                return Arrays.asList(new ValidationError("remarks", "Je moet een reden opgeven waarom je de reservatie weigert"));
            } else {
                return null;
            }
        }
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
    public static Result setReservationStatus(int reservationId) {
        Form<RemarksData> form =  Form.form(RemarksData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(); // TODO
        } else {
            RemarksData data = form.get(); // the form does not contain errors
            ReservationStatus status = ReservationStatus.valueOf(data.status);
            String remarks = data.remarks;

            if (status == ReservationStatus.REFUSED || status == ReservationStatus.ACCEPTED) {
                DataAccessContext context = DataAccess.getInjectedContext();
                ReservationDAO dao = context.getReservationDAO();
                Reservation reservation = dao.getReservation(reservationId);
                if (!(CurrentUser.hasRole(UserRole.RESERVATION_ADMIN))) {
                    // extra checks when not reservation admin
                    if (!context.getCarDAO().isCarOfUser(reservation.getCar().getId(), CurrentUser.getId())
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
                    Notifier.sendReservationApprovedByOwnerMail(DataAccess.getInjectedContext(), remarks, reservation);
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
                    return redirect(routes.Drives.index("ACCEPTED"));
                }
            } else {
                flash("danger", "Alleen de ontlener mag een reservatie annuleren!");
                return redirect(routes.Drives.index("ACCEPTED"));
            }
        }
        dao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED, null);

        // Unschedule the job for auto accept
        context.getJobDAO().deleteJob(JobType.RESERVE_ACCEPT, reservationId);

        return redirect(routes.Drives.details(reservationId));
    }

    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result provideDriveInfo(int reservationId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarRideDAO dao = context.getCarRideDAO();
        ReservationDAO rdao = context.getReservationDAO();
        Reservation reservation = rdao.getReservation(reservationId);

        Form<InfoModel> detailsForm = Form.form(InfoModel.class).bindFromRequest();
        if (detailsForm.hasErrors())
            return badRequest(detailsPage(reservation));

        CarDAO carDAO = context.getCarDAO();
        // Test if user is authorized
        boolean isOwner = context.getCarDAO().isCarOfUser(reservation.getCar().getId(), CurrentUser.getId());
        if (CurrentUser.isNot(reservation.getUser().getId()) && !isOwner && !CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
            flash("danger", "Je bent niet geauthoriseerd voor het uitvoeren van deze actie.");
            return redirect(routes.Drives.details(reservationId));
        }
        // Test if ride already exists
        CarRide ride = dao.getCarRide(reservationId);
        if (ride == null) {
            int numberOfRefuels = detailsForm.get().numberOfRefuels;

            boolean damaged = detailsForm.get().damaged;
            ride = dao.createCarRide(reservation, detailsForm.get().startKm, detailsForm.get().endKm, damaged, numberOfRefuels);
            if (numberOfRefuels > 0) {
                RefuelDAO refuelDAO = context.getRefuelDAO();
                for (int i = 0; i < numberOfRefuels; i++) {
                    refuelDAO.createRefuel(ride); // TODO: why is this? Delegate to database module?
                }
            }
            if (damaged) {
                context.getDamageDAO().createDamage(reservation); // TODO: why is this? Delegate to database module?
            }
        } else if (isOwner || CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
            // Owner is allowed to adjust the information
            ride.setStartKm(detailsForm.get().startKm);
            ride.setEndKm(detailsForm.get().endKm);
        }

        //if (isOwner) {  // TODO: compute cost only at time of invoice
        Instant instant = Instant.from(reservation.getFrom());
        BigDecimal cost = calculateDriveCost(ride.getEndKm() - ride.getStartKm(),
                ride.getReservation().isPrivileged(),
                context.getSettingDAO().getCostSettings(instant));
        ride.setCost(cost);
        dao.updateCarRide(ride);
//        } else {
//            flash("danger", "Je bent niet geauthoriseerd voor het uitvoeren van deze actie.");
//            return redirect(routes.Drives.details(reservationId));
//        }

        // Adjust the status of the reservation
        if (isOwner) { // TODO: bug? isOwner is always true at this point
            rdao.updateReservationStatus(reservationId, ReservationStatus.FINISHED, null);
        } else {
            rdao.updateReservationStatus(reservationId, ReservationStatus.DETAILS_PROVIDED, null);
            Notifier.sendReservationDetailsProvidedMail(carDAO.getCar(reservation.getCar().getId()).getOwner(), reservation);
        }

        // Commit changes
        return ok(detailsPage(reservation));
    }

    @AllowRoles({UserRole.CAR_OWNER})
    @InjectContext
    public static Result approveDriveInfo(int reservationId) {
        Form<InfoModel> detailsForm = Form.form(InfoModel.class);
        DataAccessContext context = DataAccess.getInjectedContext();
        CarRideDAO dao = context.getCarRideDAO();
        ReservationDAO rdao = context.getReservationDAO();
        Reservation reservation = rdao.getReservation(reservationId);
        // Test if reservation exists
        if (reservation == null) {
            detailsForm.reject("De reservatie kan niet opgevraagd worden. Gelieve de database administrator te contacteren.");
            return badRequest(detailsPage(reservation));
        }
        if (!context.getCarDAO().isCarOfUser(reservation.getCar().getId(), CurrentUser.getId()) && !CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
            detailsForm.reject("Je bent niet geauthoriseerd voor het uitvoeren van deze actie.");
            return badRequest(detailsPage(reservation));
        }
        CarRide ride = dao.getCarRide(reservationId);
        if (ride == null) {
            detailsForm.reject("Er is een fout gebeurd tijdens het opslaan van de gegevens. Gelieve de database administrator te contacteren.");
            return badRequest(detailsPage(reservation));
        }
        ride.setApprovedByOwner(true);
        Instant instant = Instant.from(reservation.getFrom());
        // TODO: what if mileages are blank?
        BigDecimal cost = calculateDriveCost(ride.getEndKm() - ride.getStartKm(),
                ride.getReservation().isPrivileged(),
                context.getSettingDAO().getCostSettings(instant)
        );
        ride.setCost(cost);
        dao.updateCarRide(ride);
        rdao.updateReservationStatus(reservationId, ReservationStatus.FINISHED, null);
        return ok(detailsPage(reservation));
    }

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
        ReservationHeader reservation = DataAccess.getInjectedContext().getReservationDAO().getReservationHeader(reservationId);
        LocalDateTime from = reservation.getFrom();
        LocalDateTime until = reservation.getUntil();
        return ok(shorten.render(
                reservationId,
                Form.form(ReservationData.class).fill(new ReservationData().populate(from, until)),
                from,
                until
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
        ReservationHeader reservation = dao.getReservationHeader(reservationId);
        LocalDateTime from = reservation.getFrom();
        LocalDateTime until = reservation.getUntil();
        ReservationStatus status = reservation.getStatus();

        Form<ReservationData> form = Form.form(ReservationData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(shorten.render(reservationId, form, from, until));
        }

        ReservationData data = form.get();
        if (data.from.isBefore(from)) {
            form.reject("from", "Periode mag alleen ingekort worden");
        }
        if (data.until.isAfter(until)) {
            form.reject("until", "Periode mag alleen ingekort worden");
        }
        if (form.hasErrors()) {
            return badRequest(shorten.render(reservationId, form, from, until));
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
                jdao.createJob(
                        JobType.RESERVE_ACCEPT,
                        reservationId,
                        Instant.now().plusSeconds(60 * Integer.parseInt(context.getSettingDAO().getSettingForNow("reservation_auto_accept")))
                );
            }
            return redirect(routes.Drives.details(reservationId));
        } else {
            // this means that somebody is hacking?
            return badRequest();
        }
    }

}
