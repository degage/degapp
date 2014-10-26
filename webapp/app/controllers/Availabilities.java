package controllers;

import be.ugent.degage.db.dao.AvailabilityDAO;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.CarAvailabilityInterval;
import be.ugent.degage.db.models.DayOfWeek;
import be.ugent.degage.db.models.UserRole;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import org.joda.time.LocalTime;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller which is responsible for processing car availabilities. Works in concert with Cars controller.
 */
public class Availabilities extends Controller {

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

        if (!(car.getOwner().getId() == CurrentUser.getId() || CurrentUser.hasRole(UserRole.CAR_ADMIN))) {
            flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
            return redirect(routes.Cars.detail(carId));
        }

        String[] values = valuesString.split(";");

        List<CarAvailabilityInterval> availabilitiesToCreate = new ArrayList<>();
        List<CarAvailabilityInterval> availabilitiesToUpdate = new ArrayList<>();
        List<Integer> availabilitiesToDelete = new ArrayList<>();

        for (String value : values) {
            String[] vs = value.split(",");
            if (vs.length != 5) {
                flash("error", "Er is een fout gebeurd bij het doorgeven van de beschikbaarheidswaarden.");
                return redirect(routes.Cars.detail(carId));
            }
            try {
                int id = Integer.parseInt(vs[0]);
                DayOfWeek beginDay = DayOfWeek.getDayFromInt(Integer.parseInt(vs[1]));
                String[] beginHM = vs[2].split(":");
                LocalTime beginTime = new LocalTime(Integer.parseInt(beginHM[0]), Integer.parseInt(beginHM[1]));
                DayOfWeek endDay = DayOfWeek.getDayFromInt(Integer.parseInt(vs[3]));
                String[] endHM = vs[4].split(":");
                LocalTime endTime = new LocalTime(Integer.parseInt(endHM[0]), Integer.parseInt(endHM[1]));
                if (id == 0) { // create
                    availabilitiesToCreate.add(new CarAvailabilityInterval(null, beginDay, beginTime, endDay, endTime));
                } else if (id > 0) { // update
                    availabilitiesToUpdate.add(new CarAvailabilityInterval(id, beginDay, beginTime, endDay, endTime));
                } else { // delete
                    availabilitiesToDelete.add(id);
                }
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                flash("error", "Er is een fout gebeurd bij het doorgeven van de beschikbaarheidswaarden.");
                return redirect(routes.Cars.detail(carId));
            }
        }

        boolean autoMerge = false;
        // TODO: check overlapping
        // mergeOverlappingAvailabilities(availabilitiesToAddOrUpdate, availabilitiesToDelete);

        availabilityDAO.createAvailabilities(carId, availabilitiesToCreate);
        availabilityDAO.updateAvailabilities(carId, availabilitiesToUpdate);
        availabilityDAO.deleteAvailabilties(availabilitiesToDelete);

