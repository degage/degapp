package controllers.api;

import db.DataAccess;
import db.InjectContext;
import be.ugent.degage.db.models.*;
import controllers.util.FileHelper;
import controllers.AllowRoles;

import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

/**
 * Controller responsible for uploading documents
 */
public class ApiDocuments extends Controller {
  
  @InjectContext
  @AllowRoles({ UserRole.CAR_ADMIN })
  public static Result uploadDocument(int autoId, String documentType) {
    ObjectNode result = Json.newObject();
    File file = FileHelper.getFileFromRequest("document", FileHelper.DOCUMENT_CONTENT_TYPES, FileHelper.DOCUMENT_TYPES.get(documentType), 0);
    if (file == null) {
      result.put("status", "Error");
      result.put("message", "Document file missing");
      return badRequest(result);
    } else if (file.getContentType() == null) {
      result.put("status", "Error");
      result.put("message", "Wrong image type, only image files allowed");
      return badRequest(result);
    } else {
      try {
        switch (documentType) {
          case "CONTRACTSCAR":
            DataAccess.getInjectedContext().getAutoDAO().updateAutoDocument(autoId, file.getId());
            break;
          case "CONTRACTSINSURANCE":
            DataAccess.getInjectedContext().getCarInsuranceDAO().updateCarInsuranceDocument(autoId, file.getId());
            break;
          case "CONTRACTSASSISTANCE":
            DataAccess.getInjectedContext().getCarAssistanceDAO().updateCarAssistanceDocument(autoId, file.getId());
            break;
          case "PARKINGCARDS":
            DataAccess.getInjectedContext().getCarParkingcardDAO().updateCarParkingcardDocument(autoId, file.getId());
            break; 
          default:
            break;
        }
        result.put("status", "ok");
        result.put("fileId", file.getId());
        return ok(result);
      } catch (Exception e) {
        System.err.println("Error in updateAutoDocument:" + e);
        result.put("status", "Error");
        result.put("message", e.getMessage());
        return badRequest(result);
      }
    }
  }
}