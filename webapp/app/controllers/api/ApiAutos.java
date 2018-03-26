package controllers.api;

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

import java.io.IOException;
import java.util.*;
import java.time.LocalDate;
import java.nio.file.Paths;

import controllers.Utils;
import controllers.AllowRoles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.dao.AutoDAO;
import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.dao.FileDAO;

public class ApiAutos extends Controller {

  @InjectContext
  @AllowRoles({})
  public static Result getAuto(int autoId) {
    AutoDAO aDao = DataAccess.getInjectedContext().getAutoDAO();
    Auto auto = aDao.getAuto(autoId);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(auto));
  }

  @InjectContext
  @AllowRoles({})
  public static Result getAutoByUserId(int userId) {
    AutoDAO aDao = DataAccess.getInjectedContext().getAutoDAO();
    Auto auto = aDao.getAutoByUserId(userId);
    System.out.println("getAutoByUserId:" + auto);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(auto));
  }

  @InjectContext
  @AllowRoles({})
  public static Result createAuto() {
    return updateOrCreateAuto(-1, request().body().asJson());
  }

  @InjectContext
  @AllowRoles({})
  public static Result updateAuto(int autoId) {
    return updateOrCreateAuto(autoId, request().body().asJson());
  }

  @InjectContext
  private static Result updateOrCreateAuto(int autoId, JsonNode json) {
    ObjectNode result = Json.newObject();
    try {
      String brand = json.findPath("brand").asText();
      String type = json.findPath("type").asText();
      String name = json.findPath("name").asText();
      String email = json.findPath("email").asText();
      String status = json.findPath("status").asText();

      String street = json.findPath("location").findPath("street").asText();
      String number = json.findPath("location").findPath("num").asText();
      String zip = json.findPath("location").findPath("zip").asText();
      String city = json.findPath("location").findPath("city").asText();

      int seats = json.findPath("seats").asInt();
      Boolean manual =json.findPath("manual").asBoolean();
      int year = json.findPath("year").asInt();
      int doors = json.findPath("doors").asInt();
      String fuel = json.findPath("fuel").asText();
      int fuelEco = json.findPath("fuelEconomy").asInt();
      int estVal = json.findPath("estimatedValue").asInt();
      int annualKm = json.findPath("ownerAnnualKm").asInt();
      int imagesId = json.findPath("imagesId").asInt();
      String comments = json.findPath("comments").asText();
      int locationId = json.findPath("locationId").asInt();
      int carOwnerUserId = CurrentUser.getId();

      String insuranceNameBefore = json.findPath("insurance").findPath("insuranceNameBefore").asText();
      LocalDate insuranceExpiration = Utils.toLocalDate(json.findPath("insurance").findPath("expiration").asText());

      Auto.Builder ab = new Auto.Builder(autoId, carOwnerUserId);
      ab.brand(brand).type(type).status(CarStatus.REGISTERED)
      .status(CarStatus.valueOf(status))
      .name(name).email(email)
      .seats(seats)
      .manual(manual)
      .doors(doors)
      .locationId(locationId)
      .fuel(CarFuel.valueOf(fuel))
      .fuelEconomy(fuelEco)
      .ownerAnnualKm(annualKm)
      .estimatedValue(estVal)
      .year(year)
      .comments(comments)
      .imagesId(imagesId);

      // System.out.println("Create or update auto"+ ab.build());
      if (autoId < 0) {
        autoId = DataAccess.getInjectedContext().getAutoDAO().createAuto(ab.build());
      } else {
        DataAccess.getInjectedContext().getAutoDAO().updateAuto(ab.build());
      }
      System.out.println("auto: " + ab.build().toString());
      
      AddressDAO addressDAO = DataAccess.getInjectedContext().getAddressDAO();
      if (locationId <= 0) {
        locationId = addressDAO.getAddressByAutoId(autoId).getId();
      }
      addressDAO.updateAddress(new Address(locationId, "België", zip, city, street, number, 0, 0));

      CarDAO carDAO = DataAccess.getInjectedContext().getCarDAO();
      carDAO.updateInsurance(autoId, new CarInsurance(insuranceNameBefore, insuranceExpiration));

      DataAccess.getInjectedContext().getUserRoleDAO().addUserRole(CurrentUser.getId(), UserRole.CAR_OWNER);
      UserHeader user = DataAccess.getInjectedContext().getUserDAO().getUserHeader(CurrentUser.getId());
      Set<UserRole> userRoles = DataAccess.getInjectedContext().getUserRoleDAO().getUserRoles(CurrentUser.getId());
      CurrentUser.set(user, userRoles);

        return getAuto(autoId);
    } catch (Exception e) {
      System.err.println("Error in createOrUpdateAuto:" + e);
      result.put("status", "error");
      result.put("message", e.getMessage());
      return badRequest(result);
    }
  }

  @InjectContext
  public static Result updateImage() {
    ObjectNode result = Json.newObject();
    File file = FileHelper.getFileFromRequest("image", FileHelper.IMAGE_CONTENT_TYPES, "uploads.carphotos", 0);
    if (file == null) {
        result.put("status", "Error");
        result.put("message", "Image file missing");
        return badRequest(result);
    } else if (file.getContentType() == null) {
        result.put("status", "Error");
        result.put("message", "Wrong image type, only image files allowed");
        return badRequest(result);
    } else {
        // Property property = DataAccess.getInjectedContext().getPropertyDAO().getProperty(propertyId);
        // Property.Builder propertyBuilder = new Property.Builder(property);
        // propertyBuilder.value(String.valueOf(file.getId()));
        // Property p = propertyBuilder.build();
        // DataAccess.getInjectedContext().getPropertyDAO().updateProperty(p);
        result.put("status", "ok");
        result.put("fileId", file.getId());
        return ok(result);
    }
  }

  @InjectContext
  public static Result findAutos(int page, int pageSize, int ascInt, String orderBy, String filter) {
    FilterField field = FilterField.stringToField(orderBy, FilterField.NAME);
    boolean asc = Pagination.parseBoolean(ascInt);
    Page<AutoAndUser> cars = DataAccess.getInjectedContext().getAutoDAO().listAutosAndOwners(field, asc, page, pageSize, filter);
    return ok(GsonHelper.getGson().toJson(cars));
  }

  @InjectContext
  @AllowRoles({ UserRole.CAR_OWNER, UserRole.CAR_ADMIN })
  public static Result getAutoInitialState(int autoId) {
    FileDAO fdao = DataAccess.getInjectedContext().getFileDAO();
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(fdao.getFilesByType(autoId, FileDAO.FileType.INITIAL_STATE)));
  }


  
  @AllowRoles({ UserRole.CAR_OWNER, UserRole.CAR_ADMIN })
  @InjectContext
  public static Result addAutoInitialStateFile(int autoId) {
    try {
      ObjectNode result = Json.newObject();
      Http.MultipartFormData.FilePart newFile = request().body().asMultipartFormData().getFile("file");
      if (newFile == null) {
        result.put("status", "Error");
        result.put("message", "Image file missing");
        return badRequest(result);
      } else {
        if (!FileHelper.isDocumentContentType(newFile.getContentType())) {
          result.put("status", "Error");
          result.put("message", "Wrong image type, only image files allowed");
          return badRequest(result);
        } else {
          // Now we add the file to the group
          FileDAO fdao = DataAccess.getInjectedContext().getFileDAO();
          File file = fdao.createFile(
                  FileHelper.saveFile(newFile, ConfigurationHelper.getConfigurationString("uploads.carinitialstates")).toString(),
                  newFile.getFilename(),
                  newFile.getContentType());
          fdao.addFileByType(autoId, file.getId(), FileDAO.FileType.INITIAL_STATE);
          result.put("status", "ok");
          result.put("fileId", file.getId());
          return ok(result);
        }
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex); //unchecked
    }
  }

  @AllowRoles({ UserRole.CAR_OWNER, UserRole.CAR_ADMIN })
  @InjectContext
  public static Result deleteAutoInitialStateFile() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      int autoId = json.findPath("autoId").asInt();
      int fileId = json.findPath("fileId").asInt();

      FileDAO fdao = DataAccess.getInjectedContext().getFileDAO();
      File file = fdao.getFileByType(autoId, fileId, FileDAO.FileType.INITIAL_STATE);
      if (file == null) {
        result.put("status", "Error");
        result.put("message", "File not found");
        return badRequest(result);
      } else {
        fdao.deleteFileByType(autoId, fileId, FileDAO.FileType.INITIAL_STATE);
        FileHelper.deleteFile(Paths.get(file.getPath()));
        result.put("status", "ok");
        return ok(result);
      }
    } catch (Exception e) {
      result.put("status", "error");
      result.put("message", "Missing parameter [name]");
      return badRequest(result);
    }
  }
}
