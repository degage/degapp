package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.dao.JobDAO;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.*;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.reserve.reservationDetailsPartial;
import views.html.reserve.reservations;
import views.html.reserve.reservationspage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Controller responsible to display and filter cars for reservation and to enable a user to reserve a car.
 */
public class Reserve extends Controller {

    // Formatter to translate a string to a datetime
    private static final DateTimeFormatter DATETIMEFORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

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
        public DateTime getTimeFrom() {
            return DATETIMEFORMATTER.parseDateTime(from).withSecondOfMinute(0);
        }

        /**
         * @return the end datetime of the reservation
         */
        public DateTime getTimeUntil() {
            return DATETIMEFORMATTER.parseDateTime(until).withSecondOfMinute(0);
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
            DateTime dateFrom = null;
            DateTime dateUntil;
            try {
                dateFrom = getTimeFrom();
                dateUntil = getTimeUntil();
            } catch (IllegalArgumentException ex) {
                if (dateFrom == null)
                    return "Ongeldig datum: van = " + from;
                else
                    return "Ongeldig datum: tot = " + until;
            }
            if ("".equals(dateFrom) || "".equals(dateUntil)) // TODO string compared to date
                return "Gelieve zowel een begin als einddatum te selecteren!";
            else if (dateFrom.isAfter(dateUntil) || dateFrom.isEqual(dateUntil))
                return "De einddatum kan niet voor de begindatum liggen!";
            return null;
        }

    }

    /**
     * Method: GET
     *
     * @return the reservation index page containing all cars
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result index() {
        return ok(showIndex());
    }

    /**
     * Method: GET
     *
     * @return the reservation index page containing one specific car
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result indexWithCar(String carName, int id) {
        return ok(showIndex(carName, "", "", id));
    }

    /**
     * Method: GET
     *
     * @return the reservation index page containing one specific car
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result indexWithDate() {
        Form<IndexModel> form = Form.form(IndexModel.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(showIndex());
        }
        return ok(showIndex("", form.get().from, form.get().until, -1));
    }

    /**
     * @return The html context of the reservations index page with date from and until and car added
     */
    private static Html showIndex(String carName, String from, String until, int id) {
        return reservations.render("", carName, id, from, until);
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
     * @param carId the id of the car for which the reservationsdetails ought to be rendered
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
        // Get the car object to test whether the operation is legal
        Car car;
        DataAccessContext context = DataAccess.getInjectedContext();
        CarDAO dao = context.getCarDAO();
        car = dao.getCar(carId);
        if (car == null) {
            flash("danger", "De reservatie van deze auto is onmogelijk: auto onbestaand!");
            return badRequest(showIndex());
        }
        ReservationDAO rdao = context.getReservationDAO();
        List<Reservation> res = rdao.getReservationListForCar(carId);
        // Request the form
        Form<ReservationModel> reservationForm = Form.form(ReservationModel.class).bindFromRequest();
        if (reservationForm.hasErrors()) {
            return badRequest(reservations.render(reservationForm.globalError().message(), "", -1, "", ""));
        } else {
            // Test whether the reservation is valid
            DateTime from = reservationForm.get().getTimeFrom();
            DateTime until = reservationForm.get().getTimeUntil();
            for (Reservation reservation : res) {
                if ((reservation.getStatus() != ReservationStatus.REFUSED && reservation.getStatus() != ReservationStatus.CANCELLED) &&
                        (from.isBefore(reservation.getTo()) && until.isAfter(reservation.getFrom()))) {
                    return badRequest(reservations.render("De reservatie overlapt met een reeds bestaande reservatie!", "", -1, "", ""));
                }
            }

            // Create the reservation
            User user = DataProvider.getUserProvider().getUser();
            Reservation reservation = rdao.createReservation(from, until, car, user, reservationForm.get().message);

            // Schedule the auto accept
            JobDAO jdao = context.getJobDAO();
            int minutesAfterNow = Integer.parseInt(context.getSettingDAO().getSettingForNow("reservation_auto_accept"));
            MutableDateTime autoAcceptDate = new MutableDateTime();
            autoAcceptDate.addMinutes(minutesAfterNow);
            jdao.createJob(JobType.RESERVE_ACCEPT, reservation.getId(), autoAcceptDate.toDateTime());

            context.commit();

            // Check if user is owner or privileged
            // TODO: delegate this to db module
            boolean autoAccept = car.getOwner().getId() == user.getId();
            if (! autoAccept) {
                Iterator<User> iterator = dao.getPrivileged(carId).iterator();
                while (! autoAccept && iterator.hasNext()) {
                    autoAccept = user.getId() == iterator.next().getId();
                }
            }
            if (autoAccept) {
                reservation.setStatus(ReservationStatus.ACCEPTED);
                rdao.updateReservation(reservation);
                context.commit();
            } else {
                Notifier.sendReservationApproveRequestMail(car.getOwner(), reservation);
            }
            return redirect(routes.Drives.index());
        }
    }

    // Partial

    /**
     * Method: GET
     * <p>
     * Method to render a partial page containing an amount of cars for reservation that ought to be displayed
     * corresponding a:
     * - a search string
     * - the number of cars to be rendered per page
     * - the page that's being rendered
     *
     * @param page         the page to be rendered
     * @param ascInt       boolean int, if 1 the records are ordered ascending
     * @param orderBy      the string designating the filterfield on which to order
     * @param searchString the string containing all search information
     * @return the requested page of cars for reservation
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result showCarsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();

        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        if (field == null) {
            field = FilterField.CAR_NAME;
        }
        try {
            DATETIMEFORMATTER.parseDateTime(filter.getValue(FilterField.FROM));
            DATETIMEFORMATTER.parseDateTime(filter.getValue(FilterField.UNTIL));
        } catch (IllegalArgumentException ex) {
            return ok(reservationspage.render(new ArrayList<>(), page, 0, 0, false));
        }

        Iterable<Car> listOfCars = dao.getCarList(field, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfCars(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return ok(reservationspage.render(listOfCars, page, amountOfResults, amountOfPages, true));
    }
}
