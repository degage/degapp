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
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.models.*;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import data.EurocentAmount;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import views.html.costs.carCostsAdmin;
import views.html.costs.carCostspage;
import views.html.costs.costs;
import views.html.costs.details;

import java.time.LocalDate;

/**
 * Controller for actions related to car costs
 */
public class Costs extends Controller {

    private static boolean isOwnerOrAdmin (CarHeaderShort car) {
        return CurrentUser.is(car.getOwnerId()) || CurrentUser.hasRole(UserRole.CAR_ADMIN);
    }

    private static boolean isOwnerOrAdmin (CarCost cost) {
        return CurrentUser.is(cost.getOwnerId()) || CurrentUser.hasRole(UserRole.CAR_ADMIN);
    }

    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showCostsForCar(int carId) {

        // TODO: add authorization check
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarHeaderShort car =  context.getCarDAO().getCarHeaderShort(carId);
        if (isOwnerOrAdmin(car)) {
            return ok(costs.render(
                    dao.listCostsOfCar(carId),
                    Form.form(CostData.class),
                    carId,
                    car.getName(),
                    dao.listCategories()
            ));
        } else {
            return badRequest(); // hack?
        }
    }

    /**
     * Method: POST
     *
     * @return redirect to the CarCostForm you just filled in or to the car-detail page
     */
    @AllowRoles({UserRole.CAR_OWNER})
    @InjectContext
    public static Result doCreate(int carId) {

        Form<CostData> form = Form.form(CostData.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCostDAO dao = context.getCarCostDAO();
        CarHeaderShort car =  context.getCarDAO().getCarHeaderShort(carId);
        if (!isOwnerOrAdmin(car)) {
            return badRequest(); // hack?
        }

        // additional validation of file part
        // TODO: avoid copy and paste of this type of code
        File file = FileHelper.getFileFromRequest("picture", FileHelper.DOCUMENT_CONTENT_TYPES, "uploads.refuelproofs");
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
        assert file != null; // keeps IDEA happy
        CostData data = form.get();

        dao.createCarCost(carId, carName, data.amount.getValue(), data.mileage, data.description, data.time, file.getId(), data.category);
        Notifier.sendCarCostRequest(
                data.time, carName, data.amount, data.description,
                dao.getCategory(data.category).getDescription()
        );
        return redirect(routes.Costs.showCostsForCar(carId));
    }

    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
        @InjectContext
    public static Result showCarCostDetail(int id) {
        CarCostDAO dao = DataAccess.getInjectedContext().getCarCostDAO();
        CarCost cost = dao.getCarCost(id);
        if (isOwnerOrAdmin(cost)) {
            return ok(details.render(cost));
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
    public static Result showCarCosts() {
        return ok(carCostsAdmin.render());
    }

    @InjectContext
    public static Result showCarCostsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        // Check if admin or car owner
        if (!CurrentUser.hasRole(UserRole.CAR_ADMIN)) {
            String carIdString = filter.getValue(FilterField.CAR_ID);
            // TODO: not from filter??

            if (carIdString.equals("")) {
                return badRequest();
            }
            int carId = Integer.parseInt(carIdString);

            CarDAO carDAO = DataAccess.getInjectedContext().getCarDAO();
            if (!carDAO.isCarOfUser(carId, CurrentUser.getId())) {
                flash("danger", "Je bent niet de eigenaar van deze auto.");
                return badRequest();   // TODO: redirect
            }

        }

        return ok(carCostList(page, pageSize, field, asc, filter));

    }

    // used in injected context
    private static Html carCostList(int page, int pageSize, FilterField orderBy, boolean asc, Filter filter) {
        CarCostDAO dao = DataAccess.getInjectedContext().getCarCostDAO();

        if (orderBy == null) {
            orderBy = FilterField.CAR_COST_DATE;
        }

        Iterable<CarCost> listOfResults = dao.getCarCostList(orderBy, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfCarCosts(filter);
        int amountOfPages = (amountOfResults + pageSize - 1)/ pageSize;

        return carCostspage.render(listOfResults, page, amountOfResults, amountOfPages);
    }

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
            return redirect(routes.Costs.showCarCosts());
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
            return redirect(routes.Costs.showCarCosts());
        } else {
            return redirect(routes.Cars.detail(carId));
        }
    }

    @AllowRoles
    @InjectContext
    public static Result getCarCostProof(int carCostId) {
        // TODO: check authorization
        DataAccessContext context = DataAccess.getInjectedContext();
        CarCost carCost = context.getCarCostDAO().getCarCost(carCostId);
        return FileHelper.getFileStreamResult(context.getFileDAO(), carCost.getProofId());
    }


    public static class CostData {

        @Constraints.Required
        public int category;

        @Constraints.Required
        public String description;

        @Constraints.Required
        public EurocentAmount amount;

        public int mileage;

        @Constraints.Required
        public LocalDate time;

    }
}
