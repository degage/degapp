/* Reports.java
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

import be.ugent.caagt.sheeter.*;
import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.BillingAdmDAO;
import be.ugent.degage.db.models.UserRole;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Generate reports as spreadsheet files
 */
public class Reports extends Controller {

    private static WorkbookDefinition billingCarWorkbook;

    static {
        billingCarWorkbook = new WorkbookDefinition(
                // TODO: set sheet name (dynamically)
                new TableDefinition<BillingAdmDAO.CarBillingInfo>(
                        new NumericColumnDefinition<>("Auto ID", cbi -> cbi.carId),
                        new StringColumnDefinition<>("Auto NAAM", cbi -> cbi.carName),
                        new EurocentColumnDefinition<>("Saldo brandstof", cbi -> cbi.fuel),
                        new EurocentColumnDefinition<>("Recup afschrijving", cbi -> cbi.deprec),
                        new EurocentColumnDefinition<>("Recup kosten", cbi -> cbi.costs),
                        new EurocentColumnDefinition<>("Totaal", cbi -> cbi.total),
                        new StringColumnDefinition<>(null, cbi -> cbi.structuredComment)
                ));
    }

    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result billingCarOverview(int billingId) {

        DataAccessContext context = DataAccess.getInjectedContext();
        String prefix = context.getBillingDAO().getBilling(billingId).getPrefix();
        Iterable<BillingAdmDAO.CarBillingInfo> list = context.getBillingAdmDAO().listCarBillingOverview(billingId);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            billingCarWorkbook.write(out, "E"+prefix, list);
            response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response().setHeader("Content-Disposition", "attachment; filename=E" + prefix + ".xlsx");
            response().setHeader(CACHE_CONTROL, "no-cache, must-revalidate");
            return ok(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException ex) {
            throw new RuntimeException("Could not create file to download", ex);
        }
    }

}
