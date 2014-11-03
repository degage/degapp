package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import org.joda.time.DateTime;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.receipts.receipts;
import views.html.receipts.receiptspage;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.Period;
import java.util.List;

public class Receipts extends Controller {
    private static Date date;
    private static User user;

    private static List<CarRide> rides;
    private static List<Refuel> refuels;
    private static List<CarCost> carcosts;

    private static final int PAGE_SIZE = 10;

    /**
     * @return The users index-page with all users
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result index() {
        return ok(receipts.render());
    }

    /**
     * @param page    The page in the userlists
     * @param ascInt  An integer representing ascending (1) or descending (0)
     * @param orderBy A field representing the field to order on
     * @return A partial page with a table of users of the corresponding page
     */
    // @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result showReceiptsPage(int page, int ascInt, String orderBy, String date) {
        // TODO: orderBy not as String-argument?
        FilterField receiptsField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(date);
        return ok(receiptsList(page, receiptsField, asc, filter));
    }

    // used within inject context
    private static Html receiptsList(int page, FilterField orderBy, boolean asc, Filter filter) {
        User currentUser = DataProvider.getUserProvider().getUser();
        ReceiptDAO dao = DataAccess.getInjectedContext().getReceiptDAO();

        if (orderBy == null) {
            orderBy = FilterField.RECEIPT_DATE;
        }
        List<Receipt> listOfReceipts = dao.getReceiptsList(orderBy, asc, page, PAGE_SIZE, filter, currentUser);

        int amountOfResults = dao.getAmountOfReceipts(filter, currentUser);
        //int amountOfResults = listOfReceipts.size();
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) PAGE_SIZE);

        //if(){rendernew()}
        return receiptspage.render(listOfReceipts, page, amountOfResults, amountOfPages);
    }

    // TODO: should go in a separate helper class or even a separate module
    public static void generateReceipt(User u, Date d, Costs costInfo) {
        try {
            date = d;
            user = u;

            Document document = new Document();
            String name = user.getId() + "." + date;
            String filename = FileHelper.getGeneratedFilesPath(name + ".pdf", "receipts");
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();
            addToDataBase(name, filename, generatePDF(document, costInfo));
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void addToDataBase(String name, String filename, BigDecimal price) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReceiptDAO dao = context.getReceiptDAO();
        FileDAO fdao = context.getFileDAO();
        File file = fdao.createFile(filename, name, "pdf");

        dao.createReceipt(name, new DateTime(date), file, user, price);
    }

    private static BigDecimal generatePDF(Document document, Costs costInfo) {
        BigDecimal saldo = BigDecimal.ZERO;
        try {
            String imageUrl = "https://fbcdn-sphotos-e-a.akamaihd.net/hphotos-ak-frc3/t1.0-9/969296_656566794396242_1002112915_n.jpg";
            Image image = Image.getInstance(new URL(imageUrl));
            image.setAlignment(Image.LEFT | Image.TEXTWRAP);
            image.scaleAbsolute(60f, 60f);
            document.add(image);

            Instant report = date.toInstant();

            PdfPTable table = new PdfPTable(3);
            add(table, "Afrekening n°:");
            add(table, user.getId() + "." + date, true);
            add(table, "");
            add(table, "Naam:");
            add(table, "" + user, true);
            add(table, "");
            add(table, "Adres:");
            add(table, "" + user.getAddressDomicile(), true);
            add(table, "");
            add(table, "Periode:", false, false);
            add(table, "vanaf " + new SimpleDateFormat("dd-MM-yyyy").format(report.minus(Period.ofMonths(3))), false, false);
            add(table, "t.e.m. " + new SimpleDateFormat("dd-MM-yyyy").format(report.minus(Period.ofDays(1))), false, false);

            table.setSpacingAfter(20);

            document.add(table);

            getLoanerBillData(date, user.getId());
            saldo = createLoanerTable(document, costInfo);

            DataAccessContext context = DataAccess.getContext();
            // TODO: inject context
            for (Car car : context.getCarDAO().listCarsOfUser(user.getId())) {
                getCarBillData(date, car.getId());
                saldo = saldo.add(createCarTable(document, car.getName(), costInfo));
            }

            Font f = new Font(FontFamily.COURIER, 8);
            Font f2 = new Font(FontFamily.COURIER, 6);
            document.add(new Paragraph("Rekeningnummer 523-080452986-86 -IBAN BE78 5230 8045 -BIC Code TRIOBEBB", f));
            document.add(new Paragraph("Degage! vzw - Fuchsiastraat 81, 9000 Gent", f));
            document.add(new Paragraph("Gelieve de afrekening te betalen voor " + new SimpleDateFormat("dd-MM-yyyy").format(report.plus(Period.ofMonths(3))), f));
            document.add(new Paragraph("Bij betaling, gelieve het nummer van de afrekening te vermelden", f2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return saldo;
    }

    private static BigDecimal createCarTable(Document document, String carName, Costs costInfo)
            throws DocumentException {
        document.newPage();
        document.add(new Paragraph("WAGEN: " + carName));
        PdfPTable carTable = new PdfPTable(2);
        carTable.setSpacingBefore(5);
        carTable.setSpacingAfter(10);

        int loanerDist = 0;
        int othersDist = 0;

        for (CarRide ride : rides) {
            if (ride.getReservation().getUser() == ride.getReservation().getCar().getOwner()) {
                loanerDist += ride.getEndMileage() - ride.getStartMileage();
            } else {
                othersDist += ride.getEndMileage() - ride.getStartMileage();
            }
        }

        add(carTable, "Totaal aantal kilometers:", true);
        add(carTable, (loanerDist + othersDist) + " km", true);

        // TODO: date
        double deprecation = costInfo.getDeprecation();

        if (loanerDist + othersDist > 0) {
            add(carTable, "Door eigenaar gereden:");
            add(carTable, loanerDist + " km");
            add(carTable, "Percentage eigenaar:");
            add(carTable, Math.round(100 * (double) loanerDist / (double) (loanerDist + othersDist)) + "%");
            add(carTable, "Percentage gedeeld:");
            add(carTable, Math.round(100 * (double) othersDist / (double) (loanerDist + othersDist)) + "%");
            add(carTable, "Afschrijving per kilometer:");
            add(carTable, "€ " + deprecation);

            deprecation *= (double) othersDist;
            add(carTable, "Afschrijving voor deze periode:", true);
            add(carTable, "€ " + deprecation, true);
        }

        add(carTable, "");
        add(carTable, "");

        BigDecimal carCostAmount = BigDecimal.ZERO;
        for (CarCost carcost : carcosts) {
            carCostAmount = carCostAmount.add(carcost.getAmount());
        }

        add(carTable, "Vaste kosten af te schrijven:");
        add(carTable, "€ " + carCostAmount);

        BigDecimal recupCosts = BigDecimal.ZERO;

        if (loanerDist + othersDist > 0) {
            recupCosts = (carCostAmount.multiply(new BigDecimal(othersDist)).divide(new BigDecimal(loanerDist + othersDist)));
            add(carTable, "Recuperatie vaste kosten:", true);
            add(carTable, "€ " + recupCosts, true);
            add(carTable, "Ter info: Vaste kost per kilometer:");
            add(carTable, "€ " + carCostAmount.divide(new BigDecimal(loanerDist + othersDist)));
        }

        add(carTable, "");
        add(carTable, "");

        BigDecimal refuelOthers = BigDecimal.ZERO;
        BigDecimal refuelOwner = BigDecimal.ZERO;

        for (Refuel refuel : refuels) {
            if (refuel.getCarRide().getCost().compareTo(BigDecimal.ZERO) == 0) {
                refuelOwner = refuelOwner.add(refuel.getAmount());
            } else {
                refuelOthers = refuelOthers.add(refuel.getAmount());
            }
        }

        add(carTable, "Totaal brandstof:");
        add(carTable, "€ " + (refuelOthers.add(refuelOwner)));

        BigDecimal refuelTot = BigDecimal.ZERO;

        if (loanerDist + othersDist > 0) {
            add(carTable, "Brandstof per kilometer:");
            add(carTable, "€ " + refuelOthers.add(refuelOwner).divide(new BigDecimal(loanerDist + othersDist)));
            add(carTable, "Te betalen brandstof:");
            add(carTable, "€ " + refuelOthers.add(refuelOwner).multiply(new BigDecimal(loanerDist)).divide(new BigDecimal(loanerDist + othersDist)));
            add(carTable, "Brandstof reeds betaald:");
            add(carTable, "€ " + refuelOwner);
            refuelTot = refuelOwner.subtract(refuelOthers.add(refuelOwner).multiply(new BigDecimal(loanerDist)).divide(new BigDecimal(loanerDist + othersDist)));
            add(carTable, "Saldo brandstof:", true);
            add(carTable, "€ " + refuelTot, true);
        }

        add(carTable, "");
        add(carTable, "");

        BigDecimal total = recupCosts.add(refuelTot).add(new BigDecimal(deprecation));
        add(carTable, "SALDO WAGEN '" + carName + "'", true);
        add(carTable, "€ " + total, true);

        document.add(carTable);

        return total;
    }

    private static BigDecimal createLoanerTable(Document document, Costs costInfo)
            throws DocumentException {
        document.add(new Paragraph("Ritten"));

        int levels = costInfo.getLevels();

        PdfPTable drivesTable = new PdfPTable(4 + levels);
        drivesTable.setWidthPercentage(100);
        drivesTable.setSpacingBefore(5);
        drivesTable.setSpacingAfter(10);

        add(drivesTable, "Auto", true, false);
        add(drivesTable, "Datum", true, false);
        add(drivesTable, "Afstand", true, false);

        int lower = 0;
        int upper = 0;

        for (int j = 0; j < levels; j++) {
            if (j > 0)
                lower = upper;

            if (j < levels - 1) {
                upper = costInfo.getLimit(j);
                add(drivesTable, lower + "-" + upper + " km", true, false);
            } else {
                add(drivesTable, "> " + upper + " km", true, false);
            }
        }
        add(drivesTable, "Ritprijs", true, false);

        add(drivesTable, "", true);
        add(drivesTable, "", true);
        add(drivesTable, "", true);

        for (int j = 0; j < levels; j++) {
            add(drivesTable, "€" + costInfo.getCost (j) + "/km");
        }

        add(drivesTable, "", true);

        int totalDistance = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        int[] totals = new int[levels];

        for (CarRide ride : rides) {

            int distance = ride.getEndMileage() - ride.getStartMileage();
            totalDistance += distance;

            add(drivesTable, ride.getReservation().getCar().getName());
            add(drivesTable, new SimpleDateFormat("dd-MM-yyyy").format(ride.getReservation().getFrom().toDate()));
            add(drivesTable, distance + " km");

            int level;
            lower = 0;
            for (level = 0; level < levels; level++) {
                int limit = 0;
                int d;

                // TODO: refactor
                if (level == levels - 1 || distance <= (limit = costInfo.getLimit(level)))
                    d = distance;
                else
                    d = limit - lower;

                totals[level] += d;
                distance -= d;
                add(drivesTable, d + " km");

                if (distance == 0) {
                    level++;
                    break;
                }

                lower = limit;
            }

            for (int i = level; i < levels; i++) {
                add(drivesTable, "");
            }

            totalCost = totalCost.add(ride.getCost());

            if (ride.getCost().compareTo(BigDecimal.ZERO) != 0)
                add(drivesTable, "€ " + ride.getCost(), true);
            else
                add(drivesTable, "--", true);
        }

        add(drivesTable, "TOTALEN", true);
        add(drivesTable, "");
        add(drivesTable, totalDistance + " km", true);

        for (int j = 0; j < levels; j++) {
            add(drivesTable, totals[j] + " km", true);
        }

        add(drivesTable, "€ " + totalCost, true);

        document.add(drivesTable);

        document.add(new Paragraph("Tankbeurten"));
        PdfPTable refuelsTable = new PdfPTable(3);
        refuelsTable.setWidthPercentage(100);
        refuelsTable.setSpacingBefore(5);
        refuelsTable.setSpacingAfter(10);

        add(refuelsTable, "Auto", true);
        add(refuelsTable, "Datum", true);
        add(refuelsTable, "Prijs", true);

        BigDecimal refuelTotal = BigDecimal.ZERO;

        for (Refuel refuel : refuels) {
            add(refuelsTable, refuel.getCarRide().getReservation().getCar().getName());
            add(refuelsTable, new SimpleDateFormat("dd-MM-yyyy").format(refuel.getCarRide().getReservation().getFrom().toDate()));
            if (refuel.getCarRide().getCost().compareTo(BigDecimal.ZERO) != 0) {
                add(refuelsTable, "€ " + refuel.getAmount(), true);
                refuelTotal = refuelTotal.add(refuel.getAmount());
            } else
                add(refuelsTable, "-- € " + refuel.getAmount(), true);
        }

        add(refuelsTable, "TOTAAL", true);
        add(refuelsTable, "");
        add(refuelsTable, "€ " + refuelTotal, true);

        document.add(refuelsTable);

        PdfPTable totalTable = new PdfPTable(3);
        totalTable.setSpacingBefore(5);
        totalTable.setSpacingAfter(10);

        add(totalTable, "Totaal ritten", true);
        add(totalTable, "Totaal tankbeurten", true);
        add(totalTable, "SALDO", true);

        add(totalTable, "+ € " + totalCost);
        add(totalTable, "- € " + refuelTotal);
        add(totalTable, "€ " + totalCost.subtract(refuelTotal), true);

        document.add(totalTable);

        return totalCost.subtract(refuelTotal);
    }


    private static void add(PdfPTable table, String contents, boolean fat) {
        add(table, contents, fat, true);
    }

    private static void add(PdfPTable table, String contents, boolean fat, boolean border) {
        Font f = new Font(FontFamily.COURIER, 8);
        if (fat) {
            f = new Font(FontFamily.COURIER, 8, Font.BOLD);
        }
        PdfPCell cell = new PdfPCell(new Paragraph(contents, f));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (border) {
            cell.setPaddingBottom(5);
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderColor(BaseColor.BLACK);
        } else {
            cell.setBorder(Rectangle.NO_BORDER);
        }
        table.addCell(cell);
    }

    private static void add(PdfPTable table, String contents) {
        add(table, contents, false);
    }

    private static void getLoanerBillData(Date d, int user) {
        DataAccessContext context = DataAccess.getInjectedContext();
        CarRideDAO cdao = context.getCarRideDAO();
        RefuelDAO rdao = context.getRefuelDAO();
        rides = cdao.getBillRidesForLoaner(d, user);
        refuels = rdao.getBillRefuelsForLoaner(d, user);
    }

    private static void getCarBillData(Date d, int car) {

        DataAccessContext context = DataAccess.getInjectedContext();
        CarRideDAO crdao = context.getCarRideDAO();
        RefuelDAO rdao = context.getRefuelDAO();
        CarCostDAO ccdao = context.getCarCostDAO();
        rides = crdao.getBillRidesForCar(d, car);
        refuels = rdao.getBillRefuelsForCar(d, car);
        carcosts = ccdao.getBillCarCosts(d, car);
    }
}
