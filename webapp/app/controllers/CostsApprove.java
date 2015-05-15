/* CostsApprove.java
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

import be.ugent.degage.db.dao.CarCostDAO;
import be.ugent.degage.db.models.ApprovalStatus;
import be.ugent.degage.db.models.CarCost;
import be.ugent.degage.db.models.UserRole;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.mvc.Result;

/**
 * Handles approval / rejection of costs
 */
public class CostsApprove extends CostsCommon {

    /**
     * Method: GET
     * <p>
     * Called when a car-bound cost of a car is approved by the car admin.
     *
     * @param carCostId The carCost being approved
     * @return the carcost index page if returnToDetail is 0, car detail page if 1.
     */
    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result approveCarCost(int carCostId, int returnToDetail) {
        CarCostDAO dao = DataAccess.getInjectedContext().getCarCostDAO();
        CarCost carCost = dao.getCarCost(carCostId);
        carCost.setStatus(ApprovalStatus.ACCEPTED);
        dao.updateCarCost(carCost);
        int carId = carCost.getCarId();
        Notifier.sendCarCostApproved(DataAccess.getInjectedContext().getCarDAO().getOwnerOfCar(carId), carCost);

        flash("success", "Autokost met succes geaccepteerd");
        if (returnToDetail == 0) {
            return redirect(routes.Costs.showCosts(0));
        } else {
            return redirect(routes.Cars.detail(carId));
        }
    }

    /**
     * Method: GET
     * <p>
     * Called when a car-bound cost of a car is approved by the car admin.
     *
     * @param carCostId The carCost being approved
     * @return the carcost index page
     */
    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result refuseCarCost(int carCostId, int returnToDetail) {
        // TODO: very similar to approve
        CarCostDAO dao = DataAccess.getInjectedContext().getCarCostDAO();
        CarCost carCost = dao.getCarCost(carCostId);
        carCost.setStatus(ApprovalStatus.REFUSED);
        dao.updateCarCost(carCost);
        int carId = carCost.getCarId();
        Notifier.sendCarCostRejected(DataAccess.getInjectedContext().getCarDAO().getOwnerOfCar(carId), carCost);
        flash("success", "Autokost met succes geweigerd");
        if (returnToDetail == 0) {
            return redirect(routes.Costs.showCosts(0));
        } else {
            return redirect(routes.Cars.detail(carId));
        }
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result approveOrReject(int carCostId) {
        return ok(); // TODO
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result doApproveOrReject(int carCostId) {
        return ok(); // TODO
    }
}
