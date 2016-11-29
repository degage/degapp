/* Parkingcards.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Emmanuel Isebaert
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
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.parkingcards.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class Parkingcards extends Controller {

    public static class ParkingcardModel {

        public String parkingcardCity;
        public LocalDate parkingcardExpiration;
        public String parkingcardZones;
        public String parkingcardContractNr;
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showAllParkingcardsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        Filter filter = Pagination.parseFilter(searchString);
        FilterField field = FilterField.stringToField(orderBy, FilterField.FROM);
        return ok(parkingcardsPage.render(
            DataAccess.getInjectedContext().getCarParkingcardDAO().getAllCarParkingcards(field, ascInt == 1 ? true : false, page, pageSize, filter)
            // DataAccess.getInjectedContext().getCarParkingcardDAO().getAllCarParkingcards()
        ));
    }

    /**
     * Method: GET
     *
     * @return index page containing all the parkingcards from everyone
     */
    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showAllParkingcards() {
        return ok(parkingcardsAdmin.render());
    }
}
