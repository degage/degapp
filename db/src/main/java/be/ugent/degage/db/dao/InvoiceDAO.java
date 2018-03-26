package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Invoice;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface InvoiceDAO {

    /**
     * Return a list of all invoices
     */
    public Iterable<Invoice> listAllInvoices ();

    public Iterable<InvoiceAndUser> listInvoicesAndUsersOnType(InvoiceType type, Set<UserStatus> statusFilter);
    public List<InvoiceAndUser> listAllInvoicesAndUsers();

    public Iterable<InvoiceAndUser> listInvoicesAndUsers(String user);

    public int createInvoice(Invoice invoice) throws DataAccessException;

    /**
     * Returns true if invoice doesn't exist already
     */
    public boolean checkUniqueInvoice(Invoice invoice) throws DataAccessException;

    public boolean checkExistingInvoice(int userId, InvoiceType invoiceType) throws DataAccessException;

    public void updateInvoice(Invoice invoice) throws DataAccessException;

    public Invoice getInvoice(int invoiceId) throws DataAccessException;

    public InvoiceAndUser getInvoiceAndUser(int invoiceId) throws DataAccessException;

    public InvoiceAndUser getInvoiceAndUserByNumber(String invoiceNumber) throws DataAccessException;

    //try to find invoice id using the comment
    public Invoice findInvoiceByComment(String comment) throws DataAccessException;

    public Invoice getInvoiceByStructComm(String structuredCommunication) throws DataAccessException;

    public Page<ReminderAndUserAndInvoice> listReminderAndUserAndInvoice(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException;

    public List<InvoiceAndUser> listInvoicesForUser(int userId);

    public List<InvoiceAndUser> listUnpaidInvoicesByUser(int userId);

    public Iterable<InvoiceAndUser> listSuggestedInvoicesForPayment(Payment payment);

    public int checkDueDate();

    public Iterable<Invoice> listInvoiceByNumber(String str, int limit);

    public float sumOfPaymentsForInvoice(int invoiceId);

    public String getLastMembershipInvoiceNumber(String year);

    public List<Integer> listUsersWithoutMembershipInvoices();

    public InvoiceAndUser createMembershipInvoiceAndUser(int userId) throws DataAccessException;

}
