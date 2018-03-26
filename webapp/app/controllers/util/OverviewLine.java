/* OverviewLine.java
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

package controllers.util;

import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.ReservationHeader;
import controllers.Utils;
import play.twirl.api.Html;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import db.CurrentUser;

/**
 * Represents a single line which can be displayed in a calendar overview
 */
public class OverviewLine {

    public static final int START_HOUR = 7;   // must be < 24
    public static final int END_HOUR = 24;    // can be >= 24

    private static final int MINUTES_PER_INTERVAL = 15; // must be exactly divisible into 60
    public static final int INTERVALS_PER_HOUR = 60 / MINUTES_PER_INTERVAL;

    public static final int NUMBER_OF_INTERVALS = (END_HOUR - START_HOUR) * INTERVALS_PER_HOUR;

    public static final long SECONDS_IN_DAY = 24 * 3600L;


    public int carId;

    public Html lineHeader;

    /**
    * Possible values are: FREE and all reservationStatus enums
    */
    public String[] reservationStatus = new String[NUMBER_OF_INTERVALS];

    /**
    * True if the current user made this reservation.
    * (Only valid if status is not free)
    */
    public boolean[] byCurrentUser = new boolean[NUMBER_OF_INTERVALS];

    // times for each 15-minute period. null means: not free
    public String[] freeTimes = new String[NUMBER_OF_INTERVALS];

    private void fillFreeTimes(LocalDate date, Iterable<ReservationHeader> reservations) {
        // by default all times are free
        for (int i = 0; i < freeTimes.length; i++) {
            long secondOfDay = 3600L * START_HOUR + 60L * i * MINUTES_PER_INTERVAL;
            LocalDateTime time = date.plusDays(secondOfDay / SECONDS_IN_DAY).atTime(LocalTime.ofSecondOfDay(secondOfDay % SECONDS_IN_DAY));
            freeTimes[i] = Utils.toString(time);
            reservationStatus[i] = "FREE";
        }
        // block all reserved times for this date
        LocalDateTime startMoment = date.atTime(LocalTime.of(START_HOUR, 0));

        for (ReservationHeader reservation : reservations) {
            long startIndex = Duration.between(startMoment, reservation.getFrom()).toMinutes() / MINUTES_PER_INTERVAL;
            long endIndex = (Duration.between(startMoment, reservation.getUntil()).toMinutes() + MINUTES_PER_INTERVAL - 1) / MINUTES_PER_INTERVAL;
            if (startIndex < 0) {
                startIndex = 0;
            }
            if (endIndex > freeTimes.length) {
                endIndex = freeTimes.length;
            }
            if (startIndex < freeTimes.length && endIndex > 0) {
                for (int i = (int) startIndex; i < (int) endIndex; i++) {
                    freeTimes[i] = null;
                    byCurrentUser[i] = CurrentUser.getId() == reservation.getDriverId();
                    reservationStatus[i] = reservation.getStatus().name();
                }
            }
        }
    }

    /**
    * This method is added to retrieve this value in scala files.
    * In a scala file it is hard to retrieve this value directly because the
    * field's name contains an underscore which is a special character in scala.
    */
    public int getNumberOfIntervals() {
      return NUMBER_OF_INTERVALS;
    }

    /**
     * Populate this line from a CRInfo object
     */
    public void populate(ReservationDAO.CRInfo info, LocalDate date) {
        carId = info.carId;
        lineHeader = views.html.calendars.carheader.render(info.carName, info.carId);
        fillFreeTimes(date, info.reservations);
    }

    /**
     * Populate this line from car reservations
     */
    public void populate(Iterable<ReservationHeader> reservations, int carId, LocalDate date) {
        this.carId = carId;
        lineHeader = new Html(Utils.toLocalizedWeekDayString(date));
        fillFreeTimes(date, reservations);
    }

    /**
     * Is there still a free period on this line?
     */
    public boolean hasFree() {
        for (String freeTime : freeTimes) {
            if (freeTime != null) {
                return true;
            }
        }
        return false;
    }
}
