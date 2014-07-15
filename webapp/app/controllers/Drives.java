package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import controllers.Security.RoleSecured;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.drives.driveDetails;
import views.html.drives.drives;
import views.html.drives.drivesAdmin;
import views.html.drives.drivespage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Iterator;
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
     * This model is used during the form submission when an owner does not
     * approve the reservation of his car.
     * The owner is obligated to inform the loaner why his reservation request
     * is denied.
     */
    public static class RemarksModel {
        // String containing the reason for refusing a reservation
        public String status;
        public String remarks;

        /**
         * Validates the form:
         * - the owner must explain why he does not approve a reservation
         *
         * @return an error string or null
         */
        public String validate() {
            // Should not be possible but you never know
            if (status == null || "".equals(status))
                return "Een fout deed zich voor bij het verwerken van de actie. Probeer het opnieuw";
            return null;
        }
    }

    /**
     * Class implementing a model wrapped in a form.
     * This model is used during the form submission when an loaner provides information about
     * the drive.
     */
    public static class InfoModel {
        // String containing the reason for refusing a reservation
        public Integer startMileage;
        public Integer endMileage;
        public Boolean damaged;
        public Integer refueling;

        /**
         * Validates the form:
         * - startMileage must be smaller then the endMileage;
         *
         * @return an error string or null
         */
        public String validate() {
            if (startMileage == null || endMileage == null)
                return "Gelieve zowel de start als eind kilometerstand op te geven";
            if (startMileage >= endMileage)
                return "De kilometerstand voor de rit kan niet kleiner zijn dan deze na de rit";
            if (startMileage < 0 || endMileage < 0)
                return "De kilometerstand kan niet negatief zijn";
            if (refueling == null || refueling < 0)
                return "Er werd een ongeldig aantal tankbeurten opgegeven";
            return null;
        }

    }

    /**
     * Method: GET
     *
     * @return the drives index page containing all (pending) reservations of the user or for his car
     * starting with active tab containing the approved reservations.
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result index() {
        return ok(showIndex());
    }

    /**
     * Method: GET
     *
     * @param status The status identifying the tab
     * @return the drives index page containing all (pending) reservations of the user or for his car
     * starting with the tab containing the reservations with the specified status active.
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result indexWithStatus(String status) {
        return ok(showIndex(ReservationStatus.valueOf(status)));
    }

    /**
     * @return the html page of the index page starting with the tab containing
     * the approved reservations active.
     */
    // used in injected context
    private static Html showIndex() {
        return drives.render(ReservationStatus.ACCEPTED);
    }

    /**
     * @return the html page of the index page starting with the tab containing
     * the reservations with the specified status active.
     */
    // used in injected context
    private static Html showIndex(ReservationStatus status) {
        return drives.render(status);
    }

    /**
     * Method: GET
     *
     * @return the html page of the drives page only visible for admins
     */
    @RoleSecured.RoleAuthenticated({UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result drivesAdmin() {
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
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result details(int reservationId) {
        Html result = detailsPage(reservationId);
        if (result != null)
            return ok(result);
        return badRequest(showIndex());
    }

    /**
     * Private method returning the html page of a drive with a new form.
     *
     * @param reservationId The id of the reservation
     * @return the html page
     */
    // use in injected context only
    private static Html detailsPage(int reservationId) {
        return detailsPage(reservationId, Form.form(Reserve.ReservationModel.class), Form.form(RemarksModel.class), Form.form(InfoModel.class));
    }

    /**
     * Private method returning the html page with the details of a drive
     * - an owner can approve or reject reservations of his car on this page
     * - a loaner can adjust his reservation
     * - details about the drive will be requested when the drive is finished
     * - the owner has to approve the details provided by the loaner
     *
     * @param reservationId the id of the reservation/drive
     * @param adjustForm    Form allowing the loaner to adjust his reservation
     * @param refuseForm    Form allowing the owner to refuse a reservation or when he disagrees with the provided details
     *                      concerning the drive
     * @param detailsForm   Form allowing the loaner to provided details about the drive
     * @return the html page
     */
    // should be used with an injected context only
    private static Html detailsPage(int reservationId, Form<Reserve.ReservationModel> adjustForm, Form<RemarksModel> refuseForm,
                                    Form<InfoModel> detailsForm) {
        User user = DataProvider.getUserProvider().getUser();
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        UserDAO udao = context.getUserDAO();
        CarDAO cdao = context.getCarDAO();
        CarRideDAO ddao = context.getCarRideDAO();
        Reservation reservation = rdao.getReservation(reservationId);
        if (reservation == null) {
            flash("Error", "De opgegeven reservatie is onbestaand");
            return null;
        }
        User loaner = udao.getUser(reservation.getUser().getId());
        Car car = cdao.getCar(reservation.getCar().getId());
        if (car == null || loaner == null) {
            flash("Error", "De reservatie bevat ongeldige gegevens");
            return null;
        }
        User owner = udao.getUser(car.getOwner().getId());
        if (owner == null) {
            flash("Error", "De reservatie bevat ongeldige gegevens");
            return null;
        }
        if (!isLoaner(reservation, user) && !isOwnerOfReservedCar(context, user, reservation)
                && !DataProvider.getUserRoleProvider().hasRole(user.getId(), UserRole.RESERVATION_ADMIN)) {
            flash("Error", "Je bent niet gemachtigd om deze informatie op te vragen");
            return null;
        }
        CarRide driveInfo = null;
        if (reservation.getStatus() == ReservationStatus.DETAILS_PROVIDED || reservation.getStatus() == ReservationStatus.FINISHED)
            driveInfo = ddao.getCarRide(reservationId);

        Reservation nextReservation = rdao.getNextReservation(reservation);
        if (nextReservation != null)
            nextReservation = nextReservation.getFrom().isBefore(reservation.getTo().plusDays(1)) ? nextReservation : null;
        User nextLoaner = nextReservation != null && reservation.getStatus() == ReservationStatus.ACCEPTED ?
                udao.getUser(nextReservation.getUser().getId()) : null;

        Reservation previousReservation = rdao.getPreviousReservation(reservation);
        if (previousReservation != null)
            previousReservation = previousReservation.getTo().isAfter(reservation.getFrom().minusDays(1)) ? previousReservation : null;
        User previousLoaner = previousReservation != null && reservation.getStatus() == ReservationStatus.ACCEPTED ?
                udao.getUser(previousReservation.getUser().getId()) : null;

        return driveDetails.render(adjustForm, refuseForm, detailsForm, reservation, driveInfo, car, owner, loaner,
                previousLoaner, nextLoaner);
    }

    /**
     * Method: POST
     * <p>
     * Adjust the details of a drive. That is, adjust the date and time of the drive.
     * It's only allowed to shorten the date and/or time.
     *
     * @param reservationId the id of the reservation/drive
     * @return the detail page of specific drive/reservation after the details where adjusted
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result adjustDetails(int reservationId) {
        User user = DataProvider.getUserProvider().getUser();
        Form<RemarksModel> refuseModel = Form.form(RemarksModel.class);
        Form<InfoModel> detailsForm = Form.form(InfoModel.class);
        Form<Reserve.ReservationModel> adjustForm = Form.form(Reserve.ReservationModel.class).bindFromRequest();
        if (adjustForm.hasErrors())
            return badRequest(detailsPage(reservationId, adjustForm, refuseModel, detailsForm));
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        Reservation reservation = rdao.getReservation(reservationId);
        if (reservation == null) {
            adjustForm.reject("Er is een fout gebeurt bij het opvragen van de rit.");
            return badRequest(showIndex());
        }
        if (!isLoaner(reservation, user) && !DataProvider.getUserRoleProvider().hasRole(user.getId(), UserRole.RESERVATION_ADMIN)) {
            adjustForm.reject("Je bent niet gemachtigd deze actie uit te voeren.");
            return badRequest(showIndex());
        }
        DateTime from = adjustForm.get().getTimeFrom();
        DateTime until = adjustForm.get().getTimeUntil();
        if (from.isBefore(reservation.getFrom()) || until.isAfter(reservation.getTo())) {
            adjustForm.reject("Het is niet toegestaan de reservatie te verlengen.");
            return badRequest(detailsPage(reservationId, adjustForm, refuseModel, detailsForm));
        }
        if (reservation.getStatus() != ReservationStatus.ACCEPTED && reservation.getStatus() != ReservationStatus.REQUEST) {
            adjustForm.reject("Je kan deze reservatie niet aanpassen.");
            return badRequest(detailsPage(reservationId, adjustForm, refuseModel, detailsForm));
        }
        reservation.setFrom(from);
        reservation.setTo(until);
        rdao.updateReservation(reservation);

        if (reservation.getStatus() == ReservationStatus.REQUEST) {
            // Remove old reservation auto accept and add new
            JobDAO jdao = context.getJobDAO();
            jdao.deleteJob(JobType.RESERVE_ACCEPT, reservation.getId()); //remove the old job
            int minutesAfterNow = Integer.parseInt(context.getSettingDAO().getSettingForNow("reservation_auto_accept"));
            MutableDateTime autoAcceptDate = new MutableDateTime();
            autoAcceptDate.addMinutes(minutesAfterNow);
            jdao.createJob(JobType.RESERVE_ACCEPT, reservation.getId(), autoAcceptDate.toDateTime());
        }

        return ok(detailsPage(reservationId, adjustForm, refuseModel, detailsForm));
    }

    /**
     * Method: POST
     * <p>
     * Called when a reservation of a car is refused/accepted by the owner.
     *
     * @param reservationId the id of the reservation being refused/accepted
     * @return the drives index page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    @InjectContext
    public static Result setReservationStatus(int reservationId) {
        Form<Reserve.ReservationModel> adjustForm = Form.form(Reserve.ReservationModel.class);
        Form<InfoModel> detailsForm = Form.form(InfoModel.class);
        Form<RemarksModel> remarksForm = Form.form(RemarksModel.class).bindFromRequest();
        if (remarksForm.hasErrors())
            return badRequest(detailsPage(reservationId, adjustForm, remarksForm, detailsForm));
        ReservationStatus status = ReservationStatus.valueOf(remarksForm.get().status);
        String remarks = remarksForm.get().remarks;
        if (status == ReservationStatus.REFUSED && (remarks == null || "".equals(remarks))) {
            remarksForm.reject("Gelieve aan te geven waarom je de reservatie weigert.");
            return badRequest(detailsPage(reservationId, adjustForm, remarksForm, detailsForm));
        }
        if (status != ReservationStatus.REFUSED && status != ReservationStatus.ACCEPTED) {
            remarksForm.reject("Het is niet toegestaan om de status van de reservatie aan te passen naar: " + status.toString());
            return badRequest(detailsPage(reservationId, adjustForm, remarksForm, detailsForm));
        }
        Reservation reservation = adjustStatus(reservationId, status);
        if (reservation == null) {
            return badRequest(showIndex());
        }
        if (status == ReservationStatus.REFUSED)
            Notifier.sendReservationRefusedByOwnerMail(reservation.getUser(), remarks, reservation);
        else
            Notifier.sendReservationApprovedByOwnerMail(reservation.getUser(), remarks, reservation);
        return details(reservationId);
    }

    /**
     * Method: GET
     * <p>
     * Called when a reservation of a car is cancelled by the loaner.
     *
     * @param reservationId the id of the reservation being cancelled
     * @return the drives index page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result cancelReservation(int reservationId) {
        Reservation reservation = adjustStatus(reservationId, ReservationStatus.CANCELLED);
        if (reservation == null)
            return badRequest(showIndex());
        return details(reservationId);
    }

    /**
     * Adjust the status of a given reservation for a car.
     * This method can only be called by the owner of the car and only if the reservation is not yet approved/refused.
     *
     * @param reservationId the id of the reservation for which the status ought the be adjusted
     * @param status        the status to which the reservation is to be set
     * @return the reservation if successful, null otherwise
     */
    // used in injected context
    private static Reservation adjustStatus(int reservationId, ReservationStatus status) {
        User user = DataProvider.getUserProvider().getUser();
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO dao = context.getReservationDAO();
        Reservation reservation = dao.getReservation(reservationId);
        if (reservation == null) {
            flash("danger", "De actie die je wil uitvoeren is ongeldig: reservatie onbestaand");
            return null;
        }
        // Both super user and reservation admin are allowed to adjust the status of a reservation
        if (!(DataProvider.getUserRoleProvider().hasRole(user, UserRole.SUPER_USER))
                && !((DataProvider.getUserRoleProvider().hasRole(user, UserRole.RESERVATION_ADMIN)))) {
            switch (status) {
                // Only the loaner is allowed to cancel a reservation at any time
                case CANCELLED:
                    if (!isLoaner(reservation, user) && !DataProvider.getUserRoleProvider().hasRole(user.getId(), UserRole.RESERVATION_ADMIN)) {
                        flash("Error", "Alleen de ontlener mag een reservatie annuleren!");
                        return null;
                    } else if (reservation.getStatus() != ReservationStatus.REQUEST && reservation.getStatus() != ReservationStatus.ACCEPTED) {
                        flash("Error", "De reservatie is niet meer in aanvraag en is niet goedgekeurd!");
                        return null;
                    }
                    break;
                // The owner is allowed to approve or refuse a reservation if that reservation
                // has the request or request_new status
                default:
                    if (!isOwnerOfReservedCar(context, user, reservation)
                            || reservation.getStatus() != ReservationStatus.REQUEST) {
                        flash("Error", "Alleen de eigenaar kan de status van een reservatie aanpassen");
                        return null;
                    }
            }
        }
        reservation.setStatus(status);
        dao.updateReservation(reservation);

        // Unschedule the job for auto accept
        JobDAO jdao = context.getJobDAO();
        jdao.deleteJob(JobType.RESERVE_ACCEPT, reservation.getId());

        return reservation;
    }

    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result provideDriveInfo(int reservationId) {
        User user = DataProvider.getUserProvider().getUser();
        Form<Reserve.ReservationModel> adjustForm = Form.form(Reserve.ReservationModel.class);
        Form<RemarksModel> refuseForm = Form.form(RemarksModel.class);
        Form<InfoModel> detailsForm = Form.form(InfoModel.class).bindFromRequest();
        if (detailsForm.hasErrors())
            return badRequest(detailsPage(reservationId, adjustForm, refuseForm, detailsForm));
        DataAccessContext context = DataAccess.getInjectedContext();
        CarRideDAO dao = context.getCarRideDAO();
        ReservationDAO rdao = context.getReservationDAO();
        CarDAO carDAO = context.getCarDAO();
        Reservation reservation = rdao.getReservation(reservationId);
        // Test if reservation exists
        if (reservation == null) {
            detailsForm.reject("De reservatie kan niet opgevraagd worden. Gelieve de database administrator te contacteren.");
            return badRequest(detailsPage(reservationId, adjustForm, refuseForm, detailsForm));
        }
        // Test if user is authorized
        boolean isOwner = isOwnerOfReservedCar(context, user, reservation);
        if (!isLoaner(reservation, user) && !isOwner && !DataProvider.getUserRoleProvider().hasRole(user.getId(), UserRole.RESERVATION_ADMIN)) {
            detailsForm.reject("Je bent niet geauthoriseerd voor het uitvoeren van deze actie.");
            return badRequest(detailsPage(reservationId, adjustForm, refuseForm, detailsForm));
        }
        // Test if ride already exists
        CarRide ride = dao.getCarRide(reservationId);
        if (ride == null) {
            int refueling = detailsForm.get().refueling;
            boolean damaged = detailsForm.get().damaged;
            ride = dao.createCarRide(reservation, detailsForm.get().startMileage, detailsForm.get().endMileage,
                    damaged, refueling);
            if (refueling > 0) {
                RefuelDAO refuelDAO = context.getRefuelDAO();
                for (int i = 0; i < refueling; i++) {
                    Refuel refuel = refuelDAO.createRefuel(ride);
                }
                context.commit();
            }
            if (damaged) {
                DamageDAO damageDAO = context.getDamageDAO();
                Damage damage = damageDAO.createDamage(ride);
                context.commit();
            }
        } else if (isOwner || DataProvider.getUserRoleProvider().hasRole(user.getId(), UserRole.RESERVATION_ADMIN)) {
            // Owner is allowed to adjust the information
            ride.setStartMileage(detailsForm.get().startMileage);
            ride.setEndMileage(detailsForm.get().endMileage);
        }

        if (isOwner) {
            Instant instant = reservation.getFrom().toDate().toInstant();
            calculateDriveCost(ride,
                    isOwnerOfReservedCar(context, reservation.getUser(), reservation) || isPrivilegedUserOfReservedCar(context, reservation.getUser(), reservation),
                    context.getSettingDAO().getCostSettings(instant));
            dao.updateCarRide(ride);
        } else {
            detailsForm.reject("Je bent niet geauthoriseerd voor het uitvoeren van deze actie.");
            return badRequest(detailsPage(reservationId, adjustForm, refuseForm, detailsForm));
        }
        // Unable to create or retrieve the drive
        if (ride == null) {
            detailsForm.reject("Er is een fout gebeurd tijdens het opslaan van de gegevens. Gelieve de database administrator te contacteren.");
            return badRequest(detailsPage(reservationId, adjustForm, refuseForm, detailsForm));
        }
        // Adjust the status of the reservation
        if (isOwner) {
            reservation.setStatus(ReservationStatus.FINISHED);
        } else {
            reservation.setStatus(ReservationStatus.DETAILS_PROVIDED);
            Notifier.sendReservationDetailsProvidedMail(carDAO.getCar(reservation.getCar().getId()).getOwner(), reservation);
        }

        rdao.updateReservation(reservation);
        // Commit changes
        return ok(detailsPage(reservationId, adjustForm, refuseForm, detailsForm));
    }

    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    @InjectContext
    public static Result approveDriveInfo(int reservationId) {
        User user = DataProvider.getUserProvider().getUser();
        Form<Reserve.ReservationModel> adjustForm = Form.form(Reserve.ReservationModel.class);
        Form<RemarksModel> refuseForm = Form.form(RemarksModel.class);
        Form<InfoModel> detailsForm = Form.form(InfoModel.class);
        DataAccessContext context = DataAccess.getInjectedContext();
        CarRideDAO dao = context.getCarRideDAO();
        ReservationDAO rdao = context.getReservationDAO();
        Reservation reservation = rdao.getReservation(reservationId);
        // Test if reservation exists
        if (reservation == null) {
            detailsForm.reject("De reservatie kan niet opgevraagd worden. Gelieve de database administrator te contacteren.");
            return badRequest(detailsPage(reservationId, adjustForm, refuseForm, detailsForm));
        }
        if (!isOwnerOfReservedCar(context, user, reservation) && !DataProvider.getUserRoleProvider().hasRole(user.getId(), UserRole.RESERVATION_ADMIN)) {
            detailsForm.reject("Je bent niet geauthoriseerd voor het uitvoeren van deze actie.");
            return badRequest(detailsPage(reservationId, adjustForm, refuseForm, detailsForm));
        }
        CarRide ride = dao.getCarRide(reservationId);
        if (ride == null) {
            detailsForm.reject("Er is een fout gebeurd tijdens het opslaan van de gegevens. Gelieve de database administrator te contacteren.");
            return badRequest(detailsPage(reservationId, adjustForm, refuseForm, detailsForm));
        }
        ride.setStatus(true);
        Instant instant = reservation.getFrom().toDate().toInstant();
        calculateDriveCost(ride,
                isOwnerOfReservedCar(context, reservation.getUser(), reservation) || isPrivilegedUserOfReservedCar(context, reservation.getUser(), reservation),
                context.getSettingDAO().getCostSettings(instant));
        dao.updateCarRide(ride);
        reservation.setStatus(ReservationStatus.FINISHED);
        rdao.updateReservation(reservation);
        return ok(detailsPage(reservationId, adjustForm, refuseForm, detailsForm));
    }

    private static void calculateDriveCost(CarRide ride, boolean privileged, Costs costInfo) {
        if (privileged) {
            ride.setCost(BigDecimal.ZERO);
        } else {
            double cost = 0;
            int distance = ride.getEndMileage() - ride.getStartMileage();
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

            ride.setCost(new BigDecimal(cost));
        }
    }

    /**
     * Get the number of reservations having the provided status
     *
     * @param status       The status
     * @param userIsOwner  Extra filtering specifying the user has to be owner
     * @param userIsLoaner Extra filtering specifying the user has to be loaner
     * @return The number of reservations
     */
    // must be used with injected context
    public static int reservationsWithStatus(ReservationStatus status, boolean userIsOwner, boolean userIsLoaner) {
        User user = DataProvider.getUserProvider().getUser();
        ReservationDAO dao = DataAccess.getInjectedContext().getReservationDAO();
        return dao.numberOfReservationsWithStatus(status, user.getId(), userIsOwner, userIsLoaner);
    }

    /**
     * Private method to determine whether the user is owner of the car belonging to a reservation.
     *
     * @param context     the data access context required to communicate with the database
     * @param user        the user who is possibly the owner of the car
     * @param reservation the reservation containing the car
     * @return true if the user is the owner, false otherwise
     */
    private static boolean isOwnerOfReservedCar(DataAccessContext context, User user, Reservation reservation) {
        CarDAO cdao = context.getCarDAO();
        List<Car> cars = cdao.getCarsOfUser(user.getId());
        boolean isOwner = false;
        int index = 0;
        while (!isOwner && index < cars.size()) {
            if (cars.get(index).getId() == reservation.getCar().getId())
                isOwner = true;
            index++;
        }
        return isOwner;
    }

    private static boolean isPrivilegedUserOfReservedCar(DataAccessContext context, User user, Reservation reservation) {
        CarDAO cdao = context.getCarDAO();
        boolean isPrivileged = false;
        // TODO: see also Reserve.confirmReservation
        Iterator<User> iterator = cdao.getPrivileged(reservation.getCar().getId()).iterator();
        while (!isPrivileged && iterator.hasNext()) {
            isPrivileged = user.getId() == iterator.next().getId();
        }
        return isPrivileged;
    }

    /**
     * Private method to determine whether the user is loaner of the car belonging to a reservation.
     *
     * @param reservation the reservation containing the car
     * @param user        the user who is possibly the loaner of the car
     * @return true if the user is the owner, false otherwise
     */
    private static boolean isLoaner(Reservation reservation, User user) {
        return reservation.getUser().getId() == user.getId();
    }

    // RENDERING THE PARTIAL

    /**
     * @param page         The page in the drivelists
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of cars of the corresponding page (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated()
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

        List<Reservation> listOfReservations = dao.getReservationListPage(field, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfReservations(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return ok(drivespage.render(user.getId(), Form.form(RemarksModel.class), listOfReservations, page, amountOfResults, amountOfPages, ascInt, orderBy, searchString));
    }

    /**
     * @param page         The page in the drivelists
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of cars of the corresponding page (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.RESERVATION_ADMIN})
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

        List<Reservation> listOfReservations = dao.getReservationListPage(field, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfReservations(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return ok(drivespage.render(user.getId(), Form.form(RemarksModel.class), listOfReservations, page, amountOfResults, amountOfPages, ascInt, orderBy, searchString));
    }

}
