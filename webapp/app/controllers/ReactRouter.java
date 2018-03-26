package controllers;

import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.reactrouter.*;
import be.ugent.degage.db.models.UserRole;

public class ReactRouter extends Controller {

    @AllowRoles({UserRole.INVOICE_ADMIN})
    @InjectContext
    public static Result creditManagement(String route) {
      return ok(index.render(route));
    }

    @AllowRoles({UserRole.SETTINGS_ADMIN})
    @InjectContext
    public static Result settingsManagement(String route) {
      return ok(settings.render(route));
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result carApprovals(String route) {
      return ok(carApprovals.render(route));
    }
}
