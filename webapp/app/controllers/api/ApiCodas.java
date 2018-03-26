package controllers.api;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Coda;
import be.ugent.degage.db.models.File;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.models.UserRole;
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
// import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import controllers.AllowRoles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import controllers.Codas;
import controllers.util.FileHelper;



/**
 * Controller responsible for creating, updating and showing of cars
 */
public class ApiCodas extends Controller {

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result findCodas(int page, int pageSize, int ascInt, String orderBy, String filter) {
      FilterField field = FilterField.stringToField(orderBy, FilterField.DATE);
      boolean asc = Pagination.parseBoolean(ascInt);
      Page<Coda> listOfCodas = DataAccess.getInjectedContext().getCodasDAO().listCodasPage(field, asc, page, pageSize, filter);
      return ok(GsonHelper.getGson().toJson(listOfCodas));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result uploadCoda() {
    // Http.MultipartFormData body = request().body().asMultipartFormData();
    // play.mvc.Http.MultipartFormData.FilePart codaFilePart = body.getFile("coda");
    //
    // File file = codaFilePart.getFile();
    File file = FileHelper.getFileFromRequest("coda", FileHelper.CODA_CONTENT_TYPES, "uploads.codas", 0);
    // System.out.println("coda file parent" + file.getPath().getParent());
    int numPayments = 0;
    try {
      numPayments = Codas.processCoda(file);
    } catch (Exception e) {
        e.printStackTrace();
    }
    ObjectNode result = Json.newObject();
    result.put("numberOfPayments", numPayments);
    // Http.MultipartFormData.FilePart picture = body.getFile("picture");
    return ok(result);
  }
}
