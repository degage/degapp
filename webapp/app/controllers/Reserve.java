/* Reserve.java
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
import views.html.errortablerow;
import views.html.reserve.*;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Controller responsible to display and filter cars for reservation and to enable a user to reserve a car.
 */
public class Reserve extends Controller {

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

        public ReservationData populate(LocalDateTime from, LocalDateTime until) {
            this.from = from;
            this.until = until;
            return this;
        }

    }

    /**
     * Show the page to make a reservation for a specific car during a specific period
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result reserveCar(int carId, String fromString, String untilString) {
        return ok(reservation.render(
                // query string binders are quite complicated to write :-(
                new Form<>(ReservationData.class).fill(
                        new ReservationData().populate(
                                Utils.toLocalDateTime(fromString),
                                untilString.isEmpty() ? null : Utils.toLocalDateTime(untilString)
                        )
                ),
                DataAccess.getContext().getCarDAO().getCar(carId)
        ));
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
            return ok (reservation.render(form,car));
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
            return ok (reservation.render(form,car));
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
        return redirect(routes.Drives.index("ACCEPTED"));
    }

    public static final int MINUTES_PER_INTERVAL = 15; // must be exactly divisible into 60
    public static final int INTERVALS_PER_HOUR = 60/ MINUTES_PER_INTERVAL;
    public static final int END_HOUR = 22;
    public static final int START_HOUR = 7;
    public static final LocalTime START_TIME = LocalTime.of(START_HOUR, 0);
    public static final int NUMBER_OF_INTERVALS = (END_HOUR-START_HOUR)*INTERVALS_PER_HOUR;

    private static void fillFreeTimes(LocalDate date, Iterable<ReservationHeader> reservations, String[] table) {
        // by default all times are free
        for (int i = 0; i < table.length; i++) {
            LocalTime localTime = LocalTime.ofSecondOfDay(3600L * START_HOUR + 60L * i * MINUTES_PER_INTERVAL);
            LocalDateTime time = date.atTime(localTime);
            table[i] = Utils.toString(time);
        }
        // block all reserved times for this date
        LocalDateTime startMoment = date.atTime(START_TIME);

        for (ReservationHeader reservation : reservations) {
            long startIndex = Duration.between(startMoment, reservation.getFrom()).toMinutes() / MINUTES_PER_INTERVAL;
            long endIndex = (Duration.between(startMoment, reservation.getUntil()).toMinutes() + MINUTES_PER_INTERVAL-1) / MINUTES_PER_INTERVAL;
            if (startIndex < 0) {
                startIndex = 0;
            }
            if (endIndex > table.length) {
                endIndex = table.length;
            }
            if (startIndex < table.length && endIndex > 0) {
                for (int i = (int)startIndex; i < (int)endIndex; i++) {
                    table[i] = null;
                }
            }
        }
    }

    /**
     * Represents a single line which can be displayed in an overview
     */
    public static class OverviewLine {


        public int carId;
        public String lineHeader;

        // times for each 15-minute period. null means: not free
        public String[] freeTimes = new String[NUMBER_OF_INTERVALS];

        /**
         * Populate this line from a CRInfo object
         */
        public void populate (ReservationDAO.CRInfo info, LocalDate date) {
            carId = info.carId;
            lineHeader = info.carName;
            fillFreeTimes(date, info.reservations, freeTimes);
        }

        /**
         * Populate this line from car reservations
         */
        public void populate (Iterable<ReservationHeader> reservations, int carId, LocalDate date) {
            this.carId = carId;
            lineHeader = Utils.toLocalizedDateString(date);

            fillFreeTimes(date, reservations, freeTimes);
        }


        /**
         * Is there still a free period on this line?
         */
        public boolean hasFree () {
            for (String freeTime : freeTimes) {
                if (freeTime != null) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class DateData {
        @Constraints.Required
        public String date;

    }

    /**
     * Show an overview of reservations during a certain day. Do not show cars that are not available
     * @return
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result overview (String dateString) {
        LocalDate date = dateString == null ? LocalDate.now() : Utils.toLocalDate(dateString);
        LocalDateTime from = date.atTime(START_HOUR, 0);
        LocalDateTime until = date.atTime(END_HOUR, 0);

        ReservationDAO dao = DataAccess.getInjectedContext().getReservationDAO();
        Iterable<ReservationDAO.CRInfo> list = dao.listCRInfo(from,until);
        Collection<OverviewLine> lines = new ArrayList<>();
        for (ReservationDAO.CRInfo crInfo : list) {
            OverviewLine line = new OverviewLine();
            line.populate(crInfo, date);
            if (line.hasFree()) {
                 lines.add(line);
            }
        }

        DateData data = new DateData();
        data.date = Utils.toDateString(date); // cannot use dateString!, might be empty
        return ok(overview.render(
                Form.form(DateData.class).fill(data),
                lines, date,
                date.minusDays(1L), date.plusDays(1L)
        ));

    }

    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result overviewPost () {
        DateData data = Form.form(DateData.class).bindFromRequest().get();
        return overview(data.date);
    }

    public static class CarDateData extends DateData {
        public int carId;

    @Constraints.Required
        public String carIdAsString;
    }

    /**
     * Use form information and dispatch to the car overview page
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result startCar () {
        CarDateData data = new CarDateData();
        data.date = Utils.toDateString(LocalDate.now());
        return ok(startcar.render(Form.form(CarDateData.class).fill(data)));
    }

    private static Result overviewCar(Form<CarDateData> form) {
        CarDateData data = form.get();
        LocalDateTime from = Utils.toLocalDate(data.date).atStartOfDay();
        LocalDateTime until = from.plusDays(7);
        Iterable<ReservationHeader> reservations =
                DataAccess.getInjectedContext().getReservationDAO().
                        listReservationsForCarInPeriod(data.carId, from, until);
        Collection<OverviewLine> lines = new ArrayList<>();
        for (int i = 0; i < 7; i++) { // one week
            OverviewLine line = new OverviewLine();
            line.populate(reservations, data.carId, from.toLocalDate().plusDays(i));
            lines.add(line);
        }

        return ok(overviewcar.render(form, lines));
    }

    /**
     * Same as overviewCarPost, but with car id and car name already filled in and day of today
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result indexWithCar(String carName, int carId) {
        CarDateData data = new CarDateData();
        data.carId = carId;
        data.carIdAsString = carName;
        data.date = Utils.toDateString(LocalDate.now());
        return overviewCar(Form.form(CarDateData.class).fill (data));
    }

    /**
     * Show an overview of reservations for a specific car during a certain week.
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result overviewCarPost () {
        Form form = Form.form(CarDateData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(startcar.render(form));
        } else {
            return overviewCar(form);
        }
    }

}
