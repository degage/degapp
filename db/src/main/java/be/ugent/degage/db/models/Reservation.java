/* Reservation.java
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
 *
 */
public class Reservation extends ReservationHeader {
    
    private Car car;
    private UserHeader user;

    public Reservation(int id, Car car, UserHeader user, int ownerId, LocalDateTime from, LocalDateTime until, String message) {
        super (id, car == null ? 0 : car.getId(), user == null ? 0 : user.getId(), ownerId, from, until, message);
        this.car = car;
        this.user = user;
    }

    public Car getCar() {
        return car;
    }

    public UserHeader getUser() {
        return user;
    }

    public void setUser(UserHeader user) {
        this.user = user;
        this.userId = user.getId();
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public void setUntil(LocalDateTime until) {
        this.until = until;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
