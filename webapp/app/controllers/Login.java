/* Login.java
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
import be.ugent.degage.db.dao.VerificationDAO;
import be.ugent.degage.db.models.UserHeader;
import be.ugent.degage.db.models.VerificationType;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.login.*;

import java.util.Arrays;
import java.util.List;


/**
 */
public class Login extends Controller {

    public static class LoginModel {
        public String email;
        public String password;

        public String validate() {
            if (email == null || email.length() < 5)
                return "Emailadres ontbreekt";
            else if (password == null || password.length() == 0)
                return "Wachtwoord ontbreekt";
            else return null;
        }
    }

    /**
     * Method: GET
     * Returns the login form
     *
     * @return The login index page
     */
    // needs no injected context
    public static Result login(String redirect) {
        CurrentUser.clear(); // also clears the flash...
        return ok(login.render(Form.form(LoginModel.class), redirect));
    }

    public static class EmailData {
        @Constraints.Email
        @Constraints.Required
        public String email;
    }

    /**
     * Method: GET
     *
     * @return A page where the user can request a password reset
     */
    // needs no injected context
    public static Result resetPasswordRequest() {
        return ok(singlemailform.render(Form.form(EmailData.class)));
    }

    /**
     * Method: POST
     * This sends a password reset request to the user based on the submitted form data.
     *
     * @return A status page whether the password reset was successfull
     */
    @InjectContext
    public static Result resetPasswordRequestPost() {
        Form<EmailData> resetForm = Form.form(EmailData.class).bindFromRequest();
        if (resetForm.hasErrors()) {
            return badRequest(singlemailform.render(resetForm));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            EmailData data = resetForm.get();
            UserHeader user = context.getUserDAO().getUserByEmail(data.email);
            if (user == null) {
                resetForm.reject("Gebruiker met dit adres bestaat niet.");
                // TODO: warn user
                return badRequest(singlemailform.render(resetForm));
            } else {
                String newUuid = context.getVerificationDAO().createToken(data.email, VerificationType.PWRESET);
                Notifier.sendPasswordResetMail(user, newUuid);
                return ok(pwresetrequestok.render());
            }
        }
    }

    public static class PasswordResetData extends EmailData {

        @Constraints.Required
        public String password;

        @Constraints.Required
        public String password_repeat;


        public List<ValidationError> validate() {
            // TODO: password strength validation
            if (!password.equals(password_repeat))
                return Arrays.asList(new ValidationError(
                        "password_repeat", "Beide wachtwoorden moeten gelijk zijn"
                ));
            else
                return null;
        }
    }

     /**
     * Asks the user to enter email and a new password
     */
    @InjectContext
    public static Result resetPassword(String uuid) {
        return ok(pwreset.render(Form.form(PasswordResetData.class), uuid));
    }

    /**
     * Processes the reset password form
     */
    @InjectContext
    public static Result resetPasswordPost(String uuid) {
        Form<PasswordResetData> form = Form.form(PasswordResetData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(pwreset.render(form, uuid));
        } else {
            VerificationDAO dao = DataAccess.getInjectedContext().getVerificationDAO();
            PasswordResetData data = form.get();

            switch (dao.changePasswordForToken(uuid, data.email, data.password)) {
                case INVALID_PAIR:
                    form.reject("email", "Dit moet het e-mailadres zijn waarnaar de 'wachtwoord vergetem'-e-mail is verzonden");
                    return badRequest(pwreset.render(form, uuid));
                default: // OK
                    flash ("success", "Je nieuwe wachtwoord is met succes aangemaakt. Log in om verder te gaan.");
                    return redirect(routes.Login.login(null));
            }
        }
    }

    /**
     * Method: POST
     * Processes a submitted login form
     *
     * @return Redirect to page or login form when an error occured
     */

