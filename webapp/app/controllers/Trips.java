/* Trips.java
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
import be.ugent.degage.db.dao.TripDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.trips.*;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsible for displaying information about reservations and trips.
 */
public class Trips extends Controller {

    /**
     * Overview of reservations and trips relevant to the current user.
     */
    @AllowRoles
    @InjectContext
    public static Result index(int tab) {
        if (CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
            return ok(tripsAdmin.render());
        } else {
            return ok(trips.render(tab));
        }
    }

    /**
     * Details page for  reservation or trip.
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result details(int reservationId) {
        Html result = detailsPage(DataAccess.getInjectedContext().getReservationDAO().getReservation(reservationId));
        if (result != null) {
            return ok(result);
        } else {
            return redirect(routes.Trips.index(0));
        }
    }

    /**
     * Create a details page for the given reservation or trip.
     */
    private static Html detailsPage(Reservation reservation) {
        int reservationId = reservation.getId();

        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        UserDAO udao = context.getUserDAO();
        CarRideDAO ddao = context.getCarRideDAO();
        User loaner = udao.getUser(reservation.getUserId());
        User owner = udao.getUser(reservation.getOwnerId());
        if ( ! WFCommon.isDriverOrOwnerOrAdmin(reservation)) {
            flash("danger", "Je bent niet gemachtigd om deze informatie op te vragen");
            return null;
        }
        CarRide tripInfo = null;
        if (reservation.getStatus() == ReservationStatus.DETAILS_PROVIDED || reservation.getStatus() == ReservationStatus.FINISHED)
            tripInfo = ddao.getCarRide(reservationId);

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
            return tripDetailsAsAdmin.render(
                    reservation, tripInfo, car, owner, loaner,
                    previousLoaner, nextLoaner
            );
        } else if (CurrentUser.is(owner.getId())) {
            return tripDetailsAsOwner.render(
                    reservation, tripInfo, car, loaner,
                    previousLoaner, nextLoaner
            );
        } else {
            return tripDetailsAsLoaner.render(
                    reservation, tripInfo, car, owner,
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
     * Get the number of reservations/trips having the provided status.
     */
    // must be used with injected context
    public static int reservationsWithStatus(ReservationStatus status, boolean userIsLoaner) {
        return DataAccess.getInjectedContext().getReservationDAO().numberOfReservationsWithStatus(status, CurrentUser.getId(), userIsLoaner);
    }

    // RENDERING THE PARTIAL

    /**
     * Renders a table op reservations or trips. Used by AJAX
     */
    @AllowRoles
    @InjectContext
    public static Result showTripsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
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

        return ok(tripspage.render(user.getId(), listOfReservations, page, amountOfResults, amountOfPages, ascInt, orderBy, searchString));
    }

    /**
     * Same as {@link #showTripsPage} but for an administrator.
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result showTripsAdminPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
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

        return ok(tripspage.render(user.getId(), listOfReservations, page, amountOfResults, amountOfPages, ascInt, orderBy, searchString));
    }

    private static boolean overviewAllowed (Car car) {
        return CurrentUser.hasRole(UserRole.RESERVATION_ADMIN) ||
                CurrentUser.is(car.getOwner().getId());
    }

    /**
     * Shows an overview of all trips for the given car which are in the past, starting at the given date.
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result overview(int carId, String dateString) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Car car = context.getCarDAO().getCar(carId);
        if (overviewAllowed(car)) {

            LocalDate now = LocalDate.now();
            LocalDate startDate = Utils.toLocalDate(dateString);
            if (startDate == null) {
                startDate = now;
            }
            startDate = startDate.withDayOfMonth(1);
            LocalDate endDate = startDate.plusMonths(2);
            if (endDate.isAfter(now)) {
                endDate = now;
            }
            TripDAO dao = context.getTripDAO();
            Iterable<Trip> trips = dao.listTrips(carId, startDate.atStartOfDay(), endDate.atStartOfDay());

            return ok(overview.render(
                    car,
                    trips,
                    Utils.toDateString(startDate.minusMonths(1L)),
                    dateString,
                    Utils.toDateString(startDate.plusMonths(1L))
            ));
        } else {
            return badRequest(); // hacking?
        }
    }

    public static class KmDetail {
        public Integer start;
        public Integer end;
        public boolean approve = false;
    }

    public static class KmData {
        public Map<Integer,KmDetail> km = new HashMap<>();
    }

    private static final MessageFormat RESULT_WITH_ERRORS = new MessageFormat(
        "Er {0,choice,1#is één rij|1<zijn {0} rijen} met fouten. De kilometerstanden zijn " +
        " aangepast van {1,choice,0#0 ritten|1#één rit|2<{1} ritten} en daarnaast " +
        " {2,choice,0#zijn er nog 0 ritten|1#is er nog één rit|2<zijn er nog {2} ritten} goedgekeurd."
    );

    private static final MessageFormat RESULT_WITHOUT_ERRORS = new MessageFormat(
        "De kilometerstanden zijn " +
        " aangepast van {0,choice,0#0 ritten|1#één rit|2<{0} ritten} en daarnaast " +
        " {1,choice,0#zijn er nog 0 ritten|1#is er nog één rit|2<zijn er nog {1} ritten} goedgekeurd."
    );

    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result doOverview  (int carId, String dateString) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Car car = context.getCarDAO().getCar(carId);
        if (overviewAllowed(car)) {
            KmData kmData = Form.form(KmData.class).bindFromRequest().get();
            int count = 0;
            int errorCount = 0;
            int approvedCount = 0;
            for (Map.Entry<Integer, KmDetail> entry : kmData.km.entrySet()) {
                KmDetail detail = entry.getValue();
                if (detail.approve) {
                    context.getTripDAO().approveTrip(entry.getKey());
                    approvedCount ++;
                } else if (detail.start != null && detail.end != null) {
                    if (detail.start >  0 && detail.end >= detail.start) {
                        context.getTripDAO().updateTrip(entry.getKey(), detail.start, detail.end);
                        count ++;
                    } else {
                        errorCount ++;
                    }
                } else if (detail.start != null || detail.end != null) {
                    errorCount ++;
                }
            }
            if (errorCount > 0) {
                flash ("danger", RESULT_WITH_ERRORS.format(new Integer[] {errorCount, count, approvedCount}));
            } else if (count > 0 || approvedCount > 0) {
                flash("success", RESULT_WITHOUT_ERRORS.format(new Integer[] {count, approvedCount}));
            }
            return redirect(routes.Trips.overview(carId, dateString));
        } else {
            return badRequest(); // hacking
        }
    }

}
