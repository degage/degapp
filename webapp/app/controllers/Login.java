package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserStatus;
import be.ugent.degage.db.models.VerificationType;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import providers.UserProvider;
import views.html.login.*;


/**
 * Created by Cedric on 2/16/14.
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

    public static class PasswordResetModel {
        public String password;
        public String password_repeat;

        public String validate() {
            if (password == null || !password.equals(password_repeat))
                return "Wachtwoorden komen niet overeen";
            else return null;
        }
    }

    public static class EmailFormModel {
        public String email;

        public String validate() {
            return new Constraints.EmailValidator().isValid(email) ? null : "Ongeldig emailadres";
        }
    }

    public static class RegisterModel {
        public String email;
        public String password;
        public String password_repeat;
        public String firstName;
        public String lastName;

        public String validate() {
            if (!new Constraints.EmailValidator().isValid(email))
                return "Dit is geen geldig e-mailadres.";
            else if (password == null || password.length() < 8)
                return "Wachtwoord moet minstens 8 tekens bevatten.";
            else if (!password.equals(password_repeat))
                return "Wachtwoord komt niet overeen.";
            else
                return null;
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
        // Allow a force login when the user doesn't exist anymore
        User user = DataProvider.getUserProvider().getUser(false);
        if (user == null && !session().isEmpty()) {
            session().clear();
        }

        if (user == null) {
            return ok(
                    views.html.login.login.render(Form.form(LoginModel.class), redirect)
            );
        } else {
            return redirect(
                    routes.Dashboard.index()
            );
        }
    }

    /**
     * Method: GET
     * This resets the previous email verification link and generates a new one. This can be used when the old email hasn't been received
     *
     * @return A status page whether the request for a new verification link was successful
     */
    @InjectContext
    public static Result requestNewEmailVerificationProcess(String email) {
        //TODO: prevent people from spamming this URL as this might DDOS the mailserver (CRSF token and POST instead of GET)

        UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
        User user = dao.getUser(email);
        if (user == null) {
            return badRequest("Deze gebruiker bestaat niet."); //TODO: flash
        } else {
            if (user.getStatus() == UserStatus.EMAIL_VALIDATING) {
                dao.deleteVerificationString(user, VerificationType.REGISTRATION);
                String verificationIdent = dao.createVerificationString(user, VerificationType.REGISTRATION);
                Notifier.sendVerificationMail(user, verificationIdent);
                return ok(registrationok.render(user.getId(), verificationIdent, true));
            } else {
                return badRequest("Deze gebruiker is reeds geactiveerd.");
            }
        }
    }

    /**
     * Method: GET
     *
     * @return A page where the user can request a password reset
     */
    // needs no injected context
    public static Result resetPasswordRequest() {
        return ok(singlemailform.render(Form.form(EmailFormModel.class)));
    }

    /**
     * Method: POST
     * This sends a password reset request to the user based on the submitted form data.
     *
     * @return A status page whether the password reset was successfull
     */
    @InjectContext
    public static Result resetPasswordRequestProcess() {
        Form<EmailFormModel> resetForm = Form.form(EmailFormModel.class).bindFromRequest();
        if (resetForm.hasErrors()) {
            return badRequest(singlemailform.render(resetForm));
        } else {
            UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
            User user = dao.getUser(resetForm.get().email);
            if (user == null) {
                resetForm.reject("Gebruiker met dit adres bestaat niet.");
                return badRequest(singlemailform.render(resetForm));
            } else {
                //TODO: this check should be implicit?
                if (dao.getVerificationString(user, VerificationType.PWRESET) != null) {
                    dao.deleteVerificationString(user, VerificationType.PWRESET);
                }

                String newUuid = dao.createVerificationString(user, VerificationType.PWRESET);
                Notifier.sendPasswordResetMail(user, newUuid);
                return ok(pwresetrequestok.render(user.getId(), newUuid, user.getEmail()));
            }
        }
    }

    /**
     * Method: GET
     * Finalizes the password reset procedure given a correct reset code and userID
     *
     * @param userId The userId of the user who requested the reset
     * @param uuid   The unique reset code the user received to reset the password
     * @return A status page whether reset was successfull or not
     */
    @InjectContext
    public static Result resetPassword(int userId, String uuid) {
        UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
        User user = dao.getUserPartial(userId);
        if (user == null) {
            return badRequest("Deze user bestaat niet."); //TODO: flash
        } else {
            String ident = dao.getVerificationString(user, VerificationType.PWRESET);
            if (ident == null) {
                return badRequest("There was no password reset requested on this account.");
            } else {
                // Render the password reset page

                return ok(pwreset.render(Form.form(PasswordResetModel.class), userId, uuid));
            }
        }
    }

    /**
     * Method: POST
     * Starts a password reset procedure based on submitted form data.
     *
     * @param userId The id of the user who requested the password reset
     * @param uuid
     * @return
     */
    @InjectContext
    public static Result resetPasswordProcess(int userId, String uuid) {
        Form<PasswordResetModel> resetForm = Form.form(PasswordResetModel.class).bindFromRequest();
        if (resetForm.hasErrors()) {
            return badRequest(pwreset.render(resetForm, userId, uuid));
        } else {
            UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
            User user = dao.getUser(userId);
            if (user == null) {
                return badRequest("Deze user bestaat niet."); //TODO: flash
            } else {
                String ident = dao.getVerificationString(user, VerificationType.PWRESET);
                if (ident == null) {
                    return badRequest("There was no password reset requested on this account.");
                } else if (ident.equals(uuid)) {
                    dao.deleteVerificationString(user, VerificationType.PWRESET);
                    user.setPassword(UserProvider.hashPassword(resetForm.get().password));
                    dao.updateUser(user);

                    DataProvider.getUserProvider().invalidateUser(user);
                    flash("success", "Jouw wachtwoord werd succesvol gewijzigd.");
                    LoginModel model = new LoginModel();
                    model.email = user.getEmail();

                    return ok(login.render(Form.form(LoginModel.class).fill(model), null));
                } else {
                    return badRequest("De verificatiecode komt niet overeen met onze gegevens.");
                }
            }
        }
    }

    /**
     * Method: POST
     * Processes a submitted login form
     *
     * @return Redirect to page or login form when an error occured
     */

    // needs no injected context
    public static Result authenticate(String redirect) {
        Form<LoginModel> loginForm = Form.form(LoginModel.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm, redirect));
        } else {
            User user = DataProvider.getUserProvider().getUser(loginForm.get().email);

            if (UserProvider.hasValidPassword(user, loginForm.get().password)) {
                if (user.getStatus() == UserStatus.EMAIL_VALIDATING) {
                    loginForm.reject("Deze account is nog niet geactiveerd. Gelieve je inbox te checken.");
                    loginForm.data().put("reactivate", "True");
                    //TODO: link aanvraag nieuwe bevestigingscode
                    return badRequest(login.render(loginForm, redirect));
                } else if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.DROPPED) {
                    loginForm.reject("Deze account werd verwijderd of geblokkeerd. Gelieve de administrator te contacteren.");
                    return badRequest(login.render(loginForm, redirect));
                } else {
                    session().clear();
                    DataProvider.getUserProvider().createUserSession(user);
                    if (redirect != null) {
                        return redirect(redirect);
                    } else {
                        return redirect(
                                routes.Dashboard.index() // go to dashboard page, authentication success
                        );
                    }
                }
            } else {
                loginForm.reject("Foute gebruikersnaam of wachtwoord.");
                return badRequest(login.render(loginForm, redirect));
            }
        }
    }

    /**
     * Method: GET
     *
     * @return Page to register to
     */
    // needs no injected context
    public static Result register() {
        if (DataProvider.getUserProvider().getUser() == null) {
            return ok(
                    register.render(Form.form(RegisterModel.class))
            );
        } else {
            return redirect(
                    routes.Login.login(null)
            );
        }
    }

    /**
     * Method: GET
     * Finalizes a registration procedure
     *
     * @param userId The user ID
     * @param uuid   The registration verification code
     * @return A login page when successful, or error message when verification code is invalid
     */
    @InjectContext
    public static Result register_verification(int userId, String uuid) {

        UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
        User user = dao.getUser(userId);
        if (user == null) {
            return badRequest("Deze user bestaat niet."); //TODO: flash
        } else if (user.getStatus() != UserStatus.EMAIL_VALIDATING) {
            flash("warning", "Deze gebruiker is reeds gevalideerd.");
            return badRequest(login.render(Form.form(LoginModel.class), null)); //We don't include a preset email address here since we could leak ID -> email to public
        } else {
            String ident = dao.getVerificationString(user, VerificationType.REGISTRATION);
            if (ident == null) {
                return badRequest("Oops something went wrong. Missing identifier in database?!!!!! Anyway, contact an administrator.");
            } else if (ident.equals(uuid)) {
                dao.deleteVerificationString(user, VerificationType.REGISTRATION);
                user.setStatus(UserStatus.REGISTERED);

                dao.updateUser(user);
                DataProvider.getUserProvider().invalidateUser(user);

                flash("success", "Je email werd succesvol geverifieerd. Gelieve aan te melden.");
                LoginModel model = new LoginModel();
                model.email = user.getEmail();
                Notifier.sendWelcomeMail(user);
                return ok(login.render(Form.form(LoginModel.class).fill(model), null));
            } else {
                return badRequest("De verificatiecode komt niet overeen met onze gegevens. TODO: nieuwe string voorstellen.");
            }
        }
    }

    /**
     * Method: POST
     * Creates a pending user registration
     *
     * @return Redirect and logged in session if success
     */
    @InjectContext
    public static Result register_process() {
        Form<RegisterModel> registerForm = Form.form(RegisterModel.class).bindFromRequest();
        if (registerForm.hasErrors()) {
            return badRequest(register.render(registerForm));
        } else {
            session().clear();
            User otherUser = DataProvider.getUserProvider().getUser(registerForm.get().email);
            if (otherUser != null) {
                registerForm.reject("Er bestaat reeds een gebruiker met dit emailadres.");
                return badRequest(register.render(registerForm));
            } else {
                UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
                User user = dao.createUser(registerForm.get().email, UserProvider.hashPassword(registerForm.get().password),
                        registerForm.get().firstName, registerForm.get().lastName);

                // Now we create a registration UUID
                String verificationIdent = dao.createVerificationString(user, VerificationType.REGISTRATION);
                Notifier.sendVerificationMail(user, verificationIdent);

                return ok(registrationok.render(user.getId(), verificationIdent, true));
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
        User user = DataProvider.getUserProvider().getUser();
        if (user != null) {
            DataProvider.getUserProvider().invalidateUser(user);
            DataProvider.getUserRoleProvider().invalidateRoles(user);
        }

        if (session("impersonated") != null) {
            session("email", session("impersonated"));
            session().remove("impersonated");
            return redirect(
                    routes.Dashboard.index()
            );
        } else {
            session().clear();
            return redirect(
                    routes.Application.index()
            );
        }
    }

}

