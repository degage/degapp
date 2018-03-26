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
public class ApiProperties extends Controller {

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getProperty(int propertyId) {
    Property property = DataAccess.getInjectedContext().getPropertyDAO().getProperty(propertyId);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(property));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getPropertyByKey(String key) {
    Property property = DataAccess.getInjectedContext().getPropertyDAO().getPropertyByKey(key);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(property));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result updateProperty(int propertyId) {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      String key = json.findPath("key").asText();
      String value = json.findPath("value").asText();
      Property property = DataAccess.getInjectedContext().getPropertyDAO().getProperty(propertyId);
      Property.Builder propertyBuilder = new Property.Builder(property);
      propertyBuilder.value(value);
      Property p = propertyBuilder.build();
      DataAccess.getInjectedContext().getPropertyDAO().updateProperty(p);
      return ok(GsonHelper.getGson().toJson(DataAccess.getInjectedContext().getPropertyDAO().getProperty(propertyId)));
    } catch (Exception e) {
      System.err.println("Error in updateProperty:" + e);
      result.put("status", "Error");
      result.put("message", "Missing parameter in updateProperty");
      return badRequest(result);
    }
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result updateImage(int propertyId) {
    ObjectNode result = Json.newObject();
    File file = FileHelper.getFileFromRequest("image", FileHelper.IMAGE_CONTENT_TYPES, "uploads.images", 0);
    if (file == null) {
        result.put("status", "Error");
        result.put("message", "Image file missing");
        return badRequest(result);
    } else if (file.getContentType() == null) {
        result.put("status", "Error");
        result.put("message", "Wrong image type, only image files allowed");
        return badRequest(result);
    } else {
        Property property = DataAccess.getInjectedContext().getPropertyDAO().getProperty(propertyId);
        Property.Builder propertyBuilder = new Property.Builder(property);
        propertyBuilder.value(String.valueOf(file.getId()));
        Property p = propertyBuilder.build();
        DataAccess.getInjectedContext().getPropertyDAO().updateProperty(p);
        return ok(GsonHelper.getGson().toJson(DataAccess.getInjectedContext().getPropertyDAO().getProperty(propertyId)));
    }
  }

}
