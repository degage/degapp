/* Billings.java
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
import be.ugent.degage.db.dao.BillingDAO;
import be.ugent.degage.db.dao.CheckDAO;
import be.ugent.degage.db.models.*;
import com.google.common.primitives.Ints;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import it.innove.play.pdf.PdfGenerator;
import play.mvc.Result;
import views.html.billing.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Actions related to building.
 */
public class Billings extends Application {

    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result showAnomalies(int billingId, int carId) {
        Iterable<CheckDAO.TripAnomaly> list = DataAccess.getInjectedContext().getCheckDAO().getTripAnomalies(billingId, carId);
        return ok(anomalies.render(list));
    }

    /**
     * Produce a list of all billings relevant to the current user.
     */
    @InjectContext
    @AllowRoles
    public static Result list(int userId) {
        if (userId == 0) {
            userId = CurrentUser.getId();
        } else if (CurrentUser.isNot(userId) && !CurrentUser.hasRole(UserRole.SUPER_USER)) {
            return badRequest(); // not authorised
        }
        BillingDAO dao = DataAccess.getInjectedContext().getBillingDAO();
        Iterable<Billing> billings = dao.listBillingsForUser(userId);
        // TODO: should only give non-privileged billings?
        // TODO: dispatch according to car owner and then add bills for cars

        return ok(listUser.render(billings, userId));
    }

    /**
     * Represents a view of the price info for use in the tables
     */
    public static class KmPriceInfo {
        public Iterable<String> ranges;
        public Iterable<Integer> prices;

        public KmPriceInfo(KmPriceDetails details) {
            this.prices = Ints.asList(details.getPrices());
            int[] froms = details.getFroms();

            List<String> ranges = new ArrayList<>();
            for (int i = 1; i < froms.length; i++) {
                ranges.add(String.format("%d-%d km", froms[i - 1], froms[i] - 1));
            }
            ranges.add(String.format(">%d km", froms[froms.length - 1]));
            // TODO? Reverse the list? Collections.reverse
            this.ranges = ranges;
        }
    }

    /**
     * Represents a single line in an invoice
     */
    public static class InvoiceLine {
        public String carName;
        public LocalDate date;

        // the following fields can be null in case of a refuel cost
        public Integer km;
        public Integer[] kmSub;
        public Integer kmCost;

        // the following field can be null when there was no refueling
        public Integer fuelCost;

        // not used in view
        //private int reservationId;

        // used for computing the totals
        private InvoiceLine(int size) {
            km = 0;
            this.kmSub = new Integer[size];
            Arrays.fill(kmSub, 0);
            kmCost = 0;
            fuelCost = 0;
        }

        private InvoiceLine(BillingDetails b) {
            this.carName = b.getCarName();
            this.date = b.getTime().toLocalDate();
            //this.reservationId = b.getReservationId();
        }

        public InvoiceLine(BillingDetailsTrip bt, KmPriceDetails list) {
            this(bt);
            this.km = bt.getKm();
            this.kmCost = bt.getCost();
            this.fuelCost = null;

            int[] froms = list.getFroms();
            Integer[] kmSub = new Integer[froms.length];
            int pos = 1;
            while (pos < froms.length && km >= froms[pos]) {
                kmSub[pos - 1] = froms[pos] - froms[pos - 1];
                pos++;
            }
            kmSub[pos - 1] = km - froms[pos - 1] + 1;
            this.kmSub = kmSub;
        }

        public InvoiceLine(BillingDetailsFuel bf, KmPriceDetails list) {
            this(bf);
            this.km = null;
            this.kmCost = null;
            this.fuelCost = bf.getCost();

            this.kmSub = new Integer[list.getFroms().length];

        }

        public void add(InvoiceLine line) {
            if (line.km != null) {
                km += line.km;
                for (int i = 0; i < kmSub.length; i++) {
                    if (line.kmSub[i] != null) {
                        kmSub[i] += line.kmSub[i];
                    }
                }
                kmCost += line.kmCost;
            }
            if (line.fuelCost != null) {
                fuelCost += line.fuelCost;
            }
        }
    }

    private static class ILEntry {
        public InvoiceLine t;       // trip
        public List<InvoiceLine> f; // refuels for this trip