    @InjectContext
    public static Result authenticate(String redirect) {
        Form<LoginModel> loginForm = Form.form(LoginModel.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm, redirect));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            UserHeader user = context.getUserDAO().getUserWithPassword(loginForm.get().email, loginForm.get().password);

            if (user != null) {
                if  (!user.canLogin()) {
                    loginForm.reject("Deze account werd verwijderd of geblokkeerd. Gelieve de administrator te contacteren.");
                    return badRequest(login.render(loginForm, redirect));
                } else {
                    CurrentUser.set(user, context.getUserRoleDAO().getUserRoles(user.getId()));
                    if (redirect != null) {
                        return redirect(redirect);
                    } else {
                        return redirect(
                                routes.Application.index() // go to dashboard page, authentication success
                        );
                    }
                }
            } else {
                loginForm.reject("Foute gebruikersnaam of wachtwoord.");
                return badRequest(login.render(loginForm, redirect));
            }
        }
    }

    @AllowRoles
    @InjectContext
    public static Result setAdmin () {
        if (CurrentUser.canPromote()) {
            CurrentUser.setAdmin(DataAccess.getInjectedContext().getUserRoleDAO().getUserRoles(CurrentUser.getId()));
            return redirect(routes.Application.index());
        } else {
            return badRequest(); // hacker? user should not have been given the opportunity.
        }
    }

    @AllowRoles
    @InjectContext
    public static Result clearAdmin () {
        CurrentUser.clearAdmin(DataAccess.getInjectedContext().getUserRoleDAO().getUserRoles(CurrentUser.getId()));
        return redirect(routes.Application.index());
    }

   /**
     * Shows the screen for a registration request
     */
    public static Result requestRegistration () {
        return ok(request.render(Form.form(EmailData.class)));
    }

    /**
     * Handle a registration request
     */
    @InjectContext
    public static Result requestRegistrationPost() {
        Form<EmailData> requestForm = Form.form(EmailData.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return badRequest(request.render(requestForm));
        } else {
            EmailData data = requestForm.get();
            VerificationDAO dao = DataAccess.getInjectedContext().getVerificationDAO();

            String verificationIdent = dao.createToken(data.email, VerificationType.REGISTRATION);
            Notifier.sendVerificationMail(data.email, verificationIdent);

            return ok(requested.render());
        }
    }

    /**
     * Next step in the registration procedure. Ask for password, name. etc
     */
    public static Result registerVerification(String uuid) {
        return ok (register.render(Form.form(RegisterData.class), uuid));
    }

    public static class RegisterData extends PasswordResetData {
        @Constraints.Required
        public String firstName;

        @Constraints.Required
        public String lastName;
    }

    /**
     *
     */
    @InjectContext
    public static Result registerVerificationPost(String uuid) {
        Form<RegisterData> form = Form.form(RegisterData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(register.render(form, uuid));
        } else {
            VerificationDAO dao = DataAccess.getInjectedContext().getVerificationDAO();
            RegisterData data = form.get();
            switch (dao.createUserForToken(uuid, data.email, data.password, data.firstName, data.lastName)) {
                case ALREADY_EXISTS:
                    form.reject("Er bestaat reeds een gebruiker met dit e-mailadres.");
                    // TODO: send email to that user
                    return badRequest(register.render(form, uuid));
                case INVALID_PAIR:
                    form.reject("email", "Dit moet het e-mailadres zijn waarnaar de registratie-e-mail is verzonden");
                    return badRequest(register.render(form, uuid));
                default: // OK
                    flash ("success", "Je bent geregistreerd als gebruiker bij de webapplicatie. Log in om verder te gaan.");
                    return redirect(routes.Login.login(null));
            }
        }
    }


    /**
     * Method: GET
     * Logs the user out
     *
     * @return Redirect to index page
     */
    public static Result logout() {
        DataProvider.getUserProvider().invalidateUser(CurrentUser.getId());
        CurrentUser.clear();
        return redirect(routes.Application.index());
    }

}

