package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import controllers.Security.RoleSecured;
import controllers.util.*;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import providers.DataProvider;
import views.html.profile.*;

import javax.imageio.IIOException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static controllers.util.Addresses.getCountryList;
import static controllers.util.Addresses.modifyAddress;

public class Profile extends Controller {

    public static class EditProfileModel {
        public String phone;
        public String mobile;
        public String firstName;
        public String lastName;
        public String email; // TODO: verification

        public Addresses.EditAddressModel domicileAddress;
        public Addresses.EditAddressModel residenceAddress;

        public boolean paidDeposit;

        public EditProfileModel() {
            this.domicileAddress = new Addresses.EditAddressModel();
            this.residenceAddress = new Addresses.EditAddressModel();
        }

        public void populate(User user) {
            if (user == null)
                return;

            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.phone = user.getPhone();
            this.mobile = user.getCellphone();

            this.domicileAddress.populate(user.getAddressDomicile());
            this.residenceAddress.populate(user.getAddressResidence());
            this.paidDeposit = user.isPayedDeposit();
        }

        public String validate() {
            if (nullOrEmpty(firstName) || nullOrEmpty(lastName)) {
                return "Voor- en achternaam mogen niet leeg zijn.";
            } else return null;
        }
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * The page to upload a new profile picture
     *
     * @param userId The userId for which the picture is uploaded
     * @return The page to upload
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result profilePictureUpload(int userId) {
        return ok(uploadPicture.render(userId));
    }

    /**
     * Gets the profile picture for given user Id, or default one if missing
     *
     * @param userId The user for which the image is requested
     * @return The image with correct content type
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result getProfilePicture(int userId) {
        //TODO: checks on whether other person can see this
        //TODO: Rely on heavy caching of the image ID or views since each app page includes this
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO udao = context.getUserDAO();
        User user = udao.getUser(userId);
        if (user != null && user.getProfilePictureId() >= 0) {
            return FileHelper.getFileStreamResult(context.getFileDAO(), user.getProfilePictureId());
        } else {
            return FileHelper.getPublicFile(Paths.get("images", "no_profile.png").toString(), "image/png");
        }
    }

    /**
     * Processes a profile picture upload request
     *
     * @param userId
     * @return
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result profilePictureUploadPost(int userId) {
        // First we check if the user is allowed to upload to this userId
        User user;

        // We load the other user(by id)
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO dao = context.getUserDAO();
        user = dao.getUser(userId);

        // Check if the userId exists
        if (user == null || CurrentUser.getId() != user.getId() && !CurrentUser.hasRole(UserRole.PROFILE_ADMIN)) {
            return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
        }

        // Start saving the actual picture
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart picture = body.getFile("picture");
        if (picture != null) {
            String contentType = picture.getContentType();
            if (!FileHelper.isImageContentType(contentType)) { // Check the content type using MIME
                flash("danger", "Verkeerd bestandstype opgegeven. Enkel afbeeldingen zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                return badRequest(uploadPicture.render(userId));
            } else {
                try {
                    // We do not put this inside the try-block because then we leave the connection open through file IO, which blocks it longer than it should.
                    Path relativePath;

                    // Non-resized:
                    //relativePath = FileHelper.saveFile(picture, ConfigurationHelper.getConfigurationString("uploads.profile")); // save file to disk


                    // Resized:
                    try {
                        relativePath = FileHelper.saveResizedImage(picture, ConfigurationHelper.getConfigurationString("uploads.profile"), 800);
                    } catch (IIOException ex) {
                        // This means imagereader failed.
                        Logger.error("Failed profile picture resize: " + ex.getMessage());
                        flash("danger", "Jouw afbeelding is corrupt / niet ondersteund. Gelieve het opnieuw te proberen of een administrator te contacteren.");
                        return badRequest(uploadPicture.render(userId));
                    }

                    try {                     // Save the file reference in the database
                        FileDAO fdao = context.getFileDAO();
                        UserDAO udao = context.getUserDAO();
                        try {
                            File file = fdao.createFile(relativePath.toString(), picture.getFilename(), picture.getContentType());
                            int oldPictureId = user.getProfilePictureId();
                            user.setProfilePictureId(file.getId());
                            udao.updateUser(user);

                            if (oldPictureId != -1) {  // After commit we are sure the old one can be deleted
                                try {
                                    File oldPicture = fdao.getFile(oldPictureId);
                                    FileHelper.deleteFile(Paths.get(oldPicture.getPath())); // String -> nio.Path
                                    fdao.deleteFile(oldPictureId);

                                    context.commit();
                                } catch (DataAccessException ex) {
                                    // Failed to delete old profile picture, but no rollback needed
                                    throw ex;
                                }
                            }

                            flash("success", "De profielfoto werd met succes aangepast.");
                            return redirect(routes.Profile.index(userId));
                        } catch (DataAccessException ex) {
                            FileHelper.deleteFile(relativePath);
                            throw ex;
                        }
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

    /**
     * Method: GET
     *
     * @return A profile page for the currently requesting user
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result indexWithoutId() {
        User user = DataProvider.getUserProvider().getUser(false);  //user always has to exist (roleauthenticated)
        return ok(index.render(user, getProfileCompleteness(user), canEditProfile(user)));
    }

    /**
     * Method: GET
     *
     * @param userId The userId of the user (only available to administrator or user itself)
     * @return The profilepage of the user
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result index(int userId) {
        UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
        User user = dao.getUser(userId);

        // TODO: introduce userExists
        if (user == null) {
            flash("danger", "GebruikersID " + userId + " bestaat niet.");
            return redirect(routes.Dashboard.index());
        }

        User currentUser = DataProvider.getUserProvider().getUser();

        // Only a profile admin or user itself can edit
        if (canEditProfile(user)) {
            return ok(index.render(user, getProfileCompleteness(user), canEditProfile(user)));
        } else if (DataProvider.getUserRoleProvider().isFullUser(currentUser)) { // TODO: remove reference to currentUser
            Set<UserRole> roleSet = DataAccess.getInjectedContext().getUserRoleDAO().getUserRoles(userId);

            return ok(profile.render(user,
                    roleSet.contains(UserRole.SUPER_USER) ||
                            roleSet.contains(UserRole.CAR_ADMIN) ||
                            roleSet.contains(UserRole.INFOSESSION_ADMIN) ||
                            roleSet.contains(UserRole.MAIL_ADMIN) ||
                            roleSet.contains(UserRole.RESERVATION_ADMIN)));
        } else {
            return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN, UserRole.USER}));
        }
    }

    /**
     * Returns whether the currentUser can edit the profile of user
     *
     * @param user The subject
     * @return A boolean when the profile can be edited
     */
    private static boolean canEditProfile(User user) {
        return CurrentUser.getId() == user.getId() || CurrentUser.hasRole(UserRole.PROFILE_ADMIN);
    }

