package controllers;

import be.ugent.degage.db.dao.SettingDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.Setting;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import controllers.Security.RoleSecured;
import db.DataAccess;
import db.InjectContext;
import org.joda.time.DateTime;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import providers.SettingProvider;
import providers.UserProvider;
import views.html.settings.*;

import java.time.Instant;
import java.util.List;

public class Settings extends Controller {


    public static class EditSettingModel {
        public String value;
        public String name;
        // TODO: binding for LocalDateTime
        public String after;


        public EditSettingModel() {
        }

        public EditSettingModel(String value, String name, String after) {
            this.value = value;
            this.name = name;
            this.after = after;
        }

        public String validate() {
            return null; //TODO
        }
    }

    @InjectContext
    public static Result index() {
        return ok(overview.render());
    }

    public static class ChangePasswordModel {
        public String oldpw;
        public String newpw;
        public String repeatpw;

        public String validate() {
            if (oldpw == null || oldpw.isEmpty()) {
                return "Gelieve je oud wachtwoord op te geven.";
            } else if (newpw == null || newpw.length() < 6) {
                return "Je nieuw wachtwoord moet minstens 6 tekens bevatten.";
            } else if (!newpw.equals(repeatpw)) {
                return "Wachtwoorden komen niet overeen";
            } else return null;
        }
    }

    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result changePassword() {
        return ok(changepass.render(Form.form(ChangePasswordModel.class)));
    }

    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result changePasswordPost() {
        Form<ChangePasswordModel> form = Form.form(ChangePasswordModel.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(changepass.render(form));
        } else {
            ChangePasswordModel model = form.get();
            UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
            User user = DataProvider.getUserProvider().getUser(false);

            if (!UserProvider.hasValidPassword(user, model.oldpw)) {
                form.reject("Je oude wachtwoord is incorrect.");
                return badRequest(changepass.render(form));
            } else {
                user.setPassword(UserProvider.hashPassword(model.newpw));
                dao.updateUserPartial(user);

                DataProvider.getUserProvider().invalidateUser(user);
                flash("success", "Jouw wachtwoord werd succesvol gewijzigd.");
                return redirect(routes.Settings.index());
            }

        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    @InjectContext
    public static Result sysvarsOverview() {
        return ok(sysvars.render(DataAccess.getInjectedContext().getSettingDAO().getSettings()));
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    @InjectContext
    public static Result editSysvar(String name) {
        // TODO: should not allow name to be edited as well...
        SettingDAO dao = DataAccess.getInjectedContext().getSettingDAO();
        String value = dao.getSettingForNow(name);
        if (value == null) {
            flash("danger", "Deze setting ID bestaat niet.");
            return redirect(routes.Settings.sysvarsOverview());
        } else {
            EditSettingModel model = new EditSettingModel(value, name,
                    SettingProvider.instantToString(Instant.now())
            );

            return ok(editsysvar.render(Form.form(EditSettingModel.class).fill(model)));
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    @InjectContext
    public static Result editSysvarPost() {
        SettingDAO dao = DataAccess.getInjectedContext().getSettingDAO();

        Form<EditSettingModel> form = Form.form(EditSettingModel.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(editsysvar.render(form));
        } else {
            EditSettingModel model = form.get();
            dao.createSettingAfterDate(model.name, model.value, SettingProvider.stringToInstant(model.after));
            flash("success", "De systeemvariabele werd met succes aangepast.");
            return redirect(routes.Settings.sysvarsOverview());
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    @InjectContext
    // TODO: do we need this?
    public static Result createSysvar() {
        return ok(createsysvar.render(Form.form(EditSettingModel.class).fill(new EditSettingModel(null, null, SettingProvider.instantToString(Instant.now())))));
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    @InjectContext
    // TODO: do we need this?
     public static Result createSysvarPost() {
        Form<EditSettingModel> form = Form.form(EditSettingModel.class).bindFromRequest();
        if (form.hasErrors() || form.get().name == null) {
            return badRequest(createsysvar.render(form));
        } else {
            SettingDAO dao = DataAccess.getInjectedContext().getSettingDAO();
            EditSettingModel model = form.get();
            dao.createSettingAfterDate(model.name, model.value, SettingProvider.stringToInstant(model.after));
            // context.commit(); // TODO: wat doet dit hier?
            flash("success", "De systeemvariabele werd succesvol aangemaakt.");
            return redirect(routes.Settings.sysvarsOverview());
        }
    }


}
