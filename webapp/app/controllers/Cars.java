package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.dao.CarCostDAO;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.models.*;
import controllers.Security.RoleSecured;
import controllers.util.Addresses;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import play.data.Form;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import providers.UserRoleProvider;
import views.html.cars.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static controllers.util.Addresses.getCountryList;
import static controllers.util.Addresses.modifyAddress;


/**
 * Controller responsible for creating, updating and showing of cars
 */
public class Cars extends Controller {

    private static List<String> fuelList;

    public static List<String> getFuelList() {
        if (fuelList == null) {
            fuelList = new ArrayList<>();
            CarFuel[] types = CarFuel.values();
            for (CarFuel f : types) {
                fuelList.add(f.getDescription());
            }
        }
        return fuelList;
    }


    public static class CarModel {

        public Integer userId;

        public String name;
        public String brand;
        public String type;
        public Integer seats;
        public Integer doors;
        public boolean manual;
        public boolean gps;
        public boolean hook;
        public Integer year;
        public String fuel;
        public Integer fuelEconomy;
        public Integer estimatedValue;
        public Integer ownerAnnualKm;
        public String comments;
        public boolean active;

        // TechnicalCarDetails
        public String licensePlate;
        // public String registration; // TODO: file inschrijvingsbewijs
        public String chassisNumber;

        // Insurance
        public String insuranceName;
        public Date expiration;
        public Integer bonusMalus;
        public Integer polisNr;

        public Addresses.EditAddressModel address = new Addresses.EditAddressModel();

        public void populate(Car car) {
            if (car == null) return;

            userId = car.getOwner().getId();

            name = car.getName();
            brand = car.getBrand();
            type = car.getType();
            seats = car.getSeats();
            doors = car.getDoors();
            year = car.getYear();
            manual = car.isManual();
            gps = car.isGps();
            hook = car.isHook();
            fuel = car.getFuel().getDescription();
            fuelEconomy = car.getFuelEconomy();
            estimatedValue = car.getEstimatedValue();
            ownerAnnualKm = car.getOwnerAnnualKm();
            comments = car.getComments();
            active = car.isActive();

            if (car.getTechnicalCarDetails() != null) {
                licensePlate = car.getTechnicalCarDetails().getLicensePlate();
                chassisNumber = car.getTechnicalCarDetails().getChassisNumber();
            }

            if (car.getInsurance() != null) {
                insuranceName = car.getInsurance().getName();
                if (car.getInsurance().getExpiration() != null)
                    expiration = car.getInsurance().getExpiration();
                bonusMalus = car.getInsurance().getBonusMalus();
                polisNr = car.getInsurance().getPolisNr();
            }

            address.populate(car.getLocation());
        }

        /**
         * Validates the form:
         * - Address zip and city cannot be empty
         * - Car name and brand cannot be empty
         * - There have to be at least 2 doors and seats
         *
         * @return An error string or null
         */
        public String validate() {
            String error = "";
            if (userId == null || userId == 0)
                error += "Geef een eigenaar op. ";
            if (!address.enoughFilled())
                error += "Geef het adres op.";
            if (name.length() <= 0)
                error += "Geef de autonaam op. ";
            if (brand.length() <= 0)
                error += "Geef het automerk op. ";
            if (seats == null || seats < 2)
                error += "Een auto heeft minstens 2 zitplaatsen. ";
            if (doors == null || doors < 2)
                error += "Een auto heeft minstens 2 deuren. ";

            if ("".equals(error)) return null;
            else return error;
        }
    }

