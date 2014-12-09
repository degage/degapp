package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.*;
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
import views.html.reserve.reservationDetailsPartial;
import views.html.reserve.reservations;
import views.html.reserve.start;
import views.html.reserve.availablecarspage;
import views.html.errortablerow;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Controller responsible to display and filter cars for reservation and to enable a user to reserve a car.
 */
public class Reserve extends Controller {

    public static class IndexModel {
        // Date from
        public String from;
        // Date until
        public String until;
    }

    /**
     * Class implementing a model wrapped in a form.
     * This model is used during the form submission when a user submits
     * a reservation for a car.
     * The user is obligated to provide information about the reservation:
     * - the start date and time
     * - the end date and time
     */
    public static class ReservationModel {
        // Date and time from which the user wants to loan the car
        public String from;
        // Date and time the user will return the car to the owner
        public String until;

        public String message;

        /**
         * @return the start datetime of the reservation
         */
        public LocalDateTime getTimeFrom() {
            return Utils.toLocalDateTime(from);
        }

        /**
         * @return the end datetime of the reservation
         */
        public LocalDateTime getTimeUntil() {
            return Utils.toLocalDateTime(until);
        }

        /**
         * Validates the form:
         * - the start date and time, and the end date and time are specified
         * - the start date and time of a reservation is before the end date and time
         * - the start date is after the date of today
         *
         * @return an error string or null
         */
        public String validate() {
            LocalDateTime dateFrom = null;
            LocalDateTime dateUntil;
            try {
                dateFrom = getTimeFrom();
                dateUntil = getTimeUntil();
            } catch (IllegalArgumentException ex) {
                if (dateFrom == null) {
                    return "Ongeldig datum: van = " + from;
                } else {
                    return "Ongeldig datum: tot = " + until;
                }
            }
            if ("".equals(dateFrom) || "".equals(dateUntil)) // TODO string compared to date
            {
                return "Gelieve zowel een begin als einddatum te selecteren!";
            } else if (dateFrom.isAfter(dateUntil) || dateFrom.isEqual(dateUntil)) {
                return "De einddatum kan niet voor de begindatum liggen!";
            }
            return null;
        }

    }

    /**
     * Method: GET
     *
     * @return the reservation index page containing one specific car
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result indexWithCar(String carName, int id) {
        return ok(reservations.render("", carName, id, "", ""));
    }

    /**
     * @return The html context of the reservations index page
     */
    private static Html showIndex() {
        return reservations.render("", "", -1, "", "");
    }