    public static class EditIdentityCardForm {
        public String cardNumber;
        public String nationalNumber;

        public EditIdentityCardForm() {
        }

        public EditIdentityCardForm(String cardNumber, String nationalNumber) {
            this.cardNumber = cardNumber;
            this.nationalNumber = nationalNumber;
        }

        public String validate() {
            return null; //TODO: use validation list
        }
    }

    /**
     * Method: GET
     * Generates the page where the user can upload his/her identity information
     *
     * @param userId The user the requester wants to edit
     * @return A page to edit the identity card information
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result editIdentityCard(int userId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO dao = context.getUserDAO();
        User user = dao.getUser(userId);

        if (user == null) {
            flash("danger", "GebruikersID " + userId + " bestaat niet.");
            return redirect(routes.Dashboard.index());
        }


        // Only a profile admin or user itself can edit
        if (canEditProfile(user)) {
            Form<EditIdentityCardForm> form = Form.form(EditIdentityCardForm.class);

            Iterable<File> listOfFiles = context.getFileDAO().getIdFiles(userId);
            if (user.getIdentityCard() != null) {
                form = form.fill(new EditIdentityCardForm(user.getIdentityCard().getId(), user.getIdentityCard().getRegistrationNr()));
            }

            return ok(identitycard.render(user, form, listOfFiles));
        } else {
            return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN, UserRole.USER}));
        }
    }

    /**
     * Returns the file requested after authorization checks
     *
     * @param userId
     * @param fileId
     * @return
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result viewFile(int userId, int fileId, String stype) {
        final FileType type = Enum.valueOf(FileType.class, stype);

        return FileHelper.genericFileAction(userId, fileId, new FileAction() {
            @Override
            public Result process(File file, FileDAO dao, DataAccessContext context) throws IOException, DataAccessException {
                return FileHelper.getFileStreamResult(dao, file.getId());
            }

            @Override
            public File getFile(int fileId, User user, FileDAO dao, DataAccessContext context) throws DataAccessException {
                if (!canEditProfile(user))
                    return null;

                Iterable<File> files = null;
                switch (type) {
                    case IDENTITYCARD:
                        if (user.getIdentityCard() != null)
                            files = dao.getIdFiles(userId);
                        break;
                    case DRIVERSLICENSE:
                        if (user.getLicense() != null)
                            files = dao.getLicenseFiles(userId);
                        break;
                }

                if (files == null)
                    return null;
                else
                    return getFileWithId(files, fileId);
            }

            @Override
            public Result failAction(User user) {
                return redirect(routes.Profile.editIdentityCard(user.getId()));
            }
        });
    }

    /*
     * Return the file in the list with the given idea
     */
    private static File getFileWithId(Iterable<File> files, int id) {
        // linear search is fast enough
        for (File file : files) {
            if (file.getId() == id) {
                return file;
            }
        }
        return null;
    }


