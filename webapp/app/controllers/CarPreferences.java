package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.CarPreferencesDAO;
import be.ugent.degage.db.models.CarPreference;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import views.html.carpreferences.*;

/**
 * Allows editing of car preferences
 */
public class CarPreferences extends Controller {

    public static class CarPreferencesData {
        public Map<Integer, Boolean> included;

        public CarPreferencesData() {
            included = new HashMap<>();
        }

        public CarPreferencesData(Iterable<CarPreference> preferences) {
            this();
            for (CarPreference preference : preferences) {
                if (preference.isPreferred()) {
                    included.put(preference.getCarId(), true);
                }
            }
        }
    }

    @InjectContext
    @AllowRoles({})
    public static Result edit() {
        CarPreferencesDAO dao = DataAccess.getInjectedContext().getCarPreferencesDao();
        List<CarPreference> list = dao.listPreferences(CurrentUser.getId());
        Form<CarPreferencesData> form = Form.form(CarPreferencesData.class).fill(new CarPreferencesData(list));
        return ok(preferences.render(
                form,
                Utils.splitList(list, 4)
        ));
    }

    @InjectContext
    @AllowRoles({})
    public static Result doEdit() {
        Form<CarPreferencesData> form = Form.form(CarPreferencesData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(); // this should not happen
        } else {
            DataAccess.getInjectedContext().getCarPreferencesDao().updatePreferences(
                    CurrentUser.getId(),
                    form.get().included.keySet()
            );
            return redirect(routes.Application.index());
        }
    }
}
