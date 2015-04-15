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
import play.data.Form;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.refuels.*;

/**
 *  Controller that displays information about refuels
 */
public class Refuels extends RefuelCommon {

    /**
     * Dispatches to the correct refuels page
     */
    @AllowRoles
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

    @AllowRoles
    @InjectContext
    public static Result showUserRefuelsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?

        // currently not used
        //FilterField field = FilterField.stringToField(orderBy);
        //boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        filter.putValue(FilterField.REFUEL_USER_ID, CurrentUser.getId() + "");

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
        if (WFCommon.isDriverOrOwnerOrAdmin(reservation)) {
            Refuel refuel = context.getRefuelDAO().getRefuel(refuelId);
            return FileHelper.getFileStreamResult(context.getFileDAO(), refuel.getProofId());
        } else {
            return badRequest(); // hacker
        }
    }

    /**
     *
     */
    // should only be used with injected context - used in refuel menu
    public static int numberOfRefuelRequests() {
        if (CurrentUser.hasRole(UserRole.CAR_OWNER)) {
            return DataAccess.getInjectedContext().getRefuelDAO().numberOfRefuelRequests(CurrentUser.getId());
        } else {
            return 0;
        }
    }

    /**
     * Show all refuels connected with a given ride
     */
    @AllowRoles
    @InjectContext
    public static Result showRefuelsForTrip(int reservationId) {

        DataAccessContext context = DataAccess.getInjectedContext();
        Reservation reservation = context.getReservationDAO().getReservation(reservationId);
        Iterable<Refuel> refuels = context.getRefuelDAO().getRefuelsForCarRide(reservationId);
        if (WFCommon.isDriverOrOwnerOrAdmin(reservation)) {
            return ok( refuelsForTrip.render(
                            Form.form(RefuelData.class).fill(new RefuelData().populate( new EurocentAmount(), null, 0)),
                            refuels,
                            reservation) );
        } else {
            return badRequest(); // hacker?
        }
    }

    /**
     * Show the list of all rides for a given reservation, intended for use by owners.
     */
    @AllowRoles
    @InjectContext
    public static Result showRefuelsForTripOwner(int reservationId) {
        // TODO factor out common code with showRefuelsForRide ?

        DataAccessContext context = DataAccess.getInjectedContext();
        Reservation reservation = context.getReservationDAO().getReservation(reservationId);
        Iterable<Refuel> refuels = context.getRefuelDAO().getRefuelsForCarRide(reservationId);
        if (WFCommon.isOwnerOrAdmin(reservation)) { // note: differs from showRefuelsForTrip
            return ok( refuelsForTripOwner.render(
                            Form.form(RefuelData.class).fill(new RefuelData().populate( new EurocentAmount(), null, 0)),
                            refuels,
                            reservation) );
        } else {
            return badRequest(); // hacker?
        }
    }


    @AllowRoles
    @InjectContext
    public static Result showDetails(int refuelId) {
        // TODO: code in common with approveOrReject
        DataAccessContext context = DataAccess.getInjectedContext();
        Refuel refuel = context.getRefuelDAO().getRefuel(refuelId);
        ReservationHeader reservation = context.getReservationDAO().getReservationHeaderForRefuel(refuelId);
        if (isDriverOrOwnerOrAdmin(reservation)) {
            Car car = context.getCarDAO().getCar(reservation.getCarId());
            CarRide ride = context.getCarRideDAO().getCarRide(reservation.getId());
            return ok(details.render(refuel, car, reservation, ride));
        } else {
            return badRequest(); // not authorized
        }
    }

}