    /**
     * @return The cars index-page with all cars (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showCars() {
        return ok(views.html.cars.carsAdmin.render());
    }

    /**
     * @return The cars index-page with user cars (only available to car_owners)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    @InjectContext
    public static Result showUserCars() {
        return ok(userCarList());
    }

    // should only be used with injected context
    private static Html userCarList() {
        User user = DataProvider.getUserProvider().getUser();
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        // Doesn't need to be paginated, because a single user will never have a lot of cars
        List<Car> listOfCars = dao.getCarsOfUser(user.getId());
        return views.html.cars.cars.render(listOfCars);

    }

    /**
     * @param page         The page in the carlists
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of cars of the corresponding page (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showCarsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField carField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        return ok(carList(page, pageSize, carField, asc, filter));
    }

    // used with injected context
    private static Html carList(int page, int pageSize, FilterField orderBy, boolean asc, Filter filter) {
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();

        if (orderBy == null) {
            orderBy = FilterField.CAR_NAME;
        }
        List<Car> listOfCars = dao.getCarList(orderBy, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfCars(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return views.html.cars.carspage.render(listOfCars, page, amountOfResults, amountOfPages);
    }

    /**
     * Gets the picture for given car Id, or default one if missing
     *
     * @param carId The car for which the image is requested
     * @return The image with correct content type
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result getPicture(int carId) {
        //TODO: checks on whether other person can see this
        DataAccessContext context = DataAccess.getInjectedContext();
        CarDAO carDao = context.getCarDAO();
        Car car = carDao.getCar(carId);
        if (car != null && car.getPhoto() != null && car.getPhoto().getId() > 0) {
            return FileHelper.getFileStreamResult(context.getFileDAO(), car.getPhoto().getId());
        } else {
            return FileHelper.getPublicFile(Paths.get("images", "no-photo-car.jpg").toString(), "image/jpeg");
        }
    }

    /**
     * @return A form to create a new car (only available to car_owner+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result newCar() {
        return ok(views.html.cars.edit.render(Form.form(CarModel.class), null, getCountryList(), getFuelList()));
    }

    /**
     * Method: POST
     *
     * @return redirect to the CarForm you just filled in or to the cars-index page (only available to car_owner+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result addNewCar() {
        Form<CarModel> carForm = Form.form(CarModel.class).bindFromRequest();
        if (carForm.hasErrors()) {
            return badRequest(views.html.cars.edit.render(carForm, null, getCountryList(), getFuelList()));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            CarDAO dao = context.getCarDAO();
            User user = DataProvider.getUserProvider().getUser();
            CarModel model = carForm.get();
            AddressDAO adao = context.getAddressDAO();
            Address address = modifyAddress(model.address, null, adao);

            User owner = user;
            if (DataProvider.getUserRoleProvider().hasRole(user, UserRole.SUPER_USER)
                    || DataProvider.getUserRoleProvider().hasRole(user, UserRole.CAR_ADMIN)) {
                // User is permitted to add cars for other users
                owner = context.getUserDAO().getUser(model.userId, false);
            }
            TechnicalCarDetails technicalCarDetails = null;
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart registrationFile = body.getFile("file");
            Http.MultipartFormData.FilePart photoFilePart = body.getFile("picture");
            File file = null;
            File picture = null;
            if (registrationFile != null) {
                String contentType = registrationFile.getContentType();
                if (!FileHelper.isDocumentContentType(contentType)) {
                    flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                    return badRequest(edit.render(carForm, null, getCountryList(), getFuelList()));
                } else {
                    try {
                        Path relativePath = FileHelper.saveFile(registrationFile, ConfigurationHelper.getConfigurationString("uploads.carregistrations"));
                        FileDAO fdao = context.getFileDAO();
                        file = fdao.createFile(relativePath.toString(), registrationFile.getFilename(), registrationFile.getContentType(), null);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex); //no more checked catch -> error page!
                    }
                }
            }
            if (photoFilePart != null) {
                String contentType = photoFilePart.getContentType();
                if (!FileHelper.isImageContentType(contentType)) {
                    flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                    return badRequest(edit.render(carForm, null, getCountryList(), getFuelList()));
                } else {
                    try {
                        Path relativePath = FileHelper.saveFile(photoFilePart, ConfigurationHelper.getConfigurationString("uploads.carphotos"));
                        FileDAO fdao = context.getFileDAO();
                        picture = fdao.createFile(relativePath.toString(), photoFilePart.getFilename(), photoFilePart.getContentType(), null);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex); //no more checked catch -> error page!
                    }
                }
            }
            if ((model.licensePlate != null && !model.licensePlate.equals(""))
                    || (model.chassisNumber != null && !model.chassisNumber.isEmpty()) || file != null) {
                technicalCarDetails = new TechnicalCarDetails(model.licensePlate, file, model.chassisNumber);
            }
            CarInsurance insurance = null;
            if ((model.insuranceName != null && !model.insuranceName.equals("")) || (model.expiration != null || (model.polisNr != null && model.polisNr != 0))
                    || (model.bonusMalus != null && model.bonusMalus != 0)) {
                insurance = new CarInsurance(model.insuranceName, model.expiration, model.bonusMalus, model.polisNr);
            }
            Car car = dao.createCar(model.name, model.brand, model.type, address, model.seats, model.doors,
                    model.year, model.manual, model.gps, model.hook, CarFuel.getFuelFromString(model.fuel), model.fuelEconomy, model.estimatedValue,
                    model.ownerAnnualKm, technicalCarDetails, insurance, owner, model.comments, model.active, picture);


            if (car != null) {
                return redirect(
                        routes.Cars.showCars()
                );
            } else {
                carForm.error("Failed to add the car to the database. Contact administrator.");
                return badRequest(edit.render(carForm, null, getCountryList(), getFuelList()));
            }
        }
    }

    /**
     * @param carId The car to edit
     * @return A form to edit the car (only available to the corresponding car owner or administrator)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result editCar(int carId) {
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        Car car = dao.getCar(carId);

        if (car == null) {
            flash("danger", "Auto met ID=" + carId + " bestaat niet.");
            return badRequest(userCarList());
        } else {
            User currentUser = DataProvider.getUserProvider().getUser();
            if (!(car.getOwner().getId() == currentUser.getId() || DataProvider.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.CAR_ADMIN))) {
                flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
                return badRequest(userCarList());
            }

            CarModel model = new CarModel();
            model.populate(car);

            Form<CarModel> editForm = Form.form(CarModel.class).fill(model);
            return ok(edit.render(editForm, car, getCountryList(), getFuelList()));
        }
    }

    /**
     * Method: POST
     *
     * @param carId The car to edit
     * @return Redirect to the car-index page on error or the car detail-page on succes (only available to the corresponding car owner or administrator)
     */

    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result editCarPost(int carId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarDAO dao = context.getCarDAO();
        Car car = dao.getCar(carId);

        Form<CarModel> editForm = Form.form(CarModel.class).bindFromRequest();
        if (editForm.hasErrors())
            return badRequest(edit.render(editForm, car, getCountryList(), getFuelList()));

        if (car == null) {
            flash("danger", "Car met ID=" + carId + " bestaat niet.");
            return badRequest(userCarList());
        }

        User currentUser = DataProvider.getUserProvider().getUser();
        if (!(car.getOwner().getId() == currentUser.getId() || DataProvider.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.RESERVATION_ADMIN))) {
            flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
            return badRequest(userCarList());
        }

