/* RefuelCreate.java
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
import be.ugent.degage.db.models.*;
import controllers.util.FileHelper;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.mvc.Result;

/**
 * Controller for creating refuels
 */
public class RefuelCreate extends RefuelCommon {


    /**
     * Process form from {@link Refuels#showRefuelsForTrip}
     */
    @AllowRoles
    @InjectContext
    public static Result doCreate(int reservationId, boolean ownerFlow) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Reservation reservation = context.getReservationDAO().getReservation(reservationId);

        if (!isAuthorized(reservation, ownerFlow)) {
            return badRequest(); // hacker?
        }

        Form<RefuelData> form = Form.form(RefuelData.class).bindFromRequest();
        Iterable<Refuel> refuels = context.getRefuelDAO().getRefuelsForCarRide(reservationId);
        if (form.hasErrors()) {
            form.reject("picture", "Bestand opnieuw selecteren");
            return badRequest(Refuels.refuelsForTrip(reservation, form, refuels, ownerFlow));
        } else {
            RefuelData data = form.get();
            File file = FileHelper.getFileFromRequest("picture", FileHelper.DOCUMENT_CONTENT_TYPES, "uploads.refuelproofs");
            if (file == null) {
                form.reject("picture", "Bestand met foto of scan van bonnetje is verplicht");
                return badRequest(Refuels.refuelsForTrip(reservation, form, refuels, ownerFlow));
            } else if (file.getContentType() == null) {
                form.reject("picture", "Het bestand  is van het verkeerde type");
                return badRequest(Refuels.refuelsForTrip(reservation, form, refuels, ownerFlow));
            } else {
                UserHeader owner = context.getUserDAO().getUserHeader(reservation.getOwnerId());
                newRefuel(reservation, owner, data.amount.getValue(), file.getId(), data.km, data.fuelAmount);
            }
        }
        return redirect(routes.Refuels.showRefuelsForTrip(reservationId, ownerFlow));
    }

    // use in injected context only
    static void newRefuel(Reservation reservation, UserHeader owner, int eurocents, int fileId,
                          int km, String amount) {
        boolean isAdmin = isOwnerOrAdmin(reservation);

        int refuelId = DataAccess.getInjectedContext().getRefuelDAO().createRefuel(
                reservation.getId(), eurocents, fileId,
                isAdmin ? RefuelStatus.ACCEPTED : RefuelStatus.REQUEST,
                km, amount
        );
        if (!isAdmin) {
            Notifier.sendRefuelRequest(
                    owner,
                    reservation,
                    refuelId,
                    reservation.getCar(),
                    eurocents
            );
        }
    }

}