    /**
     * Method: GET
     * <p>
     * Render the details page of a future reservation for a car where the user is able to
     * confirm the reservation and specify the start and end of the reservation
     *
     * @param carId the id of the car for which the reservation details ought to be rendered
     * @param from  the string containing the date and time of the start of the reservation
     * @param until the string containing the date and time of the end of the reservation
     * @return the details page of a future reservation for a car
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result reserve(int carId, String from, String until) {
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        Car car = dao.getCar(carId);
        if (car == null) {
            flash("danger", "De reservatie van deze auto is onmogelijk: auto onbestaand!");
            return badRequest(showIndex());
        } else {
            return ok(reservationDetailsPartial.render(car, from, until, Form.form(ReservationModel.class)));
        }
    }

    /**
     * Method: POST
     * <p>
     * Confirmation of a reservation. The reservation is validated.
     * If the reservation is valid, the reservation is created and the owner is
     * informed of the request for a reservation.
     *
     * @param carId The id of the car for which the reservation is being confirmed
     * @return the user is redirected to the drives page
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result confirmReservation(int carId) {
        // Request the form
        Form<ReservationModel> reservationForm = Form.form(ReservationModel.class).bindFromRequest();
        if (reservationForm.hasErrors()) {
            return badRequest(reservations.render(reservationForm.globalError().message(), "", -1, "", ""));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            Car car = context.getCarDAO().getCar(carId);
            // Test whether the reservation is valid
            ReservationModel formData = reservationForm.get();
            LocalDateTime from = formData.getTimeFrom();
            LocalDateTime until = formData.getTimeUntil();
            ReservationDAO rdao = context.getReservationDAO();
            // TODO: create 'checkOverlap'
            for (ReservationHeader reservation : rdao.listReservationsForCar(carId)) {
                if ((reservation.getStatus() != ReservationStatus.REFUSED && reservation.getStatus() != ReservationStatus.CANCELLED) &&
                        (from.isBefore(reservation.getUntil()) && until.isAfter(reservation.getFrom()))) {
                    return badRequest(reservations.render("De reservatie overlapt met een reeds bestaande reservatie!", "", -1, "", ""));
                }
            }

            // Create the reservationCars
            ReservationHeader reservation = rdao.createReservation(from, until, carId, CurrentUser.getId(), formData.message);
            if (reservation.getStatus() != ReservationStatus.ACCEPTED) {
                // Schedule the auto accept
                context.getJobDAO().createJob(    // TODO: number of minutes after now as parameter?
                        JobType.RESERVE_ACCEPT,
                        reservation.getId(),
                        Instant.now().plusSeconds(60 * Integer.parseInt(context.getSettingDAO().getSettingForNow("reservation_auto_accept")))
                    );

                // note: user contained in this record was null
                // TODO: avoid having to retrieve the whole record
                Reservation res = rdao.getReservation(reservation.getId());
                Notifier.sendReservationApproveRequestMail(
                        car.getOwner(), res
                );
            }
            return redirect(routes.Drives.index());
        }
    }


    /**
     * Show an initial page with a reservation search form. This page uses Ajax to show all cars available for
     * reservation in a certain period
     * @return
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result index() {
        return ok(start.render());
    }

    /**
     * Ajax call as a result of the 'search' button in  str=art.
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result listAvailableCarsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {

        FilterField field = FilterField.stringToField(orderBy);
        if (field == null) {
            field = FilterField.CAR_NAME;
        }

        boolean asc = Pagination.parseBoolean(ascInt);

        Filter filter = Pagination.parseFilter(searchString);

        String validationError = null;
        String fromString = filter.getValue(FilterField.FROM);
        String untilString = filter.getValue(FilterField.UNTIL);

        //System.err.printf("FromString = [%s], untilstring = [%s]\n", fromString, untilString);
        try {
            if (fromString.isEmpty() || untilString.isEmpty()) {
                validationError = "Gelieve beide tijdstippen in te vullen";
            }  else {
                LocalDateTime from = Utils.toLocalDateTime(fromString);
                LocalDateTime until = Utils.toLocalDateTime(untilString);
                if (!until.isAfter(from)) {
                    validationError = "Het einde van de periode moet na het begin van de periode liggen";
                }
            }
        } catch (IllegalArgumentException ex) {
            validationError = "Gelieve geldige datums in te geven";
        }
        if (validationError != null) {
            return ok (errortablerow.render(validationError));
        }

        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        Iterable<Car> listOfCars = dao.getCarList(field, asc, page, pageSize, filter);

        int numberOfResults = dao.getAmountOfCars(filter);
        int numberOfPages = (int) Math.ceil(numberOfResults / (double) pageSize);

        return ok(availablecarspage.render(listOfCars, page, numberOfResults, numberOfPages, fromString, untilString));
    }

    public static class ReservationData {
        @Constraints.Required
        public LocalDateTime from;

        @Constraints.Required
        public LocalDateTime until;

        public String message;

        public List<ValidationError> validate () {
            if (! from.isBefore(until)) {
                return Arrays.asList(new ValidationError("until", "Het einde van de periode moet na het begin van de periode liggen"));
            } else {
                return null;
            }
        }
    }

    /**
     * Show the page to make a reservation for a specific car during a specific period
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result reserveCar(int carId, String fromString, String untilString) {
        Car car = DataAccess.getContext().getCarDAO().getCar(carId);

        // query string binders are quite complicated to write :-(
        ReservationData data = new ReservationData();
        data.from = Utils.toLocalDateTime(fromString);
        data.until = Utils.toLocalDateTime(untilString);
        Form<ReservationData> form = new Form<>(ReservationData.class).fill(data);

        return ok (views.html.reserve.reservation.render(form,car));
    }

    /**
     * Process the reservation made in {@link #reserveCar}
     * @param carId
     * @return
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result doReservation(int carId) {
        Form<ReservationData> form = new Form<>(ReservationData.class).bindFromRequest();
        if (form.hasErrors()) {
            Car car = DataAccess.getContext().getCarDAO().getCar(carId);
            return ok (views.html.reserve.reservation.render(form,car));
        }


        DataAccessContext context = DataAccess.getInjectedContext();
        Car car = context.getCarDAO().getCar(carId);
        // Test whether the reservation is valid
        ReservationData data = form.get();
        LocalDateTime from = data.from;
        LocalDateTime until = data.until;
        ReservationDAO rdao = context.getReservationDAO();
        if (rdao.hasOverlap(carId, from, until))  {
            String errorMessage = "De reservatie overlapt met een bestaande reservatie";
            form.reject ("from", errorMessage);
            form.reject ("until", errorMessage);
            return ok (views.html.reserve.reservation.render(form,car));
        }

        ReservationHeader reservation = rdao.createReservation(from, until, carId, CurrentUser.getId(), data.message);
        if (reservation.getStatus() != ReservationStatus.ACCEPTED) {
            // Reservations by the owner were accepted automatically

            // Schedule the auto accept
            int minutesAfterNow = Integer.parseInt(context.getSettingDAO().getSettingForNow("reservation_auto_accept"));

            context.getJobDAO().createJob(
                    JobType.RESERVE_ACCEPT, reservation.getId(),
                    Instant.now().plusSeconds(60*Integer.parseInt(context.getSettingDAO().getSettingForNow("reservation_auto_accept")))
            );

            // note: user contained in this record was null
            // TODO: avoid having to retrieve the whole record
            Reservation res = rdao.getReservation(reservation.getId());
            Notifier.sendReservationApproveRequestMail(
                    car.getOwner(), res
            );
        }
        return redirect(routes.Drives.index());
    }

}