        CarModel model = editForm.get();
        car.setName(model.name);
        car.setBrand(model.brand);
        car.setType(model.type);
        if (model.doors != null)
            car.setDoors(model.doors);
        else
            car.setDoors(null);
        if (model.seats != null)
            car.setSeats(model.seats);
        else
            car.setSeats((null));
        car.setManual(model.manual);
        car.setGps(model.gps);
        car.setHook(model.hook);
        car.setFuel(CarFuel.getFuelFromString(model.fuel));
        if (model.year != null)
            car.setYear(model.year);
        else
            car.setYear(null);
        if (model.fuelEconomy != null)
            car.setFuelEconomy(model.fuelEconomy);
        else
            car.setFuelEconomy(null);
        if (model.estimatedValue != null)
            car.setEstimatedValue(model.estimatedValue);
        else
            car.setEstimatedValue(null);
        if (model.ownerAnnualKm != null)
            car.setOwnerAnnualKm(model.ownerAnnualKm);
        else
            car.setOwnerAnnualKm(null);
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart registrationFile = body.getFile("file");
        Http.MultipartFormData.FilePart photoFilePart = body.getFile("picture");
        File file = null;
        File picture = null;
        if (registrationFile != null) {
            String contentType = registrationFile.getContentType();
            if (!FileHelper.isDocumentContentType(contentType)) {
                flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                return redirect(routes.Cars.detail(car.getId()));
            } else {
                try {
                    Path relativePath = FileHelper.saveFile(registrationFile, ConfigurationHelper.getConfigurationString("uploads.carregistrations"));
                    FileDAO fdao = context.getFileDAO();
                    file = fdao.createFile(relativePath.toString(), registrationFile.getFilename(), registrationFile.getContentType(), null);
                } catch (IOException ex) {
                    throw new RuntimeException(ex); //no more checked catch -> error page!
                }
            }
        }

