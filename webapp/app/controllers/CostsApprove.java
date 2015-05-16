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
import com.google.common.base.Strings;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Result;
import views.html.costs.approveorreject;

import java.util.Collections;
import java.util.List;

/**
 * Handles approval / rejection of costs
 */
public class CostsApprove extends CostsCommon {


    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result approveOrReject(int carCostId) {
        return ok(approveorreject.render(
                Form.form(ApprovalData.class).fill(ApprovalData.EMPTY),
                DataAccess.getInjectedContext().getCarCostDAO().getCarCost(carCostId)
        ));
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result doApproveOrReject(int carCostId) {
        Form<ApprovalData> form = Form.form(ApprovalData.class).bindFromRequest();
        CarCostDAO dao = DataAccess.getInjectedContext().getCarCostDAO();
        if (form.hasErrors()) {
            return badRequest(approveorreject.render(
                    form, dao.getCarCost(carCostId)
            ));
        } else {
            ApprovalData data = form.get();
            switch (data.status) {
                case "EXTERNAL":
                    dao.approveCost(carCostId, 0);
//Notifier.sendCarCostApproved(DataAccess.getInjectedContext().getCarDAO().getOwnerOfCar(carId), carCost);
                    break;
                case "ACCEPTED":
                    dao.approveCost(carCostId, data.spread);
//Notifier.sendCarCostApproved(DataAccess.getInjectedContext().getCarDAO().getOwnerOfCar(carId), carCost);
                    break;
                case "REFUSED":
                    dao.rejectCost(carCostId, data.remarks);
//Notifier.sendCarCostRejected(DataAccess.getInjectedContext().getCarDAO().getOwnerOfCar(carId), carCost);
                    break;
                default:
                    return badRequest(); // hack?
            }
            // TODO: dispatch depending on the flow
            return redirect(routes.Costs.showCostDetail(carCostId));
        }
    }

    public static class ApprovalData {

        public String status;
        public String remarks;

        public Integer spread;

        public List<ValidationError> validate() {
            if ("REFUSED".equals(status)) {
                if (remarks == null || remarks.trim().isEmpty()) {
                    return Collections.singletonList(
                            new ValidationError("remarks", "Je moet een reden opgeven voor de weigering")
                    );
                }
            } else if ("ACCEPTED".equals(status)) {
                if (spread == null) {
                    return Collections.singletonList(
                            new ValidationError("spread", "Ongeldige waarde")
                    );
                }
                if (spread < 0) {
                    return Collections.singletonList(
                            new ValidationError("spread", "Spreiding mag niet negatief zijn")
                    );
                }
            }
            return null;
        }

        public static ApprovalData EMPTY;

        static {
            EMPTY = new ApprovalData();
            EMPTY.spread = 12;
        }

    }

}
