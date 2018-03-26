/* Enrollee.java
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

import java.time.LocalDate;
import com.google.gson.annotations.Expose;
/**
 *
 * @author Laurent
 */
public class Enrollee {
    @Expose
    private UserHeader user;
    @Expose
    private EnrollmentStatus status;
    @Expose
    private LocalDate dateJoined;
    private LocalDate dateContract;

    public Enrollee(UserHeader user, EnrollmentStatus status, LocalDate dateJoined, LocalDate dateContract) {
        this.user = user;
        this.status = status;
        this.dateJoined = dateJoined;
        this.dateContract = dateContract;
    }

    public Enrollee(UserHeader user, EnrollmentStatus status, LocalDate dateJoined) {
        this.user = user;
        this.status = status;
        this.dateJoined = dateJoined;
    }

    public UserHeader getUser() {
        return user;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public LocalDate getDateJoined() {
        return dateJoined;
    }

    public LocalDate getDateContract() {
        return dateContract;
    }
}