        if (photoFilePart != null) {
            String contentType = photoFilePart.getContentType();
            if (!FileHelper.isImageContentType(contentType)) {
                flash("danger", "Verkeerd bestandstype opgegeven. Enkel afbeeldingen zijn toegelaten als foto. (ontvangen MIME-type: " + contentType + ")");
                return redirect(routes.Cars.detail(car.getId()));
            } else {
                try {
                    Path relativePath = FileHelper.saveFile(photoFilePart, ConfigurationHelper.getConfigurationString("uploads.carphotos"));
                    FileDAO fdao = context.getFileDAO();
                    picture = fdao.createFile(relativePath.toString(), photoFilePart.getFilename(), photoFilePart.getContentType(), null);
                } catch (IOException ex) {
                    throw new RuntimeException(ex); //no more checked catch -> error page!
                }
            }
        }
        if (car.getTechnicalCarDetails() == null) {
            if ((model.licensePlate != null && !model.licensePlate.equals(""))
                    || (model.chassisNumber != null && !model.chassisNumber.isEmpty()) || file != null)
                car.setTechnicalCarDetails(new TechnicalCarDetails(model.licensePlate, file, model.chassisNumber));
        } else {
            if (model.licensePlate != null && !model.licensePlate.equals(""))
                car.getTechnicalCarDetails().setLicensePlate(model.licensePlate);
            else
                car.getTechnicalCarDetails().setLicensePlate(null);

            car.getTechnicalCarDetails().setRegistration(null);
            if (model.chassisNumber != null && !model.chassisNumber.isEmpty())
                car.getTechnicalCarDetails().setChassisNumber(model.chassisNumber);
            else
                car.getTechnicalCarDetails().setChassisNumber(null);
            if (file != null)
                car.getTechnicalCarDetails().setRegistration(file);
        }
        if (car.getInsurance() == null) {
            if (model.insuranceName != null && !model.insuranceName.equals("") || (model.expiration != null) || (model.bonusMalus != null && model.bonusMalus != 0)
                    || (model.polisNr != null && model.polisNr != 0))
                car.setInsurance(new CarInsurance(model.insuranceName, model.expiration, model.bonusMalus, model.polisNr));
        } else {
            if (model.insuranceName != null && !model.insuranceName.equals(""))
                car.getInsurance().setName(model.insuranceName);
            else
                car.getInsurance().setName(null);
            if (model.expiration != null)
                car.getInsurance().setExpiration(model.expiration);
            else
                car.getInsurance().setExpiration(null);
            if (model.bonusMalus != null && model.bonusMalus != 0)
                car.getInsurance().setBonusMalus(model.bonusMalus);
            else
                car.getInsurance().setBonusMalus(null);
            if (model.polisNr != null && model.polisNr != 0)
                car.getInsurance().setPolisNr(model.polisNr);
            else
                car.getInsurance().setPolisNr(null);
        }
        AddressDAO adao = context.getAddressDAO();
        Address address = car.getLocation();
        car.setLocation(modifyAddress(model.address, address, adao));

        car.setComments(model.comments);

        car.setActive(model.active);
        if (picture != null) {
            car.setPhoto(picture);
        }

