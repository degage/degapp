package controllers;


import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import controllers.Security.RoleSecured;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import org.joda.time.DateTime;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.damages.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 03/05/14.
 */
public class Damages extends Controller {

    public static class DamageModel {

        public String description;
        public DateTime time;

        public String validate() {
            if ("".equals(description))
                return "Geef aub een beschrijving op.";
            return null;
        }

        public void populate(Damage damage) {
            if (damage == null) return;
            description = damage.getDescription();
            time = damage.getTime();
        }
    }

    public static class DamageStatusModel {

        public String status;

        public String validate() {
            if ("".equals(status))
                return "Geef aub een status op.";
            return null;
        }
    }

    /**
     * Method: GET
     *
     * @return index page containing all the damages from a specific user
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showDamages() {
        return ok(damages.render());
    }

    /**
     * Method: GET
     *
     * @return index page containing all the damages from a specific owner
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showDamagesOwner() {
        return ok(damagesOwner.render());
    }

    /**
     * Method: GET
     *
     * @return index page containing all the damages from everyone
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    public static Result showAllDamages() {
        return ok(damagesAdmin.render());
    }

    @RoleSecured.RoleAuthenticated()
    public static Result showDamagesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField carField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        User user = DataProvider.getUserProvider().getUser();
        filter.putValue(FilterField.DAMAGE_USER_ID, user.getId() + "");
        filter.putValue(FilterField.DAMAGE_FINISHED, "-1");

        return ok(damageList(page, pageSize, carField, asc, filter));
    }

    @RoleSecured.RoleAuthenticated()
    public static Result showDamagesPageOwner(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField carField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        User user = DataProvider.getUserProvider().getUser();
        filter.putValue(FilterField.DAMAGE_OWNER_ID, user.getId() + "");
        filter.putValue(FilterField.DAMAGE_FINISHED, "-1");

        return ok(damageList(page, pageSize, carField, asc, filter));
    }
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    public static Result showDamagesPageAdmin(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField carField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        return ok(damageList(page, pageSize, carField, asc, filter));
    }

    private static Html damageList(int page, int pageSize, FilterField orderBy, boolean asc, Filter filter) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            DamageDAO dao = context.getDamageDAO();

            if (orderBy == null) {
                orderBy = FilterField.DAMAGE_FINISHED;
            }

            List<Damage> listOfResults = dao.getDamages(orderBy, asc, page, pageSize, filter);

            int amountOfResults = dao.getAmountOfDamages(filter);
            int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

            return damagespage.render(listOfResults, page, amountOfResults, amountOfPages);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * @return detail page containing all information about a specific damage
     */
    @RoleSecured.RoleAuthenticated()
    public static Result showDamageDetails(int damageId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            DamageDAO dao = context.getDamageDAO();
            UserDAO userDAO = context.getUserDAO();
            CarDAO carDAO = context.getCarDAO();
            DamageLogDAO damageLogDAO = context.getDamageLogDAO();
            FileDAO fileDAO = context.getFileDAO();
            Damage damage = dao.getDamage(damageId);
            List<File> proofList;
            if (damage.getProofId() != 0) {
                proofList = fileDAO.getFiles(damage.getProofId()).getList();
            } else {
                proofList = new ArrayList<>();
            }
            Car damagedCar = carDAO.getCar(damage.getCarRide().getReservation().getCar().getId());
            User owner = userDAO.getUser(damagedCar.getOwner().getId(), true);
            List<DamageLog> damageLogList = damageLogDAO.getDamageLogsForDamage(damageId);
            return ok(details.render(damage, owner, damagedCar, damageLogList, proofList));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * @return modal to edit damage information
     */
    @RoleSecured.RoleAuthenticated()
    public static Result editDamage(int damageId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            DamageDAO dao = context.getDamageDAO();
            Damage damage = dao.getDamage(damageId);

            if (damage == null) {
                flash("danger", "Schadedossier met ID=" + damageId + " bestaat niet.");
                return badRequest();
            } else {
                User currentUser = DataProvider.getUserProvider().getUser();
                if (!(damage.getCarRide().getReservation().getUser().getId() == currentUser.getId() || DataProvider.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.CAR_ADMIN))) {
                    flash("danger", "Je hebt geen rechten tot het bewerken van dit schadedossier.");
                    return badRequest();
                }

                DamageModel model = new DamageModel();
                model.populate(damage);

                Form<DamageModel> editForm = Form.form(DamageModel.class).fill(model);
                return ok(editmodal.render(editForm, damageId));
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     *
     * @return modal to provide new damage log status
     */
    @RoleSecured.RoleAuthenticated()
    public static Result addStatus(int damageId) {
        return ok(statusmodal.render(Form.form(DamageStatusModel.class), damageId));
    }

    /**
     * Method: GET
     *
     * @return modal to provide new damage proof
     */
    @RoleSecured.RoleAuthenticated()
    public static Result addProof(int damageId) {
        return ok(proofmodal.render(damageId));
    }

    /**
     * Method: POST
     *
     * @return redirect to the damage detail page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result editDamagePost(int damageId) {
        Form<DamageModel> damageForm = Form.form(DamageModel.class).bindFromRequest();
        if (damageForm.hasErrors()) {
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                DamageDAO dao = context.getDamageDAO();
                UserDAO userDAO = context.getUserDAO();
                CarDAO carDAO = context.getCarDAO();
                FileDAO fileDAO = context.getFileDAO();
                DamageLogDAO damageLogDAO = context.getDamageLogDAO();
                Damage damage = dao.getDamage(damageId);
                Car damagedCar = carDAO.getCar(damage.getCarRide().getReservation().getCar().getId());
                User owner = userDAO.getUser(damagedCar.getOwner().getId(), true);
                List<DamageLog> damageLogList = damageLogDAO.getDamageLogsForDamage(damageId);
                List<File> proofList;
                if (damage.getProofId() != 0) {
                    proofList = fileDAO.getFiles(damage.getProofId()).getList();
                } else {
                    proofList = new ArrayList<>();
                }
                flash("danger", "Beschrijving aanpassen mislukt.");
                return badRequest(details.render(damage, owner, damagedCar, damageLogList, proofList));
            } catch (DataAccessException ex) {
                throw ex; //log?
            }
        } else {

            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                DamageDAO damageDAO = context.getDamageDAO();
                Damage damage = damageDAO.getDamage(damageId);
                DamageModel model = damageForm.get();
                damage.setDescription(model.description);
                damage.setTime(model.time);
                damageDAO.updateDamage(damage);
                context.commit();
                flash("success", "De beschrijving werd gewijzigd.");
                return redirect(
                        routes.Damages.showDamageDetails(damageId)
                );
            } catch (DataAccessException ex) {
                throw ex; //TODO: show gracefully
            }
        }
    }


    /**
     * Method: POST
     *
     * @return redirect to the damage detail page
     */
    @RoleSecured.RoleAuthenticated()
    public static Result addStatusPost(int damageId) {
        Form<DamageStatusModel> damageStatusForm = Form.form(DamageStatusModel.class).bindFromRequest();
        if (damageStatusForm.hasErrors()) {
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                DamageDAO dao = context.getDamageDAO();
                UserDAO userDAO = context.getUserDAO();
                CarDAO carDAO = context.getCarDAO();
                FileDAO fileDAO = context.getFileDAO();
                DamageLogDAO damageLogDAO = context.getDamageLogDAO();
                Damage damage = dao.getDamage(damageId);
                Car damagedCar = carDAO.getCar(damage.getCarRide().getReservation().getCar().getId());
                User owner = userDAO.getUser(damagedCar.getOwner().getId(), true);
                List<DamageLog> damageLogList = damageLogDAO.getDamageLogsForDamage(damageId);
                List<File> proofList;
                if (damage.getProofId() != 0) {
                    proofList = fileDAO.getFiles(damage.getProofId()).getList();
                } else {
                    proofList = new ArrayList<>();
                }
                flash("danger", "Status toevoegen mislukt.");
                return badRequest(details.render(damage, owner, damagedCar, damageLogList, proofList));
            } catch (DataAccessException ex) {
                throw ex;
            }
        } else {

            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                DamageDAO damageDAO = context.getDamageDAO();
                DamageLogDAO damageLogDAO = context.getDamageLogDAO();
                Damage damage = damageDAO.getDamage(damageId);
                DamageStatusModel model = damageStatusForm.get();
                DamageLog damageLog = damageLogDAO.createDamageLog(damage, model.status);
                context.commit();
                if (damageLog == null) {
                    flash("danger", "Kon de damagelog niet toevoegen aan de database.");
                    return redirect(routes.Damages.showDamageDetails(damageId));
                }
                flash("success", "De status werd toegevoegd.");
                return redirect(
                        routes.Damages.showDamageDetails(damageId)
                );
            } catch (DataAccessException ex) {
                throw ex; //TODO: show gracefully
            }
        }
    }


