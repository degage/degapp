/* WorkflowAction.java
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

import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationHeader;
import controllers.routes;
import play.api.mvc.Call;
import play.twirl.api.Html;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * Various actions that are part of the workflow.
 */
public enum WorkflowAction {

    CANCEL("Annuleren", "Reservatie annuleren",
            routes.WFCancel::cancelReservation, "danger"),
    // cancel a reservation request

    AOR_RESERVATION("Goed- of afkeuren", "Goed- of afkeuren reservatie ...",
            routes.WFApprove::approveReservation, "warning"),
    // approve or reject a reservation

    SEND_REMINDER("Herinnering", "Stuur herinnering",
            routes.WFApprove::sendReminder, "warning"),
    // send a reminder to the owner

    SHORTEN("Inkorten", "Reservatieduur inkorten ...",
            routes.WFCreate::shortenReservation, "primary"),
    // shorten a reservation

    CANCEL_LATE("Niet doorgegaan", "Annuleren/markeren als niet doorgegaan ...",
            routes.WFCancel::cancelLate, "danger"),
    // cancel a reservation when the trip did not take place

    NEW_TRIP("Invullen", "Ritdetails invullen ...",
            routes.Workflow::newTripInfo, "warning"),
    // enter trip data for the first time

    EDIT_TRIP("Aanpassen", "Ritdetails aanpassen ...",
            routes.Workflow::editTripInfo, "primary"),
    // change trip data

    AOR_TRIP("Goed- of afkeuren", "Goed- of afkeuren ritinformatie ...",
            routes.Workflow::approveTripInfo, "warning"),
    // accept or reject trip data

    REFUELS("Tankbeurten", "Tankbeurten beheren ...",
            routes.Refuels::showRefuelsForRide, "primary");

    private String shortCaption;

    private String longCaption;

    private IntFunction<Call> callFunction;

    private String strength;

    WorkflowAction(String shortCaption, String longCaption, IntFunction<Call> callFunction, String strength) {
        this.shortCaption = shortCaption;
        this.longCaption = longCaption;
        this.callFunction = callFunction;
        this.strength = strength;
    }

    public Html asNarrowButton (int reservationId) {
        return views.html.workflow.button.render(shortCaption, callFunction.apply(reservationId), strength);
    }

    public Html asWideButton (int reservationId) {
        return views.html.workflow.button.render(longCaption, callFunction.apply(reservationId), strength);
    }

    /**
     * Checks whether this action is forbidden for the current user
     */
    public boolean isForbiddenForCurrentUser(ReservationHeader reservation) {
        for (WorkflowRole role : WorkflowRole.getCurrentRoles(reservation)) {
            if (role.actionsAllowed(reservation).contains(this)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return all actions on the given reservation, allowed by the current user.
     */
    public static Set<WorkflowAction> getCurrentActions (Reservation reservation) {
        Set<WorkflowAction> result = EnumSet.noneOf(WorkflowAction.class);
        for (WorkflowRole workflowRole : WorkflowRole.getCurrentRoles(reservation)) {
            result.addAll(workflowRole.actionsAllowed(reservation));
        }
        return result;
    }
}
