/* ReservationDAO.java
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
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationHeader;
import be.ugent.degage.db.models.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 */
public interface ReservationDAO {

    /**
     * Add a new reservation to the database. If the user is privileged for the car then the reservation is accepted automatically. If the
     * from date is in the past, the reservation is given status REQUEST_DETAILS automatically. No message is stored in the database.
     */
    public ReservationHeader createReservation(LocalDateTime from, LocalDateTime until, int carId, int userId) throws DataAccessException;


    /**
     * Returns the reservation with the given id.
     */
    public Reservation getReservation (int id) throws DataAccessException;

    /**
     * Same as {@link #getReservation} but includes car location
     */
    public Reservation getReservationExtended (int id) throws DataAccessException;

    public ReservationHeader getReservationHeader (int id) throws DataAccessException;

    //public ReservationHeader getReservationHeaderForRefuel (int refuelId) throws DataAccessException;

    /**
     * Update reservation status including an optional message. Should only be used when
     * refusing a reservation or cancelling a reservation that is already in the past
     */
    public void updateReservationStatus (int reservationId, ReservationStatus status, String message);

    /**
     * Update reservation status but not the message.
     */
    public void updateReservationStatus (int reservationId, ReservationStatus status);


    public void updateReservationTime (int reservationId, LocalDateTime from, LocalDateTime until);

    /**
     * Return the first accepted reservation that follows the given reservation, unless it is more
     * than a day removed (for the same car). Archived reservations are not returned.
     */
    public Reservation getNextReservation(int reservationId) throws DataAccessException;

    /**
     * Return the last accepted  reservation that preceeds the given reservation, unless it is more
     * than a day removed. Archived reservations are not returned.
     */
    public Reservation getPreviousReservation(int reservationId) throws DataAccessException;

    /**
     * Return the first trip (that has actually taken place) that follows the given reservation,
     * for the same car. Archived reservations are not returned.
     * @return 0 if no such trip
     */
    public int getNextTripId (int reservationId) throws DataAccessException;


    /**
     * Return the last trip (that has actually taken place) that preceeds the given reservation, for the same car.
     * Archived reservations are not returned.
     * @return 0 if no such trip
     */
    public int getPreviousTripId (int reservationId) throws DataAccessException;

    /**
     * Return the first trip (that has actually taken place) for a certain car after or at a certain date.
     */
    public int getFirstTripAfterDate (int carId, LocalDate date) throws DataAccessException;

    /**
     * Is there any reservation that overlaps the given period?
     */
    public boolean hasOverlap (int carId, LocalDateTime from, LocalDateTime until);



    public void deleteReservation(Reservation reservation) throws DataAccessException;

    // note: only shows reservations that are not archived
    public Page<Reservation> getReservationListPage(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    public Iterable<ReservationHeader> listReservationsForCarInPeriod (int carID, LocalDateTime from, LocalDateTime to) throws DataAccessException;

    public int numberOfReservationsWithStatus(ReservationStatus status, int userId, boolean userIsLoaner);

   /**
     * List of reservations for a certain car. Used in {@link #listCRInfo}
     */
    public static class CRInfo {
        public int carId;

        public String carName;

        public Collection<ReservationHeader> reservations;
    }

    /**
     * Return information on all reservations (except those cancelled) during a certain period of time, ordered by car.
     * @param uid User id used for sorting the cars
     */
    public Iterable<CRInfo> listCRInfo (LocalDateTime from, LocalDateTime to, int uid);


}