    /**
     * Method: GET
     * Deletes a file from the identity card filegroup
     *
     * @param userId The user to delete from
     * @param fileId The file to delete
     * @return A redirect to the identity card page overview
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result deleteFile(final int userId, int fileId, String stype) {
        final FileType type = Enum.valueOf(FileType.class, stype);

        return FileHelper.genericFileAction(userId, fileId, new FileAction() {
            @Override
            public Result process(File file, FileDAO dao, DataAccessContext context) throws IOException, DataAccessException {
                dao.deleteFile(file.getId());
                FileHelper.deleteFile(Paths.get(file.getPath()));
                context.commit();

                flash("success", file.getFileName() + " werd met succes verwijderd.");
                switch (type) {
                    case IDENTITYCARD:
                        return redirect(routes.Profile.editIdentityCard(userId));
                    case DRIVERSLICENSE:
                        return redirect(routes.Profile.editDriversLicense(userId));
                }
                return badRequest("No action specified for type: " + type);
            }

            @Override
            public File getFile(int fileId, User user, FileDAO dao, DataAccessContext context) throws DataAccessException {
                if (!canEditProfile(user))
                    return null;

                Iterable<File> files = null;
                switch (type) {
                    case IDENTITYCARD:
                        if (user.getIdentityCard() != null)
                            files = dao.getIdFiles(userId);
                        break;
                    case DRIVERSLICENSE:
                        if (user.getLicense() != null)
                            files = dao.getLicenseFiles(userId);
                        break;
                }

                if (files == null)
                    return null;
                else
                    return getFileWithId(files, fileId);
            }

            @Override
            public Result failAction(User user) {
                return redirect(routes.Profile.editIdentityCard(user.getId()));
            }
        });
    }

    /**
     * Method: POST
     * Processes the request to add files / change identity card information
     *
     * @param userId The user to edit
     * @return The overview page or error page when something went wrong
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result editIdentityCardPost(int userId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO udao = context.getUserDAO();
        FileDAO fdao = context.getFileDAO();
        User user = udao.getUser(userId);
        if (user == null || !canEditProfile(user)) {
            return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
        }
        Iterable<File> listOfFiles = fdao.getIdFiles(userId);

        Form<EditIdentityCardForm> form = Form.form(EditIdentityCardForm.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(identitycard.render(user, form, listOfFiles));
        } else {
            try {
                boolean updateUser = false; // Only perform a user update when we changed something (so not when adding a file to existing filegroup)

                Http.MultipartFormData body = request().body().asMultipartFormData();
                EditIdentityCardForm model = form.get();

                IdentityCard card = user.getIdentityCard();
                if (card == null) {
                    updateUser = true;
                    card = new IdentityCard();
                    user.setIdentityCard(card);
                }

                // Now check if we also have to create / add file to the group
                Http.MultipartFormData.FilePart newFile = body.getFile("file");
                if (newFile != null) {
                    if (!FileHelper.isDocumentContentType(newFile.getContentType())) {
                        flash("danger", "Het documentstype dat je bijgevoegd hebt is niet toegestaan. (" + newFile.getContentType() + ").");
                        return badRequest(identitycard.render(user, form, listOfFiles));
                    } else {
                        // Now we add the file to the group
                        Path relativePath = FileHelper.saveFile(newFile, ConfigurationHelper.getConfigurationString("uploads.identitycard"));
                        // TODO: combine statements in DAO
                        File file = fdao.createFile(relativePath.toString(), newFile.getFilename(), newFile.getContentType());
                        fdao.addIdFile(user.getId(), file.getId());
                    }
                }

                if ((user.getIdentityCard().getRegistrationNr() != null && !user.getIdentityCard().getRegistrationNr().equals(model.nationalNumber)) ||
                        (user.getIdentityCard().getRegistrationNr() == null && model.nationalNumber != null) ||
                        (user.getIdentityCard().getId() == null && model.cardNumber != null) ||
                        (user.getIdentityCard().getId() != null && !user.getIdentityCard().getId().equals(model.cardNumber))) {
                    card.setRegistrationNr(model.nationalNumber);
                    card.setId(model.cardNumber);
                    updateUser = true;
                }

                if (updateUser) {
                    udao.updateUser(user);
                }

                flash("success", "Jouw identiteitskaart werd succesvol bijgewerkt.");
                return ok(identitycard.render(user, form, listOfFiles));
            } catch (DataAccessException | IOException ex) { //IO or database error causes a rollback
                context.rollback();
                throw new RuntimeException(ex); //unchecked
            }
        }

    }

    public static class EditDriversLicenseModel {
        public String cardNumber;

        public EditDriversLicenseModel() {
        }

        public EditDriversLicenseModel(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        public String validate() {
            return null; //TODO: use validation list
        }
    }

    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result editDriversLicense(int userId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO dao = context.getUserDAO();
        User user = dao.getUser(userId);

        if (user == null) {
            flash("danger", "GebruikersID " + userId + " bestaat niet.");
            return redirect(routes.Dashboard.index());
        }

        // Only a profile admin or user itself can edit
        if (canEditProfile(user)) {
            Form<EditDriversLicenseModel> form = Form.form(EditDriversLicenseModel.class);

            Iterable<File> listOfFiles = context.getFileDAO().getLicenseFiles(userId);
            if (user.getLicense() != null) {
                form = form.fill(new EditDriversLicenseModel(user.getLicense()));
            }

            return ok(driverslicense.render(user, form, listOfFiles));
        } else {
            return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN, UserRole.USER}));
        }
    }

    // TODO: a LOT of code overlap with identity card!!
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result editDriversLicensePost(int userId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO udao = context.getUserDAO();
        FileDAO fdao = context.getFileDAO();
        User user = udao.getUser(userId);

        if (user == null || !canEditProfile(user)) {
            return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
        }

        Iterable<File> listOfFiles = context.getFileDAO().getLicenseFiles(userId);
        Form<EditDriversLicenseModel> form = Form.form(EditDriversLicenseModel.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(driverslicense.render(user, form, listOfFiles));
        } else {
            try {
                boolean updateUser = false; // Only perform a user update when we changed something (so not when adding a file to existing filegroup)

                Http.MultipartFormData body = request().body().asMultipartFormData();
                EditDriversLicenseModel model = form.get();

                String card = user.getLicense();


                // Now check if we also have to create / add file to the group
                Http.MultipartFormData.FilePart newFile = body.getFile("file");
                if (newFile != null) {
                    if (!FileHelper.isDocumentContentType(newFile.getContentType())) {
                        flash("danger", "Het documentstype dat je bijgevoegd hebt is niet toegestaan. (" + newFile.getContentType() + ").");
                        return badRequest(driverslicense.render(user, form, listOfFiles));
                    } else {
                        // Now we add the file to the group
                        Path relativePath = FileHelper.saveFile(newFile, ConfigurationHelper.getConfigurationString("uploads.driverslicense"));
                        File file = fdao.createFile(relativePath.toString(), newFile.getFilename(), newFile.getContentType());
                        fdao.addLicenseFile(user.getId(), file.getId());
                    }
                }

                if (user.getLicense() != null && !user.getLicense().equals(model.cardNumber) ||
                        model.cardNumber != null && user.getLicense() == null) {
                    user.setLicense(model.cardNumber);
                    updateUser = true;
                }

                if (updateUser) {
                    udao.updateUser(user);
                }

                flash("success", "Je rijbewijs werd succesvol bijgewerkt.");
                return ok(driverslicense.render(user, form, listOfFiles));
            } catch (IOException ex) { //IO or database error causes a rollback
                throw new RuntimeException(ex); //unchecked
            }
        }

    }


    /**
     * Method: GET
     * Creates a prefilled form to edit the profile
     *
     * @param userId The user to edit
     * @return A user edit page
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result edit(int userId) {
        UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
        User user = dao.getUser(userId);

        if (!canEditProfile(user)) {
            return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
        }

        EditProfileModel model = new EditProfileModel();
        model.populate(user);
        return ok(edit.render(Form.form(EditProfileModel.class).fill(model), user, getCountryList()));
    }

    /**
     * Returns a quotum on how complete the profile is.
     *
     * @param user The user to quote
     * @return Completeness in percents
     */
    // TODO: should go into a helper class? (Also used by Dashboard)
    public static int getProfileCompleteness(User user) {
        int total = 0;

        if (user.getAddressDomicile() != null) {
            total++;
        }
        if (user.getAddressResidence() != null) {
            total++;
        }
        if (!nullOrEmpty(user.getCellphone())) {
            total++;
        }
        if (!nullOrEmpty(user.getFirstName())) {
            total++;
        }
        if (!nullOrEmpty(user.getLastName())) {
            total++;
        }
        if (!nullOrEmpty(user.getPhone())) {
            total++;
        }
        if (!nullOrEmpty(user.getEmail())) {
            total++;
        }
        if (user.getIdentityCard() != null) {
            total++;
        }
        return (int) (((float) total / 8) * 100); //9 records
    }

