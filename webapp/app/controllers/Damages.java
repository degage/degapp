/* Damages.java
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
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.damages.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

/**
 * Created by Stefaan Vermassen on 03/05/14.
 */
public class Damages extends Controller {

    public static class DamageModel {

        public String description;
        public LocalDate time;

        public String validate() {
            if ("".equals(description))
                return "Geef aub een beschrijving op.";
            return null;
        }

        public void populate(Damage damage) {
            if (damage == null) return;
            description = damage.getDescription();
            time = damage.getDate();
        }
    }

    public static class DamageStatusModel {

        public String status;

        public String validate() {
            if (status == null || status.trim().isEmpty()) {
                return "Geef aub een status op.";
            } else {
                return null;
            }
        }
    }

    /**
     * Method: GET
     *
     * @return index page containing all the damages from a specific user
     */
    @AllowRoles
    @InjectContext
    public static Result showDamages() {
        return ok(damages.render());
    }

    /**
     * Method: GET
     *
     * @return index page containing all the damages from a specific owner
     */
    @AllowRoles
    @InjectContext
    public static Result showDamagesOwner() {
        return ok(damagesOwner.render());
    }

    /**
     * Method: GET
     *
     * @return index page containing all the damages from everyone
     */
    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showAllDamages() {
        return ok(damagesAdmin.render());
    }

