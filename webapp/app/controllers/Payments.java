package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.PaymentDAO;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.dao.PaymentInvoiceDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.DataAccessException;
import java.sql.SQLException;
import com.google.common.primitives.Ints;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.payments.*;
import java.util.*;
import controllers.util.Pagination;
import play.data.validation.Constraints;
import java.time.LocalDate;
import play.data.Form;


/**
 * Actions related to payments.
 */
public class Payments extends Controller {

  public static class PaymentModel {
    public int paymentId;
    public int number;
    public LocalDate date;
    public String accountNumber;
    public int userId;
    public int invoiceId;
    public String invoiceNumber;
    public float invoiceAmount;
    public String name;
    public String address;
    public String bank;
    public float amount;
    public PaymentStatus status;
    public String comment;
    public String structuredCommunication;
    public String currency;
    public String filename;
    public int currentHash;
    public int previousHash;
    public int nextHash;

    public void populate(Payment payment) {
        if (payment == null) {
            return;
        }
        number = payment.getNumber();
        date = payment.getDate();
        accountNumber = payment.getAccountNumber();
        userId = payment.getUserId();
        name = payment.getName();
        address = payment.getAddress();
        bank = payment.getBank();
        amount = payment.getAmount();
        status = payment.getStatus();
        if (status != PaymentStatus.UNASSIGNED
          && !DataAccess.getInjectedContext().getPaymentInvoiceDAO().listInvoicesForPayment(payment.getId()).equals(Collections.emptyList())) {
            invoiceId = DataAccess.getInjectedContext().getPaymentInvoiceDAO().listInvoicesForPayment(payment.getId()).iterator().next().getId();
        } else {
            invoiceId = 0;
        }
        if (status != PaymentStatus.UNASSIGNED
          && !DataAccess.getInjectedContext().getPaymentInvoiceDAO().listInvoicesForPayment(payment.getId()).equals(Collections.emptyList())) {
            invoiceNumber = DataAccess.getInjectedContext().getPaymentInvoiceDAO().listInvoicesForPayment(payment.getId()).iterator().next().getNumber();
        } else {
            invoiceNumber = "";
        }
        if (status != PaymentStatus.UNASSIGNED
                && !DataAccess.getInjectedContext().getPaymentInvoiceDAO().listInvoicesForPayment(payment.getId()).equals(Collections.emptyList())) {
            invoiceAmount = DataAccess.getInjectedContext().getPaymentInvoiceDAO().listInvoicesForPayment(payment.getId()).iterator().next().getAmount();
        } else {
            invoiceAmount = 0;
        }
        comment = payment.getComment();
        structuredCommunication = payment.getStructuredCommunication();
        currency = payment.getCurrency();
        filename = payment.getFilename();
        currentHash = payment.getCurrentHash();
        previousHash = payment.getPreviousHash();
        nextHash = payment.getNextHash();
   }

    // public String validate() {
    //   /* TODO: dit moeten Field Errors worden, en niet één global error */
    //   String error = "";
    //   if (number == 0) {
    //       error += "Geef het betalingsnummer op. ";
    //   }
    //   if (date == null) {
    //       error += "Geef de datum op. ";
    //   }
    //   if (accountNumber == null) {
    //       error += "Geef het rekeningnummer op. ";
    //   }
    //   if (userId == 0) {
    //       error += "Geef de gebruiker op. ";
    //   }
    //   if (bank == null) {
    //       error += "Geef de bankcode op.";
    //   }
    //   if (amount == 0) {
    //       error += "Geef het bedrag op. ";
    //   }
    //   if (structuredCommunication == null) {
    //       error += "Geef de gestructureerde mededeling op. ";
    //   }
    //   if (currency == null) {
    //       error += "Geef de munteenheid op. ";
    //   }
    //
    //   if ("".equals(error)) {
    //       return null;
    //   } else {
    //       return error;
    //   }
    // }

  }

    /**
     * Produce a list of all payments
     */
    @InjectContext
    @AllowRoles
    public static Result getAll() {
        DataAccessContext context = DataAccess.getInjectedContext();
        PaymentDAO dao = context.getPaymentDAO();
        Iterable<Payment> payments = dao.listAllPayments();
        return ok(list.render(payments));
    }

