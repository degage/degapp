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
import be.ugent.degage.db.models.ApprovalStatus;
import be.ugent.degage.db.models.CarHeaderShort;
import be.ugent.degage.db.models.File;
import be.ugent.degage.db.models.UserRole;
import controllers.util.FileHelper;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.mvc.Result;
import views.html.costs.costs;

/**
 * Controller for registering new costs
 */
public class CostsCreate extends CostsCommon {


    /**
     * Process form from {@link Costs#showCostsForCar(int)}
     */
    @AllowRoles({UserRole.CAR_OWNER})
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
        if (! isAdmin) {
            Notifier.sendCarCostRequest(
                    data.time, carName, data.amount, data.description,
                    dao.getCategory(data.category).getDescription()
            );
        }
        return redirect(routes.Costs.showCostsForCar(carId));
    }
}
