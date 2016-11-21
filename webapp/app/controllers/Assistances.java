/* Assistances.java
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
import views.html.assistances.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class Assistances extends Controller {

    public static class AssistanceModel {

        public String assistanceName;
        public LocalDate assistanceExpiration;
        public String assistanceType;
        public String assistanceContractNr;
    }

    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showAllAssistancesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        Filter filter = Pagination.parseFilter(searchString);
        FilterField field = FilterField.stringToField(orderBy, FilterField.FROM);
        return ok(assistancesPage.render(
            DataAccess.getInjectedContext().getCarAssistanceDAO().getAllCarAssistances(field, ascInt == 1 ? true : false, page, pageSize, filter)
            // DataAccess.getInjectedContext().getCarAssistanceDAO().getAllCarAssistances()
        ));
    }

    /**
     * Method: GET
     *
     * @return index page containing all the assistances from everyone
     */
    @AllowRoles({UserRole.CAR_ADMIN})
    @InjectContext
    public static Result showAllAssistances() {
        return ok(assistancesAdmin.render());
    }
}