        public ILEntry() {
            f = new ArrayList<>();
        }
    }

    private static Iterable<InvoiceLine> getInvoiceLines(
            Iterable<BillingDetailsTrip> trips, Iterable<BillingDetailsFuel> fuels,
            KmPriceDetails kmPrices) {
        // store all invoice lines according to reservation id
        Map<Integer, ILEntry> map = new HashMap<>();

        for (BillingDetailsTrip trip : trips) {
            int reservationId = trip.getReservationId();
            ILEntry ile = new ILEntry();
            ile.t = new InvoiceLine(trip, kmPrices);
            map.put(reservationId, ile);
        }

        for (BillingDetailsFuel fuel : fuels) {
            int reservationId = fuel.getReservationId();
            ILEntry ile = map.get(reservationId);
            if (ile == null) {
                ile = new ILEntry();
                map.put(reservationId, ile);
            }
            ile.f.add(new InvoiceLine(fuel, kmPrices));
        }

        // create the resulting list
        List<InvoiceLine> result = new ArrayList<>();
        for (ILEntry ile : map.values()) {
            // merge top invoice lines
            if (ile.t != null && !ile.f.isEmpty()) {
                ile.t.fuelCost = ile.f.remove(0).fuelCost;
            }
            if (ile.t != null) {
                result.add(ile.t);
            }
            result.addAll(ile.f);
        }
        Collections.sort(result, (x, y) -> x.date.compareTo(y.date));
        return result;
    }

    private static InvoiceLine total(Iterable<InvoiceLine> list, int size) {
        InvoiceLine result = new InvoiceLine(size);
        for (InvoiceLine line : list) {
            result.add(line);
        }
        return result;


    }

    private static String structuredComment (int billingId, int xtra, BillingDetailsUser bdu) {
        int pre = (3 * billingId + xtra) % 1000;
        int mid = (bdu.getIndex() % 10000);
        int end = (bdu.getUserId() % 1000);

        int mod = (10000000*pre + 1000*mid + end) % 97;
        if (mod == 0) {
            mod =97;
        }
        return String.format ("+++%03d/%04d/%03d%02d+++", pre, mid, end, mod);
    }

    @InjectContext
    @AllowRoles
    public static Result userDetails(int billingId, int userId) {
        if (userId == 0) {
            userId = CurrentUser.getId();
        } else if (CurrentUser.isNot(userId) && !CurrentUser.hasRole(UserRole.SUPER_USER)) {
            return badRequest(); // not authorised
        }

        DataAccessContext context = DataAccess.getInjectedContext();
        BillingDAO dao = context.getBillingDAO();
        Billing billing = dao.getBilling(billingId);
        BillingDetailsUser bUser = dao.getUserDetails(billingId, userId);
        if (bUser != null) {

            KmPriceDetails priceDetails = dao.getKmPriceDetails(billingId);

            Iterable<InvoiceLine> invoiceLines = getInvoiceLines(
                    dao.listTripDetails(billingId, userId, false),
                    dao.listFuelDetails(billingId, userId, false),
                    priceDetails
            );
            String billNr = String.format("A%s-%04d", billing.getPrefix(), bUser.getIndex());
            response().setHeader("Content-Disposition", "attachment; filename=" + billNr + ".pdf");
            switch (billing.getStatus()) {
                case SIMULATION:

                    return PdfGenerator.ok(userInvoiceSimulation.render(
                            billing,
                            billNr,
                            context.getUserDAO().getUser(userId),
                            new KmPriceInfo(priceDetails),
                            invoiceLines,
                            total(invoiceLines, priceDetails.getFroms().length)
                    ), null);
                case USERS_DONE:
                case ALL_DONE:
                    return PdfGenerator.ok(userInvoiceFinal.render(
                            billing,
                            billNr,
                            context.getUserDAO().getUser(userId),
                            new KmPriceInfo(priceDetails),
                            invoiceLines,
                            total(invoiceLines, priceDetails.getFroms().length),
                            structuredComment(billingId, 0, bUser)
                    ), null);
                default:
                    return badRequest();
            }
        } else {
            return badRequest();
        }
    }
}
