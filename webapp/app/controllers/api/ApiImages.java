package controllers.api;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import com.google.common.base.Strings;
import controllers.util.Addresses;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.cars.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import controllers.AllowRoles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import be.ugent.degage.db.models.UserRole;

/**
 * Controller responsible for creating, updating and showing of cars
 */
public class ApiImages extends Controller {

  @InjectContext
  public static Result getImage(int imageId) {
    DataAccessContext context = DataAccess.getInjectedContext();
    if (imageId > 0) {
        return FileHelper.getFileStreamResult(context.getFileDAO(), imageId);
    } else {
        return FileHelper.getPublicFile("images/car.png", "image/png");
    }
  }

  @InjectContext
  public static Result getThumbnail(int imageId) {
    DataAccessContext context = DataAccess.getInjectedContext();
    if (imageId > 0) {
        return FileHelper.getThumbnailFileStreamResult(context.getFileDAO(), imageId);
    } else {
        return FileHelper.getPublicFile("images/car.png", "image/png");
    }
  }

}