    /**
     * Method: GET
     * <p/>
     * Called when a damage is closed/opened
     *
     * @param damageId The carCost being approved
     * @return the carcost index page
     */
    public static Result setDamageFinished(int damageId, int status) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            DamageDAO dao = context.getDamageDAO();
            Damage damage = dao.getDamage(damageId);
            damage.setFinished(status != 0);
            dao.updateDamage(damage);
            context.commit();
            flash("success", "Status van schadedossier succesvol aangepast");
            return redirect(routes.Damages.showDamageDetails(damageId));
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: POST
     * Processes the request to add files
     *
     * @param damageId The damage to edit
     * @return The detials page of a damage
     */
    @RoleSecured.RoleAuthenticated()
    public static Result addProofPost(int damageId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            DamageDAO damageDAO = context.getDamageDAO();
            Damage damage = damageDAO.getDamage(damageId);
            FileDAO fdao = context.getFileDAO();
            if (damage == null) {
                return redirect(routes.Damages.showDamageDetails(damageId));
            }
            try {
                boolean updateDamage = false; // Only perform a damage update when filegroup doesn't exist
                Http.MultipartFormData body = request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart newFile = body.getFile("file");
                if (newFile != null) {
                    if (!FileHelper.isDocumentContentType(newFile.getContentType())) {
                        flash("danger", "Het documentstype dat je bijgevoegd hebt is niet toegestaan. (" + newFile.getContentType() + ").");
                        return badRequest();
                    } else {
                        FileGroup group;
                        if (damage.getProofId() == 0) {
                            // Create new filegroup
                            group = fdao.createFileGroup();
                            damage.setProofId(group.getId());
                            updateDamage = true;

                        } else {
                            group = fdao.getFiles(damage.getProofId());
                        }
                        // Now we add the file to the group
                        Path relativePath = FileHelper.saveFile(newFile, ConfigurationHelper.getConfigurationString("uploads.damages"));
                        File file = fdao.createFile(relativePath.toString(), newFile.getFilename(), newFile.getContentType(), group.getId());
                        group.addFile(file);
                    }
                }
                if (updateDamage) {
                    damageDAO.updateDamage(damage);
                }
                context.commit();
                flash("success", "Bestand succesvol toegevoegd.");
                return redirect(routes.Damages.showDamageDetails(damageId));
            } catch (DataAccessException | IOException ex) { //IO or database error causes a rollback
                context.rollback();
                throw new RuntimeException(ex); //unchecked
            }
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     * @return proof url
     */
    @RoleSecured.RoleAuthenticated()
    public static Result getProof(int proofId) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            return FileHelper.getFileStreamResult(context.getFileDAO(), proofId);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    /**
     * Method: GET
     * Deletes a file from the damage filegroup
     * @param damageId The damage to delete from
     * @param fileId The file to delete
     * @return A redirect to the damage details
     */
    @RoleSecured.RoleAuthenticated()
    public static Result deleteProof(int damageId, int fileId){
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            FileDAO fileDAO = context.getFileDAO();
            File file = fileDAO.getFile(fileId);
            fileDAO.deleteFile(file.getId());
            FileHelper.deleteFile(Paths.get(file.getPath()));
            context.commit();
            flash("success", "Bestand succesvol verwijderd.");
            return redirect(routes.Damages.showDamageDetails(damageId));
        }catch (DataAccessException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the number of open damages
     * @return The number of damages
     */
    public static int openDamages() {
        User user = DataProvider.getUserProvider().getUser();
        try(DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            DamageDAO dao = context.getDamageDAO();
            return dao.getAmountOfOpenDamages(user.getId());
        } catch(DataAccessException ex) {
            throw ex;
        }
    }
}
