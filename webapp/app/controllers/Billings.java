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
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.DataAccessException;
import com.google.common.primitives.Ints;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import it.innove.play.pdf.PdfGenerator;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.billing.*;
import controllers.util.Pagination;

import java.time.LocalDate;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;

/**
 * Actions related to billing (reporting).
 */
public class Billings extends Controller {

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
        DataAccessContext context = DataAccess.getInjectedContext();
        BillingDAO dao = context.getBillingDAO();
        Iterable<BillingInfo> billings = dao.listBillingsForUser(userId);
        Iterable<InvoiceAndUser> invoices = context.getInvoiceDAO().listInvoicesForUser(userId);
        return ok(listUser.render(billings, invoices, userId));
    }

    /**
     * Produce a pdf invoice
     */
    @InjectContext
    @AllowRoles
    public static Result invoicePdf(int invoiceId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        InvoiceAndUser invoiceAndUser = context.getInvoiceDAO().getInvoiceAndUser(invoiceId);
        if (CurrentUser.isNot(invoiceAndUser.getInvoice().getUserId()) && !CurrentUser.hasRole(UserRole.INVOICE_ADMIN)) {
            return badRequest(); // not authorised
        }
        if (invoiceAndUser == null) return badRequest();
        if (invoiceAndUser.getInvoice().getType() == InvoiceType.CAR_MEMBERSHIP) {
            response().setHeader("Content-Disposition", "attachment; filename=" + invoiceAndUser.getInvoice().getNumber() + ".pdf");
            return PdfGenerator.ok(carMembershipInvoice.render(invoiceAndUser), null);
        }
        return null;
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
     * A shortened version of {@link InvoiceLine} for use with car invoices
     */
    public static class ShortInvoiceLine {
        public LocalDate date;
        public Integer km;
        public Integer fuelCost;

        public ShortInvoiceLine() {
            this.date = null;
            this.km = 0;
            this.fuelCost = 0;
        }

        public ShortInvoiceLine(BillingDetailsTrip bt) {
            this.date = bt.getTime().toLocalDate();
            this.km = bt.getKm();
            this.fuelCost = null;
        }

        public ShortInvoiceLine(BillingDetailsFuel bf) {
            this.date = bf.getTime().toLocalDate();
            this.km = null;
            this.fuelCost = bf.getCost();
        }

        public void add(ShortInvoiceLine line) {
            if (line.km != null) {
                km += line.km;
            }
            if (line.fuelCost != null) {
                fuelCost += line.fuelCost;
            }
        }
    }

    /**
     * Represents a single line in an invoice
     */
    public static class InvoiceLine extends ShortInvoiceLine {
        public String carName;
        //public LocalDate date;

        // the following fields can be null in case of a refuel cost
        //public Integer km;
        public Integer[] kmSub;
        public Integer kmCost;

        // the following field can be null when there was no refueling
        //public Integer fuelCost;

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

        public int getTotal() {
            return (this.kmCost-this.fuelCost);
        }
    }

    private static class ILEntry<T extends ShortInvoiceLine> {
        public T t;       // trip
        public List<T> f; // refuels for this trip

        public ILEntry() {
            f = new ArrayList<>();
        }
    }

    public static Iterable<InvoiceLine> getInvoiceLines(
            Iterable<BillingDetailsTrip> trips, Iterable<BillingDetailsFuel> fuels,
            KmPriceDetails kmPrices) {
        // store all invoice lines according to reservation id
        Map<Integer, ILEntry<InvoiceLine>> map = new HashMap<>();

        for (BillingDetailsTrip trip : trips) {
            int reservationId = trip.getReservationId();
            ILEntry<InvoiceLine> ile = new ILEntry<>();
            ile.t = new InvoiceLine(trip, kmPrices);
            map.put(reservationId, ile);
        }

        for (BillingDetailsFuel fuel : fuels) {
            int reservationId = fuel.getReservationId();
            ILEntry<InvoiceLine> ile = map.get(reservationId);
            if (ile == null) {
                ile = new ILEntry<>();
                map.put(reservationId, ile);
            }
            ile.f.add(new InvoiceLine(fuel, kmPrices));
        }

        // create the resulting list
        List<InvoiceLine> result = new ArrayList<>();
        for (ILEntry<InvoiceLine> ile : map.values()) {
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

    // TODO: factor out code in common with getInvoiceLines
    private static List<ShortInvoiceLine> getShortInvoiceLines(
            Iterable<BillingDetailsTrip> trips, Iterable<BillingDetailsFuel> fuels) {
        // store all invoice lines according to reservation id
        Map<Integer, ILEntry<ShortInvoiceLine>> map = new HashMap<>();

        for (BillingDetailsTrip trip : trips) {
            int reservationId = trip.getReservationId();
            ILEntry<ShortInvoiceLine> ile = new ILEntry<>();
            ile.t = new ShortInvoiceLine(trip);
            map.put(reservationId, ile);
        }

        for (BillingDetailsFuel fuel : fuels) {
            int reservationId = fuel.getReservationId();
            ILEntry<ShortInvoiceLine> ile = map.get(reservationId);
            if (ile == null) {
                ile = new ILEntry<>();
                map.put(reservationId, ile);
            }
            ile.f.add(new ShortInvoiceLine(fuel));
        }

        // create the resulting list
        List<ShortInvoiceLine> result = new ArrayList<>();
        for (ILEntry<ShortInvoiceLine> ile : map.values()) {
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

    public static class OwnerTable {
        public String name;
        public List<ShortInvoiceLine> lines;
        public ShortInvoiceLine total;
    }

    private static List<OwnerTable> getOwnerTables(Iterable<BillingDetailsOwner> list) {
        List<OwnerTable> result = new ArrayList<>();
        for (BillingDetailsOwner details : list) {
            OwnerTable ot = new OwnerTable();
            ot.name = details.getUser();
            ot.lines = getShortInvoiceLines(details.getTrips(), details.getRefuels());
            ot.total = total(ot.lines);
            result.add(ot);
        }
        return result;
    }

    public static InvoiceLine total(Iterable<InvoiceLine> list, int size) {
        InvoiceLine result = new InvoiceLine(size);
        for (InvoiceLine line : list) {
            result.add(line);
        }
        return result;
    }

    private static ShortInvoiceLine total(Iterable<ShortInvoiceLine> list) {
        ShortInvoiceLine result = new ShortInvoiceLine();
        for (ShortInvoiceLine line : list) {
            result.add(line);
        }
        return result;
    }

    private static ShortInvoiceLine tableTotal(Iterable<OwnerTable> tables) {
        ShortInvoiceLine result = new ShortInvoiceLine();
        for (OwnerTable table : tables) {
            result.add(table.total);
        }
        return result;
    }

    public static String structuredComment(int billingId, int xtra, BillingDetailsUser bdu) {
        return structuredComment(billingId, xtra, bdu.getIndex(), bdu.getUserId());
    }

    public static String structuredComment(int billingId, int xtra, int index, int id) {
        int pre = (3 * billingId + xtra) % 1000;
        int mid = (index % 10000);
        int end = (id % 1000);

        int mod = (10000000 * pre + 1000 * mid + end) % 97;
        if (mod == 0) {
            mod = 97;
        }
        return String.format("+++%03d/%04d/%03d%02d+++", pre, mid, end, mod);
    }

    public static String structuredComment(int first, int second, int third) {
        int pre = (3 * first) % 1000;
        int mid = (second % 10000);
        int end = (third % 1000);

        int mod = (10000000 * pre + 1000 * mid + end) % 97;
        if (mod == 0) {
            mod = 97;
        }
        return String.format("+++%03d/%04d/%03d%02d+++", pre, mid, end, mod);
    }

    @InjectContext
    @AllowRoles
    public static Result userInvoices(int billingId, int page, int pageSize) throws java.io.IOException {
      if (!CurrentUser.hasRole(UserRole.SUPER_USER)) {
          return badRequest(); // not authorised
      }
      DataAccessContext context = DataAccess.getInjectedContext();
      BillingDAO dao = context.getBillingDAO();
      Billing billing = dao.getBilling(billingId);
      String billsNr = String.format("A%s", billing.getPrefix());
      response().setHeader("Content-Disposition", "attachment; filename=" + billsNr + ".zip");
      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        Page<User> userList = context.getUserDAO().getUserList(FilterField.NAME, true, page, pageSize, Pagination.parseFilter("role=CAR_USER,user_status=FULL"));
        for (User user: userList){
          BillingDetailsUser bUser = dao.getUserDetails(billingId, user.getId());
          if (bUser != null) {
            KmPriceDetails priceDetails = dao.getKmPriceDetails(billingId);
            Iterable<InvoiceLine> invoiceLines = getInvoiceLines(
                    dao.listTripDetails(billingId, user.getId(), false),
                    dao.listFuelDetails(billingId, user.getId(), false),
                    priceDetails
            );
            String billNr = getUserBillNr(billing.getPrefix(), bUser.getIndex());
            byte[] pdf = PdfGenerator.toBytes(userInvoiceFinal.render(
                    billing,
                    billNr,
                    context.getUserDAO().getUser(user.getId()),
                    new KmPriceInfo(priceDetails),
                    invoiceLines,
                    total(invoiceLines, priceDetails.getFroms().length),
                    structuredComment(billingId, 0, bUser)),
                    null);
            ZipEntry entry = new ZipEntry(billNr + ".pdf");
            entry.setSize(pdf.length);
            zos.putNextEntry(entry);
            zos.write(pdf);
            zos.closeEntry();
          }
        }
        zos.flush();
        baos.flush();
        zos.close();
        baos.close();
    		return play.mvc.Results.ok(baos.toByteArray()).as("application/zip");
      } catch (java.io.IOException e) {
        throw new java.io.IOException("Could not get user invoices", e);
      } catch (DataAccessException e) {
        throw new DataAccessException("Could not get user invoices", e);
      }
    }

    public static String getBillNr(InvoiceAndUser invoiceAndUser, DataAccessContext context){
        BillingDAO dao = context.getBillingDAO();
        int billingId = invoiceAndUser.getInvoice().getBillingId();
        int userId  = invoiceAndUser.getUser().getId();
        Billing billing = dao.getBilling(billingId);
        BillingDetailsUser bUser = dao.getUserDetails(billingId, userId);
        InvoiceType type = invoiceAndUser.getInvoice().getType();
        switch (type) {
            case CAR_MEMBERSHIP:
            case CAR_USER:{
                return getUserBillNr(billing.getPrefix(), bUser.getIndex());
            }

            case CAR_OWNER:
                return getCarBillNr(billing.getPrefix());
            default:
            throw new IllegalArgumentException("Invoice type does not exists: " + type.name() );
        }
        
    }
    public static String getUserBillNr(String prefix, int index){
        return  String.format("A%s-%04d", prefix, index);

    }


    public static String getCarBillNr(String prefix){
        return String.format("E%s", prefix);
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
            String billNr = getUserBillNr(billing.getPrefix(), bUser.getIndex());
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
                case ARCHIVED:
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

    @InjectContext
    public static Result carInvoices(int billingId) throws java.io.IOException {
      if (!CurrentUser.hasRole(UserRole.SUPER_USER)) {
          return badRequest(); // not authorised
      }
      DataAccessContext context = DataAccess.getInjectedContext();
      try {
        Page<CarHeaderAndOwner> cars = context.getCarDAO().listCarsAndOwners(FilterField.NAME, true, 1, 100000, Pagination.parseFilter(""));
        BillingDAO dao = context.getBillingDAO();
        Billing billing = dao.getBilling(billingId);
        if (billing.getStatus() != BillingStatus.ALL_DONE && billing.getStatus() != BillingStatus.ARCHIVED) {
            throw new RuntimeException("Wrong billing status"); // hacker?
        }
        String billsNr = getCarBillNr(billing.getPrefix());
        response().setHeader("Content-Disposition", "attachment; filename=" + billsNr + ".zip");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        for (CarHeaderAndOwner carHeaderAndOwner: cars){
          CarInvoiceDetails carInvoiceDetails = getCarInvoiceDetails(carHeaderAndOwner, billing);
          if (carInvoiceDetails != null) {
            byte[] pdf = PdfGenerator.toBytes(carInvoice.render(
              carInvoiceDetails.getBilling(),
              carInvoiceDetails.getBillNr(),
              carInvoiceDetails.getCar(),
              carInvoiceDetails.getUser(),
              carInvoiceDetails.getTables(),
              carInvoiceDetails.getTableTotal(),
              carInvoiceDetails.getBCar(),
              carInvoiceDetails.getRemainingValue(),
              carInvoiceDetails.getStruct()
            ), null);
            ZipEntry entry = new ZipEntry(carInvoiceDetails.getBillNr() + ".pdf");
            entry.setSize(pdf.length);
            zos.putNextEntry(entry);
            zos.write(pdf);
            zos.closeEntry();
          }
        }
        zos.flush();
        baos.flush();
        zos.close();
        baos.close();
    		return play.mvc.Results.ok(baos.toByteArray()).as("application/zip");
      } catch (java.io.IOException e){
        throw new java.io.IOException("Could not get car owner invoices", e);
      } catch (DataAccessException e){
        throw new DataAccessException("Could not get car owner invoices", e);
      }
    }

    @InjectContext
    @AllowRoles({UserRole.CAR_OWNER})
    public static Result carDetails(int billingId, int carId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarHeaderShort car = context.getCarDAO().getCarHeaderShort(carId);
        if (CurrentUser.is(car.getOwnerId()) || CurrentUser.hasRole(UserRole.SUPER_USER)) {
            BillingDAO dao = context.getBillingDAO();
            Billing billing = dao.getBilling(billingId);
            CarInvoiceDetails carInvoiceDetails = getCarInvoiceDetails(car, billing);
            response().setHeader("Content-Disposition", "attachment; filename=" + carInvoiceDetails.getBillNr() + ".pdf");
            return PdfGenerator.ok(
              carInvoice.render(
                carInvoiceDetails.getBilling(),
                carInvoiceDetails.getBillNr(),
                carInvoiceDetails.getCar(),
                carInvoiceDetails.getUser(),
                carInvoiceDetails.getTables(),
                carInvoiceDetails.getTableTotal(),
                carInvoiceDetails.getBCar(),
                carInvoiceDetails.getRemainingValue(),
                carInvoiceDetails.getStruct()
              ), null);
        } else {
            return badRequest(); // hacker?
        }
    }

    public static CarInvoiceDetails getCarInvoiceDetails(CarHeaderShort car, Billing billing) throws RuntimeException {
      DataAccessContext context = DataAccess.getInjectedContext();
      UserHeader owner = context.getUserDAO().getUserHeader(car.getOwnerId());
      BillingDetailsCar bCar = context.getBillingDAO().getCarDetails(billing.getId(), car.getId());
      if (bCar == null) {
        return null;
      }
      String billNr = String.format("E%s-%04d", billing.getPrefix(), bCar.getIndex());
      Iterable<BillingDetailsOwner> ownerDetails = context.getBillingDAO().listOwnerDetails(billing.getId(), car.getId());
      List<OwnerTable> tables = getOwnerTables(ownerDetails);
      CarDepreciation depreciation = context.getCarDAO().getDepreciation(car.getId());
      int remainingValue = depreciation.getLimit() - bCar.getLastKm();
      if (remainingValue <= 0) {
          remainingValue = 0;
      } else {
          remainingValue = remainingValue * depreciation.getCentsPerTenKilometer() / 1000;
      }
      return new CarInvoiceDetails(
        billing,
        billNr,
        car,
        owner,
        tables,
        tableTotal(tables),
        bCar,
        remainingValue,
        structuredComment(billing.getId(), 1, bCar.getIndex(), car.getId())
      );
    }

    public static class UserInvoiceDetails {
      private Billing billing;
      private String billNr;
      private User user;
      private Billings.KmPriceInfo kpi;
      private Iterable<Billings.InvoiceLine> lines;
      private Billings.InvoiceLine totals;
      private String struct;

      public UserInvoiceDetails(
        Billing billing,
        String billNr,
        User user,
        Billings.KmPriceInfo kpi,
        Iterable<Billings.InvoiceLine> lines,
        Billings.InvoiceLine totals,
        String struct) {
          this.billing = billing;
          this.billNr = billNr;
          this.user = user;
          this.kpi = kpi;
          this.lines = lines;
          this.totals = totals;
          this.struct= struct;
      }
      public Billing getBilling() { return billing; }
      public String getBillNr() { return billNr; }
      public User getUser() { return user; }
      public Billings.KmPriceInfo getKpi() { return kpi; }
      public Iterable<Billings.InvoiceLine> getLines() { return lines; }
      public Billings.InvoiceLine getTotals() { return totals; }
      public String getStruct() { return struct; }
    }

    public static class CarInvoiceDetails {
      private Billing billing;
      private String billNr;
      private CarHeaderShort car;
      private UserHeader user;
      private List<OwnerTable> tables;
      private ShortInvoiceLine tableTotal;
      private BillingDetailsCar bCar;
      private int remainingValue;
      private String struct;

      public CarInvoiceDetails(
        Billing billing,
        String billNr,
        CarHeaderShort car,
        UserHeader user,
        List<OwnerTable> tables,
        ShortInvoiceLine tableTotal,
        BillingDetailsCar bCar,
        int remainingValue,
        String struct) {
          this.billing = billing;
          this.billNr = billNr;
          this.car = car;
          this.user = user;
          this.tables = tables;
          this.tableTotal = tableTotal;
          this.bCar = bCar;
          this.remainingValue = remainingValue;
          this.struct= struct;
      }
      public Billing getBilling() { return billing; }
      public String getBillNr() { return billNr; }
      public CarHeaderShort getCar() { return car; }
      public UserHeader getUser() { return user; }
      public List<OwnerTable> getTables() { return tables; }
      public ShortInvoiceLine getTableTotal() { return tableTotal; }
      public BillingDetailsCar getBCar() { return bCar; }
      public int getRemainingValue() { return remainingValue; }
      public String getStruct() { return struct; }
    }
}
