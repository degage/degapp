package controllers.api;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.Reminder;
import be.ugent.degage.db.models.Invoice;
import be.ugent.degage.db.models.InvoiceAndUser;
import be.ugent.degage.db.models.InvoiceStatus;
import be.ugent.degage.db.models.ReminderAndUserAndInvoice;
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import controllers.util.Pagination;
import notifiers.Notifier;
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

import controllers.AllowRoles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.time.LocalDate;
import controllers.Reminders;


/**
 * Controller responsible for creating, updating and showing of cars
 */
public class ApiReminders extends Controller {

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getReminderAndUserAndInvoice(int reminderId) {
    ReminderAndUserAndInvoice reminder = DataAccess.getInjectedContext().getReminderDAO().getReminderAndUserAndInvoice(reminderId);
    return ok(GsonHelper.getGson().toJson(reminder));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result findReminders(int page, int pageSize, int ascInt, String orderBy, String filter) {
      FilterField field = FilterField.stringToField(orderBy, FilterField.DATE);
      boolean asc = Pagination.parseBoolean(ascInt);
      Page<ReminderAndUserAndInvoice> listOfReminders = DataAccess.getInjectedContext().getReminderDAO().listReminderAndUserAndInvoice(field, asc, page, pageSize, filter);
      return ok(GsonHelper.getGson().toJson(listOfReminders));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result createReminders() {
    Reminders.updateReminders(DataAccess.getInjectedContext());
    ObjectNode result = Json.newObject();
    result.put("status", "ok");
    result.put("message", "Reminders werden succesvol aangemaakt.");
    return ok(result);
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result sendMail(int reminderId) {
    ReminderDAO rdao = DataAccess.getInjectedContext().getReminderDAO();
    ReminderAndUserAndInvoice reminderAndUserAndInvoice = rdao.getReminderAndUserAndInvoice(reminderId);
    LocalDate lastPaymentDate = DataAccess.getInjectedContext().getPaymentDAO().getLastPaymentDate();
    Notifier.sendPaymentReminderMail(DataAccess.getInjectedContext(), reminderAndUserAndInvoice, lastPaymentDate);
    return getReminderAndUserAndInvoice(reminderId);
  }
}
