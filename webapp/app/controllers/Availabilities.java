package controllers;

import be.ugent.degage.db.dao.AvailabilityDAO;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.CarAvailabilityInterval;
import be.ugent.degage.db.models.UserRole;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Controller which is responsible for processing car availabilities. Works in concert with Cars controller.
 */
public class Availabilities extends Controller {

    private static final int SECONDS_IN_MINUTE = 60;
    private static final int SECONDS_IN_HOUR = 3600;
    private static final int SECONDS_IN_DAY = 24*SECONDS_IN_HOUR;
    private static final int SECONDS_IN_WEEK = 7* SECONDS_IN_DAY;

    /**
     * Transfer object for use in 'detail' view
     */
    public static class Availability {
        public String id;
        public String startDay; // 0 is sunday
        public String startTime;
        public String endDay;
        public String endTime;

        /**
         * Convert a CarAvailabilityInterval to an object of this type
         */
        public Availability (CarAvailabilityInterval interval) {
            int minutes; // temporary variable

            id = Integer.toString(interval.getId());

            int start = interval.getStart();
            startDay = Integer.toString ((start / SECONDS_IN_DAY) % 7);
            minutes = (start % SECONDS_IN_DAY) / 60;
            startTime = String.format("%02d:%02d", minutes / 60, minutes % 60);

            int end = interval.getEnd();
            endDay = Integer.toString ((end / SECONDS_IN_DAY) % 7);
            minutes = (end % SECONDS_IN_DAY) / 60;
            endTime = String.format("%02d:%02d", minutes / 60, minutes % 60);
        }
    }

    static Iterable<Availability> convertToView (Iterable<CarAvailabilityInterval> list) {

        Collection<Availability> result = new ArrayList<>();
        for (CarAvailabilityInterval interval : list) {
            result.add (new Availability(interval));
        }
        return result;
    }

    /**
     * Create an interval from a string produced by javascript/updateCarAvailabilities.js
     * @param str
     * @return
     */
    private static CarAvailabilityInterval createInterval(String str) {
        String[] vs = str.split(",");
        if (vs.length != 5) {
            return null;
        }

        int startDay = Integer.parseInt(vs[1]);
        String[] startHM = vs[2].split(":");
        int start = SECONDS_IN_DAY * startDay
                + SECONDS_IN_HOUR * Integer.parseInt(startHM[0])
                + SECONDS_IN_MINUTE * Integer.parseInt(startHM[1]);
        int endDay = Integer.parseInt(vs[3]);
        String[] endHM = vs[4].split(":");
        int end = SECONDS_IN_DAY * endDay
                + SECONDS_IN_HOUR * Integer.parseInt(endHM[0])
                + SECONDS_IN_MINUTE * Integer.parseInt(endHM[1]);

        if (end <= start) {
            end += SECONDS_IN_WEEK;
        }

        return new CarAvailabilityInterval(
                Integer.parseInt(vs[0]),
                start,
                end
        );
    }

    /**
     * Method: POST
     *
     * @return redirect to the car detailPage
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result updateAvailabilities(int carId, String valuesString) {
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        AvailabilityDAO availabilityDAO = DataAccess.getInjectedContext().getAvailabilityDAO();
        Car car = dao.getCar(carId);

        if (CurrentUser.isNot(car.getOwner().getId()) && ! CurrentUser.hasRole(UserRole.CAR_ADMIN)) {
            flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
            return redirect(routes.Cars.detail(carId));
        }

        String[] values = valuesString.split(";");

        Collection<CarAvailabilityInterval> availabilitiesToCreate = new ArrayList<>();
        Collection<CarAvailabilityInterval> availabilitiesToUpdate = new ArrayList<>();
        Collection<Integer> availabilitiesToDelete = new ArrayList<>();


        for (String value : values) {
            try {
                CarAvailabilityInterval interval = createInterval(value);
                int id = interval.getId();
                if (id == 0) { // create
                    availabilitiesToCreate.add(interval);
                } else if (id > 0) { // update
                    availabilitiesToUpdate.add(interval);
                } else { // delete
                    availabilitiesToDelete.add(- id);
                }
            } catch (Exception e) {
                flash("error", "Er is een fout gebeurd bij het doorgeven van de beschikbaarheidswaarden.");
                return redirect(routes.Cars.detail(carId));
            }
        }

        availabilityDAO.createAvailabilities(carId, availabilitiesToCreate);
        availabilityDAO.updateAvailabilities(carId, availabilitiesToUpdate);
        availabilityDAO.deleteAvailabilties(availabilitiesToDelete);

        flash("success", "Je wijzigingen werden met success toegepast.");
        return redirect(routes.Cars.detail(carId));
    }

}
