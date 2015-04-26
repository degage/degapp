/* RefuelExtended.java
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

package be.ugent.degage.db.models;

import java.time.LocalDateTime;

/**
 * Extends {@link Refuel} with context information (reservation/car/owner)
 */
public class RefuelExtended extends Refuel {

    private int carId;
    private String carName;

    private int reservationId;
    private LocalDateTime reservationFrom;
    private LocalDateTime reservationUntil;

    private int driverId;
    private String driverName;

    private int ownerId; // TODO: not used?

    private int startKm;   // 0  when not filled in
    private int endKm;

    public RefuelExtended(int id, int proofId, int eurocents, ApprovalStatus status, int km, String amount, String message,
                          int carId, String carName,
                          int reservationId, LocalDateTime reservationFrom, LocalDateTime reservationUntil,
                          int driverId, String driverName, int ownerId, int startKm, int endKm ) {
        super(id, proofId, eurocents, status, km, amount, message);
        this.carId = carId;
        this.carName = carName;
        this.reservationId = reservationId;
        this.reservationFrom = reservationFrom;
        this.reservationUntil = reservationUntil;
        this.driverId = driverId;
        this.driverName = driverName;
        this.ownerId = ownerId;
        this.startKm = startKm;
        this.endKm = endKm;
    }

    public int getCarId() {
        return carId;
    }

    public String getCarName() {
        return carName;
    }

    public int getReservationId() {
        return reservationId;
    }

    public LocalDateTime getReservationFrom() {
        return reservationFrom;
    }

    public LocalDateTime getReservationUntil() {
        return reservationUntil;
    }

    public int getDriverId() {
        return driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getStartKm() {
        return startKm;
    }

    public int getEndKm() {
        return endKm;
    }
}
