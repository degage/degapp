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
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import data.EurocentAmount;
import views.html.refuels.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class Refuels extends Controller {

    public static class RefuelData {

        public EurocentAmount amount;

        public String picture; // only used to enable field error messages

        public RefuelData populate(EurocentAmount amount) {
            this.amount = amount;
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
     * Method: GET
     *
     * @return index page containing all the refuel requests from a specific user
     */
    @AllowRoles
    @InjectContext
    public static Result showRefuels() {
        return ok(refuels.render());
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

    /**
     * Method: GET
     *
     * @return index page containing all the refuel requests to a specific owner
     */
    @AllowRoles({UserRole.CAR_OWNER})
    @InjectContext
    public static Result showOwnerRefuels() {
        return ok(refuelsOwner.render());
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

    /**
     * Method: GET
     *
     * @return index page containing all the refuel requests to a specific owner
     */
    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showAllRefuels() {
        return ok(refuelsAdmin.render());
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
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return refuelspage.render(dao.getRefuels(page, pageSize, filter), page, amountOfResults, amountOfPages);
    }

    /**
     * Method: GET
     *
     * @return proof url
     */
    @AllowRoles
    @InjectContext
    public static Result getProof(int proofId) {
        return FileHelper.getFileStreamResult(DataAccess.getInjectedContext().getFileDAO(), proofId);
    }

    /**
     * Method: GET
     * <p>
     * Called when a refuel of a car is refused by the car owner.
     *
     * @param refuelId The refuel being refused
     * @return the refuel admin page
     */
    @AllowRoles({UserRole.CAR_OWNER})
    @InjectContext
    public static Result refuseRefuel(int refuelId) {
        RefuelDAO dao = DataAccess.getInjectedContext().getRefuelDAO();
        dao.rejectRefuel(refuelId);
        Notifier.sendRefuelStatusChanged(dao.getRefuel(refuelId), false);
        flash("success", "Tankbeurt succesvol geweigerd");
        return redirect(routes.Refuels.showOwnerRefuels());
    }

    /**
     * Method: GET
     * <p>
     * Called when a refuel of a car is accepted by the car owner.
     *
     * @param refuelId The refuel being approved
     * @return the refuel admin page
     */
    @AllowRoles({UserRole.CAR_OWNER})
    @InjectContext
    public static Result approveRefuel(int refuelId) {
        RefuelDAO dao = DataAccess.getInjectedContext().getRefuelDAO();
        dao.acceptRefuel(refuelId);
        Notifier.sendRefuelStatusChanged(dao.getRefuel(refuelId), true);
        flash("success", "Tankbeurt succesvol geaccepteerd");
        return redirect(routes.Refuels.showOwnerRefuels());
    }

    /**
     * Method: GET
     * <p>
     * Called when a refuel of a car is put back to the request status by the car owner.
     *
     * @param refuelId The refuel being put back to the request status
     * @return the refuel admin page
     */
    @AllowRoles({UserRole.CAR_OWNER})
    @InjectContext
    public static Result makeRefuelStatusRequest(int refuelId) {
        RefuelDAO dao = DataAccess.getInjectedContext().getRefuelDAO();
        Refuel refuel = dao.getRefuel(refuelId);
        refuel.setStatus(RefuelStatus.REQUEST);
        dao.updateRefuel(refuel);
        // TODO: also send notification?
        flash("success", "Tankbeurt succesvol op status REQUEST gezet.");
        return redirect(routes.Refuels.showOwnerRefuels());
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
                            Form.form(RefuelData.class).fill(new RefuelData().populate(new EurocentAmount())),
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
            return ok( refuelsForRide.render(form, refuels, reservation));
        } else if (Drives.isDriverOrOwnerOrAdmin(reservation)) {
            RefuelData data = form.get();
            File file = FileHelper.getFileFromRequest("picture", FileHelper.DOCUMENT_CONTENT_TYPES, "uploads.refuelproofs");
            if (file.getContentType() == null) {
                form.reject("picture", "Het bestand  is van het verkeerde type");
                return badRequest(refuelsForRide.render(form, refuels, reservation));
            } else {
                UserHeader owner = context.getUserDAO().getUserHeader(reservation.getOwnerId());
                newRefuel(reservation, owner, data.amount.getValue(), file);
            }
            return redirect(routes.Refuels.showRefuelsForRide(reservationId));
        } else {
            return badRequest(); // hacker?
        }

    }

    // use in injected context only
    static void newRefuel(Reservation reservation, UserHeader owner, int eurocents, File file) {
        boolean isAdmin = Drives.isOwnerOrAdmin(reservation);

        DataAccess.getInjectedContext().getRefuelDAO().createRefuel(
                reservation.getId(), eurocents, file,
                isAdmin ? RefuelStatus.ACCEPTED : RefuelStatus.REQUEST);
        if (!isAdmin) {
            Notifier.sendRefuelRequest(
                    owner,
                    reservation.getCar(),
                    eurocents
            );
        }
    }

}



