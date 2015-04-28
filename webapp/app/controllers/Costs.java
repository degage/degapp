package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarCostDAO;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.models.*;
import controllers.util.ConfigurationHelper;
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
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;
import views.html.costs.addcarcostmodal;
import views.html.costs.carCostsAdmin;
import views.html.costs.carCostspage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Controller for actions related to car costs
 */
public class Costs extends Controller {


    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result getCarCostModal(int id) {
        // TODO: hide from other users (badRequest)

        Car car = DataAccess.getInjectedContext().getCarDAO().getCar(id);
        return ok(addcarcostmodal.render(Form.form(CostData.class), car));
    }

    /**
     * Method: POST
     *
     * @return redirect to the CarCostForm you just filled in or to the car-detail page
     */
    @AllowRoles({UserRole.CAR_OWNER})
    @InjectContext
    public static Result addNewCarCost(int carId) {
        Form<CostData> carCostForm = Form.form(CostData.class).bindFromRequest();
        if (carCostForm.hasErrors()) {
            flash("danger", "Kost toevoegen mislukt.");
            return redirect(routes.Cars.detail(carId));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            CarCostDAO dao = context.getCarCostDAO();
            CostData model = carCostForm.get();
            CarDAO cardao = context.getCarDAO();
            Car car = cardao.getCar(carId);
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart proof = body.getFile("picture");
            if (proof != null) {
                String contentType = proof.getContentType();
                if (!FileHelper.isDocumentContentType(contentType)) {
                    flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                    return redirect(routes.Cars.detail(carId));
                } else {
                    try {
                        Path relativePath = FileHelper.saveFile(proof, ConfigurationHelper.getConfigurationString("uploads.carboundproofs"));
                        FileDAO fdao = context.getFileDAO();
                        try {
                            File file = fdao.createFile(relativePath.toString(), proof.getFilename(), proof.getContentType());
                            CarCost carCost = dao.createCarCost(carId, car.getName(), model.amount.getValue(), model.mileage, model.description, model.time, file.getId());
                            if (carCost == null) {
                                flash("danger", "Failed to add the carcost to the database. Contact administrator.");
                                return redirect(routes.Cars.detail(carId));
                            }
                            Notifier.sendCarCostRequest(carCost);
                            flash("success", "Je autokost werd toegevoegd.");
                            return redirect(routes.Cars.detail(carId));
                        } catch (DataAccessException ex) {
                            FileHelper.deleteFile(relativePath);
                            throw ex;
                        }

                    } catch (IOException ex) {
                        throw new RuntimeException(ex); //no more checked catch -> error page!
                    }
                }
            } else {
                flash("error", "Missing file");
                return redirect(routes.Application.index());
            }
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
        public String description;

        @Constraints.Required
        public EurocentAmount amount;

        public int mileage;

        @Constraints.Required
        public LocalDate time;

    }
}