    @AllowRoles
    @InjectContext
    public static Result showDetails(int paymentId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        PaymentDAO dao = context.getPaymentDAO();
        PaymentAndUser paymentAndUser = dao.getPaymentAndUser(paymentId);
        Iterable<Invoice> invoices = context.getPaymentInvoiceDAO().listInvoicesForPayment(paymentId);
        // if (CurrentUser.hasRole(UserRole.PAYMENT_ADMIN)) {
             return ok(details.render(paymentAndUser, invoices));
        // } else {
        //     return badRequest(); // not authorized
        // }
    }

    /**
     * @param page         The page in the paymentlist
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string with form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of payments of the corresponding page
     */
    @AllowRoles
    @InjectContext
    public static Result showPaymentsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {

        return ok(views.html.payments.paymentspage.render(
              DataAccess.getInjectedContext().getPaymentDAO().listPaymentsAndUser(
                      FilterField.stringToField(orderBy, FilterField.NUMBER),
                      Pagination.parseBoolean(ascInt),
                      page, pageSize,
                      Pagination.parseFilter(searchString)
              )
        ));
    }

    /**
     * @return A form to create a new payment
     */
    @AllowRoles
    @InjectContext
    public static Result newPayment() {
      // Payment payment =  new Payment.Builder(0, null, "", 1)
      //     .build();test
        return ok(views.html.payments.add.render(Form.form(PaymentModel.class)));
    }

    /**
     * Method: POST
     *
     * @return redirect to the PaymentForm you just filled in or to the payments-index page
     */
    @AllowRoles
    @InjectContext
    public static Result createNewPayment() {
        Form<PaymentModel> paymentForm = Form.form(PaymentModel.class).bindFromRequest();
        if (paymentForm.hasErrors()) {
            return badRequest(views.html.payments.add.render(paymentForm));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            PaymentDAO pDao = context.getPaymentDAO();
            InvoiceDAO iDao = context.getInvoiceDAO();
            PaymentInvoiceDAO piDao = context.getPaymentInvoiceDAO();
            PaymentModel model = paymentForm.get();

            Payment newPayment = new Payment.Builder(model.number, model.date, model.accountNumber)
                .userId(model.userId)
                .name(model.name)
                .address(model.address)
                .bank(model.bank)
                .amount(model.amount)
                .comment(model.comment)
                .structuredCommunication(model.structuredCommunication)
                .currency(model.currency)
                .filename(model.filename)
                .currentHash(model.currentHash)
                .previousHash(model.previousHash)
                .nextHash(model.nextHash)
                .build();

            pDao.createPayment(newPayment);
            if (model.invoiceId != 0) {
                Invoice invoice = iDao.getInvoice(model.invoiceId);
                Invoice.Builder i = new Invoice.Builder(invoice);
                i.status(InvoiceStatus.PAID).paymentDate(newPayment.getDate());
                DataAccess.getInjectedContext().getInvoiceDAO().updateInvoice(i.build());
                piDao.createPaymentInvoiceRelationship(newPayment, invoice);
            }

            if (newPayment != null) {
                return redirect(routes.Payments.getAll());
            } else {
                paymentForm.error("Failed to add the payment to the database. Contact administrator.");
                flash("danger", "Er trad een onverwachte fout op");
                return badRequest(add.render(paymentForm));
            }
        }
    }

    @AllowRoles
    @InjectContext
    public static Result getInvoicesForPayment(int paymentId) {
        Iterable<Invoice> invoices = DataAccess.getInjectedContext().getPaymentInvoiceDAO().listInvoicesForPayment(paymentId);
        return ok(views.html.invoices.invoicesForPayment.render(invoices));
    }

    @AllowRoles
    @InjectContext
    public static Result getSuggestedInvoicesForPayment(int paymentId) {
        PaymentAndUser paymentAndUser = DataAccess.getInjectedContext().getPaymentDAO().getPaymentAndUser(paymentId);
        Iterable<InvoiceAndUser> invoices = DataAccess.getInjectedContext().getInvoiceDAO().listSuggestedInvoicesForPayment(paymentAndUser.getPayment());
        return ok(views.html.invoices.suggestedInvoicesForPayment.render(paymentAndUser, invoices));
    }

