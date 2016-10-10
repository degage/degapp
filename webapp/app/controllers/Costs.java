/* Costs.java
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
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarCostDAO;
import be.ugent.degage.db.models.*;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Result;
import views.html.costs.carCostsAdmin;
import views.html.costs.carCostspage;
import views.html.costs.costs;
import views.html.costs.details;

/**
 * Controller for actions related to car costs
 */
public class Costs extends CostsCommon {

    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showCostsForCar(int carId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarHeaderShort car =  context.getCarDAO().getCarHeaderShort(carId);
        if (isOwnerOrAdmin(car)) {
            return ok(costs.render(
                    dao.listCostsOfCar(carId),
                    Form.form(CostData.class).fill(CostData.EMPTY),
                    carId,
                    car.getName(),
                    dao.listCategories()
            ));
        } else {
            return badRequest(); // hack?
        }
    }

    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showCostDetail(int id) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarCost cost = dao.getCarCost(id);
        if (isOwnerOrAdmin(cost)) {
            return ok(details.render(
                    cost,
                    context.getFileDAO().getFile(cost.getProofId()).isImage(),
                    dao.getNextCostId(cost.getId()),
                    dao.getPreviousCostId(cost.getId()),
                    Form.form(CostData.class).fill(CostData.EMPTY),
                    dao.listCategories()
            ));
        } else {
            return badRequest(); // not authorized
        }
    }


    /**
     * Method: GET
     *
     * @return index page containing all the carcost requests
     */
    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showCosts(int tab) {
        return ok(carCostsAdmin.render(tab));
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showCostsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy, FilterField.CAR_COST_TIME);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        CarCostDAO dao = DataAccess.getInjectedContext().getCarCostDAO();

        return ok(carCostspage.render(
                dao.getCarCostList(field, asc, page, pageSize, filter),
                searchString.endsWith("ACCEPTED") || searchString.endsWith("FROZEN")
        ));
    }

    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result getCarCostProof(int carCostId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCost carCost = context.getCarCostDAO().getCarCost(carCostId);
        if (isOwnerOrAdmin(carCost)) {
            return FileHelper.getFileStreamResult(context.getFileDAO(), carCost.getProofId());
        } else {
            return badRequest();
        }
    }


}
