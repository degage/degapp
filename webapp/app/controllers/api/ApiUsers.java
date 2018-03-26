package controllers.api;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import com.google.common.base.Strings;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.cars.*;
import db.CurrentUser;

import java.io.IOException;

import controllers.AllowRoles;
import controllers.util.Pagination;
import com.google.gson.Gson;

import com.google.gson.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;


/**
 * Controller responsible for creating, updating and showing of cars
 */
public class ApiUsers extends Controller {

  private static final int MAX_VISIBLE_RESULTS = 10;

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getUser(int userId) {
    User user = DataAccess.getInjectedContext().getUserDAO().getUser(userId);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(user));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result filterUsers(String search, String status) {
    if (search.isEmpty()) {
        return ok(); // normally does not occur
    } else {
      Iterable<UserHeaderShort> users = DataAccess.getInjectedContext().getUserDAO().listUserByName(search, status != "" ? Arrays.asList(status.split(",")) : new ArrayList<String>(), MAX_VISIBLE_RESULTS);
      return ok(GsonHelper.getGson().toJson(users));
    }
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result findUsers(int page, int pageSize, int ascInt, String orderBy, String filter) {
      FilterField field = FilterField.stringToField(orderBy, FilterField.NAME);
      boolean asc = Pagination.parseBoolean(ascInt);
      Page<User> listOfUsers = DataAccess.getInjectedContext().getUserDAO().findUsers(field, asc, page, pageSize, filter);
      return ok(GsonHelper.getGson().toJson(listOfUsers));
  }

  @InjectContext
  //@AllowRoles({UserRole.CONTRACT_ADMIN})
  public static Result updateContractDate(int userId) {
    JsonNode json = request().body().asJson();
    String contractDate = json.findPath("contract_date").asText();
    //DataAccess.getInjectedContext().getMembershipDAO().updateUserContract(userId, contractDate);
    return ok(GsonHelper.getGson().toJson(contractDate));
  }

  public static Result createUserRole() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      int userId = json.findPath("userId").asInt();
      String role = json.findPath("role").asText();
      Set<UserRole> userRoles = DataAccess.getInjectedContext().getUserRoleDAO().getUserRoles(userId);
      if (role.equals("CAR_USER")) {
        if (!userRoles.contains(UserRole.CAR_USER)){
          DataAccess.getInjectedContext().getUserRoleDAO().addUserRole(userId, UserRole.CAR_USER);
        }
        if (userRoles.contains(UserRole.CAR_OWNER)){
          DataAccess.getInjectedContext().getUserRoleDAO().removeUserRole(userId, UserRole.CAR_OWNER);
        }
      } else if (role.equals("CAR_OWNER")) {
        if (!userRoles.contains(UserRole.CAR_USER)){
          DataAccess.getInjectedContext().getUserRoleDAO().addUserRole(userId, UserRole.CAR_USER);
        }
        if (!userRoles.contains(UserRole.CAR_OWNER)){
          DataAccess.getInjectedContext().getUserRoleDAO().addUserRole(userId, UserRole.CAR_OWNER);
        }

      }
      UserHeader user = DataAccess.getInjectedContext().getUserDAO().getUserHeader(userId);
      userRoles = DataAccess.getInjectedContext().getUserRoleDAO().getUserRoles(userId);
      CurrentUser.set(user, userRoles);
      result.put("status", "ok");
      return ok(result);
    } catch (Exception e) {
      System.err.println("Error in createAuto:" + e);
      result.put("status", "error");
      result.put("message", e.getMessage());
      return badRequest(result);
    }
  }
}