    @AllowRoles
    @InjectContext
    public static Result showDamagesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        return ok(damagespage.render(DataAccess.getInjectedContext().getDamageDAO().listDamagesForDriver(CurrentUser.getId()), 0, 0, 0));
    }

    @AllowRoles
    @InjectContext
    public static Result showDamagesPageOwner(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        return ok(damagespage.render(DataAccess.getInjectedContext().getDamageDAO().listDamagesForOwner(CurrentUser.getId()), 0, 0, 0));
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showDamagesPageAdmin(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy, asc not used

        Filter filter = Pagination.parseFilter(searchString);
        DamageDAO dao = DataAccess.getInjectedContext().getDamageDAO();

        int amountOfResults = dao.getAmountOfDamages(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return ok(damagespage.render( dao.getDamages(page, pageSize, filter), page, amountOfResults, amountOfPages));
    }

    /**
     * Method: GET
     *
     * @return detail page containing all information about a specific damage
     */
    @AllowRoles
    @InjectContext
    public static Result showDamageDetails(int damageId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Damage damage = context.getDamageDAO().getDamage(damageId);
        Car damagedCar = context.getCarDAO().getCar(damage.getCarId());
        return ok(details.render(
                damage,
                context.getUserDAO().getUser(damagedCar.getOwner().getId()),
                damagedCar,
                context.getDamageLogDAO().getDamageLogsForDamage(damageId),
                context.getFileDAO().getDamageFiles(damageId)
        ));
    }

    /**
     * Method: GET
     *
     * @return modal to edit damage information
     */
    @AllowRoles
    @InjectContext
    public static Result editDamage(int damageId) {
        DamageDAO dao = DataAccess.getInjectedContext().getDamageDAO();
        Damage damage = dao.getDamage(damageId);

        if (damage == null) {
            flash("danger", "Schadedossier met ID=" + damageId + " bestaat niet.");
            return badRequest();
        } else {
            //User currentUser = DataProvider.getUserProvider().getUser();
            if (CurrentUser.is(damage.getDriverId()) || CurrentUser.hasRole(UserRole.CAR_ADMIN)) {

                DamageModel model = new DamageModel();
                model.populate(damage);

                Form<DamageModel> editForm = Form.form(DamageModel.class).fill(model);
                return ok(editmodal.render(editForm, damageId));
            } else {
                flash("danger", "Je hebt geen rechten tot het bewerken van dit schadedossier.");
                return badRequest();
            }
        }
    }

    /**
     * Method: GET
     *
     * @return modal to provide new damage log status
     */
    @AllowRoles
    @InjectContext
    public static Result addStatus(int damageId) {
        return ok(statusmodal.render(Form.form(DamageStatusModel.class), damageId));
    }

    /**
     * Method: GET
     *
     * @return modal to provide new damage proof
     */
    @AllowRoles
    @InjectContext
    public static Result addProof(int damageId) {
        return ok(proofmodal.render(damageId));
    }

    /**
     * Method: POST
     *
     * @return redirect to the damage detail page
     */
    @AllowRoles
    @InjectContext
    public static Result editDamagePost(int damageId) {
        Form<DamageModel> damageForm = Form.form(DamageModel.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        if (damageForm.hasErrors()) {
            flash("danger", "Beschrijving aanpassen mislukt.");
        } else {

            DamageDAO damageDAO = context.getDamageDAO();
            DamageModel model = damageForm.get();
            damageDAO.updateDamageDetails(damageId, model.description, model.time);
            flash("success", "De beschrijving werd gewijzigd.");
        }
        return redirect(
                routes.Damages.showDamageDetails(damageId)
        );
    }


    /**
     * Method: POST
     *
     * @return redirect to the damage detail page
     */
    @AllowRoles
    @InjectContext
    public static Result addStatusPost(int damageId) {
        Form<DamageStatusModel> damageStatusForm = Form.form(DamageStatusModel.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        if (damageStatusForm.hasErrors()) {
            flash("danger", "Logbericht mag niet blanko zijn");
        } else {
            DamageLogDAO damageLogDAO = context.getDamageLogDAO();
            DamageStatusModel model = damageStatusForm.get();
            damageLogDAO.addDamageLog(damageId, model.status);
            flash("success", "Het logbericht werd toegevoegd.");
        }
        return redirect(
                routes.Damages.showDamageDetails(damageId)
        );

    }


    /**
     * Method: GET
     * <p>
     * Called when a damage is closed/opened
     *
     * @param damageId The carCost being approved
     * @return the carcost index page
     */
    @InjectContext
    public static Result setDamageFinished(int damageId, int status) {
        DataAccess.getInjectedContext().getDamageDAO().updateDamageFinished(damageId, status != 0);
        flash("success", "Status van schadedossier succesvol aangepast");
        return redirect(routes.Damages.showDamageDetails(damageId));
    }

    /**
     * Method: POST
     * Processes the request to add files
     *
     * @param damageId The damage to edit
     * @return The detials page of a damage
     */
    @AllowRoles
    @InjectContext
    public static Result addProofPost(int damageId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        DamageDAO damageDAO = context.getDamageDAO();
        Damage damage = damageDAO.getDamage(damageId);
        FileDAO fdao = context.getFileDAO();
        if (damage == null) {
            return redirect(routes.Damages.showDamageDetails(damageId));
        }
        try {
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart newFile = body.getFile("file");
            if (newFile != null) {
                if (!FileHelper.isDocumentContentType(newFile.getContentType())) {
                    flash("danger", "Het documentstype dat je bijgevoegd hebt is niet toegestaan. (" + newFile.getContentType() + ").");
                    return badRequest();
                } else {
                   // Now we add the file to the group
                    Path relativePath = FileHelper.saveFile(newFile, ConfigurationHelper.getConfigurationString("uploads.damages"));
                    File file = fdao.createFile(relativePath.toString(), newFile.getFilename(), newFile.getContentType());
                    fdao.addDamageFile(damageId, file.getId()); // TODO: make this one (atomic) call to fdao
                }
            }
            flash("success", "Bestand succesvol toegevoegd.");
            return redirect(routes.Damages.showDamageDetails(damageId));
        } catch (IOException ex) { //IO or database error causes a rollback
            throw new RuntimeException(ex); //unchecked
        }
    }

    /**
     * Method: GET
     *
     * @return proof url
     */
    @AllowRoles
    @InjectContext
    public static Result getProof(int proofId) {
        return FileHelper.getFileStreamResult(DataAccess.getInjectedContext().getFileDAO(), proofId);
    }

    /**
     * Method: GET
     * Deletes a file from the damage filegroup
     *
     * @param damageId The damage to delete from
     * @param fileId   The file to delete
     * @return A redirect to the damage details
     */
    @AllowRoles
    @InjectContext
    public static Result deleteProof(int damageId, int fileId) {
        FileDAO fileDAO = DataAccess.getInjectedContext().getFileDAO();
        File file = fileDAO.getFile(fileId);
        fileDAO.deleteFile(file.getId());
        FileHelper.deleteFile(Paths.get(file.getPath()));
        flash("success", "Bestand succesvol verwijderd.");
        return redirect(routes.Damages.showDamageDetails(damageId));
    }

    /**
     * Get the number of open damages. Used in the application menu
     *
     * @return The number of damages
     */
    // should only be used with injected context
    // TODO: merge database call with other application menu items
    public static int openDamages() {
        return DataAccess.getInjectedContext().getDamageDAO().getAmountOfOpenDamages(CurrentUser.getId());
    }
}
