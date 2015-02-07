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
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import com.google.common.base.Strings;
import controllers.util.*;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import providers.DataProvider;
import views.html.profile.*;

import javax.imageio.IIOException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static controllers.util.Addresses.updateAddress;

public class Profile extends Controller {

    private static Result mustBeProfileAdmin() {
        return badRequest(views.html.unauthorized.render(new UserRole[]{UserRole.PROFILE_ADMIN}));
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
        return ok(uploadPicture.render(userId));
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
        DataAccessContext context = DataAccess.getInjectedContext();
        int profilePictureId = context.getUserDAO().getUser(userId).getProfilePictureId();
        if (profilePictureId >= 0) {
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
        // We load the other user(by id)
        if (canEditProfile(userId)) {

            // Start saving the actual picture
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart picture = body.getFile("picture");

            if (picture == null) {
                flash("danger", "Geen bestand gekozen");
                return redirect(routes.Profile.profilePictureUpload(userId));
            }

            String contentType = picture.getContentType();
            if (!FileHelper.isImageContentType(contentType)) { // Check the content type using MIME
                flash("danger", "Verkeerd bestandstype opgegeven. Enkel afbeeldingen zijn toegelaten.");
                return redirect(routes.Profile.profilePictureUpload(userId));
            }

            try {
                Path relativePath = FileHelper.saveResizedImage(picture, ConfigurationHelper.getConfigurationString("uploads.profile"), 450);

                try {
                    DataAccessContext context = DataAccess.getInjectedContext();
                    UserDAO dao = context.getUserDAO();
                    User user = dao.getUser(userId);

                    FileDAO fdao = context.getFileDAO();
                    UserDAO udao = context.getUserDAO();
                    File file = fdao.createFile(relativePath.toString(), picture.getFilename(), picture.getContentType());
                    int oldPictureId = user.getProfilePictureId();
                    udao.updateUserPicture(userId, file.getId());

                    if (oldPictureId != -1) {
                        File oldPicture = fdao.getFile(oldPictureId);
                        FileHelper.deleteFile(Paths.get(oldPicture.getPath())); // String -> nio.Path
                        fdao.deleteFile(oldPictureId);
                    }

                    flash("success", "De profielfoto werd met succes aangepast.");
                    return redirect(routes.Profile.index(userId));
                } catch (DataAccessException ex) {
                    FileHelper.deleteFile(relativePath);
                    throw ex;
                }
            } catch (IIOException ex) {
                // This means imagereader failed.
                Logger.error("Failed profile picture resize: " + ex.getMessage());
                flash("danger", "Er is iets mis met het afbeeldingsbestand. Probeer opnieuw met een ander bestand.");
                return redirect(routes.Profile.profilePictureUpload(userId));
            } catch (IOException ex) {
                throw new DataAccessException("Fout bij het uploaden", ex); //no more checked catch -> error page!
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
        User user = DataAccess.getInjectedContext().getUserDAO().getUser(userId);

        // Only a profile admin or user itself can edit
        if (canEditProfile(userId)) {
            return ok(index.render(user, getProfileCompleteness(user)));
        } else if (CurrentUser.hasFullStatus()) { // TODO: remove reference to currentUser
            return ok(profile.render(user)); // public profile
        } else { // if not yet full user and profile is not ones own
            return mustBeProfileAdmin();
        }
    }

    /**
     * Returns whether the currentUser can edit the profile of the given user
     */
    private static boolean canEditProfile(int userId) {
        return CurrentUser.is(userId) || CurrentUser.hasRole(UserRole.PROFILE_ADMIN);
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
                            context.getFileDAO().getIdFiles(userId))
            );
        } else {
            return mustBeProfileAdmin();
        }
    }

    /**
     * Returns the file requested after authorization checks
     *
     * @param userId
     * @param fileId
     * @return
     */
    @AllowRoles({})
    @InjectContext
    public static Result viewIdentityFile(int userId, int fileId) {

        if (canEditProfile(userId)) {
            FileDAO fdao = DataAccess.getInjectedContext().getFileDAO();
            File file = getFileWithId(fdao.getIdFiles(userId), fileId);
            if (file == null) {
                flash("danger", "Bestand niet gevonden.");
                return redirect(routes.Profile.editIdentityCard(userId));
            } else {
                return FileHelper.getFileStreamResult(fdao, file.getId());
            }
        } else {
            return mustBeProfileAdmin();
        }
    }

    /**
     * Returns the file requested after authorization checks
     *
     * @param userId
     * @param fileId
     * @return
     */
    @AllowRoles({})
    @InjectContext
    public static Result viewLicenseFile(int userId, int fileId) {
        if (canEditProfile(userId)) {
            FileDAO fdao = DataAccess.getInjectedContext().getFileDAO();
            File file = getFileWithId(fdao.getLicenseFiles(userId), fileId);
            if (file == null) {
                flash("danger", "Bestand niet gevonden.");
                return redirect(routes.Profile.editIdentityCard(userId));
            } else {
                return FileHelper.getFileStreamResult(fdao, file.getId());
            }
        } else {
            return mustBeProfileAdmin();
        }
    }