        User user = DataProvider.getUserProvider().getUser();
        if (DataProvider.getUserRoleProvider().hasRole(user, UserRole.SUPER_USER)
                || DataProvider.getUserRoleProvider().hasRole(user, UserRole.CAR_ADMIN)) {
            // User is permitted to add cars for other users
            car.setOwner(context.getUserDAO().getUser(model.userId, false));
        }

        dao.updateCar(car);

        flash("success", "Jouw wijzigingen werden succesvol toegepast.");
        return redirect(routes.Cars.detail(car.getId()));
    }

    /**
     * Method: POST
     *
     * @return redirect to the car detailPage
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result updateAvailabilities(int carId, String valuesString) {
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        Car car = dao.getCar(carId);

        if (car == null) {
            flash("danger", "Car met ID=" + carId + " bestaat niet.");
            return badRequest(userCarList());
        }

        User currentUser = DataProvider.getUserProvider().getUser();
        if (!(car.getOwner().getId() == currentUser.getId() || DataProvider.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.CAR_ADMIN))) {
            flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
            return badRequest(userCarList());
        }

        String[] values = valuesString.split(";");

        List<CarAvailabilityInterval> availabilitiesToAddOrUpdate = new ArrayList<>();
        List<CarAvailabilityInterval> availabilitiesToDelete = new ArrayList<>();

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
                    availabilitiesToAddOrUpdate.add(new CarAvailabilityInterval(beginDay, beginTime, endDay, endTime));
                } else if (id > 0) { // update
                    availabilitiesToAddOrUpdate.add(new CarAvailabilityInterval(id, beginDay, beginTime, endDay, endTime));
                } else { // delete
                    availabilitiesToDelete.add(new CarAvailabilityInterval(-id, beginDay, beginTime, endDay, endTime));
                }
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                flash("error", "Er is een fout gebeurd bij het doorgeven van de beschikbaarheidswaarden.");
                return redirect(routes.Cars.detail(carId));
            }
        }

        boolean autoMerge = mergeOverlappingAvailabilities(availabilitiesToAddOrUpdate, availabilitiesToDelete);

        dao.addOrUpdateAvailabilities(car, availabilitiesToAddOrUpdate);
        dao.deleteAvailabilties(availabilitiesToDelete);

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
        if (day1 < day2 || (day1 == day2 && (time1.isBefore(time2) || (orEqual && time1.equals(time2)))))
            return true;
        return false;
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

    /**
     * Method: POST
     *
     * @return redirect to the car detailPage
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result updatePrivileged(int carId, String valuesString) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarDAO dao = context.getCarDAO();
        Car car = dao.getCar(carId);

        if (car == null) {
            flash("danger", "Car met ID=" + carId + " bestaat niet.");
            return badRequest(userCarList());
        }

        User currentUser = DataProvider.getUserProvider().getUser();
        if (!(car.getOwner().getId() == currentUser.getId() || DataProvider.getUserRoleProvider().hasRole(currentUser.getId(), UserRole.CAR_ADMIN))) {
            flash("danger", "Je heeft geen rechten tot het bewerken van deze wagen.");
            return badRequest(userCarList());
        }

        String[] values = valuesString.split(";");

        List<User> privileged = car.getPrivileged();

        List<User> usersToAdd = new ArrayList<>();
        List<User> usersToDelete = new ArrayList<>();

        for (String value : values) {
            try {
                int id = Integer.parseInt(value);
                User user;
                if (id > 0) { // create
                    user = context.getUserDAO().getUser(id, false);
                    if (!userInList(id, privileged))
                        usersToAdd.add(user);
                } else { // delete
                    user = context.getUserDAO().getUser(-1 * id, false);
                    usersToDelete.add(user);
                }
                if (user == null) {
                    flash("error", "De opgegeven gebruiker bestaat niet.");
                    return redirect(routes.Cars.detail(carId));
                }
            } catch (NumberFormatException e) {
                flash("error", "Er is een fout gebeurd bij het doorgeven van de geprivilegieerden.");
                return redirect(routes.Cars.detail(carId));
            }
        }

        dao.addPrivileged(car, usersToAdd);
        dao.deletePrivileged(car, usersToDelete);

        flash("success", "Je wijzigingen werden succesvol toegepast.");
        return redirect(routes.Cars.detail(car.getId()));
    }

    private static boolean userInList(int userId, List<User> users) {
        for (User u : users) {
            if (u.getId() == userId) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param carId The car to show details of
     * @return A detail page of the car (only available to car_user+)
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN, UserRole.CAR_ADMIN})
    @InjectContext
    public static F.Promise<Result> detail(int carId) {

        // TODO: why is this a Promise
        // TODO: does this still work with injection?
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        final Car car = dao.getCar(carId);

        if (car == null) {
            flash("danger", "Auto met ID=" + carId + " bestaat niet.");
            return F.Promise.promise(new F.Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    return badRequest(userCarList());
                }
            });
        } else {
            if (DataProvider.getSettingProvider().getBoolOrDefault("show_maps", true)) {
                return Maps.getLatLongPromise(car.getLocation().getId()).map(
                        new F.Function<F.Tuple<Double, Double>, Result>() {
                            public Result apply(F.Tuple<Double, Double> coordinates) {
                                return ok(detail.render(car, coordinates == null ? null : new Maps.MapDetails(coordinates._1, coordinates._2, 14)));
                            }
                        }
                );
            } else {
                return F.Promise.promise(new F.Function0<Result>() {
                    @Override
                    public Result apply() throws Throwable {
                        return ok(detail.render(car, null));
                    }
                });
            }
        }
    }

/**
 * *********************
 * Car costs       *
 * **********************
 */

