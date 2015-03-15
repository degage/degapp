/* WFCommon.java
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

import be.ugent.degage.db.models.ReservationHeader;
import controllers.util.WorkflowRole;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Collections;
import java.util.List;

/**
 * Common code for all workflow related controllers. All these controllers extend this class.
 */
public class WFCommon extends Controller {

    public static boolean isOwnerOrAdmin(ReservationHeader reservation) {
        return WorkflowRole.OWNER.isCurrentRoleFor(reservation) ||
                WorkflowRole.ADMIN.isCurrentRoleFor(reservation);
    }

    public static boolean isDriverOrOwnerOrAdmin(ReservationHeader reservation) {
        return WorkflowRole.OWNER.isCurrentRoleFor(reservation) ||
                WorkflowRole.ADMIN.isCurrentRoleFor(reservation) ||
                 WorkflowRole.DRIVER.isCurrentRoleFor(reservation);
    }

    public static Result redirectToDetails(int reservationId) {
        return redirect(routes.Trips.details(reservationId));
    }

    public static class RemarksData {

        public String status;
        public String remarks;

        private String compareWith;

        private String message;

        public RemarksData(String compareWith, String message) {
            this.compareWith = compareWith;
            this.message = message;
        }

        public RemarksData() {
            this ("REFUSED", "Je moet een reden opgeven voor de weigering");
        }

        public List<ValidationError> validate() {
            if (compareWith.equals(status) &&
                    (remarks == null || remarks.trim().isEmpty()) ) {
                return Collections.singletonList(
                        new ValidationError("remarks", message)
                );
            } else {
                return null;
            }
        }
    }
}
