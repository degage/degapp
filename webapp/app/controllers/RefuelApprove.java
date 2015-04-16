/* RefuelApprove.java
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

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.RefuelDAO;
import be.ugent.degage.db.models.*;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.mvc.Result;
import views.html.refuels.approveorreject;

/**
 * Controller which handles approval/refusal of refuels
 */
public class RefuelApprove extends RefuelCommon {


    /**
     * Show the page that allows approval or rejection of a refuel by the owner
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result approveOrReject(int refuelId, boolean ownerFlow) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Refuel refuel = context.getRefuelDAO().getRefuel(refuelId);
        ReservationHeader reservation = context.getReservationDAO().getReservationHeaderForRefuel(refuelId);
        if (isOwnerOrAdmin(reservation)) {
            return ok(approveorreject.render(
                    Form.form(RemarksData.class),
                    refuel,
                    context.getCarDAO().getCar(reservation.getCarId()),
                    reservation,
                    context.getCarRideDAO().getCarRide(reservation.getId()),
                    ownerFlow));
        } else {
            return badRequest(); // not authorized
        }
    }

    /**
     * Process result of {@link #approveOrReject}
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result doApproveOrReject(int refuelId, boolean ownerFlow) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationHeader reservation = context.getReservationDAO().getReservationHeaderForRefuel(refuelId);

        if (!isOwnerOrAdmin(reservation)) {
            return badRequest(); // should not happen
        }

        RefuelDAO dao = context.getRefuelDAO();
        Refuel refuel = dao.getRefuel(refuelId);
        if (refuel.getStatus() != RefuelStatus.REQUEST) {
            return badRequest(); // should not happen
        }

        Form<RemarksData> form =  Form.form(RemarksData.class).bindFromRequest();

        if (form.hasErrors()) {
            Car car = context.getCarDAO().getCar(reservation.getCarId());
            CarRide ride = context.getCarRideDAO().getCarRide(reservation.getId());
            return badRequest(approveorreject.render(form, refuel, car, reservation, ride, ownerFlow));
        } else {
            RemarksData data = form.get();

            if ("REFUSED".equals(data.status)) {
                dao.rejectRefuel(refuelId, data.remarks);
                Notifier.sendRefuelRejected(refuel, data.remarks);
                flash("success", "Uw opmerking wordt gemaild naar de bestuurder.");
            } else {
                dao.updateRefuelStatus(RefuelStatus.ACCEPTED, refuelId);
                Notifier.sendRefuelApproved(refuel);
            }
            return redirect(routes.Refuels.showRefuelsForTrip(reservation.getId(), ownerFlow));
        }

    }

    /**
     * Approve the indicated refuel directly
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result approve(int refuelId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationHeader reservation = context.getReservationDAO().getReservationHeaderForRefuel(refuelId);
        if (!isOwnerOrAdmin(reservation)) {
            return badRequest(); // should not happen
        }

        RefuelDAO dao = context.getRefuelDAO();
        Refuel refuel = dao.getRefuel(refuelId);
        if (refuel.getStatus() != RefuelStatus.REQUEST) {
            return badRequest(); // should not happen
        }

        dao.updateRefuelStatus(RefuelStatus.ACCEPTED, refuelId);
        Notifier.sendRefuelApproved(refuel);
        flash("success", "De tankbeurt werd goedgekeurd");
        return redirect(routes.Refuels.showRefuelsForTrip(reservation.getId(), true));
    }
}
