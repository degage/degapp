package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Payment;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

import java.time.Instant;
import java.time.LocalDate;

public interface PaymentDAO {

    /**
     * Return a list of all Payments
     */
    public Iterable<Payment> listAllPayments();

    public Iterable<PaymentAndUser> listAllPaymentsAndUsers();

    /**
     * Returns true if payment doesn't exist already
     */
    public boolean checkUniquePayment(Payment payment) throws DataAccessException;

    public int createPayment(Payment payment) throws DataAccessException;

    public Payment getPayment(int paymentId) throws DataAccessException;

    public PaymentAndUser getPaymentAndUser(int paymentId) throws DataAccessException;

    public void updatePayment(Payment payment) throws DataAccessException;

    public Page<PaymentAndUser> listPaymentsAndUser(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    public Page<PaymentAndUser> listPaymentsAndUsers(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException;

    public Iterable<PaymentAndUser> listSuggestedPaymentsForInvoice(InvoiceAndUser invoiceAndUser);

    public Iterable<Payment> listPaymentsForUser(int userId);

    public LocalDate getLastPaymentDate() throws DataAccessException;

}
