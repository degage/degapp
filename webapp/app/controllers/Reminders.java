package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.dao.PaymentInvoiceDAO;
import be.ugent.degage.db.dao.BillingDAO;
import be.ugent.degage.db.dao.ReminderDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.DataAccessException;
import com.google.common.primitives.Ints;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.invoices.*;
import views.html.reminders.*;
import controllers.util.Pagination;
import controllers.Billings.*;
import play.data.validation.Constraints;
import java.time.LocalDate;
import play.data.Form;
import java.util.ArrayList;


/**
 * Actions related to payment reminders.
 */
public class Reminders extends Controller {

    public final static int DAYS_FIRST_REMINDER = 5;
    public final static int DAYS_SECOND_REMINDER = 16;
    public final static int DAYS_THIRD_REMINDER = 30;

    @AllowRoles
    @InjectContext
    public static Result sendReminders() throws DataAccessException {
        DataAccessContext context = DataAccess.getInjectedContext();
        InvoiceDAO idao = context.getInvoiceDAO();
        ReminderDAO rdao = context.getReminderDAO();
        Iterable<Invoice> invoices = idao.listAllInvoices();
        ArrayList<Integer> paidInvoiceIds = new ArrayList<Integer>();

        for (Invoice i : invoices) {
            if (i.getStatus() == InvoiceStatus.OVERDUE && i.getDate().isAfter(LocalDate.parse("2016-01-01"))) {
                LocalDate firstReminder = i.getDueDate().plusDays(DAYS_FIRST_REMINDER);
                LocalDate secondReminder = i.getDueDate().plusDays(DAYS_SECOND_REMINDER);
                LocalDate thirdReminder = i.getDueDate().plusDays(DAYS_THIRD_REMINDER);
                if (LocalDate.now().isAfter(firstReminder)) { //send first reminder
                    rdao.createFirstReminder(i);
                }
                if (LocalDate.now().isAfter(secondReminder)) { //send second reminder
                    rdao.createSecondReminder(i);
                }
                if (LocalDate.now().isAfter(thirdReminder)) { //send third reminder
                    rdao.createThirdReminder(i);
                }
            } else if (i.getStatus() == InvoiceStatus.PAID) {
                paidInvoiceIds.add(i.getId());
            }
        }
        if (paidInvoiceIds.size() > 0) {
            rdao.setInvoiceRemindersPaid(paidInvoiceIds);
        }

        flash("success", "De rappels werden verzonden.");
        return ok(views.html.invoices.list.render(invoices));
    }


    @InjectContext
    @AllowRoles
    public static Result getAll() {
        DataAccessContext context = DataAccess.getInjectedContext();
        ReminderDAO dao = context.getReminderDAO();
        Iterable<Reminder> reminders = dao.listAllReminders();
        return ok(listAll.render(reminders));
    }

    /**
     * @param page         The page in the reminders list
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string with form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of payments of the corresponding page
     */
    @AllowRoles
    @InjectContext
    public static Result showRemindersPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {

        return ok(views.html.reminders.reminderspage.render(
                DataAccess.getInjectedContext().getReminderDAO().listReminderAndUserAndInvoice(
                        FilterField.stringToField(orderBy, FilterField.NUMBER),
                        Pagination.parseBoolean(ascInt),
                        page, pageSize,
                        Pagination.parseFilter(searchString)
                )
        ));
    }

    public static void updateReminders(DataAccessContext context) {
      InvoiceDAO idao = context.getInvoiceDAO();

      // update payment status for invoice
      int numChanged = idao.checkDueDate();

      ReminderDAO rdao = context.getReminderDAO();
      Iterable<Invoice> invoices = idao.listAllInvoices();
      ArrayList<Integer> paidInvoiceIds = new ArrayList<Integer>();
      LocalDate afterDate = LocalDate.parse("2017-01-01"); 
      for (Invoice i : invoices) {
          if (i.getAmount() > 0 && i.getStatus() == InvoiceStatus.OPEN && i.getDate().isAfter(afterDate)){
              rdao.createInitialReminder(i);
          }

          if (i.getAmount() > 0 && i.getStatus() == InvoiceStatus.OVERDUE && i.getDate().isAfter(afterDate)) {
              LocalDate firstReminder = i.getDueDate().plusDays(Reminders.DAYS_FIRST_REMINDER);
              LocalDate secondReminder = i.getDueDate().plusDays(Reminders.DAYS_SECOND_REMINDER);
              LocalDate thirdReminder = i.getDueDate().plusDays(Reminders.DAYS_THIRD_REMINDER);
              if (LocalDate.now().isAfter(firstReminder)) { //send first reminder
                  rdao.createFirstReminder(i);
              }
              if (LocalDate.now().isAfter(secondReminder)) { //send second reminder
                  rdao.createSecondReminder(i);
              }
              if (LocalDate.now().isAfter(thirdReminder)) { //send third reminder
                  rdao.createThirdReminder(i);
              }
          } else if (i.getStatus() == InvoiceStatus.PAID) {
              paidInvoiceIds.add(i.getId());
          }
      }
      if (paidInvoiceIds.size() > 0) {
          rdao.setInvoiceRemindersPaid(paidInvoiceIds);
      }
    }

}
