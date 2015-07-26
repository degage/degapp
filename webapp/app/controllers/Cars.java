/* Cars.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.dao.PrivilegedDAO;
import be.ugent.degage.db.models.*;
import com.google.common.base.Strings;
import controllers.util.Addresses;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.api.data.validation.ValidationError;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.cars.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Controller responsible for creating, updating and showing of cars
 */
public class Cars extends Controller {

    // TODO: also in CostsCommon
    private static boolean isOwnerOrAdmin(CarHeaderShort car) {
        return CurrentUser.is(car.getOwnerId()) || CurrentUser.hasRole(UserRole.CAR_ADMIN);
    }


    public static class CarModel {

        @Constraints.Required
        public String name;

        // TODO: add email

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
        public String bonusMalus;
        public String polisNr;

        public Addresses.EditAddressModel address = new Addresses.EditAddressModel();

        public void populate(Car car) {
            if (car == null) {
                return;
            }

            name = car.getName();
            brand = car.getBrand();
            type = car.getType();
            seats = car.getSeats();
            doors = car.getDoors();
            year = car.getYear();
            manual = car.isManual();
            gps = car.isGps();
            hook = car.isHook();
            fuel = car.getFuel().name();
            fuelEconomy = car.getFuelEconomy();
            estimatedValue = car.getEstimatedValue();
            ownerAnnualKm = car.getOwnerAnnualKm();
            comments = car.getComments();
            active = car.isActive();

            TechnicalCarDetails technicalCarDetails = car.getTechnicalCarDetails();
            licensePlate = technicalCarDetails.getLicensePlate();
            chassisNumber = technicalCarDetails.getChassisNumber();

            CarInsurance insurance = car.getInsurance();
            insuranceName = insurance.getName();
            expiration = insurance.getExpiration();
            bonusMalus = insurance.getBonusMalus();
            polisNr = insurance.getPolisNr();

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
            if (Strings.isNullOrEmpty(address.street)) {
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

    // TODO: extend form UserPickerData
    public static class CarModelExtended extends CarModel {
        public Integer userId;

        @Constraints.Required
        public String userIdAsString;

        public void populate(Car car) {
            if (car == null) {
                return;
            }
            super.populate(car);
            userId = car.getOwner().getId();
            userIdAsString = car.getOwner().getFullName();
        }

        public String validate() {
            String error = Strings.nullToEmpty(super.validate());
            if (userId == null || userId == 0) {
                error += "Geef een eigenaar op. ";
            }
            if ("".equals(error)) {
                return null;
            } else {
                return error;
            }
        }
    }


    /**
     * @return The cars index-page with all cars (only available to car admin)
     */
    @AllowRoles({UserRole.CAR_ADMIN, UserRole.CAR_OWNER})
    @InjectContext
    public static Result showCars() {
        if (CurrentUser.hasRole(UserRole.CAR_ADMIN)) {
            return ok(views.html.cars.carsAdmin.render());
        } else {
            return ok(cars.render(DataAccess.getInjectedContext().getCarDAO().listCarsOfUser(CurrentUser.getId())));
        }
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
        Iterable<CarHeaderAndOwner> listOfCars = dao.listCarsAndOwners(carField, asc, page, pageSize, filter, false);

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
        int photoId = context.getCarDAO().getCarPicture(carId);
        if (photoId > 0) {
            return FileHelper.getFileStreamResult(context.getFileDAO(), photoId);
        } else {
            return FileHelper.getPublicFile("images/car.png", "image/png");
        }
    }

    /**
     * @return A form to create a new car (only available to car_owner+)
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result newCar() {
        CarModelExtended model = new CarModelExtended();
        model.userId = CurrentUser.getId();
        model.userIdAsString = CurrentUser.getFullName();
        return ok(views.html.cars.add.render(Form.form(CarModelExtended.class).fill(model)));
    }

    /**
     * Method: POST
     *
     * @return redirect to the CarForm you just filled in or to the cars-index page (only available to car_owner+)
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result addNewCar() {
        Form<CarModelExtended> carForm = Form.form(CarModelExtended.class).bindFromRequest();
        if (carForm.hasErrors()) {
            return badRequest(views.html.cars.add.render(carForm));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            CarDAO dao = context.getCarDAO();
            CarModelExtended model = carForm.get();

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
            int registrationPictureFileId = 0;
            if (registrationFile != null) {
                String contentType = registrationFile.getContentType();
                if (!FileHelper.isDocumentContentType(contentType)) {
                    flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                    return badRequest(add.render(carForm));
                } else {
                    try {
                        Path relativePath = FileHelper.saveFile(registrationFile, ConfigurationHelper.getConfigurationString("uploads.carregistrations"));
                        FileDAO fdao = context.getFileDAO();
                        registrationPictureFileId = fdao.createFile(
                                relativePath.toString(),
                                registrationFile.getFilename(),
                                registrationFile.getContentType()
                        ).getId();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex); //no more checked catch -> error page!
                    }
                }
            }

            TechnicalCarDetails technicalCarDetails =
                    new TechnicalCarDetails(model.licensePlate, registrationPictureFileId, model.chassisNumber);
            CarInsurance insurance =
                    new CarInsurance(model.insuranceName, model.expiration, model.bonusMalus, model.polisNr);

            // TODO: fill in real email address
            Car car = dao.createCar(
                    model.name, model.name.toLowerCase() + "@degage.be",
                    model.brand, model.type,
                    model.address.toAddress(), model.seats, model.doors,
                    model.year, model.manual, model.gps, model.hook,
                    CarFuel.valueOf(model.fuel), model.fuelEconomy, model.estimatedValue,
                    model.ownerAnnualKm, technicalCarDetails, insurance, owner,
                    model.comments, model.active
            );


            if (car != null) {
                return redirect(routes.Cars.detail(car.getId()));
            } else {
                carForm.error("Failed to add the car to the database. Contact administrator.");
                // not needed?
                flash("danger", "unexpected error");
                return badRequest(add.render(carForm));
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

        if (isOwnerOrAdmin(car)) {
            CarModel model = new CarModel();
            model.populate(car);
            Form<CarModel> editForm = Form.form(CarModel.class).fill(model);
            return ok(edit.render(editForm, car));
        } else {
            return badRequest();  // hacker!
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

        Form<CarModel> editForm = Form.form(CarModel.class).bindFromRequest();
        if (editForm.hasErrors()) {
            // niet nodig?
            flash("danger", "Form has errors");
            return badRequest(edit.render(editForm, car));
        }
        if (!isOwnerOrAdmin(car)) {
            return badRequest(); // hacker!
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
        car.setFuel(CarFuel.valueOf(model.fuel));
        car.setYear(model.year);
        car.setFuelEconomy(model.fuelEconomy);
        car.setEstimatedValue(model.estimatedValue);
        car.setOwnerAnnualKm(model.ownerAnnualKm);

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart registrationFile = body.getFile("file");
        Http.MultipartFormData.FilePart photoFilePart = body.getFile("picture");
        int fileId = 0;
        if (registrationFile != null) {
            String contentType = registrationFile.getContentType();
            if (!FileHelper.isDocumentContentType(contentType)) {
                flash("danger", "Verkeerd bestandstype opgegeven. Enkel documenten zijn toegelaten. (ontvangen MIME-type: " + contentType + ")");
                return redirect(routes.Cars.detail(car.getId()));
            } else {
                try {
                    // TODO: similar code occurs several times
                    Path relativePath = FileHelper.saveFile(registrationFile, ConfigurationHelper.getConfigurationString("uploads.carregistrations"));
                    FileDAO fdao = context.getFileDAO();
                    fileId = fdao.createFile(
                            relativePath.toString(), registrationFile.getFilename(), registrationFile.getContentType()
                    ).getId();
                } catch (IOException ex) {
                    throw new RuntimeException(ex); //no more checked catch -> error page!
                }
            }
        }

        TechnicalCarDetails technicalCarDetails = car.getTechnicalCarDetails();
        technicalCarDetails.setLicensePlate(model.licensePlate);
        technicalCarDetails.setChassisNumber(model.chassisNumber);
        if (fileId != 0) {
            technicalCarDetails.setRegistrationId(fileId);
        }

        CarInsurance insurance = car.getInsurance();
        insurance.setName(model.insuranceName);
        insurance.setExpiration(model.expiration);
        insurance.setBonusMalus(model.bonusMalus);
        insurance.setPolisNr(model.polisNr);

        car.setLocation(model.address.toAddress());
        car.setComments(model.comments);

        car.setActive(model.active);

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

        if (CurrentUser.isNot(car.getOwner().getId()) && !CurrentUser.hasRole(UserRole.CAR_ADMIN)) {
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
        Car car = dao.getCar(carId);
        return ok(detail.render(
                        car,
                        context.getPrivilegedDAO().getPrivileged(carId),
                        null)
        );
    }


    @AllowRoles
    @InjectContext
    public static Result getCarInfoModal(int id) {
        Car car = DataAccess.getInjectedContext().getCarDAO().getCar(id);
        return ok(carinfomodal.render(car));

    }


    @AllowRoles
    @InjectContext
    public static Result getRegistrationPicture(int carId) {
        // TODO: check authorization
        DataAccessContext context = DataAccess.getInjectedContext();
        Car car = context.getCarDAO().getCar(carId);
        if (isOwnerOrAdmin(car)) {
            TechnicalCarDetails details = car.getTechnicalCarDetails(); // TODO: no need to get the whole car for this
            return FileHelper.getFileStreamResult(context.getFileDAO(), details.getRegistrationId());
        } else {
            return badRequest(); // hacker!
        }
    }

    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result pictureUpload(int carId) {
        CarHeaderShort car = DataAccess.getInjectedContext().getCarDAO().getCarHeaderShort(carId);
        if (isOwnerOrAdmin(car)) {
            return ok(uploadPicture.render(carId, car.getName()));
        } else {
            return badRequest();
        }
    }

    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result doPictureUpload(int carId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarDAO dao = context.getCarDAO();
        if (isOwnerOrAdmin(dao.getCarHeaderShort(carId))) {
            File file = FileHelper.getFileFromRequest("picture", FileHelper.IMAGE_CONTENT_TYPES, "uploads.carphotos", 0);
            if (file == null) {
                flash("danger", "Je moet een bestand kiezen");
                return redirect(routes.Cars.pictureUpload(carId));
            } else if (file.getContentType() == null) {
                flash("danger", "Verkeerd bestandstype opgegeven. Enkel afbeeldingen zijn toegelaten.");
                return redirect(routes.Cars.pictureUpload(carId));
            } else {
                int oldPictureId = dao.getCarPicture(carId);
                dao.updateCarPicture(carId, file.getId());
                FileHelper.deleteOldFile(oldPictureId);
                return redirect(routes.Cars.detail(carId));
            }
        } else {
            return badRequest();
        }
    }

    public static class DepreciationData {
        @Constraints.Required
        public int cents;

        @Constraints.Required
        public int limit;

        public int last;

    }

    @AllowRoles({UserRole.CAR_OWNER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showDepreciation(int carId) {
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        CarHeaderShort car = dao.getCarHeaderShort(carId);
        if (isOwnerOrAdmin(car)) {
            CarDepreciation deprec = dao.getDepreciation(carId);
            if (deprec.getLastKm() == 0 && CurrentUser.hasRole(UserRole.CAR_ADMIN)) {
                DepreciationData data = new DepreciationData();
                data.cents = deprec.getCentsPerTenKilometer();
                data.limit = deprec.getLimit();
                Form<DepreciationData> form = Form.form(DepreciationData.class).fill(data);
                return ok(editdeprec.render(form, carId, car.getName()));
            } else {
                // cannot be edited
                return ok(depreciation.render(carId, car.getName(), deprec));
            }
        } else {
            return badRequest();
        }
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result doEditDepreciation(int carId) {
        Form<DepreciationData> form = Form.form(DepreciationData.class).bindFromRequest();
        CarDAO dao = DataAccess.getInjectedContext().getCarDAO();
        if (form.hasErrors()) {
            CarHeaderShort car = dao.getCarHeaderShort(carId);
            return ok(editdeprec.render(form, carId, car.getName()));
        } else {
            DepreciationData data = form.get();
            dao.updateDepreciation(carId, data.cents, data.limit, data.last);
            return redirect(routes.Cars.showDepreciation(carId));
        }
    }
}
