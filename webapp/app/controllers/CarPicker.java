package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.jdbc.JDBCFilter;
import be.ugent.degage.db.models.Car;
import controllers.Security.RoleSecured;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;

import java.util.List;

public class CarPicker extends Controller {

    private static final int MAX_VISIBLE_RESULTS = 10;

    @RoleSecured.RoleAuthenticated()
    public static Result getList(String search) {
        search = search.trim();
        if (search != "") {
            search = search.replaceAll("\\s+", " ");
            try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
                CarDAO dao = context.getCarDAO();
                String cars = "";
                Filter filter = new JDBCFilter();
                filter.putValue(FilterField.CAR_NAME, search);
                List<Car> results = dao.getCarList(FilterField.CAR_NAME, true, 1, MAX_VISIBLE_RESULTS, filter);
                for (Car car : results) {
                    String value = car.getName();
                    for (String part : search.split(" ")) {
                        value = value.replaceAll("(?i)\\b(" + part + ")", "<#>$1</#>");
                    }

                    cars += "<li data-uid=\"" + car.getId() + "\"><a href=\"javascript:void(0)\"><span>" + value.replace("#", "strong") + "</span> (" + car.getId() + ")</a></li>";
                }
                return ok(cars);
            } catch (DataAccessException ex) {
                throw ex;//TODO?
            }
        } else {
            return ok();
        }
    }
}