public static class CarCostModel {

    public String description;
    public BigDecimal amount;
    public BigDecimal mileage;
    public DateTime time;


    public String validate() {
        if ("".equals(description))
            return "Geef aub een beschrijving op.";
        return null;
    }

}


    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result getCarCostModal(int id) {
        // TODO: hide from other users (badRequest)

        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        Car car = dao.getCar(id);
        if (car == null) {
            return badRequest("Fail."); //TODO: error in flashes?
        } else {
            return ok(addcarcostmodal.render(Form.form(CarCostModel.class), car));
        }
    }

    /**
     * Method: POST
     *
     * @return redirect to the CarCostForm you just filled in or to the car-detail page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    @InjectContext
    public static Result addNewCarCost(int carId) {
        Form<CarCostModel> carCostForm = Form.form(CarCostModel.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        if (carCostForm.hasErrors()) {
            CarDAO dao = context.getCarDAO();
            Car car = dao.getCar(carId);
            flash("danger", "Kost toevoegen mislukt.");
            return redirect(routes.Cars.detail(carId));

        } else {
            CarCostDAO dao = context.getCarCostDAO();
            CarCostModel model = carCostForm.get();
            CarDAO cardao = context.getCarDAO();
            Car car = cardao.getCar(carId);
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart proof = body.getFile("picture");
            if (proof != null) {
                String contentType = proof.getContentType();
                if (!FileHelper.isDocumentContentType(contentType)) {
                    flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                    return redirect(routes.Cars.detail(carId));
                } else {
                    try {
                        Path relativePath = FileHelper.saveFile(proof, ConfigurationHelper.getConfigurationString("uploads.carboundproofs"));
                        FileDAO fdao = context.getFileDAO();
                        try {
                            File file = fdao.createFile(relativePath.toString(), proof.getFilename(), proof.getContentType(), null);
                            CarCost carCost = dao.createCarCost(car, model.amount, model.mileage, model.description, model.time, file.getId());
                            if (carCost == null) {
                                flash("danger", "Failed to add the carcost to the database. Contact administrator.");
                                return redirect(routes.Cars.detail(carId));
                            }
                            Notifier.sendCarCostRequest(carCost);
                            flash("success", "Je autokost werd toegevoegd.");
                            return redirect(routes.Cars.detail(carId));
                        } catch (DataAccessException ex) {
                            FileHelper.deleteFile(relativePath);
                            throw ex;
                        }

                    } catch (IOException ex) {
                        throw new RuntimeException(ex); //no more checked catch -> error page!
                    }
                }
            } else {
                flash("error", "Missing file");
                return redirect(routes.Application.index());
            }
        }
    }


    /**
     * Method: GET
     *
     * @return index page containing all the carcost requests
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showCarCosts() {
        return ok(carCostsAdmin.render());
    }

    @InjectContext
    public static Result showCarCostsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        // Check if admin or car owner
        User user = DataProvider.getUserProvider().getUser();
        UserRoleProvider userRoleProvider = DataProvider.getUserRoleProvider();
        if (!userRoleProvider.hasRole(user, UserRole.CAR_ADMIN) || !userRoleProvider.hasRole(user, UserRole.SUPER_USER)) {
            String carIdString = filter.getValue(FilterField.CAR_ID);
            int carId;
            if (carIdString.equals("")) {
                carId = -1;
            } else {
                carId = Integer.parseInt(carIdString);
            }
            CarDAO carDAO = DataAccess.getInjectedContext().getCarDAO();
            List<Car> listOfCars = carDAO.getCarsOfUser(user.getId());
            // Check if carId in cars
            boolean isCarOfUser = false;
            for (Car c : listOfCars) {
                if (c.getId() == carId) {
                    isCarOfUser = true;
                    break;
                }
            }
            if (!isCarOfUser) {
                flash("danger", "Je bent niet de eigenaar van deze auto.");
                return badRequest(userCarList());
            }

        }

        return ok(carCostList(page, pageSize, field, asc, filter));

    }

    // used in injected context
    private static Html carCostList(int page, int pageSize, FilterField orderBy, boolean asc, Filter filter) {
        CarCostDAO dao = DataAccess.getInjectedContext().getCarCostDAO();

        if (orderBy == null) {
            orderBy = FilterField.CAR_COST_DATE;
        }

        List<CarCost> listOfResults = dao.getCarCostList(orderBy, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfCarCosts(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return carCostspage.render(listOfResults, page, amountOfResults, amountOfPages);
    }

    /**
     * Method: GET
     * <p>
     * Called when a car-bound cost of a car is approved by the car admin.
     *
     * @param carCostId The carCost being approved
     * @return the carcost index page if returnToDetail is 0, car detail page if 1.
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result approveCarCost(int carCostId, int returnToDetail) {
        CarCostDAO dao = DataAccess.getInjectedContext().getCarCostDAO();
        CarCost carCost = dao.getCarCost(carCostId);
        carCost.setStatus(CarCostStatus.ACCEPTED);
        dao.updateCarCost(carCost);
        Notifier.sendCarCostStatusChanged(carCost.getCar().getOwner(), carCost, true);

        flash("success", "Autokost succesvol geaccepteerd");
        if (returnToDetail == 0) {
            return redirect(routes.Cars.showCarCosts());
        } else {
            return redirect(routes.Cars.detail(carCost.getCar().getId()));
        }


    }

    /**
     * Method: GET
     * <p>
     * Called when a car-bound cost of a car is approved by the car admin.
     *
     * @param carCostId The carCost being approved
     * @return the carcost index page
     */
    @RoleSecured.RoleAuthenticated({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result refuseCarCost(int carCostId, int returnToDetail) {
        CarCostDAO dao = DataAccess.getInjectedContext().getCarCostDAO();
        CarCost carCost = dao.getCarCost(carCostId);
        carCost.setStatus(CarCostStatus.REFUSED);
        dao.updateCarCost(carCost);
        Notifier.sendCarCostStatusChanged(carCost.getCar().getOwner(), carCost, false);
        if (returnToDetail == 0) {
            flash("success", "Autokost succesvol geweigerd");
            return redirect(routes.Cars.showCarCosts());
        } else {
            flash("success", "Autokost succesvol geweigerd");
            return redirect(routes.Cars.detail(carCost.getCar().getId()));
        }
    }

    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result getProof(int proofId) {
        return FileHelper.getFileStreamResult(DataAccess.getInjectedContext().getFileDAO(), proofId);
    }

}