    /*
     * Return the file in the list with the given idea
     */
    private static File getFileWithId(Iterable<File> files, int id) {
        if (files != null) {
            // linear search is fast enough
            for (File file : files) {
                if (file.getId() == id) {
                    return file;
                }
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
    @AllowRoles({})
    @InjectContext
    public static Result deleteIdentityFile(final int userId, int fileId) {

        if (canEditProfile(userId)) {
            FileDAO fdao = DataAccess.getInjectedContext().getFileDAO();
            File file = getFileWithId(fdao.getIdFiles(userId), fileId);
            if (file == null) {
                flash("danger", "Bestand niet gevonden.");
                return redirect(routes.Profile.editIdentityCard(userId));
            } else {
                fdao.deleteIdFile(userId, file.getId());
                FileHelper.deleteFile(Paths.get(file.getPath()));
                //flash("success", file.getFileName() + " werd met succes verwijderd.");
                return redirect(routes.Profile.editIdentityCard(userId));
            }
        } else {
            return mustBeProfileAdmin();
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
    @AllowRoles({})
    @InjectContext
    public static Result deleteLicenseFile(final int userId, int fileId) {

        if (canEditProfile(userId)) {
            DataAccessContext context = DataAccess.getInjectedContext();
            FileDAO fdao = context.getFileDAO();
            File file = getFileWithId(fdao.getLicenseFiles(userId), fileId);
            if (file == null) {
                flash("danger", "Bestand niet gevonden.");
                return redirect(routes.Profile.editIdentityCard(userId));
            } else {
                fdao.deleteLicenseFile(userId, file.getId());
                FileHelper.deleteFile(Paths.get(file.getPath()));
                // flash("success", file.getFileName() + " werd met succes verwijderd.");
                return redirect(routes.Profile.editDriversLicense(userId));
            }
        } else {
            return mustBeProfileAdmin();
        }
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
            Iterable<File> listOfFiles = context.getFileDAO().getIdFiles(userId);
            Form<IdentityCardData> form = Form.form(IdentityCardData.class).bindFromRequest();
            if (form.hasErrors()) {
                return badRequest(identitycard.render(userId, form, listOfFiles));
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
                        fdao.addIdFile(user.getId(), file.getId());
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
                            context.getFileDAO().getLicenseFiles(userId)
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
            Iterable<File> listOfFiles = context.getFileDAO().getLicenseFiles(userId);
            Form<DriversLicenseData> form = Form.form(DriversLicenseData.class).bindFromRequest();
            if (form.hasErrors()) {
                return badRequest(driverslicense.render(userId, form, listOfFiles));
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
                    fdao.addLicenseFile(userId, file.getId());
                }
                return redirect(routes.Profile.editDriversLicense(userId));
            } catch (IOException ex) {
                throw new RuntimeException(ex); //unchecked
            }
        } else {
            return mustBeProfileAdmin();
        }
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

        if (!Strings.isNullOrEmpty(user.getAddressDomicile().getStreet())) {
            total++;
        }
        if (!Strings.isNullOrEmpty(user.getAddressResidence().getStreet())) {
            total++;
        }
        if (!Strings.isNullOrEmpty(user.getCellphone())) {
            total++;
        }
        if (!Strings.isNullOrEmpty(user.getFirstName())) {
            total++;
        }
        if (!Strings.isNullOrEmpty(user.getLastName())) {
            total++;
        }
        if (!Strings.isNullOrEmpty(user.getPhone())) {
            total++;
        }
        if (!Strings.isNullOrEmpty(user.getEmail())) {
            total++;
        }
        if (user.getIdentityId() != null) {
            total++;
        }
        return (int) (((float) total / 8) * 100); //9 records
    }

    public static class UserStatusData {
        public String status;
    }

    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result editUserStatus(int userId) {
        UserDAO udao = DataAccess.getInjectedContext().getUserDAO();
        UserHeader user = udao.getUserHeader(userId);
        UserStatusData data = new UserStatusData();
        UserStatus userStatus = user.getStatus();
        data.status = userStatus.name();
        switch (userStatus) {
            case REGISTERED:
            case FULL_VALIDATING:
                return ok(editstatusRegistered.render(user));
            case FULL:
                return ok(editstatusFull.render(user));
            default: // BLOCKED or DROPPED
                return ok(editstatusBlocked.render(user));
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

    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result makeFullUser(int userId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        context.getUserDAO().makeUserFull(userId);
        context.getUserRoleDAO().addUserRole(userId, UserRole.CAR_USER);
        return redirect(routes.Profile.editUserStatus(userId));
    }

    public static class DepositData {
        public Integer amount;

        public DepositData populate(Integer amount) {
            this.amount = amount;
            return this;
        }
    }

    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result deposit (int userId) {
        User user = DataAccess.getInjectedContext().getUserDAO().getUser(userId);
        return ok(deposit.render(
                        Form.form(DepositData.class).fill(new DepositData().populate(user.getDeposit())),
                        user.getId())
        );
    }

    @AllowRoles({UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result depositPost (int userId) {
        Form<DepositData> form = Form.form(DepositData.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(deposit.render(form, userId));
        } else {
            DataAccess.getInjectedContext().getUserDAO().updateUserDeposit(userId, form.get().amount);
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
            this.mobile = user.getCellphone();

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
            user.setCellphone(data.mobile);
            user.setFirstName(data.firstName);
            user.setLastName(data.lastName);
            dao.updateUserMainProfile(user);

            AddressDAO adao = context.getAddressDAO();
            updateAddress(data.domicileAddress, user.getAddressDomicile().getId(), adao);
            updateAddress(data.residenceAddress, user.getAddressResidence().getId(), adao);

            flash("success", "De profielgegevens werden met succes aangepast");

            DataProvider.getUserProvider().invalidateUser(userId); //invalidate cache
            return redirect(routes.Profile.index(userId));
        } else {
            return mustBeProfileAdmin();
        }
    }
}
