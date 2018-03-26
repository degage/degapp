package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Payment;
import be.ugent.degage.db.models.Invoice;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;
import java.sql.SQLException;

public interface PaymentInvoiceDAO {

    public int createPaymentInvoiceRelationship(Payment payment, Invoice invoice) throws DataAccessException;

    public Iterable<Payment> listPaymentsForInvoice(int invoiceId);

    public Iterable<Invoice> listInvoicesForPayment(int paymentId);

    public Page<PaymentInvoice> listAllPaymentInvoiceRelationships(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    // public void updateInvoice(Payment payment, Invoice invoice);

    public boolean existsPaymentInvoiceRelationship(int paymentId, int invoiceId) throws DataAccessException;

    public boolean existsPaymentInvoiceRelationshipForPayment(int paymentId) throws DataAccessException;

    public void unlinkPayment(int paymentId) throws SQLException;

    public void unlinkInvoice(int invoiceId) throws SQLException;

}
