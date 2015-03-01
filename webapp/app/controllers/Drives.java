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
import be.ugent.degage.db.dao.CarRideDAO;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.drives.*;

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
        if ( ! Workflow.isDriverOrOwnerOrAdmin(reservation)) {
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



}
