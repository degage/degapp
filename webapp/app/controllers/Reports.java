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
import be.ugent.degage.db.models.BillingDetailsUserKm;
import be.ugent.degage.db.models.UserRole;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate reports as spreadsheet files
 */
public class Reports extends Controller {

    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result billingCarOverview(int billingId) {

        DataAccessContext context = DataAccess.getInjectedContext();
        String prefix = context.getBillingDAO().getBilling(billingId).getPrefix();
        Iterable<BillingAdmDAO.CarBillingInfo> list = context.getBillingAdmDAO().listCarBillingOverview(billingId);

        TableDefinition<BillingAdmDAO.CarBillingInfo> tableDefinition = new TableDefinition<>(
                new StringColumnDefinition<>("Afrekening", cbi -> String.format("E%s-%04d", prefix, cbi.seqNr)),
                new NumericColumnDefinition<>("Auto ID", cbi -> cbi.carId),
                new StringColumnDefinition<>("Auto NAAM", cbi -> cbi.carName),
                new StringColumnDefinition<>("Eigenaar", cbi -> cbi.ownerName),
                new EurocentColumnDefinition<>("Saldo brandstof", cbi -> cbi.fuel),
                new EurocentColumnDefinition<>("Recup afschrijving", cbi -> cbi.deprec),
                new EurocentColumnDefinition<>("Recup kosten", cbi -> cbi.costs),
                new EurocentColumnDefinition<>("Totaal te ontv.", cbi -> cbi.total),
                new StringColumnDefinition<>(null, cbi -> cbi.structuredComment),
                new NumericColumnDefinition<>("Totaal km", cbi -> cbi.totalKm),
                new NumericColumnDefinition<>("door eigenaar", cbi -> cbi.ownerKm),
                new NumericColumnDefinition<>("door ontlener", cbi -> cbi.totalKm - cbi.ownerKm),
                new NumericColumnDefinition<>("Af te schr. km", cbi -> cbi.deprecKm),
                new NumericColumnDefinition<>("Afschr/km", cbi -> 0.001 * cbi.depreciationFactor),
                new NumericColumnDefinition<>("Brandstof/km", cbi -> 0.001 * cbi.fuelPerKm),
                new NumericColumnDefinition<>("Kosten/km", cbi -> 0.001 * cbi.costsPerKm),
                new NumericColumnDefinition<>("Restwaarde auto", cbi -> (double) cbi.remainingCarValue)
        );

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            new WorkbookDefinition(tableDefinition).write(out, "E" + prefix, list);
            response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response().setHeader("Content-Disposition", "attachment; filename=E" + prefix + ".xlsx");
            response().setHeader(CACHE_CONTROL, "no-cache, must-revalidate");
            return ok(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException ex) {
            throw new RuntimeException("Could not create file to download", ex);
        }
    }

    private static class Combined {
        public BillingAdmDAO.UserBillingInfo b;
        public BillingDetailsUserKm d;

        public Integer getKm(int index) {
            int[] tab = d.getKilometersInRange();
            if (tab == null || index >= tab.length) {
                return 0;
            } else {
                return tab[index];
            }
        }
    }

    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result billingUserOverview(int billingId) {

        // TODO: simplify this!   Maybe a full join of the two views ?

        DataAccessContext context = DataAccess.getInjectedContext();
        String prefix = context.getBillingDAO().getBilling(billingId).getPrefix();

        BillingAdmDAO dao = context.getBillingAdmDAO();
        // first part of the overview
        List<BillingAdmDAO.UserBillingInfo> list = dao.listUserBillingOverview(billingId);
        // second part of the overview
        List<BillingDetailsUserKm> kmDetails = dao.getUserKmDetails(billingId);

        // merge
        if (list.size() < kmDetails.size()) {
            throw new RuntimeException("Incompatible list size for billing details");
        }
        List<Combined> result = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < list.size() && j < kmDetails.size()) {
            Combined combined = new Combined();
            combined.b = list.get(i);
            combined.d = kmDetails.get(j);
            //System.err.println(combined.b.userId + "," + combined.d.getUserId() + ", " + combined.b.km + ", " + combined.d.getTotalKilometers());

            if (combined.b.userId == combined.d.getUserId()) {
                // usual case
                result.add(combined);
                i++;
                j++;
            } else if (combined.b.km == 0) {
                combined.d = new BillingDetailsUserKm(combined.b.userId, 0);
                result.add(combined);
                i++;
            } else {
                throw new RuntimeException("Incompatible billing details lists");
            }
        }
        while (i < list.size()) {
            Combined combined = new Combined();
            combined.b = list.get(i);
            combined.d = new BillingDetailsUserKm(combined.b.userId, 0);
            result.add(combined);
            i++;
        }


        // workbook table definition
        TableDefinition<Combined> tableDefinition = new TableDefinition<>();

        tableDefinition.addColumn(new StringColumnDefinition<>("Afrekening", c -> String.format("A%s-%04d", prefix, c.b.seqNr)));
        tableDefinition.addColumn(new NumericColumnDefinition<>("Gebruiker ID", c -> c.b.userId));
        tableDefinition.addColumn(new StringColumnDefinition<>("Gebruiker NAAM", c -> c.b.userName));
        tableDefinition.addColumn(new EurocentColumnDefinition<>("Kost kilometers", c -> c.b.km));
        tableDefinition.addColumn(new EurocentColumnDefinition<>("Brandstof betaald", c -> c.b.fuel));
        tableDefinition.addColumn(new EurocentColumnDefinition<>("Totaal", c -> c.b.total));
        tableDefinition.addColumn(new StringColumnDefinition<>(null, c -> c.b.structuredComment));

        tableDefinition.addColumn(new NumericColumnDefinition<>("Aantal ritten", c -> c.d.getNrOfTrips()));
        tableDefinition.addColumn(new NumericColumnDefinition<>("Aantal kilometer", c -> c.d.getTotalKilometers()));
        tableDefinition.addColumn(new NumericColumnDefinition<>("Opgesplitst", c -> c.getKm(0)));
        tableDefinition.addColumn(new NumericColumnDefinition<>(null, c -> c.getKm(1)));
        tableDefinition.addColumn(new NumericColumnDefinition<>(null, c -> c.getKm(2))); // TODO: do not hardcode this number

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            new WorkbookDefinition(tableDefinition).write(out, "A" + prefix, result);
            response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response().setHeader("Content-Disposition", "attachment; filename=A" + prefix + ".xlsx");
            response().setHeader(CACHE_CONTROL, "no-cache, must-revalidate");
            return ok(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException ex) {
            throw new RuntimeException("Could not create file to download", ex);
        }
    }
}
