/* CarRideDAO.java
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
import be.ugent.degage.db.models.CarRide;
import be.ugent.degage.db.models.Reservation;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by HannesM on 10/03/14.
 */
public interface CarRideDAO {
    public CarRide createCarRide(Reservation reservation, int startMileage, int endMileage, boolean damaged) throws DataAccessException;

    /**
     * Returns the trip details of the given ride. Returns null if no trip details are available.
     */
    public CarRide getCarRide(int id) throws DataAccessException;

    public void updateCarRide(CarRide carRide) throws DataAccessException;
    public void updateCarRideKm(int rideId, int startKm, int endKm) throws DataAccessException;
    public void endPeriod() throws DataAccessException;
    public List<CarRide> getBillRidesForLoaner(LocalDate date, int user) throws DataAccessException;
    public List<CarRide> getBillRidesForCar(LocalDate date, int car) throws DataAccessException;

    /**
     * Indicate that the car ride information stored by the loaner has been approved  by the owner.
     */
    public void approveInfo (int id);

    /**
     * Return the startKm field of the next reservation, or 0 if no next km is filled in
     */
    public int getNextStartKm (int reservationId);

    /**
     * Return the endKm field of the previous reservation, or 0 if no previous km is filled in
     */
    public int getPrevEndKm (int reservationId);


}
