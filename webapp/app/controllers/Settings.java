/* Settings.java
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

import be.ugent.degage.db.dao.SettingDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.settings.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Settings extends Controller {


    private static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String instantToString(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault()).format(INSTANT_FORMATTER);
    }

    public static class EditSettingModel {
        public String value;
        // TODO: binding for LocalDateTime
        public Date after;


        public EditSettingModel() {
        }

        public EditSettingModel(String value, Date after) {
            this.value = value;
            this.after = after;
        }
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

    @AllowRoles({})
    @InjectContext
    public static Result changePassword() {
        return ok(changepass.render(Form.form(ChangePasswordModel.class)));
    }

    @AllowRoles({})
    @InjectContext
    public static Result changePasswordPost() {
        Form<ChangePasswordModel> form = Form.form(ChangePasswordModel.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(changepass.render(form));
        } else {
            ChangePasswordModel model = form.get();
            UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
            User user = DataProvider.getUserProvider().getUser(false);

            if (dao.changePassword(user.getId(), model.oldpw, model.newpw)) {
                DataProvider.getUserProvider().invalidateUser(user.getId());
                flash("success", "Jouw wachtwoord werd met succes gewijzigd.");
                return redirect(routes.Application.index());
            } else {
                form.reject("Je oude wachtwoord is incorrect.");
                return badRequest(changepass.render(form));
            }

        }
    }

    @AllowRoles({UserRole.SUPER_USER})
    @InjectContext
    public static Result sysvarsOverview() {
        return ok(sysvars.render(DataAccess.getInjectedContext().getSettingDAO().getSettings()));
    }

    @AllowRoles({UserRole.SUPER_USER})
    @InjectContext
    public static Result editSysvar(String name) {
        SettingDAO dao = DataAccess.getInjectedContext().getSettingDAO();
        String value = dao.getSettingForNow(name);
        if (value == null) {
            flash("danger", "Deze setting ID bestaat niet.");
            return redirect(routes.Settings.sysvarsOverview());
        } else {
            EditSettingModel model = new EditSettingModel(value, new Date());

            return ok(editsysvar.render(name, Form.form(EditSettingModel.class).fill(model)));
        }
    }

    @AllowRoles({UserRole.SUPER_USER})
    @InjectContext
    public static Result editSysvarPost(String name) {
        SettingDAO dao = DataAccess.getInjectedContext().getSettingDAO();

        Form<EditSettingModel> form = Form.form(EditSettingModel.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(editsysvar.render(name, form));
        } else {
            EditSettingModel model = form.get();
            dao.createSettingAfterDate(name, model.value, model.after.toInstant());
            flash("success", "De systeemvariabele werd met succes aangepast.");
            return redirect(routes.Settings.sysvarsOverview());
        }
    }

}
