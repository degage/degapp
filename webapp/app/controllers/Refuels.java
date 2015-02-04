/* Refuels.java
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
import be.ugent.degage.db.dao.RefuelDAO;
import be.ugent.degage.db.models.*;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import data.EurocentAmount;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.refuels.*;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class Refuels extends Controller {

    public static class RefuelData {

        @Constraints.Required
        public EurocentAmount amount;

        public String picture; // only used to enable field error messages

        @Constraints.Required
        public String fuelAmount;

        @Constraints.Required
        @Constraints.Min(value=1, message="Ongeldige kilometerstand")
        public int km;

        public RefuelData populate(EurocentAmount amount, String fuelAmount, int km) {
            this.amount = amount;
            this.fuelAmount = fuelAmount;
            this.km = km;
            return this;
        }

        public List<ValidationError> validate () {
            if (amount.getValue() <= 0) {
                return Arrays.asList(new ValidationError("amount", "Bedrag moet groter zijn dan 0"));
            } else {
                return null;
            }
        }
    }


    /**
     * Dispatches to the correct refuels page
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result showRefuels() {
        if (CurrentUser.hasRole(UserRole.CAR_ADMIN)) {
            return ok(refuelsAdmin.render());
        } else if (CurrentUser.hasRole(UserRole.CAR_OWNER)) {
            return ok(refuelsOwner.render());
        } else {
            return ok(refuels.render());
        }
    }

    @InjectContext
    public static Result showUserRefuelsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?

        // currently not used
        //FilterField field = FilterField.stringToField(orderBy);
        //boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        User user = DataProvider.getUserProvider().getUser();
        filter.putValue(FilterField.REFUEL_USER_ID, user.getId() + "");

        // TODO: Check if admin or car owner/user

        return ok(refuelList(page, pageSize, filter));

    }

    @InjectContext
    public static Result showOwnerRefuelsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?

        // currently not used
        //FilterField field = FilterField.stringToField(orderBy);
        //boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        User user = DataProvider.getUserProvider().getUser();
        filter.putValue(FilterField.REFUEL_OWNER_ID, user.getId() + "");

        // TODO: Check if admin or car owner/user

        return ok(refuelList(page, pageSize, filter));
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showAllRefuelsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        // Currently not used
        //FilterField field = FilterField.stringToField(orderBy);
        //boolean asc = Pagination.parseBoolean(ascInt);

        Filter filter = Pagination.parseFilter(searchString);

        //filter.putValue(FilterField.REFUEL_NOT_STATUS, RefuelStatus.CREATED.toString());

        return ok(refuelList(page, pageSize, filter));
    }

    // should be used with an injected context only
    private static Html refuelList(int page, int pageSize, Filter filter) {
        RefuelDAO dao = DataAccess.getInjectedContext().getRefuelDAO();
        int amountOfResults = dao.getAmountOfRefuels(filter);
        return refuelspage.render(
                dao.getRefuels(page, pageSize, filter),
                page,
                amountOfResults,
                (amountOfResults + pageSize - 1) / pageSize);
    }

    /**
     * Method: GET
     *
     * @return proof url
     */
    @AllowRoles
    @InjectContext
    public static Result getProof(int refuelId) {

        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationHeader reservation = context.getReservationDAO().getReservationHeaderForRefuel(refuelId);
        if (Drives.isDriverOrOwnerOrAdmin(reservation)) {
            Refuel refuel = context.getRefuelDAO().getRefuel(refuelId);
            return FileHelper.getFileStreamResult(context.getFileDAO(), refuel.getProofId());
        } else {
            return badRequest(); // hacker
        }
    }

    /**
     * Get the number of refuels having the provided status
     *
     * @param status The status
     * @return The number of refuels
     */
    // should only be used with injected context
    public static int refuelsWithStatus(RefuelStatus status) {
        User user = DataProvider.getUserProvider().getUser();
        RefuelDAO dao = DataAccess.getInjectedContext().getRefuelDAO();
        return dao.getAmountOfRefuelsWithStatus(status, user.getId());
    }

    /**
     * Show all refuels connected with a given ride
     */
    @AllowRoles({UserRole.CAR_USER})
    @InjectContext
    public static Result showRefuelsForRide(int reservationId) {

        DataAccessContext context = DataAccess.getInjectedContext();
        Reservation reservation = context.getReservationDAO().getReservation(reservationId);
        Iterable<Refuel> refuels = context.getRefuelDAO().getRefuelsForCarRide(reservationId);
        if (Drives.isDriverOrOwnerOrAdmin(reservation)) {
            return ok( refuelsForRide.render(
                            Form.form(RefuelData.class).fill(new RefuelData().populate( new EurocentAmount(), null, 0)),
                            refuels,
                            reservation) );
        } else {
            return badRequest(); // hacker?
        }
    }

    /**
     * Process form from {@link #showRefuelsForRide}
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result newRefuelForRidePost(int reservationId) {
        Form<RefuelData> form = Form.form(RefuelData.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        Reservation reservation = context.getReservationDAO().getReservation(reservationId);
        Iterable<Refuel> refuels = context.getRefuelDAO().getRefuelsForCarRide(reservationId);
        if (form.hasErrors()) {
            form.reject("picture", "Bestand opnieuw selecteren");
            return badRequest( refuelsForRide.render(form, refuels, reservation));
        } else if (Drives.isDriverOrOwnerOrAdmin(reservation)) {
            RefuelData data = form.get();
            File file = FileHelper.getFileFromRequest("picture", FileHelper.DOCUMENT_CONTENT_TYPES, "uploads.refuelproofs");
            if (file == null) {
                form.reject("picture", "Bestand met foto of scan van bonnetje is verplicht");
                return badRequest(refuelsForRide.render(form, refuels, reservation));
            } else if (file.getContentType() == null) {
                form.reject("picture", "Het bestand  is van het verkeerde type");
                return badRequest(refuelsForRide.render(form, refuels, reservation));
            } else {
                UserHeader owner = context.getUserDAO().getUserHeader(reservation.getOwnerId());
                newRefuel(reservation, owner, data.amount.getValue(), file.getId(), data.km, data.fuelAmount);
            }
            return redirect(routes.Refuels.showRefuelsForRide(reservationId));
        } else {
            return badRequest(); // hacker?
        }

    }

    // use in injected context only
    static void newRefuel(Reservation reservation, UserHeader owner, int eurocents, int fileId,
                          int km, String amount
                          ) {
        boolean isAdmin = Drives.isOwnerOrAdmin(reservation);

        int refuelId = DataAccess.getInjectedContext().getRefuelDAO().createRefuel(
                reservation.getId(), eurocents, fileId,
                isAdmin ? RefuelStatus.ACCEPTED : RefuelStatus.REQUEST,
                km, amount
                );
        if (!isAdmin) {
            Notifier.sendRefuelRequest(
                    owner,
                    reservation,
                    refuelId,
                    reservation.getCar(),
                    eurocents
            );
        }
    }

    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result showDetails(int refuelId) {
        // TODO: code in common with approveOrReject
        DataAccessContext context = DataAccess.getInjectedContext();
        Refuel refuel = context.getRefuelDAO().getRefuel(refuelId);
        ReservationHeader reservation = context.getReservationDAO().getReservationHeaderForRefuel(refuelId);
        Car car = context.getCarDAO().getCar(reservation.getCarId());
        CarRide ride = context.getCarRideDAO().getCarRide(reservation.getId());
        return ok(details.render(refuel, car, reservation, ride));
    }

    /**
     * Show the page that allows approval or rejection of a refuel by the owner
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result approveOrReject(int refuelId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Refuel refuel = context.getRefuelDAO().getRefuel(refuelId);
        ReservationHeader reservation = context.getReservationDAO().getReservationHeaderForRefuel(refuelId);
        Car car = context.getCarDAO().getCar(reservation.getCarId());
        CarRide ride = context.getCarRideDAO().getCarRide(reservation.getId());
        return ok(approveorreject.render(
                Form.form(Drives.RemarksData.class),
                refuel, car, reservation,
                ride));
    }

    /**
     * Process result of {@link #approveOrReject}
     */
    @AllowRoles({UserRole.CAR_OWNER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result approveOrRejectPost(int refuelId) {
        // TODO lots of code in common with Drives.approveOrRejectPost
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationHeader reservation = context.getReservationDAO().getReservationHeaderForRefuel(refuelId);
        RefuelDAO dao = context.getRefuelDAO();
        Refuel refuel = dao.getRefuel(refuelId);
        Form<Drives.RemarksData> form =  Form.form(Drives.RemarksData.class).bindFromRequest();
        if (form.hasErrors()) {
            Car car = context.getCarDAO().getCar(reservation.getCarId());
            CarRide ride = context.getCarRideDAO().getCarRide(reservation.getId());
            return badRequest(approveorreject.render(form, refuel, car, reservation, ride));
        } else {
            Drives.RemarksData data = form.get(); // the form does not contain errors
            RefuelStatus status = RefuelStatus.valueOf(data.status);
            String remarks = data.remarks;
            if (status == RefuelStatus.REFUSED || status == RefuelStatus.ACCEPTED) {
                if (!(CurrentUser.hasRole(UserRole.RESERVATION_ADMIN))) {
                    // extra checks when not reservation admin
                    if (CurrentUser.isNot(reservation.getOwnerId())
                            || refuel.getStatus() != RefuelStatus.REQUEST) {
                        flash("danger", "Alleen de eigenaar kan een tankbeurt goed- of afkeuren");
                        return redirect(routes.Refuels.showRefuelsForRide(reservation.getId()));
                    }
                }

                dao.acceptOrRejectRefuel(status, refuelId, remarks);
                if (status == RefuelStatus.REFUSED) {
                    // note: refuel contains value that is not yet updated, and hence
                    // does not yet contain the remarks
                    Notifier.sendRefuelRejected(refuel, remarks);
                } else {
                    Notifier.sendRefuelApproved(refuel, remarks);
                }
                return redirect(routes.Refuels.showRefuelsForRide(reservation.getId()));
            }  else { // other cases only happen when somebody is hacking
                return badRequest();
            }
        }

    }

}



