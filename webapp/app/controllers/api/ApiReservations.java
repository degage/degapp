package controllers.api;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.dao.TripDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Page;
import controllers.util.Pagination;
import controllers.util.Addresses;
import controllers.util.ConfigurationHelper;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import notifiers.Notifier;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import controllers.Utils;
import controllers.AllowRoles;
import controllers.WFCommon;
import controllers.util.WorkflowAction;
import controllers.util.WorkflowRole;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import controllers.util.FileHelper;

import com.google.gson.JsonObject;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.annotations.Expose;



/**
 * Controller responsible for creating, updating and showing of reservations
 */
public class ApiReservations extends Controller {

  @InjectContext
  @AllowRoles({UserRole.RESERVATION_ADMIN})
  public static Result cancelReservation() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      int reservationId = json.findPath("reservationId").asInt();
      DataAccessContext context = DataAccess.getInjectedContext();
      ReservationDAO dao = context.getReservationDAO();
      ReservationHeader reservation = dao.getReservationHeader(reservationId);

      if (WorkflowAction.CANCEL.isForbiddenForCurrentUser(reservation)) {
          flash("danger", "U kan deze reservatie niet (meer) annuleren");
          result.put("status", "error");
          result.put("message", "U kan deze reservatie niet (meer) annuleren");
          return ok(result);
      }

      // one special case: already accepted and in the past (and not owner or admin)
      if (reservation.getStatus() == ReservationStatus.ACCEPTED
              && reservation.getFrom().isAfter(LocalDateTime.now())
              && !WorkflowRole.OWNER.isCurrentRoleFor(reservation)
              && !WorkflowRole.ADMIN.isCurrentRoleFor(reservation)
              ) {
          result.put("status", "error");
          result.put("message", "Deze reservatie was reeds goedgekeurd! " +
                        "Je moet daarom verplicht een reden opgeven voor de annulatie");
          return ok(result);
      } else {
          dao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED);
          return ApiTrips.getTrip(reservationId);
      }
    } catch (Exception e) {
      System.err.println("Error in cancelReservation:" + e);
      result.put("status", "error");
      result.put("message", e.getMessage());
      return badRequest(result);
    }
  }

  @InjectContext
  @AllowRoles({UserRole.RESERVATION_ADMIN})
  public static Result doCreate(int carId){
    ObjectNode result = Json.newObject();
    JsonNode json = request().body().asJson();
    try {
      DataAccessContext context = DataAccess.getInjectedContext();
      Car car = context.getCarDAO().getCar(carId);
      

      LocalDateTime from = Utils.toLocalDateTime(json.findPath("from").asText());
      LocalDateTime until = Utils.toLocalDateTime(json.findPath("until").asText());
      String message = json.findPath("message").asText();
      int currentUserId = json.findPath("currentUserId").asInt();

      if (from.isBefore(LocalDateTime.now()) && CurrentUser.isNot(car.getOwner().getId())){
        throw new IllegalArgumentException("Een reservatie uit het verleden kan enkel door de eigenaar worden ingebracht");
      }

      ReservationDAO rdao = context.getReservationDAO();
      if (rdao.hasOverlap(carId, from, until)) {
        throw new IllegalArgumentException("De reservatie overlapt met een bestaande reservatie");
      }

      if (from.isAfter(until)){
        throw new IllegalArgumentException("De eind datum / uur moet na de start datum /uur liggen");
      }

      ReservationHeader reservation = rdao.createReservation(from, until, carId, CurrentUser.getId(), message);
      if (reservation.getStatus() == ReservationStatus.REQUEST){
        Reservation res = rdao.getReservation(reservation.getId());
        Notifier.sendReservationApproveRequestMail(car.getOwner(), res, car.getName());
        Gson gson = GsonHelper.getGson();
        return ok(gson.toJson(res));

      }
      result.put("message", "Er ging iets fout in ApiReservations.java");
      return ok(result);



    } catch (Exception e) {
      System.err.println("Error in doCreate: " + e);
      result.put("status", "error");
      result.put("message", e.getMessage());
      return badRequest(result);
    }
  }

}
