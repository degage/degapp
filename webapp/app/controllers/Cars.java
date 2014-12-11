package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import controllers.util.Addresses;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;
import views.html.cars.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
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

    // TODO: extend form UserPickerData
    public static class CarModel {

        public Integer userId;

        @Constraints.Required
        public String userIdAsString;

        @Constraints.Required
        public String name;
        @Constraints.Required
        public String brand;
        @Constraints.Required
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
        public LocalDate expiration;
        public Integer bonusMalus;
        public Integer polisNr;

        public Addresses.EditAddressModel address = new Addresses.EditAddressModel();

        public void populate(Car car) {
            if (car == null) {
                return;
            }

            userId = car.getOwner().getId();
            userIdAsString = car.getOwner().getFullName();

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
                if (car.getInsurance().getExpiration() != null) {
                    expiration = car.getInsurance().getExpiration();
                }
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
            /* TODO: dit moeten Field Errors worden, en niet één global error */
            String error = "";
            if (userId == null || userId == 0) {
                error += "Geef een eigenaar op. ";
            }
            if (address.isEmpty()) {
                error += "Geef het adres op.";
            }
            if (name.length() <= 0) {
                error += "Geef de autonaam op. ";
            }
            if (brand.length() <= 0) {
                error += "Geef het automerk op. ";
            }
            if (seats == null || seats < 2) {
                error += "Een auto heeft minstens 2 zitplaatsen. ";
            }
            if (doors == null || doors < 2) {
                error += "Een auto heeft minstens 2 deuren. ";
            }

            if ("".equals(error)) {
                return null;
            } else {
                return error;
            }
        }
    }

    /**
     * @return The cars index-page with all cars (only available to car_user+)
     */
    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showCars() {
        return ok(views.html.cars.carsAdmin.render());
    }

    /**
     * @return The cars index-page with user cars (only available to car_owners)
     */
    @AllowRoles({UserRole.CAR_OWNER})
    @InjectContext
    public static Result showUserCars() {
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        Iterable<Car> listOfCars = dao.listCarsOfUser(CurrentUser.getId());

        return ok(cars.render(listOfCars));
    }

    /**
     * @param page         The page in the carlists
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string witth form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of cars of the corresponding page (only available to car_user+)
     */
    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showCarsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField carField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();

        if (carField == null) {
            carField = FilterField.CAR_NAME;
        }
        Iterable<Car> listOfCars = dao.listCars(carField, asc, page, pageSize, filter);

        int numberOfResults = dao.countCars(filter);
        int numberOfPages = (int) Math.ceil(numberOfResults / (double) pageSize);

        return ok(views.html.cars.carspage.render(listOfCars, page, numberOfResults, numberOfPages));
    }

    /**
     * Gets the picture for given car Id, or default one if missing
     *
     * @param carId The car for which the image is requested
     * @return The image with correct content type
     */
    @AllowRoles
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
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result newCar() {
        CarModel model = new CarModel();
        model.userId = CurrentUser.getId();
        model.userIdAsString = CurrentUser.getFullName();
        return ok(views.html.cars.add.render(
                Form.form(CarModel.class).fill(model),
                getCountryList(), getFuelList()));
    }

    /**
     * Method: POST
     *
     * @return redirect to the CarForm you just filled in or to the cars-index page (only available to car_owner+)
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result addNewCar() {
        Form<CarModel> carForm = Form.form(CarModel.class).bindFromRequest();
        if (carForm.hasErrors()) {
            return badRequest(views.html.cars.add.render(carForm, getCountryList(), getFuelList()));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            CarDAO dao = context.getCarDAO();
            CarModel model = carForm.get();
            AddressDAO adao = context.getAddressDAO();
            Address address = modifyAddress(model.address, null, adao);

            UserHeader owner;
            if (CurrentUser.hasRole(UserRole.CAR_ADMIN)) {
                // User is permitted to add cars for other users
                owner = context.getUserDAO().getUserHeader(model.userId);
            } else {
                owner = context.getUserDAO().getUserHeader(CurrentUser.getId()); // TODO: can this be avoided?
            }
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart registrationFile = body.getFile("file");
            Http.MultipartFormData.FilePart photoFilePart = body.getFile("picture");
            File registrationPictureFile = null;
            File carPictureFile = null;
            if (registrationFile != null) {
                String contentType = registrationFile.getContentType();
                if (!FileHelper.isDocumentContentType(contentType)) {
                    flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                    return badRequest(add.render(carForm, getCountryList(), getFuelList()));
                } else {
                    try {
                        Path relativePath = FileHelper.saveFile(registrationFile, ConfigurationHelper.getConfigurationString("uploads.carregistrations"));
                        FileDAO fdao = context.getFileDAO();
                        registrationPictureFile = fdao.createFile(relativePath.toString(), registrationFile.getFilename(), registrationFile.getContentType());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex); //no more checked catch -> error page!
                    }
                }
            }
            if (photoFilePart != null) {
                String contentType = photoFilePart.getContentType();
                if (!FileHelper.isImageContentType(contentType)) {
                    flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                    return badRequest(add.render(carForm, getCountryList(), getFuelList()));
                } else {
                    try {
                        Path relativePath = FileHelper.saveFile(photoFilePart, ConfigurationHelper.getConfigurationString("uploads.carphotos"));
                        FileDAO fdao = context.getFileDAO();
                        carPictureFile = fdao.createFile(relativePath.toString(), photoFilePart.getFilename(), photoFilePart.getContentType());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex); //no more checked catch -> error page!
                    }
                }
            }

            TechnicalCarDetails technicalCarDetails = new TechnicalCarDetails(model.licensePlate, registrationPictureFile, model.chassisNumber);
            CarInsurance insurance = new CarInsurance(model.insuranceName, model.expiration, model.bonusMalus, model.polisNr);

            Car car = dao.createCar(model.name, model.brand, model.type, address, model.seats, model.doors,
                    model.year, model.manual, model.gps, model.hook, CarFuel.getFuelFromString(model.fuel), model.fuelEconomy, model.estimatedValue,
                    model.ownerAnnualKm, technicalCarDetails, insurance, owner, model.comments, model.active, carPictureFile);


            if (car != null) {
                return redirect(routes.Cars.detail(car.getId()));
            } else {
                carForm.error("Failed to add the car to the database. Contact administrator.");
                // not needed?
                flash("danger", "unexpected error");
                return badRequest(add.render(carForm, getCountryList(), getFuelList()));
            }
        }
    }

    /**
     * @param carId The car to edit
     * @return A form to edit the car (only available to the corresponding car owner or administrator)
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result editCar(int carId) {
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        Car car = dao.getCar(carId);

        if (car == null) {
            flash("danger", "Auto met ID=" + carId + " bestaat niet.");
            return badRequest();
        } else {
            if (CurrentUser.is(car.getOwner().getId()) || CurrentUser.hasRole(UserRole.CAR_ADMIN)) {

                CarModel model = new CarModel();
                model.populate(car);

                Form<CarModel> editForm = Form.form(CarModel.class).fill(model);
                return ok(edit.render(editForm, car, getCountryList(), getFuelList()));
            } else {
                flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
                return badRequest();  // TODO: redirect
            }
        }
    }

    /**
     * Method: POST
     *
     * @param carId The car to edit
     * @return Redirect to the car-index page on error or the car detail-page on succes (only available to the corresponding car owner or administrator)
     */

    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result editCarPost(int carId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarDAO dao = context.getCarDAO();
        Car car = dao.getCar(carId);
        // TODO: only needed here: id, owner id and file numbers for photo's
        // but first: remove all 'badrequests' and replace by redirects

        Form<CarModel> editForm = Form.form(CarModel.class).bindFromRequest();
        if (editForm.hasErrors()) {
            // niet nodig?
            flash("danger", "Form has errors");
            return badRequest(edit.render(editForm, car, getCountryList(), getFuelList()));
        }
        if (car == null) {
            flash("danger", "Car met ID=" + carId + " bestaat niet.");
            return badRequest();
        }

        if (CurrentUser.isNot(car.getOwner().getId()) && !CurrentUser.hasRole(UserRole.RESERVATION_ADMIN)) {
            flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
            return badRequest();
        }

        CarModel model = editForm.get();
        car.setName(model.name);
        car.setBrand(model.brand);
        car.setType(model.type);
        car.setDoors(model.doors);
        car.setSeats(model.seats);
        car.setManual(model.manual);
        car.setGps(model.gps);
        car.setHook(model.hook);
        car.setFuel(CarFuel.getFuelFromString(model.fuel));
        car.setYear(model.year);
        car.setFuelEconomy(model.fuelEconomy);
        car.setEstimatedValue(model.estimatedValue);
        car.setOwnerAnnualKm(model.ownerAnnualKm);

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
                    file = fdao.createFile(relativePath.toString(), registrationFile.getFilename(), registrationFile.getContentType());
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
                    picture = fdao.createFile(relativePath.toString(), photoFilePart.getFilename(), photoFilePart.getContentType());
                } catch (IOException ex) {
                    throw new RuntimeException(ex); //no more checked catch -> error page!
                }
            }
        }


        TechnicalCarDetails technicalCarDetails = car.getTechnicalCarDetails();
        technicalCarDetails.setLicensePlate(model.licensePlate);
        technicalCarDetails.setChassisNumber(model.chassisNumber);
        if (file != null) {
            technicalCarDetails.setRegistration(file);
        }

        CarInsurance insurance = car.getInsurance();
        insurance.setName(model.insuranceName);
        insurance.setExpiration(model.expiration);
        insurance.setBonusMalus(model.bonusMalus);
        insurance.setPolisNr(model.polisNr);

        AddressDAO adao = context.getAddressDAO();
        Address address = car.getLocation();
        car.setLocation(modifyAddress(model.address, address, adao));

        car.setComments(model.comments);

        car.setActive(model.active);
        if (picture != null) {
            car.setPhoto(picture);
        }

        if (CurrentUser.hasRole(UserRole.CAR_ADMIN)) {
            // User is permitted to add cars for other users
            car.setOwner(context.getUserDAO().getUserHeader(model.userId));
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
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result updatePrivileged(int carId, String valuesString) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Car car = context.getCarDAO().getCar(carId);

        if (car == null) {
            flash("danger", "Car met ID=" + carId + " bestaat niet.");
            return badRequest();
        }

        if (CurrentUser.isNot(car.getOwner().getId()) && ! CurrentUser.hasRole(UserRole.CAR_ADMIN)) {
            flash("danger", "Je hebt geen rechten tot het bewerken van deze wagen.");
            return badRequest();
        }

        Collection<Integer> usersToAdd = new ArrayList<>();
        Collection<Integer> usersToDelete = new ArrayList<>();

        for (String value : valuesString.split(";")) {
            try {
                int id = Integer.parseInt(value);
                if (id > 0) {
                    usersToAdd.add(id);
                } else if (id < 0) { // delete
                    usersToDelete.add(-id);
                } else {
                    flash("error", "De opgegeven gebruiker bestaat niet.");
                    return redirect(routes.Cars.detail(carId));
                }
            } catch (NumberFormatException e) {
                flash("error", "Er is een fout gebeurd bij het doorgeven van de geprivilegieerden.");
                return redirect(routes.Cars.detail(carId));
            }
        }

        PrivilegedDAO pdao = context.getPrivilegedDAO();
        pdao.addPrivileged(carId, usersToAdd);
        pdao.deletePrivileged(carId, usersToDelete);

        flash("success", "Je wijzigingen werden succesvol toegepast.");
        return redirect(routes.Cars.detail(car.getId()));
    }

    /**
     * @param carId The car to show details of
     * @return A detail page of the car (only available to car_user+)
     */
    /*   // TODO: make maps work again
    @RoleSecured.RoleAuthenticated({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN, UserRole.CAR_ADMIN})
    @InjectContext
    public static F.Promise<Result> detail(int carId) {

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
            if (DataProvider.getSettingProvider().getBooleanOrDefault("show_maps", true)) {
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
     */

    /**
     * @param carId The car to show details of
     * @return A detail page of the car (only available to car_user+)
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result detail(int carId) {

        DataAccessContext context = DataAccess.getInjectedContext();
        CarDAO dao = context.getCarDAO();
        AvailabilityDAO availabilityDAO = context.getAvailabilityDAO();
        Car car = dao.getCar(carId);

        Iterable<CarAvailabilityInterval> list = availabilityDAO.getAvailabilities(carId);

        return ok(detail.render(
                        car,
                        Availabilities.convertToView(list),
                        CarAvailabilityInterval.containsOverlap(list),
                        context.getPrivilegedDAO().getPrivileged(carId),
                        null)
        );
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
        public LocalDate time;

        public String validate() {
            if ("".equals(description)) {
                return "Geef aub een beschrijving op.";
            }
            return null;
        }

    }


    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
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
    @AllowRoles({UserRole.CAR_OWNER})
    @InjectContext
    public static Result addNewCarCost(int carId) {
        Form<CarCostModel> carCostForm = Form.form(CarCostModel.class).bindFromRequest();
        if (carCostForm.hasErrors()) {
            flash("danger", "Kost toevoegen mislukt.");
            return redirect(routes.Cars.detail(carId));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
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
                            File file = fdao.createFile(relativePath.toString(), proof.getFilename(), proof.getContentType());
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
    @AllowRoles({UserRole.CAR_ADMIN})
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
        if (!CurrentUser.hasRole(UserRole.CAR_ADMIN)) {
            String carIdString = filter.getValue(FilterField.CAR_ID);
            // TODO: not from filter??

            if (carIdString.equals("")) {
                return badRequest();
            }
            int carId = Integer.parseInt(carIdString);

            CarDAO carDAO = DataAccess.getInjectedContext().getCarDAO();
            if (!carDAO.isCarOfUser(carId, CurrentUser.getId())) {
                flash("danger", "Je bent niet de eigenaar van deze auto.");
                return badRequest();   // TODO: redirect
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
    @AllowRoles({UserRole.CAR_ADMIN})
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
    @AllowRoles({UserRole.CAR_ADMIN})
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

    @AllowRoles
    @InjectContext
    public static Result getProof(int proofId) {
        return FileHelper.getFileStreamResult(DataAccess.getInjectedContext().getFileDAO(), proofId);
    }

}
