/* WorkflowRole.java
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

import be.ugent.degage.db.models.ReservationHeader;
import be.ugent.degage.db.models.UserRole;
import db.CurrentUser;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Various roles pertinent to reservation workflow. A person can have more than one such role
 * with respect to a given reservation.
 */
public enum WorkflowRole {

    DRIVER {
        @Override
        public boolean isCurrentRoleFor(ReservationHeader reservation) {
            return CurrentUser.is(reservation.getDriverId());
        }

        @Override
        public Set<WorkflowAction> actionsAllowed(ReservationHeader reservation) {
            switch (reservation.getStatus()) {
                case REQUEST:
                    Set<WorkflowAction> result;
                    if (reservation.getFrom().isBefore(LocalDateTime.now())) {
                        result = EnumSet.noneOf(WorkflowAction.class);
                    } else {
                        result = EnumSet.of(WorkflowAction.SHORTEN, WorkflowAction.CANCEL);
                    }
                    if (reservation.isOld()) {
                        result.add(WorkflowAction.SEND_REMINDER);
                    }
                    return result;
                case ACCEPTED:
                    return EnumSet.of(WorkflowAction.SHORTEN, WorkflowAction.CANCEL);
                case REQUEST_DETAILS:
                    return EnumSet.of(WorkflowAction.EDIT_TRIP);
                case DETAILS_REJECTED:
                case DETAILS_PROVIDED:
                    return EnumSet.of(WorkflowAction.EDIT_TRIP, WorkflowAction.REFUELS);
                case FINISHED:
                    return EnumSet.of(WorkflowAction.REFUELS);
                default:
                    return NO_ACTIONS;
            }
        }
    },
    OWNER {
        @Override
        public boolean isCurrentRoleFor(ReservationHeader reservation) {
            return CurrentUser.is(reservation.getOwnerId());
        }

        @Override
        public Set<WorkflowAction> actionsAllowed(ReservationHeader reservation) {
            switch (reservation.getStatus()) {
                case REQUEST:
                    return EnumSet.of(WorkflowAction.AOR_RESERVATION);
                case REQUEST_DETAILS:
                    return EnumSet.of(WorkflowAction.EDIT_TRIP, WorkflowAction.CANCEL_LATE,
                            WorkflowAction.REFUELS, WorkflowAction.SHORTEN);
                case DETAILS_REJECTED:
                    return EnumSet.of(WorkflowAction.EDIT_TRIP,
                            WorkflowAction.REFUELS, WorkflowAction.SHORTEN);
                case DETAILS_PROVIDED:
                    return EnumSet.of(WorkflowAction.EDIT_TRIP, WorkflowAction.AOR_TRIP,
                            WorkflowAction.REFUELS, WorkflowAction.SHORTEN);
                case FINISHED:
                    return EnumSet.of(WorkflowAction.EDIT_TRIP, WorkflowAction.REFUELS, WorkflowAction.SHORTEN);
                case ACCEPTED:
                    return NO_ACTIONS;
                default:
                    return NO_ACTIONS;
            }
        }
    },
    ADMIN {
        @Override
        public boolean isCurrentRoleFor(ReservationHeader reservation) {
            return CurrentUser.hasRole(UserRole.RESERVATION_ADMIN);
        }

        @Override
        public Set<WorkflowAction> actionsAllowed(ReservationHeader reservation) {
            switch (reservation.getStatus()) {
                case REQUEST:
                    Set<WorkflowAction> result;
                    if (reservation.getFrom().isBefore(LocalDateTime.now())) {
                        result = EnumSet.of(WorkflowAction.AOR_RESERVATION);
                    } else {
                        result = EnumSet.of(WorkflowAction.AOR_RESERVATION, WorkflowAction.SHORTEN, WorkflowAction.CANCEL);
                    }
                    if (reservation.isOld()) {
                        result.add(WorkflowAction.SEND_REMINDER);
                    }
                    return result;
                case ACCEPTED:
                    return EnumSet.of(WorkflowAction.SHORTEN, WorkflowAction.CANCEL);
                case REQUEST_DETAILS:
                    return EnumSet.of(WorkflowAction.EDIT_TRIP, WorkflowAction.CANCEL_LATE,
                            WorkflowAction.REFUELS, WorkflowAction.SHORTEN);
                case DETAILS_REJECTED:
                case DETAILS_PROVIDED:
                case FINISHED:
                case FROZEN:
                    return EnumSet.of(WorkflowAction.EDIT_TRIP, WorkflowAction.REFUELS, WorkflowAction.SHORTEN);
                default:
                    return NO_ACTIONS;
            }
        }
    };

    private static final Set<WorkflowAction> NO_ACTIONS = EnumSet.noneOf(WorkflowAction.class);

    /**
     * Checks whether the current user satisfies the given role for the given reservation
     */
    public abstract boolean isCurrentRoleFor(ReservationHeader reservation);

    /**
     * All actions that are allowed for this role and the given reservation. Mostly depends
     * on the status of the reservation, but may also depend on the time of the reservation
     */
    public abstract Set<WorkflowAction> actionsAllowed(ReservationHeader reservation);

    /**
     * Returns all roles for the current user and the given reservation
     */
    public static Set<WorkflowRole> getCurrentRoles(ReservationHeader reservation) {
        Set<WorkflowRole> result = EnumSet.noneOf(WorkflowRole.class);
        for (WorkflowRole role : values()) {
            if (role.isCurrentRoleFor(reservation)) {
                result.add(role);
            }
        }
        return result;
    }

}
