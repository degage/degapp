package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.dao.PaymentDAO;
import be.ugent.degage.db.dao.ReminderDAO;
import be.ugent.degage.db.dao.PaymentInvoiceDAO;
import be.ugent.degage.db.dao.BillingDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.DataAccessException;
import java.sql.SQLException;
import com.google.common.primitives.Ints;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.invoices.*;
import views.html.payments.*;
import controllers.util.Pagination;
import controllers.Billings.*;
import play.data.validation.Constraints;
import java.time.LocalDate;
import play.data.Form;


/**
 * Actions related to invoices.
 */
public class Invoices extends Controller {

    public static class InvoiceData {
        public String number;
        public LocalDate date;
        public LocalDate paymentDate;
        public LocalDate dueDate;
        public float amount;
        @Constraints.Required
        public InvoiceStatus status;
        public String structuredCommunication;

        public void populate(Invoice invoice) {
            if (invoice == null) {
                return;
            }
            number = invoice.getNumber();
            date = invoice.getDate();
            paymentDate = invoice.getPaymentDate();
            dueDate = invoice.getDueDate();
            amount = invoice.getAmount();
            status = invoice.getStatus();
            structuredCommunication = invoice.getStructuredCommunication();
       }
    }

    /**
     * Produce a list of all invoices
     */
    @InjectContext
    @AllowRoles
    public static Result getAll() {
        DataAccessContext context = DataAccess.getInjectedContext();
        InvoiceDAO dao = context.getInvoiceDAO();
        Iterable<Invoice> invoices = dao.listAllInvoices();
        return ok(views.html.invoices.list.render(invoices));
    }

    @InjectContext
    @AllowRoles
    public static Result saveInvoices(int billingId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        InvoiceDAO iDao = context.getInvoiceDAO();
        BillingDAO bDao = context.getBillingDAO();
        Billing billing = bDao.getBilling(billingId);

        //all user invoices
        Page<User> userList = context.getUserDAO().getUserList(FilterField.NAME, true, 1, 1000000, Pagination.parseFilter("role=CAR_USER"));
        for (User user: userList) {
            BillingDetailsUser bUser = bDao.getUserDetails(billingId, user.getId());

            if (bUser != null) {

                KmPriceDetails priceDetails = bDao.getKmPriceDetails(billingId);
                Iterable<Billings.InvoiceLine> invoiceLines = Billings.getInvoiceLines(
                        bDao.listTripDetails(billingId, user.getId(), false),
                        bDao.listFuelDetails(billingId, user.getId(), false),
                        priceDetails
                );
                Billings.InvoiceLine il = Billings.total(invoiceLines, priceDetails.getFroms().length);
                float amount = (float) il.getTotal()/100; //getTotal returns eurocents, amount is in euro

                String invoiceNumber = String.format("A%s-%04d", billing.getPrefix(), bUser.getIndex());

                Invoice i =  new Invoice.Builder(invoiceNumber, user.getId(), billingId)
                        .date(billing.getDriversDate())
                        .dueDate(billing.getDriversDate().plusWeeks(2))
                        .amount(amount)
                        .type(InvoiceType.CAR_USER)
                        .structuredCommunication(Billings.structuredComment(billingId, 0, bUser))
                        .build();

                if (iDao.checkUniqueInvoice(i)) {
                    int id = iDao.createInvoice(i);
                } else {
                    //invoice already exists
                }
            }
        }

        //all car(owner) invoices
        Page<CarHeaderAndOwner> cars = context.getCarDAO().listCarsAndOwners(FilterField.NAME, true, 1, 100000, Pagination.parseFilter(""));
        for (CarHeaderAndOwner carHeaderAndOwner: cars){
            CarInvoiceDetails carInvoiceDetails = Billings.getCarInvoiceDetails(carHeaderAndOwner, billing);

            if (carInvoiceDetails != null) {

                BillingDetailsCar details = carInvoiceDetails.getBCar();
                UserHeader user = carInvoiceDetails.getUser();
                String invoiceNumber = carInvoiceDetails.getBillNr();
                ShortInvoiceLine sil = carInvoiceDetails.getTableTotal();

                float amount = (float) (-details.getRecuperatedCarCosts()+details.getOwnerFuelDue()-details.getOwnerFuelPaid()-details.getRecuperatedDepreciationCost())/100;

                Invoice i =  new Invoice.Builder(invoiceNumber, user.getId(), billingId)
                        .date(billing.getOwnersDate())
                        .dueDate(billing.getOwnersDate().plusWeeks(2))
                        .amount(amount)
                        .type(InvoiceType.CAR_OWNER)
                        .structuredCommunication(carInvoiceDetails.getStruct())
                        .carId(carHeaderAndOwner.getId())
                        .build();

                if (iDao.checkUniqueInvoice(i)) {
                    int id = iDao.createInvoice(i);
                } else {
                    //invoice already exists
                }
            }
        }

        return redirect(routes.BillingsAdmin.listAll());
    }

    /**
     * @param page         The page in the invoicelists
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string with form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of invoices of the corresponding page
     */
    @AllowRoles
    @InjectContext
    public static Result showInvoicesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {

        return ok(views.html.invoices.invoicespage.render(
            DataAccess.getInjectedContext().getInvoiceDAO().listReminderAndUserAndInvoice(
                FilterField.stringToField(orderBy, FilterField.DEFAULT),
                Pagination.parseBoolean(ascInt),
                page, pageSize,
                searchString
            )
        ));
    }

