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
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import controllers.AllowRoles;
import controllers.WFCommon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import controllers.util.FileHelper;

import com.google.gson.JsonObject;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.google.gson.annotations.Expose;



/**
 * Controller responsible for creating, updating and showing of cars
 */
public class ApiTrips extends Controller {

  @InjectContext
  @AllowRoles({UserRole.RESERVATION_ADMIN})
  public static Result findTripsByCar(int carId) {
    DataAccessContext context = DataAccess.getInjectedContext();
    Car car = context.getCarDAO().getCar(carId);
    LocalDate now = LocalDate.now();
    // LocalDate startDate = Utils.toLocalDate(dateString);
    LocalDate startDate = LocalDate.of(2017, 3, 1);
    if (startDate == null) {
        startDate = now;
    }
    startDate = startDate.withDayOfMonth(1);
    LocalDate endDate = startDate.plusMonths(2);
    if (endDate.isAfter(now)) {
        endDate = now;
    }
    Iterable<Trip> trips = context.getTripDAO().listTrips(car.getId(), startDate.atStartOfDay(), endDate.atStartOfDay());
    List<Trip> target = new ArrayList<Trip>();
    trips.forEach(target::add);
    TripsJson tripsJson = new TripsJson();
    tripsJson.car = car;
    tripsJson.trips = target;
    tripsJson.startDate = startDate;
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(tripsJson));
  }

  @InjectContext
  public static Result getTrip(int tripId) {
    DataAccessContext context = DataAccess.getInjectedContext();
    TripDAO tripdao = context.getTripDAO();
    ReservationDAO rdao = context.getReservationDAO();
    UserDAO udao = context.getUserDAO();
    TripAndCar trip = tripdao.getTripAndCar(tripId, false);
    UserHeader driver = udao.getUserHeader(trip.getDriverId());
    UserHeader owner = udao.getUserHeader(trip.getOwnerId());
    // if ( ! WFCommon.isDriverOrOwnerOrAdmin(trip)) {
    //   return null;
    // }
    ReservationStatus status = trip.getStatus();

    UserHeader previousDriver = null;
    UserHeader nextDriver = null;
    String nextDate = null;
    if (status != ReservationStatus.CANCELLED) { //reservation status can't be cancelled
        Reservation nextReservation = rdao.getNextReservation(tripId);
        if (nextReservation != null) {
            nextDriver = udao.getUserHeader(nextReservation.getDriverId());
            nextDate = nextReservation.getFrom().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy' om 'HH:mm"));
        }
        Reservation previousReservation = rdao.getPreviousReservation(tripId);
        if (previousReservation != null) {
            previousDriver = udao.getUserHeader(previousReservation.getDriverId());
        }
    }

    TripJson tripJson = new TripJson();
    tripJson.driver = driver;
    tripJson.previousDriver = previousDriver;
    tripJson.nextDriver = nextDriver;
    tripJson.owner = owner;
    tripJson.trip = trip;
    tripJson.nextDate = nextDate;
    Gson gson = GsonHelper.getGson();
    // if (CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
    //   ;
    // } else if (CurrentUser.is(owner.getId())) {
    //   tripJson.owner = null;
    // } else {
    //   tripJson.driver = null;
    // }
    return ok(gson.toJson(tripJson));
  }

  static class TripJson {
    @Expose
    public UserHeader driver;
    @Expose
    public UserHeader previousDriver;
    @Expose
    public UserHeader nextDriver;
    @Expose
    public UserHeader owner;
    @Expose
    public Trip trip;
    @Expose
    public String nextDate;
  }

  static class TripsJson {
    @Expose
    public Car car;
    @Expose
    public List<Trip> trips;
    @Expose
    public LocalDate startDate;
  }

}
