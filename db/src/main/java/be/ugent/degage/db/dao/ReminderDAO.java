package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Invoice;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;


public interface ReminderDAO {

    public Iterable<Reminder> listAllReminders() throws DataAccessException;

    public void updateReminder(Reminder reminder) throws DataAccessException;

    //creates the initial reminder for an invoice
    public int createInitialReminder(Invoice invoice)throws DataAccessException;

    //creates the first reminder for an invoice
    public int createFirstReminder(Invoice invoice) throws DataAccessException;

    //creates the second reminder for an invoice
    public int createSecondReminder(Invoice invoice) throws DataAccessException;

    //creates the third reminder for an invoice
    public int createThirdReminder(Invoice invoice) throws DataAccessException;

    public Iterable<Reminder> listRemindersForUser(int userId) throws DataAccessException;

    public List<ReminderAndUserAndInvoice> listUnsentReminders() throws DataAccessException;

    public Page<ReminderAndUserAndInvoice> listReminderAndUserAndInvoice(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    public Page<ReminderAndUserAndInvoice> listReminderAndUserAndInvoice(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException;
    /**
     * When an invoice is paid all reminders should be marked paid
     */
    public void setInvoiceRemindersPaid(Invoice invoice) throws DataAccessException;

    public void setInvoiceRemindersPaid(ArrayList<Integer> invoiceIds) throws DataAccessException;

    public ReminderAndUserAndInvoice getReminderAndUserAndInvoice(int reminderId) throws DataAccessException;

    public List<Reminder> listRemindersForInvoice(int invoiceId) throws DataAccessException;

}
