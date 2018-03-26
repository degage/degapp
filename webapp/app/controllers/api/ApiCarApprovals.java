package controllers.api;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.dao.CarApprovalDAO;
import be.ugent.degage.db.dao.AutoDAO;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import controllers.util.Pagination;
import com.google.common.base.Strings;
import controllers.util.Addresses;
import controllers.util.ConfigurationHelper;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import controllers.AllowRoles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;


/**
 * Controller responsible for creating, updating and showing of cars
 */
public class ApiCarApprovals extends Controller {

  @InjectContext
  @AllowRoles({UserRole.CAR_ADMIN})
  public static Result findCarApprovals(int page, int pageSize, int ascInt, String orderBy, String filter) {
      FilterField field = FilterField.stringToField(orderBy, FilterField.NAME);
      boolean asc = Pagination.parseBoolean(ascInt);
      Page<CarApproval> cars = DataAccess.getInjectedContext().getCarApprovalDAO().listCarApprovals(field, asc, page, pageSize, filter);
      return ok(GsonHelper.getGson().toJson(cars));
  }

  @InjectContext
  @AllowRoles({ UserRole.CAR_ADMIN })
  public static Result findCarAdmins() {
    Page<User> listOfUsers = DataAccess.getInjectedContext().getUserDAO().getUserList(FilterField.NAME, true, 1, 100, Pagination.parseFilter("role=CAR_ADMIN"));
    return ok(GsonHelper.getGson().toJson(listOfUsers));
  }

  @InjectContext
  @AllowRoles({ UserRole.CAR_ADMIN})
  public static Result updateCarApproval(int carApprovalId) {
    JsonNode json = request().body().asJson();
    ObjectNode result = Json.newObject();
    try {
      String adminMessage = json.findPath("adminMessage").asText();
      String status = json.findPath("status").asText();
      int adminId = json.findPath("adminId").asInt();

      CarApprovalDAO caDao = DataAccess.getInjectedContext().getCarApprovalDAO();
      CarApproval carApproval = caDao.getCarApproval(carApprovalId);
      CarApproval.Builder ca = new CarApproval.Builder(carApproval);
      if (status != "") {
        ca.status(ApprovalStatus.valueOf(status));
      }
      if (adminMessage != "") {
        ca.adminMessage(adminMessage);
      }
      if (adminId != 0) {
        User admin = DataAccess.getInjectedContext().getUserDAO().getUser(adminId);
        ca.admin(admin);
      }
      caDao.updateCarApproval(ca.build());
      
      if (status.equals("ACCEPTED") || status.equals("REFUSED")) {
        AutoDAO aDao = DataAccess.getInjectedContext().getAutoDAO();
        Auto auto = aDao.getAuto(carApproval.getCarId());
        Auto.Builder ab = new Auto.Builder(auto);
        if (status.equals("ACCEPTED")) {
          ab.status(CarStatus.valueOf("FULL"));
        } else if (status.equals("REFUSED")) {
          ab.status(CarStatus.valueOf("REFUSED"));
        } 
        aDao.updateAuto(ab.build());
      }

      result.put("status", "ok");
      return ok(result);
    } catch (Exception e) {
      System.err.println("Error in createOrUpdateAuto:" + e);
      result.put("status", "error");
      result.put("message", e.getMessage());
      return badRequest(result);
    }
  }
}
