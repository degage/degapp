package controllers;

import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.ReservationHeader;
import be.ugent.degage.db.models.TripAndCar;

import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
import java.util.ArrayList;

/**
 * Controller responsible to create calendat events.
 */
public class CalendarEvents extends Controller {


    /**
     * Calendar event for reservation.
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result getEventForReservation(int reservationId, String separator) {
      TripAndCar tripAndCar = DataAccess.getInjectedContext().getTripDAO().getTripAndCar(reservationId, true);
      if (tripAndCar != null) {
        String[] calendarEvent = {
            "BEGIN:VCALENDAR",
            "VERSION:2.0",
            "BEGIN:VEVENT",
            "CLASS:PUBLIC",
            "DESCRIPTION:" + "http://www.degage.be/degapp/trip?id=" + reservationId,
            "DTSTART;VALUE=DATE:" + tripAndCar.getFromIcs(),
            "DTEND;VALUE=DATE:" + tripAndCar.getUntilIcs(),
            "LOCATION:" + tripAndCar.getCar().getLocation().toString(),
            "SUMMARY;LANGUAGE=en-us:" + tripAndCar.getCar().getName(),
            "TRANSP:TRANSPARENT",
            "END:VEVENT",
            "END:VCALENDAR"
        };
        return ok(String.join(separator, calendarEvent));
      } else {
        return null;
      }
    }

    /**
     * Calendar events for car.
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_USER})
    @InjectContext
    public static Result getEventsForCar(int carId, String separator) {
      ReservationDAO.CRInfo cRInfo = DataAccess.getInjectedContext().getReservationDAO().listFutureReservationsForCar(carId);
      if (cRInfo != null) {
        List<String> calendarEvent = new ArrayList<String>();
        calendarEvent.add("BEGIN:VCALENDAR");
        calendarEvent.add("VERSION:2.0");
        if (cRInfo.reservations != null){
          for (ReservationHeader reservation : cRInfo.reservations) {
            calendarEvent.add("BEGIN:VEVENT");
            calendarEvent.add("CLASS:PUBLIC");
            calendarEvent.add("DESCRIPTION:" + "http://www.degage.be/degapp/trip?id=" + reservation.getId());
            calendarEvent.add("DTSTART;VALUE=DATE:" + reservation.getFromIcs());
            calendarEvent.add("DTEND;VALUE=DATE:" + reservation.getUntilIcs());
            calendarEvent.add("LOCATION:" + "test");
            calendarEvent.add("SUMMARY;LANGUAGE=en-us:" + cRInfo.carName);
            calendarEvent.add("TRANSP:TRANSPARENT");
            calendarEvent.add("END:VEVENT");
          }
        }
        calendarEvent.add("END:VCALENDAR");
        return ok(String.join(separator, calendarEvent.toArray(new String[calendarEvent.size()])));
      } else {
        return null;
      }
    }


}

