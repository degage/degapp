/* TripDAO.java
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

import be.ugent.degage.db.models.Trip;
import be.ugent.degage.db.models.TripWithCar;

import java.time.LocalDateTime;

/**
 * Provides information on trips (reservation + kilometers)
 */
public interface TripDAO {

    /**
     * Return a list of 'trips' between the given dates. If the end date is later
     * than now it is reduced to now. (Reservations in the future are not considered to be trips.)
     * Does not show reservations that are cancelled or refused.
     */
    public Iterable<Trip> listTrips (int carId, LocalDateTime from, LocalDateTime until);



    // TODO: CarRideDAO contains similar methods as those below

    /**
     * Indicate that this trip has been approved by the owner. Only works with trips with a
     * status of DETAILS_PROVIDED
     */
    // TODO: this field is never used?
    public void approveTrip (int tripId);

    /**
     * Update the km entries for the given trip. Only works with trips with a status of DETAILS_REJECTED
     * or REQUEST_DETAILS. Automatically approveds the trip.
     */
    public void updateTrip (int tripId, int start, int end);


    /**
     * Return the details for the given trip, including a car header
     * @param withLocation also fill in the location of the car
     */
    public TripWithCar getTripAndCar (int id, boolean withLocation);





}