    @AllowRoles
    @InjectContext
    public static Result createPaymentInvoiceRelationship(int invoiceId, int paymentId) {
        Invoice invoice = DataAccess.getInjectedContext().getInvoiceDAO().getInvoice(invoiceId);
        Payment payment = DataAccess.getInjectedContext().getPaymentDAO().getPayment(paymentId);
        Payment.Builder p = new Payment.Builder(payment);
        p.userId(invoice == null ? null : invoice.getUserId())
            .status(invoice == null ? PaymentStatus.UNASSIGNED : PaymentStatus.OK);
        DataAccess.getInjectedContext().getPaymentDAO().updatePayment(p.build());

        Invoice.Builder i = new Invoice.Builder(invoice);
        i.status(InvoiceStatus.PAID).paymentDate(payment.getDate());
        DataAccess.getInjectedContext().getInvoiceDAO().updateInvoice(i.build());

        int relationship = DataAccess.getInjectedContext().getPaymentInvoiceDAO().createPaymentInvoiceRelationship(payment, invoice);
        Iterable<Invoice> invoices = DataAccess.getInjectedContext().getPaymentInvoiceDAO().listInvoicesForPayment(paymentId);
        return ok(views.html.invoices.invoicesForPayment.render(invoices));
    }

    @AllowRoles
    @InjectContext
    public static Result getPaymentsForUser(int userId) {
        Iterable<Payment> payments = DataAccess.getInjectedContext().getPaymentDAO().listPaymentsForUser(userId);
        return ok(views.html.payments.paymentsForUser.render(payments));
    }

    @AllowRoles
    @InjectContext
    public static Result editPayment(int paymentId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        PaymentDAO dao = context.getPaymentDAO();
        Payment payment = dao.getPayment(paymentId);

        // if (isAdmin) {
            PaymentModel paymentModel = new PaymentModel();
            paymentModel.populate(payment);
            Form<PaymentModel> editForm = Form.form(PaymentModel.class).fill(paymentModel);
            return ok(views.html.payments.editPayment.render(editForm, payment));
        // } else {
        //     return badRequest();
        // }
    }

    @AllowRoles
    @InjectContext
    public static Result editPaymentPost(int paymentId) throws SQLException {
        DataAccessContext context = DataAccess.getInjectedContext();
        PaymentDAO pDao = context.getPaymentDAO();
        InvoiceDAO iDao = context.getInvoiceDAO();
        PaymentInvoiceDAO piDao = context.getPaymentInvoiceDAO();
        Payment payment = pDao.getPayment(paymentId);

        Form<PaymentModel> editForm = Form.form(PaymentModel.class).bindFromRequest();
        if (editForm.hasErrors()) {
            // niet nodig?
            flash("danger", "Formulier bevat fouten.");
            return badRequest(views.html.payments.editPayment.render(editForm, payment));
        }
        // if (!isAdmin()) {
        //     return badRequest();
        // }

        PaymentModel paymentModel = editForm.get();
        Invoice invoice = paymentModel.invoiceId == 0 ? null : iDao.getInvoice(paymentModel.invoiceId);

        Payment p = new Payment.Builder(payment.getNumber(), payment.getDate(), payment.getAccountNumber())
            .id(paymentId)
            .userId(invoice == null ? paymentModel.userId : invoice.getUserId())
            .status(invoice == null ? paymentModel.status : PaymentStatus.OK)
            .amount(paymentModel.amount)
            .build();

        pDao.updatePayment(p);

        if (piDao.existsPaymentInvoiceRelationshipForPayment(paymentId)) {
            piDao.unlinkPayment(paymentId);
        }

        if (paymentModel.invoiceId != 0) {
            piDao.createPaymentInvoiceRelationship(pDao.getPayment(paymentId), invoice);
        }

        if (p != null) {
            flash("success", "De betaling werd gewijzigd.");
            return redirect(routes.Payments.showDetails(payment.getId()));
        } else {
            editForm.error("Failed to edit the payment. Contact administrator.");
            flash("danger", "Er trad een onverwachte fout op");
            return badRequest(views.html.payments.editPayment.render(editForm, payment));
        }
    }

}
