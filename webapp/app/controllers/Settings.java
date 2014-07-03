package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.SettingDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.Setting;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import controllers.Security.RoleSecured;
import org.joda.time.DateTime;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import providers.UserProvider;
import views.html.settings.*;

import java.util.List;

public class Settings extends Controller {


    public static class EditSettingModel {
        public String value;
        public String name;
        public DateTime after;


        public EditSettingModel() {
        }

        public EditSettingModel(String value, String name, DateTime after) {
            this.value = value;
            this.name = name;
            this.after = after;
        }

        public String validate() {
            return null; //TODO
        }
    }

    public static Result index() {
        return ok(overview.render());
    }

    public static class ChangePasswordModel {
        public String oldpw;
        public String newpw;
        public String repeatpw;

        public String validate(){
            if(oldpw == null || oldpw.isEmpty()){
                return "Gelieve je oud wachtwoord op te geven.";
            } else if(newpw == null || newpw.length() < 6){
                return "Je nieuw wachtwoord moet minstens 6 tekens bevatten.";
            } else if(!newpw.equals(repeatpw)){
                return "Wachtwoorden komen niet overeen";
            } else return null;
        }
    }

    @RoleSecured.RoleAuthenticated()
    public static Result changePassword(){
        return ok(changepass.render(Form.form(ChangePasswordModel.class)));
    }

    @RoleSecured.RoleAuthenticated()
    public static Result changePasswordPost(){
        Form<ChangePasswordModel> form = Form.form(ChangePasswordModel.class).bindFromRequest();
        if(form.hasErrors()){
            return badRequest(changepass.render(form));
        } else {
            try(DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()){
                ChangePasswordModel model = form.get();
                UserDAO dao = context.getUserDAO();
                User user = DataProvider.getUserProvider().getUser(false);

                if(!UserProvider.hasValidPassword(user, model.oldpw)){
                    form.reject("Je oude wachtwoord is incorrect.");
                    return badRequest(changepass.render(form));
                } else {
                    user.setPassword(UserProvider.hashPassword(model.newpw));
                    dao.updateUser(user, false);
                    context.commit();

                    DataProvider.getUserProvider().invalidateUser(user);
                    flash("success", "Jouw wachtwoord werd succesvol gewijzigd.");
                    return redirect(routes.Settings.index());
                }
            }
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result sysvarsOverview() {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            List<Setting> settings = dao.getSettings();
            return ok(sysvars.render(settings));
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result editSysvar(int id) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();
            Setting setting = dao.getSetting(id);
            if (setting == null) {
                flash("danger", "Deze setting ID bestaat niet.");
                return redirect(routes.Settings.sysvarsOverview());
            } else {
                EditSettingModel model = new EditSettingModel(setting.getValue(), setting.getName(), setting.getAfterDate());

                return ok(editsysvar.render(Form.form(EditSettingModel.class).fill(model), setting));
            }
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result editSysvarPost(int id) {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            SettingDAO dao = context.getSettingDAO();

            Form<EditSettingModel> form = Form.form(EditSettingModel.class).bindFromRequest();
            if (form.hasErrors()) {
                return badRequest(editsysvar.render(form, dao.getSetting(id)));
            } else {
                EditSettingModel model = form.get();
                dao.updateSetting(id, model.name, model.value, model.after);
                context.commit();
                flash("success", "De systeemvariabele werd succesvol aangepast.");
                return redirect(routes.Settings.sysvarsOverview());
            }
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result createSysvar() {
        return ok(createsysvar.render(Form.form(EditSettingModel.class).fill(new EditSettingModel(null, null, DateTime.now()))));
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result createSysvarPost() {
        Form<EditSettingModel> form = Form.form(EditSettingModel.class).bindFromRequest();
        if (form.hasErrors() || form.get().name == null) {
            return badRequest(createsysvar.render(form));
        } else {
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                SettingDAO dao = context.getSettingDAO();
                EditSettingModel model = form.get();
                dao.createSettingAfterDate(model.name, model.value, model.after);
                context.commit();
                flash("success", "De systeemvariabele werd succesvol aangemaakt.");
                return redirect(routes.Settings.sysvarsOverview());
            }
        }
    }


}
