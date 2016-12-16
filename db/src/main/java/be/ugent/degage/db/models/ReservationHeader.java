/* ReservationHeader.java
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
import java.text.DateFormat;
import java.time.format.DateTimeFormatter;

/**
 * Contains the most essential information for reservations.
 * @see be.ugent.degage.db.models.Reservation
 */
public class ReservationHeader {

    protected int id;
    protected ReservationStatus status;
    protected int carId;
    protected int driverId;
    protected int ownerId;
    protected LocalDateTime from;
    protected LocalDateTime until;
    protected String message;
    protected boolean privileged;
    protected boolean old;
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    protected LocalDateTime createdAt;

    public ReservationHeader(int id, int carId, int driverId, int ownerId, LocalDateTime from, LocalDateTime until, String message, boolean old, LocalDateTime createdAt) {
        this.id = id;
        this.carId = carId;
        this.driverId = driverId;
        this.ownerId = ownerId;
        this.from = from;
        this.until = until;
        this.message = message;
        this.old = old;
        this.createdAt = createdAt;
    }
    
    public int getId() {
        return id;
    }

    public boolean isOld() {
        return old;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public int getCarId() {
        return carId;
    }

    public int getDriverId() {
        return driverId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public String getFromIcs() {
        return getFrom().format(dateFormat);
    }

    public LocalDateTime getUntil() {
        return until;
    }

    public String getUntilIcs() {
        return getUntil().format(dateFormat);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public void setStatus(ReservationStatus status) {
            this.status = status;
        }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