    @AllowRoles
    @InjectContext
    public static Result showDetails(int invoiceId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        InvoiceDAO dao = context.getInvoiceDAO();
        Invoice invoice = dao.getInvoice(invoiceId);
        // if (CurrentUser.hasRole(UserRole.INVOICE_ADMIN)) {
             return ok(views.html.invoices.details.render(invoice));
        // } else {
        //     return badRequest(); // not authorized
        // }
    }

    @AllowRoles
    @InjectContext
    public static Result editInvoice(int invoiceId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        InvoiceDAO dao = context.getInvoiceDAO();
        Invoice invoice = dao.getInvoice(invoiceId);

        // if (isAdmin) {
            InvoiceData invoiceData = new InvoiceData();
            invoiceData.populate(invoice);
            Form<InvoiceData> editForm = Form.form(InvoiceData.class).fill(invoiceData);
            return ok(views.html.invoices.editInvoice.render(editForm, invoice));
        // } else {
        //     return badRequest();
        // }
    }

    @AllowRoles
    @InjectContext
    public static Result editInvoicePost(int invoiceId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        InvoiceDAO dao = context.getInvoiceDAO();
        Invoice invoice = dao.getInvoice(invoiceId);

        Form<InvoiceData> editForm = Form.form(InvoiceData.class).bindFromRequest();
        if (editForm.hasErrors()) {
            // niet nodig?
            flash("danger", "Formulier bevat fouten.");
            return badRequest(editInvoice.render(editForm, invoice));
        }
        // if (!isAdmin()) {
        //     return badRequest();
        // }

        InvoiceData invoiceData = editForm.get();

        Invoice i =  new Invoice.Builder(invoice.getNumber(), invoice.getUserId(), invoice.getBillingId())
                .id(invoice.getId())
                .amount(invoice.getAmount())
                .date(invoice.getDate())
                .paymentDate(invoiceData.paymentDate)
                .status(invoiceData.status)
                .build();

        //update the invoice and set the reminders on "paid" if status = PAID
        dao.updateInvoice(i);
        if (i.getStatus() == InvoiceStatus.PAID) {
            context.getReminderDAO().setInvoiceRemindersPaid(i);
        }

        flash("success", "Jouw wijzigingen werden met succes toegepast.");
        return redirect(routes.Invoices.showDetails(invoice.getId()));
    }


    @AllowRoles
    @InjectContext
    public static Result getSuggestedPaymentsForInvoice(int invoiceId) {
        InvoiceAndUser invoiceAndUser = DataAccess.getInjectedContext().getInvoiceDAO().getInvoiceAndUser(invoiceId);
        Iterable<PaymentAndUser> payments = DataAccess.getInjectedContext().getPaymentDAO().listSuggestedPaymentsForInvoice(invoiceAndUser);
        return ok(views.html.payments.suggestedPaymentsForInvoice.render(invoiceAndUser, payments));
    }

    @AllowRoles
    @InjectContext
    public static Result getPaymentsForInvoice(int invoiceId) {
        Iterable<Payment> payments = DataAccess.getInjectedContext().getPaymentInvoiceDAO().listPaymentsForInvoice(invoiceId);
        return ok(views.html.payments.paymentsForInvoice.render(payments));
    }

    @AllowRoles
    @InjectContext
    public static Result getInvoicesForUser(int userId) {
        Iterable<InvoiceAndUser> invoices = DataAccess.getInjectedContext().getInvoiceDAO().listInvoicesForUser(userId);
        return ok(views.html.invoices.invoicesForUser.render(invoices));
    }

    @AllowRoles
    @InjectContext
    public static Result checkDueDate() throws DataAccessException {
      DataAccessContext context = DataAccess.getInjectedContext();
      InvoiceDAO dao = context.getInvoiceDAO();
      Iterable<Invoice> invoices = dao.listAllInvoices();
      int numChanged = dao.checkDueDate();
      flash("success", "Er werden " + numChanged + " facturen gewijzigd.");
      return ok(views.html.invoices.list.render(invoices));
    }


    @AllowRoles
    @InjectContext
    public static Result unlinkInvoice(int invoiceId) throws SQLException {
        try {
            DataAccessContext context = DataAccess.getInjectedContext();
            InvoiceDAO iDao = context.getInvoiceDAO();
            PaymentDAO pDao = context.getPaymentDAO();
            PaymentInvoiceDAO piDao = context.getPaymentInvoiceDAO();

            Iterable<Payment> payments = piDao.listPaymentsForInvoice(invoiceId);
            for (Payment p : payments) {
                Payment.Builder pb = new Payment.Builder(p);
                pb.status(PaymentStatus.UNASSIGNED).userId(0);
                //TODO what if payment is linked to other invoice ?
                pDao.updatePayment(pb.build());
            }

            piDao.unlinkInvoice(invoiceId);

            Invoice i = iDao.getInvoice(invoiceId);
            Invoice.Builder ib = new Invoice.Builder(i);
            ib.status(InvoiceStatus.OPEN).paymentDate(null);
            iDao.updateInvoice(ib.build());

            flash("success", "De link(s) met afrekening " + i.getNumber() + " werd(en) succesvol verwijderd.");
            return showDetails(invoiceId);
        } catch (SQLException e) {
            throw new SQLException("De link met afrekening kon niet verwijderd worden. " + e.getMessage());
        }
    }

}
