package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.dao.RefuelDAO;
import be.ugent.degage.db.models.*;
import controllers.Security.RoleSecured;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.refuels.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class Refuels extends Controller {

    public static class RefuelModel {

        public BigDecimal amount;

        public String validate() {
            if (amount.compareTo(new BigDecimal(200)) == 1)
                return "Bedrag te hoog";
            return null;
        }
    }


    /**
     * Method: GET
     *
     * @return index page containing all the refuel requests from a specific user
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showRefuels() {
        return ok(refuels.render());
    }

    @InjectContext
    public static Result showUserRefuelsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        User user = DataProvider.getUserProvider().getUser();
        filter.putValue(FilterField.REFUEL_USER_ID, user.getId() + "");

        // TODO: Check if admin or car owner/user

        return ok(refuelList(page, pageSize, field, asc, filter));

    }

    /**
     * Method: GET
     *
     * @return index page containing all the refuel requests to a specific owner
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    public static Result showOwnerRefuels() {
        return ok(refuelsOwner.render());
    }

    @InjectContext
    public static Result showOwnerRefuelsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        User user = DataProvider.getUserProvider().getUser();
        filter.putValue(FilterField.REFUEL_OWNER_ID, user.getId() + "");
        filter.putValue(FilterField.REFUEL_NOT_STATUS, RefuelStatus.CREATED.toString());

        // TODO: Check if admin or car owner/user

        return ok(refuelList(page, pageSize, field, asc, filter));
    }

    /**
     * Method: GET
     *
     * @return index page containing all the refuel requests to a specific owner
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    public static Result showAllRefuels() {
        return ok(refuelsAdmin.render());
    }

    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showAllRefuelsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        filter.putValue(FilterField.REFUEL_NOT_STATUS, RefuelStatus.CREATED.toString());

        return ok(refuelList(page, pageSize, field, asc, filter));
    }

    // should be used with an injected context only
    private static Html refuelList(int page, int pageSize, FilterField orderBy, boolean asc, Filter filter) {
        RefuelDAO dao = DataAccess.getInjectedContext().getRefuelDAO();

        if (orderBy == null) {
            orderBy = FilterField.REFUEL_NOT_STATUS; // not neccessary, but orderBy cannot be null
        }
        List<Refuel> listOfResults = dao.getRefuels(orderBy, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfRefuels(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return refuelspage.render(listOfResults, page, amountOfResults, amountOfPages);
    }

    /**
     * Method: GET
     *
     * @return modal to provide refuel information
     */
    @RoleSecured.RoleAuthenticated()
    public static Result provideRefuelInfo(int refuelId) {
        return ok(editmodal.render(Form.form(RefuelModel.class), refuelId));
    }

    /**
     * Method: POST
     *
     * @return redirect to the index page containing all the refuel requests
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result provideRefuelInfoPost(int refuelId) {
        Form<RefuelModel> refuelForm = Form.form(RefuelModel.class).bindFromRequest();
        if (refuelForm.hasErrors()) {
            flash("danger", "Info verstrekken mislukt.");
            return redirect(routes.Refuels.showRefuels());

        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            RefuelDAO dao = context.getRefuelDAO();
            RefuelModel model = refuelForm.get();
            Refuel refuel = dao.getRefuel(refuelId);
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart proof = body.getFile("picture");
            if (proof != null) {
                String contentType = proof.getContentType();
                if (!FileHelper.isDocumentContentType(contentType)) {
                    flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                    return redirect(routes.Refuels.showRefuels());
                } else {
                    try {
                        Path relativePath = FileHelper.saveFile(proof, ConfigurationHelper.getConfigurationString("uploads.refuelproofs"));
                        FileDAO fdao = context.getFileDAO();
                        try {
                            File file = fdao.createFile(relativePath.toString(), proof.getFilename(), proof.getContentType());
                            refuel.setAmount(model.amount);
                            refuel.setStatus(RefuelStatus.REQUEST);
                            refuel.setProof(file);
                            dao.updateRefuel(refuel);
                            context.commit();
                            Notifier.sendRefuelRequest(refuel.getCarRide().getReservation().getCar().getOwner(), refuel);
                            flash("success", "Uw tankbeurt wordt voorgelegd aan de auto-eigenaar.");
                            return redirect(routes.Refuels.showRefuels());
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
                return redirect(routes.Refuels.showRefuels());
            }
        }
    }

    /**
     * Method: GET
     *
     * @return proof url
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result getProof(int proofId) {
        return FileHelper.getFileStreamResult(DataAccess.getInjectedContext().getFileDAO(), proofId);
    }

    /**
     * Method: GET
     * <p>
     * Called when a refuel of a car is refused by the car owner.
     *
     * @param refuelId The refuel being refused
     * @return the refuel admin page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    @InjectContext
    public static Result refuseRefuel(int refuelId) {
        RefuelDAO dao = DataAccess.getInjectedContext().getRefuelDAO();
        dao.rejectRefuel(refuelId);
        Refuel refuel = dao.getRefuel(refuelId);
        Notifier.sendRefuelStatusChanged(refuel.getCarRide().getReservation().getUser(), refuel, false);
        flash("success", "Tankbeurt succesvol geweigerd");
        return redirect(routes.Refuels.showOwnerRefuels());
    }

    /**
     * Method: GET
     * <p>
     * Called when a refuel of a car is accepted by the car owner.
     *
     * @param refuelId The refuel being approved
     * @return the refuel admin page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    @InjectContext
    public static Result approveRefuel(int refuelId) {
        RefuelDAO dao = DataAccess.getInjectedContext().getRefuelDAO();
        dao.acceptRefuel(refuelId);
        Refuel refuel = dao.getRefuel(refuelId);
        Notifier.sendRefuelStatusChanged(refuel.getCarRide().getReservation().getUser(), refuel, true);
        flash("success", "Tankbeurt succesvol geaccepteerd");
        return redirect(routes.Refuels.showOwnerRefuels());
    }

    /**
     * Method: GET
     * <p>
     * Called when a refuel of a car is put back to the request status by the car owner.
     *
     * @param refuelId The refuel being put back to the request status
     * @return the refuel admin page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    @InjectContext
    public static Result makeRefuelStatusRequest(int refuelId) {
        RefuelDAO dao = DataAccess.getInjectedContext().getRefuelDAO();
        Refuel refuel = dao.getRefuel(refuelId);
        refuel.setStatus(RefuelStatus.REQUEST);
        dao.updateRefuel(refuel);
        // TODO: also send notification?
        flash("success", "Tankbeurt succesvol op status REQUEST gezet.");
        return redirect(routes.Refuels.showOwnerRefuels());
    }

    /**
     * Get the number of refuels having the provided status
     *
     * @param status The status
     * @return The number of refuels
     */
    @InjectContext
    public static int refuelsWithStatus(RefuelStatus status) {
        User user = DataProvider.getUserProvider().getUser();
        RefuelDAO dao = DataAccess.getInjectedContext().getRefuelDAO();
        return dao.getAmountOfRefuelsWithStatus(status, user.getId());
    }
}
