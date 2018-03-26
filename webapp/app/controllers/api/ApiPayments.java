package controllers.api;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Payment;
import be.ugent.degage.db.models.PaymentAndUser;
import be.ugent.degage.db.models.PaymentStatus;
import be.ugent.degage.db.models.Invoice;
import be.ugent.degage.db.models.InvoiceStatus;
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import com.google.common.base.Strings;
import controllers.util.Addresses;
import controllers.util.ConfigurationHelper;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.cars.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import controllers.AllowRoles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import be.ugent.degage.db.models.UserRole;

/**
 * Controller responsible for creating, updating and showing of cars
 */
public class ApiPayments extends Controller {

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getPayment(int paymentId) {
    Payment payment = DataAccess.getInjectedContext().getPaymentDAO().getPayment(paymentId);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(payment));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getPaymentAndUser(int paymentId) {
    PaymentAndUser payment = DataAccess.getInjectedContext().getPaymentDAO().getPaymentAndUser(paymentId);
    return ok(GsonHelper.getGson().toJson(payment));
  }

    /**
   * Gets the list of payments
   *
   * @return The list of payments in json format
   */
  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getPayments() {
    Iterable<Payment> payments = DataAccess.getInjectedContext().getPaymentDAO().listAllPayments();
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(payments));
  }

  /**
  * Gets the list of payments
  *
  * @return The list of payments in json format
  */
  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getPaymentsAndUsers() {
    Iterable<PaymentAndUser> payments = DataAccess.getInjectedContext().getPaymentDAO().listAllPaymentsAndUsers();
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(payments));
  }

    /**
   * Gets the list of payments
   *
   * @return The list of payments in json format
   */
  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result filterPaymentsAndUsers(String search) {
    Iterable<PaymentAndUser> payments = DataAccess.getInjectedContext().getPaymentDAO().listPaymentsAndUsers(null, true, 1, 100000000, search);
    Gson gson = GsonHelper.getGson();
    return ok(gson.toJson(payments));
  }

  /**
   * @param page         The page in the paymentsists
   * @param ascInt       An integer representing ascending (1) or descending (0)
   * @param orderBy      A field representing the field to order on
   * @param filter        A string to filter on
   * @return A collection of payments
   */
  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result findPayments(int page, int pageSize, int ascInt, String orderBy, String filter) {
      FilterField field = FilterField.stringToField(orderBy, FilterField.DATE);
      boolean asc = Pagination.parseBoolean(ascInt);
      Page<PaymentAndUser> listOfPayments = DataAccess.getInjectedContext().getPaymentDAO().listPaymentsAndUsers(field, asc, page, pageSize, filter);
      return ok(GsonHelper.getGson().toJson(listOfPayments));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result createPaymentInvoiceRelationship() {
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
      ApiInvoices.updateInvoiceStatus(invoiceId);
      return getPaymentAndUser(paymentId);
    } catch (Exception e) {
      result.put("status", "error");
      result.put("message", e.getMessage());
      return badRequest(result);
    }
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result removeInvoicesForPayment() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      int paymentId = json.findPath("paymentId").asInt();

      Iterable<Invoice> invoices = DataAccess.getInjectedContext().getPaymentInvoiceDAO().listInvoicesForPayment(paymentId);
      Iterator it = invoices.iterator();
      while (it.hasNext()) {
         Invoice invoice = (Invoice)it.next();
         Invoice.Builder i = new Invoice.Builder(invoice);
         i.status(InvoiceStatus.OPEN).paymentDate(null);
         DataAccess.getInjectedContext().getInvoiceDAO().updateInvoice(i.build());
      }

      Payment payment = DataAccess.getInjectedContext().getPaymentDAO().getPayment(paymentId);
      Payment.Builder p = new Payment.Builder(payment);
      p.status(PaymentStatus.UNASSIGNED);
      DataAccess.getInjectedContext().getPaymentDAO().updatePayment(p.build());

      DataAccess.getInjectedContext().getPaymentInvoiceDAO().unlinkPayment(paymentId);
      return getPaymentAndUser(paymentId);
    } catch (Exception e) {
      result.put("status", "error");
      result.put("message", "Missing parameter [name]");
      return badRequest(result);
    }
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result updateUserForPayment() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      int paymentId = json.findPath("paymentId").asInt();
      int userId = json.findPath("userId").asInt();

      Payment payment = DataAccess.getInjectedContext().getPaymentDAO().getPayment(paymentId);
      Payment.Builder p = new Payment.Builder(payment);
      p.userId(userId);
      DataAccess.getInjectedContext().getPaymentDAO().updatePayment(p.build());
      return getPaymentAndUser(paymentId);
    } catch (Exception e) {
      result.put("status", "error");
      result.put("message", e.getMessage());
      return badRequest(result);
    }
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result updateStatusForPayment() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      int paymentId = json.findPath("paymentId").asInt();
      String status = json.findPath("status").asText();
      Payment payment = DataAccess.getInjectedContext().getPaymentDAO().getPayment(paymentId);
      Payment.Builder p = new Payment.Builder(payment);
      p.status(PaymentStatus.valueOf(status));
      DataAccess.getInjectedContext().getPaymentDAO().updatePayment(p.build());
      return getPaymentAndUser(paymentId);
    } catch (Exception e) {
      result.put("status", "error");
      result.put("message", e.getMessage());
      return badRequest(result);
    }
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result updateIncludeInBalanceForPayment() {
    ObjectNode result = Json.newObject();
    try {
      JsonNode json = request().body().asJson();
      int paymentId = json.findPath("paymentId").asInt();
      boolean includeInBalance = json.findPath("includeInBalance").asBoolean();

      Payment payment = DataAccess.getInjectedContext().getPaymentDAO().getPayment(paymentId);
      Payment.Builder p = new Payment.Builder(payment);
      p.includeInBalance(includeInBalance);
      DataAccess.getInjectedContext().getPaymentDAO().updatePayment(p.build());
      return getPaymentAndUser(paymentId);
    } catch (Exception e) {
      result.put("status", "error");
      result.put("message", e.getMessage());
      return badRequest(result);
    }
  }

}
