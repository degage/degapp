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
        Html result = detailsPage(DataAccess.getInjectedContext().getTripDAO().getTripAndCar(reservationId,true));
        if (result != null) {
            return ok(result);
        } else {
            return redirect(routes.Trips.index(0));
        }
    }

    /**
     * Create a details page for the given reservation or trip.
     */
    private static Html detailsPage(TripAndCar trip) {
        int reservationId = trip.getId();

        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO rdao = context.getReservationDAO();
        UserDAO udao = context.getUserDAO();
        UserHeader driver = udao.getUserHeader(trip.getDriverId());
        UserHeader owner = udao.getUserHeader(trip.getOwnerId());
        if ( ! WFCommon.isDriverOrOwnerOrAdmin(trip)) {
            flash("danger", "Je bent niet gemachtigd om deze informatie op te vragen");
            return null;
        }
        ReservationStatus status = trip.getStatus();

        UserHeader previousDriver = null;
        UserHeader nextDriver = null;
        if (status == ReservationStatus.ACCEPTED) {
            Reservation nextReservation = rdao.getNextReservation(reservationId);
            if (nextReservation != null) {
                nextDriver = udao.getUserHeader(nextReservation.getDriverId());
            }
            Reservation previousReservation = rdao.getPreviousReservation(reservationId);
            if (previousReservation != null) {
                previousDriver = udao.getUserHeader(previousReservation.getDriverId());
            }
        }

        if (CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
            return tripDetailsAsAdmin.render(
                    trip, owner, driver, previousDriver, nextDriver
            );
        } else if (CurrentUser.is(owner.getId())) {
            return tripDetailsAsOwner.render(
                    trip, driver, previousDriver, nextDriver
            );
        } else {
            return tripDetailsAsDriver.render(
                    trip, owner, previousDriver, nextDriver
            );
        }
    }


    /**
     * Get the number of reservations/trips having the provided status.
     */
    // must be used with injected context - used in driver menu
    public static int reservationsWithStatus(ReservationStatus status, boolean userIsDriver) {
        return DataAccess.getInjectedContext().getReservationDAO().numberOfReservationsWithStatus(status, CurrentUser.getId(), userIsDriver);
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

        ReservationDAO dao = DataAccess.getInjectedContext().getReservationDAO();

        if (field == null) {
            field = FilterField.FROM;
        }

        // We only want reservations from the current user (or his car(s))
        filter.putValue(FilterField.RESERVATION_USER_OR_OWNER_ID, CurrentUser.getId());

        Iterable<Reservation> listOfReservations = dao.getReservationListPage(field, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfReservations(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return ok(tripspage.render(CurrentUser.getId(), listOfReservations, page, amountOfResults, amountOfPages, ascInt, orderBy, searchString));
    }

    /**
     * Same as {@link #showTripsPage} but for an administrator.
     */
    @AllowRoles({UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result showTripsAdminPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        ReservationDAO dao = DataAccess.getInjectedContext().getReservationDAO();

        if (field == null) {
            field = FilterField.FROM;
        }

        Iterable<Reservation> listOfReservations = dao.getReservationListPage(field, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfReservations(filter);
        int amountOfPages = (amountOfResults + pageSize - 1)/ pageSize;

        return ok(tripspage.render(CurrentUser.getId(), listOfReservations, page, amountOfResults, amountOfPages, ascInt, orderBy, searchString));
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
            return overviewPage(Form.form(KmData.class), dateString, car);
        } else {
            return badRequest(); // hacking?
        }
    }

    // common code of overview and doOverview
    private static Result overviewPage(Form<KmData> form, String dateString, Car car) {
        DataAccessContext context = DataAccess.getInjectedContext();
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
        Iterable<Trip> trips = dao.listTrips(car.getId(), startDate.atStartOfDay(), endDate.atStartOfDay());

        return ok(overview.render(
                car,
                trips,
                form,
                Utils.toDateString(startDate.minusMonths(1L)),
                dateString,
                Utils.toDateString(startDate.plusMonths(1L)),
                Utils.toLocalizedDateString(startDate)
        ));
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
            Form<KmData> form = Form.form(KmData.class).bindFromRequest();
            if (! form.hasErrors()) {
                int errors = 0;
                KmData kmData = form.get();
                for (Map.Entry<Integer, KmDetail> entry : kmData.km.entrySet()) {
                    KmDetail detail = entry.getValue();
                    Integer id = entry.getKey();
                    if (detail.approve) {
                        context.getTripDAO().approveTrip(id);
                    } else if (detail.start != null && detail.end != null) {
                        if (detail.start > 0 && detail.end >= detail.start) {
                            context.getTripDAO().updateTrip(id, detail.start, detail.end);
                        } else {
                            errors ++;
                            form.reject("km[" + id +"].start","Ongeldige km-stand");
                        }
                    } else if (detail.start != null || detail.end != null) {
                        errors ++;
                        form.reject("km[" + id +"].start","Beide waarden invullen aub!");
                    }
                }
                if (errors > 0) {
                   form.reject ("Opgelet. Enkele gegevens konden niet worden doorgestuurd " +
                           "(zie rode velden in de tabel).");
                }
            } else {
                form.reject ("Er werden ongeldige waarden ingevuld (zie rode velden in de tabel). " +
                        "Geen enkel van de ingevulde gegevens werd doorgestuurd.");
            }
            return overviewPage(form, dateString, car);
        } else {
            return badRequest(); // hacking
        }
    }

}