    @RoleSecured.RoleAuthenticated({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result editUserStatus(int userId) {
        UserDAO udao = DataAccess.getInjectedContext().getUserDAO();
        User user = udao.getUser(userId);
        if (user == null) {
            flash("danger", "GebruikersID bestaat niet.");
            return redirect(routes.Users.showUsers());
        } else {
            return ok(editstatus.render(user));
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result editUserStatusPost(int userId) {
        UserDAO udao = DataAccess.getInjectedContext().getUserDAO();
        User user = udao.getUser(userId);
        if (user == null) {
            flash("danger", "GebruikersID bestaat niet.");
            return redirect(routes.Users.showUsers());
        } else {
            String strStatus = Form.form().bindFromRequest().get("status");
            UserStatus status = Enum.valueOf(UserStatus.class, strStatus);
            if (user.getStatus() != status) {
                user.setStatus(status);
                udao.updateUser(user);

                DataProvider.getUserProvider().invalidateUser(user); //wipe the status from ram

                flash("success", "De gebruikersstatus werd succesvol aangepast.");
            } else {
                flash("warning", "De gebruiker had reeds de opgegeven status.");
            }
            return ok(editstatus.render(user));
        }
    }


    /**
     * Method: POST
     * Changes the users profile based on submitted form data
     *
     * @param userId The user id to change
     * @return The new profile page, or the edit form when errors occured
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result editPost(int userId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO dao = context.getUserDAO();
        User user = dao.getUser(userId);

        boolean isProfileAdmin = CurrentUser.hasRole(UserRole.PROFILE_ADMIN);

        if (CurrentUser.getId() != user.getId() && !isProfileAdmin) {
            return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
        }

        Form<EditProfileModel> profileForm = Form.form(EditProfileModel.class).bindFromRequest();
        if (profileForm.hasErrors()) {
            return badRequest(edit.render(profileForm, user, getCountryList()));
        } else {
            EditProfileModel model = profileForm.get();
            AddressDAO adao = context.getAddressDAO();

            user.setPhone(model.phone);
            user.setCellphone(model.mobile);
            user.setFirstName(model.firstName);
            user.setLastName(model.lastName);

            // Admins can change the payment status
            if (isProfileAdmin) {
                user.setPayedDeposit(model.paidDeposit);
            }

            // Because of constraints with FK we have to set them to NULL first in user before deleting them

            // Check domicile address
            Address domicileAddress = user.getAddressDomicile();
            boolean deleteDomicile = model.domicileAddress.isEmpty() && domicileAddress != null;
            user.setAddressDomicile(deleteDomicile || model.domicileAddress.isEmpty() ? null : modifyAddress(model.domicileAddress, domicileAddress, adao));

            // Residence address
            Address residenceAddress = user.getAddressResidence();
            boolean deleteResidence = model.residenceAddress.isEmpty() && residenceAddress != null;
            user.setAddressResidence(deleteResidence || model.residenceAddress.isEmpty() ? null : modifyAddress(model.residenceAddress, residenceAddress, adao));

            dao.updateUser(user); // Full update (includes FKs)

            // Finally we can delete the addresses since there are no references left (this assumes all other code uses copies of addresses)
            // TODO: soft-delete addresses and keep references
            if (deleteDomicile)
                adao.deleteAddress(domicileAddress.getId());

            if (deleteResidence)
                adao.deleteAddress(residenceAddress.getId());

            //TODO: identity card & numbers, profile picture

            flash("success", "Het profiel werd succesvol aangepast.");

            DataProvider.getUserProvider().invalidateUser(user); //invalidate cache
            return redirect(routes.Profile.index(userId));
        }
    }
}
