package controllers.api;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.InvoiceDAO;
import controllers.util.Pagination;
import controllers.Billings;
import com.google.common.base.Strings;
import controllers.util.Addresses;
import controllers.util.ConfigurationHelper;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.time.LocalDate;

import controllers.AllowRoles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.List;


/**
 * Controller responsible for creating, updating and showing of cars
 */
public class ApiInvoices extends Controller {

    /**
   * Gets the list of invoices
   *
   * @return The list of invoices in json format
   */
  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getInvoices() {
    Iterable<Invoice> invoices = DataAccess.getInjectedContext().getInvoiceDAO().listAllInvoices();
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(invoices));
  }

  /**
  * Gets the list of invoices
  *
  * @return The invoice in json format
  */
  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getInvoiceAndUser(int invoiceId) {
    InvoiceAndUser invoice = DataAccess.getInjectedContext().getInvoiceDAO().getInvoiceAndUser(invoiceId);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(invoice));
  }

  /**
  * Gets the list of invoices
  *
  * @return The invoice in json format
  */
  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getInvoiceAndUserByNumber(String number) {
    InvoiceAndUser invoice = DataAccess.getInjectedContext().getInvoiceDAO().getInvoiceAndUserByNumber(number);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(invoice));
  }

  /**
   * Gets the list of invoices
   *
   * @return The list of invoices in json format
   */
   @InjectContext
   @AllowRoles({UserRole.INVOICE_ADMIN})
   public static Result getInvoicesAndUsers() {
     Iterable<InvoiceAndUser> invoices = DataAccess.getInjectedContext().getInvoiceDAO().listAllInvoicesAndUsers();
     Gson gson = GsonHelper.getGson();
     return ok(gson.toJson(invoices));
   }

   @InjectContext
   public static Result getUnpaidInvoices() {
     List<InvoiceAndUser> invoices = DataAccess.getInjectedContext().getInvoiceDAO().listUnpaidInvoicesByUser(CurrentUser.getId());
     return ok(GsonHelper.getGson().toJson(invoices));
   }

    /**
   * Gets the list of invoices
   *
   * @return The list of invoices in json format
   */
  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result filterInvoicesAndUsers(String search) {
    Iterable<InvoiceAndUser> invoices = DataAccess.getInjectedContext().getInvoiceDAO().listInvoicesAndUsers(search);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(invoices));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result findInvoices(int page, int pageSize, int ascInt, String orderBy, String filter) {
      FilterField field = FilterField.stringToField(orderBy, FilterField.DATE);
      boolean asc = Pagination.parseBoolean(ascInt);
      Page<ReminderAndUserAndInvoice> listOfInvoices = DataAccess.getInjectedContext().getInvoiceDAO().listReminderAndUserAndInvoice(field, asc, page, pageSize, filter);
      return ok(GsonHelper.getGson().toJson(listOfInvoices));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result updateInvoice() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      int invoiceId = json.findPath("invoiceId").asInt();
      InvoiceStatus status = InvoiceStatus.valueOf(json.findPath("status").asText());
      Invoice invoice = DataAccess.getInjectedContext().getInvoiceDAO().getInvoice(invoiceId);
      Invoice.Builder invoiceBuilder = new Invoice.Builder(invoice);
      invoiceBuilder.status(status);
      Invoice i = invoiceBuilder.build();
      DataAccess.getInjectedContext().getInvoiceDAO().updateInvoice(i);
      if (i.getStatus() == InvoiceStatus.PAID) {
          DataAccess.getInjectedContext().getReminderDAO().setInvoiceRemindersPaid(i);
      }
      return ok(GsonHelper.getGson().toJson(DataAccess.getInjectedContext().getInvoiceDAO().getInvoiceAndUser(invoiceId)));
    } catch (Exception e) {
      System.err.println("Error in updateInvoice:" + e);
      result.put("status", "KO");
      result.put("message", "Missing parameter in updateInvoice");
      return badRequest(result);
    }
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result createInvoicePaymentRelationship() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      int invoiceId = json.findPath("invoiceId").asInt();
      int paymentId = json.findPath("paymentId").asInt();
      Invoice invoice = DataAccess.getInjectedContext().getInvoiceDAO().getInvoice(invoiceId);
      Payment payment = DataAccess.getInjectedContext().getPaymentDAO().getPayment(paymentId);
      Payment.Builder p = new Payment.Builder(payment);
      p.userId(invoice == null ? null : invoice.getUserId())
          .status(invoice == null ? PaymentStatus.UNASSIGNED : PaymentStatus.OK);
      DataAccess.getInjectedContext().getPaymentDAO().updatePayment(p.build());

      Invoice.Builder i = new Invoice.Builder(invoice);
      i.paymentDate(payment.getDate());
      DataAccess.getInjectedContext().getInvoiceDAO().updateInvoice(i.build());

      int relationship = DataAccess.getInjectedContext().getPaymentInvoiceDAO().createPaymentInvoiceRelationship(payment, invoice);
      updateInvoiceStatus(invoiceId);
      return getInvoiceAndUser(invoiceId);
    } catch (Exception e) {
      result.put("status", "error");
      result.put("message", "Missing parameter [name]");
      return badRequest(result);
    }
  }

  public static void updateInvoiceStatus(int invoiceId) {
    // check if invoice is fully paid, only in that case set status to PAID
    InvoiceAndUser invoiceAndUser = DataAccess.getInjectedContext().getInvoiceDAO().getInvoiceAndUser(invoiceId);
    if (invoiceAndUser.getInvoice().getStatus() != InvoiceStatus.PAID &&
      invoiceAndUser.getInvoice().getPaidAmount() >= invoiceAndUser.getInvoice().getAmount() - controllers.Codas.AMOUNT_DEVIATION) {
        Invoice.Builder i = new Invoice.Builder(invoiceAndUser.getInvoice());
        i.status(InvoiceStatus.PAID);
        DataAccess.getInjectedContext().getInvoiceDAO().updateInvoice(i.build());
    }
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result removePaymentsForInvoice() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      int invoiceId = json.findPath("invoiceId").asInt();

      Iterable<Payment> payments = DataAccess.getInjectedContext().getPaymentInvoiceDAO().listPaymentsForInvoice(invoiceId);
      Iterator it = payments.iterator();
      while (it.hasNext()) {
          Payment payment = (Payment)it.next();
          Payment.Builder p = new Payment.Builder(payment);
          p.status(PaymentStatus.UNASSIGNED);
          DataAccess.getInjectedContext().getPaymentDAO().updatePayment(p.build());
      }

      Invoice invoice = DataAccess.getInjectedContext().getInvoiceDAO().getInvoice(invoiceId);
      Invoice.Builder i = new Invoice.Builder(invoice);
      i.status(InvoiceStatus.OPEN).paymentDate(null);
      DataAccess.getInjectedContext().getInvoiceDAO().updateInvoice(i.build());

      DataAccess.getInjectedContext().getPaymentInvoiceDAO().unlinkInvoice(invoiceId);
      return getInvoiceAndUser(invoiceId);
    } catch (Exception e) {
      result.put("status", "error");
      result.put("message", "Missing parameter [name]");
      return badRequest(result);
    }
  }

  @AllowRoles
  @InjectContext
  public static Result createMembershipInvoice(int userId) {
    InvoiceAndUser invoiceAndUser = DataAccess.getInjectedContext().getInvoiceDAO().createMembershipInvoiceAndUser(userId);
    if (invoiceAndUser != null) {
      Gson gson = GsonHelper.getGson();
      return ok(gson.toJson(invoiceAndUser));
    } else {
      ObjectNode result = Json.newObject();
      result.put("status", "error");
      result.put("message", ("Error while creating membership invoice for user " + userId));
      return badRequest(result);
    }
  }

}
