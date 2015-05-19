/* CostsCreate.java
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
import be.ugent.degage.db.models.*;
import controllers.util.FileHelper;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.mvc.Result;
import views.html.costs.costs;
import views.html.costs.editcost;

/**
 * Controller for registering new costs
 */
public class CostsCreate extends CostsCommon {


    /**
     * Process form from {@link Costs#showCostsForCar(int)}
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result doCreate(int carId) {

        Form<CostData> form = Form.form(CostData.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarHeaderShort car = context.getCarDAO().getCarHeaderShort(carId);
        if (!isOwnerOrAdmin(car)) {
            return badRequest(); // hack?
        }

        // additional validation of file part
        // TODO: avoid copy and paste of this type of code
        File file = FileHelper.getFileFromRequest("picture", FileHelper.DOCUMENT_CONTENT_TYPES, "uploads.costs");
        if (file == null) {
            form.reject("picture", "Bestand met foto of scan  is verplicht");
        } else if (file.getContentType() == null) {
            form.reject("picture", "Het bestand  is van het verkeerde type");
        } else if (form.hasErrors()) {
            form.reject("picture", "Bestand opnieuw selecteren");
        }

        String carName = car.getName();
        if (form.hasErrors()) {
            return badRequest(costs.render(
                    dao.listCostsOfCar(carId),
                    form, carId, carName, dao.listCategories()
            ));
        }

        CostData data = form.get();

        boolean isAdmin = CurrentUser.hasRole(UserRole.CAR_ADMIN);

//        if (isAdmin && data.spread == null) {
//            form.reject("spread", "Gelieve een spreiding op te geven");
//        }

        assert file != null; // keeps IDEA happy

        dao.createCarCost(carId, carName, data.amount.getValue(), data.mileage, data.description, data.time,
                isAdmin ? ApprovalStatus.ACCEPTED : ApprovalStatus.REQUEST,
                isAdmin ? 12 : data.spread,
                file.getId(), data.category);
        if (!isAdmin) {
            Notifier.sendCarCostRequest(
                    data.time, carName, data.amount, data.description,
                    dao.getCategory(data.category).getDescription()
            );
        }
        return redirect(routes.Costs.showCostsForCar(carId));
    }

    public static boolean isAllowedEdit(CarCost cost) {
        return CurrentUser.hasRole(UserRole.CAR_ADMIN) ||
                CurrentUser.is(cost.getOwnerId()) && cost.getStatus() == ApprovalStatus.REQUEST;
    }

    /**
     * Show a form for editing a cost and for changing the proof document
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showEdit(int costId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarCost cost = dao.getCarCost(costId);
        if (isAllowedEdit(cost)) {
            return ok(editcost.render(
                Form.form(CostData.class).fill(new CostData(cost)),
                    dao.listCategories(),
                    costId,
                    cost.getCarId(),
                    cost.getCarName(),
                    CurrentUser.hasRole(UserRole.CAR_ADMIN)
            ));
        } else {
            return badRequest();
        }
    }


    /**
     * Process the form for editing a cost
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result doEdit(int costId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarCost cost = dao.getCarCost(costId);
        if (isAllowedEdit(cost)) {
            Form<CostData> form = Form.form(CostData.class).bindFromRequest();
            boolean isAdmin = CurrentUser.hasRole(UserRole.CAR_ADMIN);
            if (form.hasErrors()) {
                return badRequest(editcost.render(
                    form,
                    dao.listCategories(),
                    costId,
                    cost.getCarId(),
                    cost.getCarName(),
                    isAdmin
                ));
            } else {
                CostData data = form.get();
                if (!isAdmin) {
                    data.spread = cost.getSpread();
                }
                dao.updateCarCost(costId, data.amount.getValue(), data.description, data.time, data.mileage, data.spread, data.category);
            }
            return redirect(routes.Costs.showCostDetail(costId));
        } else {
            return badRequest();
        }
    }

    /**
     * Process the form for changing the proof
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result doUpdateProof(int costId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarCost cost = dao.getCarCost(costId);
        if (isAllowedEdit(cost)) {
            // additional validation of file part
            // TODO: avoid copy and paste of this type of code
            // TODO: remove old file
            Form<CostData> form = Form.form(CostData.class).fill(new CostData(cost));
            File file = FileHelper.getFileFromRequest("picture", FileHelper.DOCUMENT_CONTENT_TYPES, "uploads.costs");
            if (file == null) {
                form.reject("picture", "Bestand met foto of scan  is verplicht");
            } else if (file.getContentType() == null) {
                form.reject("picture", "Het bestand  is van het verkeerde type");
            } else if (form.hasErrors()) {
                form.reject("picture", "Bestand opnieuw selecteren");
            }
            // TODO: lots of code in common with doEdit
            if (form.hasErrors()) {
                return badRequest(editcost.render(
                    form,
                    dao.listCategories(),
                    costId,
                    cost.getCarId(),
                    cost.getCarName(),
                    CurrentUser.hasRole(UserRole.CAR_ADMIN)
                ));
            } else {
                dao.updateProof (costId, file.getId());
                return redirect(routes.Costs.showCostDetail(costId));
            }
        } else {
            return badRequest();
        }
    }


}
