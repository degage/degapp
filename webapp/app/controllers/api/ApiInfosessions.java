package controllers.api;

import be.ugent.degage.db.models.InfoSession;
import be.ugent.degage.db.models.CarStatus;
import be.ugent.degage.db.models.Page;
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
import be.ugent.degage.db.dao.InfoSessionDAO;

public class ApiInfosessions extends Controller {

  @InjectContext
  @AllowRoles({})
  public static Result getInfosession(int autoId) {
    InfoSessionDAO isDao = DataAccess.getInjectedContext().getInfoSessionDAO();
    InfoSession auto = isDao.getInfoSession(autoId);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(auto));
  }

  @InjectContext
  @AllowRoles({})
  public static Result getUpcomingInfosessions() {
    InfoSessionDAO isDao = DataAccess.getInjectedContext().getInfoSessionDAO();
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(isDao.getUpcomingInfoSessions()));
  }

  @InjectContext
  @AllowRoles({})
  public static Result getAttendingInfoSession() {
    InfoSessionDAO isDao = DataAccess.getInjectedContext().getInfoSessionDAO();
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(isDao.getAttendingInfoSession(CurrentUser.getId())));
  }

}