        flash("success", "Je wijzigingen werden succesvol toegepast." + (autoMerge ? "<br />Overlappende intervallen werden automatisch samengevoegd." : ""));
        return redirect(routes.Cars.detail(car.getId()));
    }

    private static boolean mergeOverlappingAvailabilities(List<CarAvailabilityInterval> addOrUpdate, List<CarAvailabilityInterval> delete) {
        boolean adapted = false;
        boolean repeat = true;

        while (repeat) {
            repeat = false;
            repeatLoop:
            for (int i = 0; i < addOrUpdate.size(); i++) {
                for (int j = i + 1; j < addOrUpdate.size(); j++) {
                    CarAvailabilityInterval interval1 = addOrUpdate.get(i);
                    CarAvailabilityInterval interval2 = addOrUpdate.get(j);

                    CarAvailabilityInterval merged = mergeIntervals(interval1, interval2);

                    if (merged != null) {
                        if (interval1.getId() != null)
                            delete.add(interval1);

                        if (interval2.getId() != null)
                            delete.add(interval2);

                        addOrUpdate.remove(interval1);
                        addOrUpdate.remove(interval2);

                        addOrUpdate.add(merged);

                        adapted = true;
                        repeat = true;
                        break repeatLoop;
                    }
                }
            }
        }

        return adapted;
    }

    private static CarAvailabilityInterval mergeIntervals(CarAvailabilityInterval interval1, CarAvailabilityInterval interval2) {
        int[] interval1Days = {interval1.getBeginDayOfWeek().getI(), interval1.getEndDayOfWeek().getI()};
        LocalTime[] interval1Times = {interval1.getBeginTime(), interval1.getEndTime()};

        int[] interval2Days = {interval2.getBeginDayOfWeek().getI(), interval2.getEndDayOfWeek().getI()};
        LocalTime[] interval2Times = {interval2.getBeginTime(), interval2.getEndTime()};

        interval1Days = refactorWeekDays(interval1Days, interval1Times);
        interval2Days = refactorWeekDays(interval2Days, interval2Times);

        // Interval covers any time span
        if (interval1Days[0] == interval1Days[1] && interval1Times[0].equals(interval1Times[1]))
            return interval1;
        if (interval2Days[0] == interval2Days[1] && interval2Times[0].equals(interval2Times[1]))
            return interval2;

        if (isAfter(interval1Days[1], interval1Times[1], interval2Days[0], interval2Times[0], true)) {
            if (isBefore(interval1Days[0], interval1Times[0], interval2Days[0], interval2Times[0], true)) {
                if (isAfter(interval1Days[1], interval1Times[1], interval2Days[1], interval2Times[1], true)) {
                    // Interval 1 comprises interval 2
                    return interval1;
                } else if (interval2Days[1] < 7 || isAfter(interval1Days[0], interval1Times[0], interval2Days[1] % 7, interval2Times[1], false)) {
                    // End of interval 1 overlaps with beginning of interval 2
                    return new CarAvailabilityInterval(interval1.getBeginDayOfWeek(), interval1.getBeginTime(), interval2.getEndDayOfWeek(), interval2.getEndTime());
                } else {
                    // Combination of intervals covers any time span
                    return new CarAvailabilityInterval(interval1.getBeginDayOfWeek(), interval1.getBeginTime(), interval1.getBeginDayOfWeek(), interval1.getBeginTime());
                }
            } else if (isAfter(interval1Days[0], interval1Times[0], interval2Days[1], interval2Times[1], false)) {
                if (interval1Days[1] > 6 && isAfter(interval1Days[1] % 7, interval1Times[1], interval2Days[0], interval2Times[0], true)) {
                    // End of interval 1 overlaps with beginning of interval 2
                    return new CarAvailabilityInterval(interval1.getBeginDayOfWeek(), interval1.getBeginTime(), interval2.getEndDayOfWeek(), interval2.getEndTime());
                } else {
                    // Intervals do not overlap
                    return null;
                }
            } else if (isAfter(interval1Days[1], interval1Times[1], interval2Days[1], interval2Times[1], false)) {
                // End of interval 2 overlaps with beginning of interval 1
                return new CarAvailabilityInterval(interval2.getBeginDayOfWeek(), interval2.getBeginTime(), interval1.getEndDayOfWeek(), interval1.getEndTime());
            } else {
                // Interval 2 comprises interval 1
                return interval2;
            }
        } else if (interval2Days[1] > 6 && isBefore(interval1Days[0], interval1Times[0], interval2Days[1] % 7, interval2Times[1], true)) {
            // End of interval 2 overlaps with beginning of interval 1
            return new CarAvailabilityInterval(interval2.getBeginDayOfWeek(), interval2.getBeginTime(), interval1.getEndDayOfWeek(), interval1.getEndTime());
        } else {
            // Intervals do not overlap
            return null;
        }
    }

    // Is day1 - time1 before (or equal to) day2 - time2
    private static boolean isBefore(int day1, LocalTime time1, int day2, LocalTime time2, boolean orEqual) {
        return day1 < day2 || (day1 == day2 && (time1.isBefore(time2) || (orEqual && time1.equals(time2))));
    }

    private static boolean isBefore(int day1, LocalTime time1, int day2, LocalTime time2) {
        return isBefore(day1, time1, day2, time2, false);
    }

    // Is day1 - time1 after (or equal to) day2 - time2
    private static boolean isAfter(int day1, LocalTime time1, int day2, LocalTime time2, boolean orEqual) {
        return !isBefore(day1, time1, day2, time2, !orEqual);
    }

    private static boolean isAfter(int day1, LocalTime time1, int day2, LocalTime time2) {
        return isAfter(day1, time1, day2, time2, false);
    }

    private static int[] refactorWeekDays(int[] days, LocalTime[] times) {
        days[0]--;
        days[1]--; // Easier for modular arithmetic
        if (isAfter(days[0], times[0], days[1], times[1]))
            days[1] += 7;
        return days;
    }

}
