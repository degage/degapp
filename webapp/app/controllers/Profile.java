/* Profile.java
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
import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import controllers.util.Addresses;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import data.ProfileCompleteness;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.profile.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;

import static be.ugent.degage.db.dao.FileDAO.UserFileType.ID;
import static be.ugent.degage.db.dao.FileDAO.UserFileType.LICENSE;
import static controllers.util.Addresses.updateAddress;

public class Profile extends Controller {

    private static Result mustBeProfileAdmin() {
        return badRequest(views.html.errors.unauthorized.render());
    }

    /**
     * The page to upload a new profile picture
     *
     * @param userId The userId for which the picture is uploaded
     * @return The page to upload
     */
    @AllowRoles({})
    @InjectContext
    public static Result profilePictureUpload(int userId) {
        if (canEditProfile(userId)) {
            // TODO avoid these checks by introducing an annotation
            UserHeader user = DataAccess.getInjectedContext().getUserDAO().getUserHeader(userId);
            return ok(uploadPicture.render(userId, user.toString()));
        } else {
            return mustBeProfileAdmin();
        }
    }

    /**
     * Gets the profile picture for given user Id, or default one if missing
     *
     * @param userId The user for which the image is requested
     * @return The image with correct content type
     */
    @AllowRoles({})
    @InjectContext
    public static Result getProfilePicture(int userId) {
        int profilePictureId = 0;
        DataAccessContext context = DataAccess.getInjectedContext();
        if (canSeeProfile(userId)) {
            profilePictureId = context.getUserDAO().getUserPicture(userId);
        }
        if (profilePictureId > 0) {
            return FileHelper.getFileStreamResult(context.getFileDAO(), profilePictureId);
        } else {
            return FileHelper.getPublicFile("images/user.png", "image/png");
        }
    }

    /**
     * Processes a profile picture upload request
     */
    @AllowRoles({})
    @InjectContext
    public static Result profilePictureUploadPost(int userId) {
        if (canEditProfile(userId)) {
            UserDAO dao = DataAccess.getInjectedContext().getUserDAO();

            File file = FileHelper.getFileFromRequest("picture", FileHelper.IMAGE_CONTENT_TYPES, "uploads.profile", 450);
            if (file == null) {
                flash("danger", "Je moet een bestand kiezen");
                return redirect(routes.Profile.profilePictureUpload(userId));
            } else if (file.getContentType() == null) {
                flash("danger", "Verkeerd bestandstype opgegeven. Enkel afbeeldingen zijn toegelaten.");
                return redirect(routes.Profile.profilePictureUpload(userId));
            } else {
                int oldPictureId = dao.getUserPicture(userId);
                dao.updateUserPicture(userId, file.getId());
                FileHelper.deleteOldFile(oldPictureId);
                return redirect(routes.Profile.index(userId));
            }
        } else {
            return mustBeProfileAdmin();
        }
    }

    /**
     * @return A profile page for the currently requesting user
     */
    @AllowRoles({})
    @InjectContext
    public static Result indexWithoutId() {
        return index(CurrentUser.getId());
    }

    /**
     * @param userId The userId of the user
     * @return A profile page for the user
     */
    @AllowRoles({})
    @InjectContext
    public static Result index(int userId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO dao = context.getUserDAO();
        User user = dao.getUser(userId);

        // Only a profile admin or user itself can edit
        if (canEditProfile(userId)) {
            ProfileCompleteness pc = new ProfileCompleteness(
                    user,
                    context.getFileDAO().hasUserFile(userId, LICENSE),
                    dao.getUserPicture(userId) > 0
            );
            return ok(index.render(user, pc.getPercentage()));
        } else if (canSeeProfile(userId)) {
            return ok(profile.render(user)); // only 'public' part of the profile
        } else { // if not yet full user and profile is not ones own
            return badRequest(views.html.errors.privacy.render());
        }
    }

    /**
     * Returns whether the current user can edit the profile of the given user
     */
    private static boolean canEditProfile(int userId) {
        return CurrentUser.is(userId) || CurrentUser.hasRole(UserRole.PROFILE_ADMIN);
    }

    /**
     * Returns whether the current user is allowed to see the main profile page of the
     * given user.
     */
    private static boolean canSeeProfile(int userId) {
        // use in injected context only
        if (canEditProfile(userId)) {
            return true;
        } else if (!CurrentUser.hasFullStatus()) {
            return false;
        } else {
            // TODO: contract admin can see profiles
            UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
            return dao.canSeeProfileAsUser(CurrentUser.getId(), userId) ||
                    (CurrentUser.hasRole(UserRole.CAR_OWNER) &&
                            dao.canSeeProfileAsOwner(CurrentUser.getId(), userId));
        }
    }

    public static class IdentityCardData {
        public String cardNumber;
        public String nationalNumber;

        public IdentityCardData() {
        }

        public IdentityCardData populate(String cardNumber, String nationalNumber) {
            this.cardNumber = cardNumber;
            this.nationalNumber = nationalNumber;
            return this;
        }
    }

    /**
     * Method: GET
     * Generates the page where the user can upload his/her identity information
     *
     * @param userId The user the requester wants to edit
     * @return A page to edit the identity card information
     */
    @AllowRoles({})
    @InjectContext
    public static Result editIdentityCard(int userId) {

        if (canEditProfile(userId)) {
            DataAccessContext context = DataAccess.getInjectedContext();
            User user = context.getUserDAO().getUser(userId);
            return ok(identitycard.render(
                    userId,
                    Form.form(IdentityCardData.class).fill(new IdentityCardData().populate(
                            user.getIdentityId(), user.getNationalId())),
                    context.getFileDAO().getUserFiles(userId, ID),
                    CurrentUser.hasRole(UserRole.PROFILE_ADMIN))
            );
        } else {
            return mustBeProfileAdmin();
        }
    }

    // use in injected context only
    private static Result viewUserFile(int userId, int fileId, FileDAO.UserFileType uft) {
        if (canEditProfile(userId)) {
            FileDAO fdao = DataAccess.getInjectedContext().getFileDAO();
            File file = fdao.getUserFile(userId, fileId, uft);
            if (file == null) {
                flash("danger", "Bestand niet gevonden.");
                return null;
            } else {
                return FileHelper.getFileStreamResult(fdao, fileId);
            }
        } else {
            return mustBeProfileAdmin();
        }

    }

    /**
     * Returns the file requested after authorization checks
     */
    @AllowRoles({})
    @InjectContext
    public static Result viewIdentityFile(int userId, int fileId) {
        Result result = viewUserFile(userId, fileId, ID);
        if (result == null) {
            return redirect(routes.Profile.editIdentityCard(userId));
        } else {
            return result;
        }
    }

    /**
     * Returns the file requested after authorization checks
     */
    @AllowRoles({})
    @InjectContext
    public static Result viewLicenseFile(int userId, int fileId) {
        Result result = viewUserFile(userId, fileId, LICENSE);
        if (result == null) {
            return redirect(routes.Profile.editDriversLicense(userId));
        } else {
            return result;
        }
    }

    // use only in injected context
    private static boolean deleteUserFile(int userId, int fileId, FileDAO.UserFileType uft) {
        FileDAO fdao = DataAccess.getInjectedContext().getFileDAO();
        File file = fdao.getUserFile(userId, fileId, uft);
        if (file == null) {
            flash("danger", "Bestand niet gevonden.");
            return false;
        } else if (canEditProfile(userId)) {
            fdao.deleteUserFile(userId, fileId, uft);
            FileHelper.deleteFile(Paths.get(file.getPath()));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method: GET
     * Deletes a file from the identity card filegroup
     *
     * @param userId The user to delete from
     * @param fileId The file to delete
     * @return A redirect to the identity card page overview
     */
    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result deleteIdentityFile(final int userId, int fileId) {
        deleteUserFile(userId, fileId, ID);
        return redirect(routes.Profile.editIdentityCard(userId));
    }


    /**
     * Method: GET
     * Deletes a file from the identity card filegroup
     *
     * @param userId The user to delete from
     * @param fileId The file to delete
     * @return A redirect to the identity card page overview
     */
    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result deleteLicenseFile(final int userId, int fileId) {
        deleteUserFile(userId, fileId, LICENSE);
        return redirect(routes.Profile.editDriversLicense(userId));
    }

    /**
     * Method: POST
     * Processes the request to add files / change identity card information
     *
     * @param userId The user to edit
     * @return The overview page or error page when something went wrong
     */
    @AllowRoles({})
    @InjectContext
    public static Result editIdentityCardPost(int userId) {
        if (canEditProfile(userId)) {
            DataAccessContext context = DataAccess.getInjectedContext();
            Iterable<File> listOfFiles = context.getFileDAO().getUserFiles(userId, ID);
            Form<IdentityCardData> form = Form.form(IdentityCardData.class).bindFromRequest();
            if (form.hasErrors()) {
                return badRequest(identitycard.render(userId, form, listOfFiles,
                        CurrentUser.hasRole(UserRole.PROFILE_ADMIN)));
            } else {
                IdentityCardData data = form.get();
                context.getUserDAO().updateUserIdentityData(userId, data.cardNumber, data.nationalNumber);

                flash("success", "De identiteitsgegevens werden bijgewerkt.");
                return redirect(routes.Profile.editIdentityCard(userId));
            }
        } else {
            return mustBeProfileAdmin();
        }
    }

    @AllowRoles({})
    @InjectContext
    public static Result addIdentityCardFile(int userId) {
        if (canEditProfile(userId)) {
            try {
                DataAccessContext context = DataAccess.getInjectedContext();
                UserDAO udao = context.getUserDAO();
                User user = udao.getUser(userId);

                Http.MultipartFormData.FilePart newFile = request().body().asMultipartFormData().getFile("file");
                if (newFile == null) {
                    flash("danger", "Geen bestand gekozen");
                } else {
                    if (!FileHelper.isDocumentContentType(newFile.getContentType())) {
                        flash("danger", "Het document of de afbeelding heeft een type dat niet wordt herkend of toegestaan");
                    } else {
                        // Now we add the file to the group
                        FileDAO fdao = context.getFileDAO();
                        // TODO: combine statements in DAO
                        File file = fdao.createFile(
                                FileHelper.saveFile(newFile, ConfigurationHelper.getConfigurationString("uploads.identitycard")).toString(),
                                newFile.getFilename(),
                                newFile.getContentType());
                        fdao.addUserFile(user.getId(), file.getId(), ID);
                    }
                }
                return redirect(routes.Profile.editIdentityCard(userId));
            } catch (IOException ex) {
                throw new RuntimeException(ex); //unchecked
            }
        } else {
            return mustBeProfileAdmin();
        }
    }

    public static class DriversLicenseData {
        public String cardNumber;

        public LocalDate cardDate;

        public DriversLicenseData() {
        }

        public DriversLicenseData populate(String cardNumber, LocalDate cardDate) {
            this.cardNumber = cardNumber;
            this.cardDate = cardDate;
            return this;
        }
    }

    @AllowRoles({})
    @InjectContext
    public static Result editDriversLicense(int userId) {
        if (canEditProfile(userId)) {
            DataAccessContext context = DataAccess.getInjectedContext();
            User user = context.getUserDAO().getUser(userId);
            return ok(driverslicense.render(
                    userId,
                    Form.form(DriversLicenseData.class).fill(
                            new DriversLicenseData().populate(user.getLicense(), user.getLicenseDate())),
                    context.getFileDAO().getUserFiles(userId, LICENSE),
                    CurrentUser.hasRole(UserRole.PROFILE_ADMIN)
                    )
            );
        } else {
            return mustBeProfileAdmin();
        }
    }

    // TODO: a LOT of code overlap with identity card!!
    @AllowRoles({})
    @InjectContext
    public static Result editDriversLicensePost(int userId) {

        if (canEditProfile(userId)) {
            DataAccessContext context = DataAccess.getInjectedContext();
            Iterable<File> listOfFiles = context.getFileDAO().getUserFiles(userId, LICENSE);
            Form<DriversLicenseData> form = Form.form(DriversLicenseData.class).bindFromRequest();
            if (form.hasErrors()) {
                return badRequest(driverslicense.render(userId, form, listOfFiles,
                        CurrentUser.hasRole(UserRole.PROFILE_ADMIN)));
            } else {
                UserDAO udao = context.getUserDAO();
                DriversLicenseData data = form.get();
                udao.updateUserLicenseData(userId, data.cardNumber, data.cardDate);

                flash("success", "De rijbewijsgegevens werden bijgewerkt.");
                return redirect(routes.Profile.editDriversLicense(userId));
            }
        } else {
            return mustBeProfileAdmin();
        }
    }

    @AllowRoles({})
    @InjectContext
    public static Result addDriversLicenseFile(int userId) {

        if (canEditProfile(userId)) {

            try {
                DataAccessContext context = DataAccess.getInjectedContext();
                Http.MultipartFormData.FilePart newFile = request().body().asMultipartFormData().getFile("file");
                if (newFile == null) {
                    flash("danger", "Geen bestand gekozen");
                } else if (!FileHelper.isDocumentContentType(newFile.getContentType())) {
                    flash("danger", "Het document of de afbeelding heeft een type dat niet wordt herkend of toegestaan");
                } else {
                    FileDAO fdao = context.getFileDAO();
                    File file = fdao.createFile(
                            FileHelper.saveFile(newFile, ConfigurationHelper.getConfigurationString("uploads.driverslicense")).toString(),
                            newFile.getFilename(),
                            newFile.getContentType()
                    );
                    fdao.addUserFile(userId, file.getId(), LICENSE);
                }
                return redirect(routes.Profile.editDriversLicense(userId));
            } catch (IOException ex) {
                throw new RuntimeException(ex); //unchecked
            }
        } else {
            return mustBeProfileAdmin();
        }
    }


    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result editUserStatus(int userId) {
        UserDAO udao = DataAccess.getInjectedContext().getUserDAO();
        UserHeader user = udao.getUserHeader(userId);
        switch (user.getStatus()) {
            case FULL:
                return ok(editstatusFull.render(user));
            case BLOCKED:
            case DROPPED:
                return ok(editstatusBlocked.render(user));
            default: // this page should not be called when user not yet full
                return badRequest();
        }
    }

    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result blockUser(int userId) {
        DataAccess.getInjectedContext().getUserDAO().updateUserStatus(userId, UserStatus.BLOCKED);
        return redirect(routes.Profile.editUserStatus(userId));
    }

    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result unblockUser(int userId) {
        DataAccess.getInjectedContext().getUserDAO().updateUserStatus(userId, UserStatus.REGISTERED);
        return redirect(routes.Profile.editUserStatus(userId));
    }

    public static class DepositData {
        public Integer amount; // deposit
        public Integer fee;

        public DepositData populate(Membership membership) {
            this.amount = membership.getDeposit();
            this.fee = membership.getFee();
            return this;
        }
    }

    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result deposit(int userId) {
        Membership membership = DataAccess.getInjectedContext().getUserDAO().getMembership(userId);
        return ok(deposit.render(
                Form.form(DepositData.class).fill(new DepositData().populate(membership)),
                userId,
                membership.getFullName())
        );
    }

    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result depositPost(int userId) {
        Form<DepositData> form = Form.form(DepositData.class).bindFromRequest();
        UserDAO userDAO = DataAccess.getInjectedContext().getUserDAO();
        if (form.hasErrors()) {
            Membership membership = userDAO.getMembership(userId);
            return badRequest(deposit.render(form, membership.getId(), membership.getFullName()));
        } else {
            DepositData data = form.get();
            userDAO.updateUserMembership(userId, data.amount, data.fee);

            return redirect(routes.Profile.index(userId));
        }
    }

    public static class MainProfileData {
        public String phone;
        public String mobile;

        @Constraints.Required
        public String firstName;


        @Constraints.Required
        public String lastName;

        public Addresses.EditAddressModel domicileAddress;
        public Addresses.EditAddressModel residenceAddress;

        public MainProfileData() {
            this.domicileAddress = new Addresses.EditAddressModel();
            this.residenceAddress = new Addresses.EditAddressModel();
        }

        public MainProfileData populate(User user) {
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.phone = user.getPhone();
            this.mobile = user.getCellPhone();

            this.domicileAddress.populate(user.getAddressDomicile());
            this.residenceAddress.populate(user.getAddressResidence());
            return this;
        }
    }

    /**
     * Show the main profile editing page
     */
    @AllowRoles({})
    @InjectContext
    public static Result edit(int userId) {
        if (canEditProfile(userId)) {
            User user = DataAccess.getInjectedContext().getUserDAO().getUser(userId);
            return ok(edit.render(
                    Form.form(MainProfileData.class).fill(new MainProfileData().populate(user)),
                    user)
            );
        } else {
            return mustBeProfileAdmin();
        }
    }

    /**
     * Change the main profile data and the addresses of a user
     */
    @AllowRoles({})
    @InjectContext
    public static Result editPost(int userId) {
        Form<MainProfileData> form = Form.form(MainProfileData.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        UserDAO dao = context.getUserDAO();
        User user = dao.getUser(userId);
        if (form.hasErrors()) {
            return badRequest(edit.render(form, user));
        } else if (canEditProfile(userId)) {
            MainProfileData data = form.get();
            user.setPhone(data.phone);
            user.setCellPhone(data.mobile);
            user.setFirstName(data.firstName);
            user.setLastName(data.lastName);
            dao.updateUserMainProfile(user);

            AddressDAO adao = context.getAddressDAO();
            updateAddress(data.domicileAddress, user.getAddressDomicile().getId(), adao);
            updateAddress(data.residenceAddress, user.getAddressResidence().getId(), adao);

            flash("success", "De profielgegevens werden met succes aangepast");

            return redirect(routes.Profile.index(userId));
        } else {
            return mustBeProfileAdmin();
        }
    }

    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result updateEmail(int userId) {
        UserHeader user = DataAccess.getInjectedContext().getUserDAO().getUserHeader(userId);
        Login.EmailData data = new Login.EmailData();
        data.email = user.getEmail();
        return ok(updateEmail.render(
                Form.form(Login.EmailData.class).fill(data), user
        ));
    }

    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result updateEmailPost(int userId) {

        Form<Login.EmailData> form = Form.form(Login.EmailData.class).bindFromRequest();
        UserDAO userDAO = DataAccess.getInjectedContext().getUserDAO();
        UserHeader user = userDAO.getUserHeader(userId);
        if (form.hasErrors()) {
            return badRequest(updateEmail.render(form, user));
        } else {
            if (userDAO.updateUserEmail(userId, form.get().email)) {
                return redirect(routes.Profile.index(userId));
            } else {
                form.reject("Dit e-mailadres is al in gebruik");
                return badRequest(updateEmail.render(form, user));
            }
        }
    }
}
