package controllers.api;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.api.CarApiDAO;
import be.ugent.degage.db.models.api.*;
import be.ugent.degage.db.models.*;
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

import controllers.AllowRoles;
import com.google.gson.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

/**
 * Controller responsible for creating, updating and showing of cars
 */
public class ApiCars extends Controller {


      /**
     * Gets the list of car stands
     *
     * @return The list of car stands in json format
     */
    @InjectContext
    public static Result getStands() {
      Iterable<CarStand> carStands = DataAccess.getInjectedContext().getCarApiDAO().listCarStands();
      Gson gson = GsonHelper.getGson();
      JsonArray array = new JsonArray();
      for (CarStand carStand : carStands) {
        JsonObject geoPosition = new JsonObject();
        geoPosition.addProperty("latitude", carStand.getLatitude());
        geoPosition.addProperty("longitude", carStand.getLongitude());
        JsonObject vehicleInformation = new JsonObject();
        vehicleInformation.addProperty("fuelType", carStand.getFuelType());
        JsonObject element = new JsonObject();
        element.add("geoPosition", geoPosition);
        element.addProperty("displayName", carStand.getName());
        element.addProperty("stationType", "fixed");
        element.add("vehicleInformation", vehicleInformation);
        element.addProperty("vehicleId", carStand.getCarId());

        array.add(element);
      }
      // JsonArray arr = gson.toJson(carStands);
      return ok(gson.toJson(array));
    }

  @InjectContext
  @AllowRoles({UserRole.CAR_ADMIN})
  public static Result getCar(int carId) {
    CarHeaderLong carHeaderLong = DataAccess.getInjectedContext().getCarDAO().getCarHeaderLong(carId);
    return ok(GsonHelper.getGson().toJson(carHeaderLong));
  }

  @InjectContext
  @AllowRoles({UserRole.CAR_ADMIN})
  public static Result createCar() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      String name = json.findPath("name").asText();
      String brand = json.findPath("brand").asText();
      String type = json.findPath("type").asText();
      String fuel = json.findPath("fuel").asText();
      String email = json.findPath("email").asText();
      int carOwnerUserId = CurrentUser.getId();

      int carId = DataAccess.getInjectedContext().getCarApiDAO().createCar(name, name + "@degage.be", brand, type, carOwnerUserId, fuel);
      return getCar(carId);

    } catch (Exception e) {
      System.err.println("Error in createCar:" + e);
      result.put("status", "error");
      result.put("message", e.getMessage());
      return badRequest(result);
    }
  }

  @InjectContext
  public static Result findCars(int page, int pageSize, int ascInt, String orderBy, String filter) {
    FilterField field = FilterField.stringToField(orderBy, FilterField.NAME);
    boolean asc = Pagination.parseBoolean(ascInt);
    Page<CarHeaderAndOwner> cars = DataAccess.getInjectedContext().getCarDAO().listCarsAndOwners(field, asc, page, pageSize, filter);
    return ok(GsonHelper.getGson().toJson(cars));
  }

}
