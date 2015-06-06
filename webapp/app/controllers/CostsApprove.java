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

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.CarCostDAO;
import be.ugent.degage.db.models.CarCost;
import be.ugent.degage.db.models.UserHeader;
import be.ugent.degage.db.models.UserRole;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Result;
import views.html.costs.approveorreject;

import java.time.LocalDate;

/**
 * Handles approval / rejection of costs
 */
public class CostsApprove extends CostsCommon {


    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result approveOrReject(int carCostId, boolean horizontal) {
        CarCost cost = DataAccess.getInjectedContext().getCarCostDAO().getCarCost(carCostId);
        return ok(approveorreject.render(
                Form.form(ApprovalData.class).fill(new ApprovalData(12, cost.getDate())),
                Form.form(RemarksData.class),
                cost,
                horizontal
        ));
    }

    private static Result redirectAfterAOR(CarCost cost, boolean horizontal) {
        if (horizontal) {
            // return to next cost for same car
            int nextId = DataAccess.getInjectedContext().getCarCostDAO().getNextCostId(cost.getId());
            if (nextId > 0) {
                return redirect(routes.Costs.showCostDetail(nextId));
            }
        }
        // else return to overview
        return redirect(routes.Costs.showCostsForCar(cost.getCarId()));

    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result doApprove(int carCostId, boolean horizontal) {
        Form<ApprovalData> form = Form.form(ApprovalData.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarCost cost = dao.getCarCost(carCostId);
        if (form.hasErrors()) {
            return badRequest(approveorreject.render(
                    form, Form.form(RemarksData.class), cost, horizontal
            ));
        } else {
            UserHeader owner = context.getUserDAO().getUserHeader(cost.getOwnerId());
            ApprovalData data = form.get();
            dao.approveCost(carCostId, data.spread, data.start);
            Notifier.sendCarCostApproved(owner, cost, data.spread);
            return redirectAfterAOR(cost, horizontal);
        }
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result doApproveExternal(int carCostId, boolean horizontal) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarCost cost = dao.getCarCost(carCostId);
        UserHeader owner = context.getUserDAO().getUserHeader(cost.getOwnerId());
        dao.approveCost(carCostId, 0, cost.getDate());
        Notifier.sendCarCostApproved(owner, cost, 0);

        return redirectAfterAOR(cost, horizontal);
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result doReject(int carCostId, boolean horizontal) {
        Form<RemarksData> form = Form.form(RemarksData.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarCost cost = dao.getCarCost(carCostId);
        if (form.hasErrors()) {
            return badRequest(approveorreject.render(
                    Form.form(ApprovalData.class).fill(new ApprovalData(12, cost.getDate())),
                    form, cost, horizontal
            ));
        } else {
            UserHeader owner = context.getUserDAO().getUserHeader(cost.getOwnerId());
            RemarksData data = form.get();
            dao.rejectCost(carCostId, data.remarks);
            Notifier.sendCarCostRejected(owner, cost, data.remarks);
            return redirectAfterAOR(cost, horizontal);
        }
    }

    public static class ApprovalData {

        @Constraints.Required
        @Constraints.Min(value = 0, message = "Mag niet negatief zijn") // {0} in error.min does not seem to work
        public int spread;

        @Constraints.Required
        public LocalDate start;

        public ApprovalData() {} // there must be a defaut constructor ??

        public ApprovalData(int spread, LocalDate start) {
            this.spread = spread;
            this.start = start;
        }


    }

    public static class RemarksData {
        @Constraints.Required
        public String remarks;
    }


}
