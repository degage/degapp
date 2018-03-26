package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.PaymentDAO;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.dao.PaymentInvoiceDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;
import java.util.Calendar;

/**
 * JDBC implementation of {@link PaymentInvoiceDAO}
 */
public class JDBCPaymentInvoiceDAO extends AbstractDAO implements PaymentInvoiceDAO {

    public JDBCPaymentInvoiceDAO(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public int createPaymentInvoiceRelationship(Payment payment, Invoice invoice) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
          "SELECT * FROM payment_invoice WHERE payment_id = ? AND invoice_id = ?"
        )) {
            ps.setInt(1, payment.getId());
            ps.setInt(2, invoice.getId());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("id");
            } else {
              PreparedStatement ps2 = prepareStatement("INSERT INTO payment_invoice(payment_id, invoice_id) VALUES (?,?)", "id");
              ps2.setInt(1, payment.getId());
              ps2.setInt(2, invoice.getId());
              ps2.executeUpdate();
              try (ResultSet keys = ps2.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                int id = keys.getInt(1);
                return id;
              }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create payment-invoice relationship.", ex);
        }
    }

    @Override
    public boolean existsPaymentInvoiceRelationship(int paymentId, int invoiceId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
            "SELECT * FROM payment_invoice WHERE payment_id = ? AND invoice_id = ?"
        )) {
            ps.setInt(1, paymentId);
            ps.setInt(2, invoiceId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not check if payment-invoice relationship exists.", ex);
        }
    }

    @Override
    public boolean existsPaymentInvoiceRelationshipForPayment(int paymentId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT COUNT(*) FROM payment_invoice WHERE payment_id = ?"
        )) {
            ps.setInt(1, paymentId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create payment-invoice relationship.", ex);
        }
    }

    // @Override
    // public void updateInvoice(Payment payment, Invoice invoice) throws DataAccessException {
    //   String status = "OPEN";
    //   // Calendar today = Calendar.getInstance();
    //   // today.set(Calendar.HOUR_OF_DAY, 0);
    //   // today.set(Calendar.MINUTE, 0);
    //   // today.set(Calendar.SECOND, 0);
    //   Calendar paymentPaymentDate = Calendar.getInstance();
    //   paymentPaymentDate.setTime(Date.valueOf(payment.getDate()));
    //   Calendar dueDate = Calendar.getInstance();
    //   dueDate.setTime(Date.valueOf(invoice.getDueDate()));
    //   if (invoice.getAmount() == payment.getAmount()) {
    //       status = "PAID";
    //   } else if (paymentPaymentDate.before(dueDate) && invoice.getAmount() != payment.getAmount()) {
    //       status = "OPEN";
    //   } else {
    //       status = "OVERDUE";
    //   }
    //   try (PreparedStatement ps = prepareStatement(
    //           "UPDATE invoices SET invoice_payment_date = ?, invoice_status = ? WHERE invoice_id = ?"
    //   )) {
    //       ps.setDate(1,payment.getDate() == null ? null : Date.valueOf(payment.getDate()));
    //       ps.setString(2, status);
    //       ps.setInt(3, invoice.getId());
    //       ps.executeUpdate();

    //   } catch (SQLException ex) {
    //       throw new DataAccessException("Could not update invoice.", ex);
    //   }
    // }

    /**
     * @param orderBy  The field you want to order by
     * @param asc      Ascending
     * @param page     The page you want to see
     * @param pageSize The page size
     * @param filter   The filter you want to apply
     * @return List of payments with custom ordering and filtering
     */
    @Override
    public Page<PaymentInvoice> listAllPaymentInvoiceRelationships(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(
                "SELECT SQL_CALC_FOUND_ROWS id, payment_invoice.payment_id, payment_invoice.invoice_id, " +
                        "invoices.invoice_number " +
                        "FROM payment_invoice " +
                        "JOIN payments ON payment_invoice.payment_id = payments.payment_id " +
                        "JOIN invoices ON payment_invoice.invoice_id = invoices.invoice_id "
        );

        // add filters
        StringBuilder filterBuilder = new StringBuilder();

        FilterUtils.appendContainsFilter(filterBuilder, "invoices.invoice_number", filter.getValue(FilterField.NUMBER));

        if (filterBuilder.length() > 0) {
            builder.append("WHERE ").append(filterBuilder.substring(4));
        }

        // add order
        switch (orderBy) {
            case NUMBER:
                builder.append(" ORDER BY invoices.invoice_number ");
                builder.append(asc ? "ASC" : "DESC");
                break;
        }

        builder.append (" LIMIT ").append (pageSize).append(" OFFSET ").append((page-1)*pageSize);

        try (PreparedStatement ps = prepareStatement(builder.toString())) {

            return toPage(ps, pageSize, rs -> new PaymentInvoice(

                  new Payment.Builder(rs.getInt("payments.payment_number"), rs.getDate("payments.payment_date").toLocalDate(), rs.getString("payments.payment_account_number"))
                          .id(rs.getInt("payments.payment_id"))
                          .userId(rs.getInt("payments.payment_user_id"))
                          .name(rs.getString("payments.payment_name"))
                          .address(rs.getString("payments.payment_address"))
                          .bank(rs.getString("payments.payment_bank"))
                          .amount(rs.getFloat("payments.payment_amount"))
                          .comment(rs.getString("payments.payment_comment"))
                          .structuredCommunication(rs.getString("payments.payment_structured_comm"))
                          .status(PaymentStatus.valueOf(rs.getString("payments.payment_status")))
                          .currency(rs.getString("payments.payment_currency"))
                          .filename(rs.getString("payments.payment_filename"))
                          .build(),
                  new Invoice.Builder(rs.getString("invoices.invoice_number"), rs.getInt("invoices.invoice_user_id"), rs.getInt("invoices.invoice_billing_id"))
                          .id(rs.getInt("invoices.invoice_id"))
                          .amount(rs.getFloat("invoices.invoice_amount"))
                          .status(InvoiceStatus.valueOf(rs.getString("invoices.invoice_status")))
                          .date(rs.getDate("invoices.invoice_date") == null ? null : rs.getDate("invoices.invoice_date").toLocalDate())
                          .paymentDate(rs.getDate("invoices.invoice_payment_date") == null ? null : rs.getDate("invoices.invoice_payment_date").toLocalDate())
                          .dueDate(rs.getDate("invoices.invoice_due_date") == null ? null : rs.getDate("invoices.invoice_due_date").toLocalDate())
                          .comment(rs.getString("invoices.invoice_comment"))
                          .structuredCommunication(rs.getString("invoices.invoice_structured_comm"))
                          .build()
            ));
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }


    @Override
    public Iterable<Payment> listPaymentsForInvoice(int invoiceId) {
        try (PreparedStatement ps = prepareStatement(
          "SELECT payment_invoice.payment_id, payments.payment_number, payments.payment_date, payments.payment_account_number, payments.payment_user_id," +
          "payments.payment_name, payments.payment_address, payments.payment_bank, payments.payment_amount," +
          "payments.payment_comment, payments.payment_structured_comm, payments.payment_status, payments.payment_currency, payments.payment_filename " +
                  "FROM payment_invoice " +
                  "JOIN payments ON payment_invoice.payment_id = payments.payment_id " +
                  "WHERE payment_invoice.invoice_id = ? ORDER BY payments.payment_id ASC"
          )) {
              ps.setInt(1, invoiceId);
              return toList(ps, JDBCPaymentInvoiceDAO::populatePayment);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all payments for invoice", ex);
        }
    }


    private static Payment populatePayment(ResultSet rs) throws SQLException {
        Payment newPayment = new Payment.Builder(rs.getInt("payments.payment_number"), rs.getDate("payments.payment_date").toLocalDate(), rs.getString("payments.payment_account_number"))
            .id(rs.getInt("payment_invoice.payment_id"))
            .userId(rs.getInt("payments.payment_user_id"))
            .name(rs.getString("payments.payment_name"))
            .address(rs.getString("payments.payment_address"))
            .bank(rs.getString("payments.payment_bank"))
            .amount(rs.getFloat("payments.payment_amount"))
            .comment(rs.getString("payments.payment_comment"))
            .structuredCommunication(rs.getString("payments.payment_structured_comm"))
            .currency(rs.getString("payments.payment_currency"))
            .status(PaymentStatus.valueOf(rs.getString("payments.payment_status")))
            .filename(rs.getString("payments.payment_filename"))
            .build();

            return newPayment;
    }

    @Override
    public Iterable<Invoice> listInvoicesForPayment(int paymentId) {
        try (PreparedStatement ps = prepareStatement(
          "SELECT payment_invoice.invoice_id, invoices.invoice_id, invoices.invoice_number, invoices.invoice_date, invoices.invoice_payment_date, " +
          "invoices.invoice_due_date, invoices.invoice_user_id, invoices.invoice_billing_id, invoices.invoice_amount, invoices.invoice_comment, " +
          "invoices.invoice_status, invoices.invoice_structured_comm " +
                  "FROM payment_invoice " +
                  "JOIN invoices ON payment_invoice.invoice_id = invoices.invoice_id " +
                  "WHERE payment_invoice.payment_id = ? ORDER BY invoices.invoice_id ASC"
          )) {
              ps.setInt(1, paymentId);
              return toList(ps, JDBCPaymentInvoiceDAO::populateInvoice);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all invoices for payment", ex);
        }
    }

    @Override
    public void unlinkInvoice(int invoiceId) throws SQLException {
        PreparedStatement ps = prepareStatement("DELETE FROM payment_invoice where invoice_id = ?");
        ps.setInt(1, invoiceId);
        ps.executeUpdate();
    }

    @Override
    public void unlinkPayment(int paymentId) throws SQLException {
      PreparedStatement ps = prepareStatement("DELETE FROM payment_invoice WHERE payment_id = ?");
      ps.setInt(1, paymentId);
      ps.executeUpdate();
    }

    private static Invoice populateInvoice(ResultSet rs) throws SQLException {
        Date date = rs.getDate("invoice_date");
        Date paymentDate = rs.getDate("invoice_payment_date");
        Date dueDate = rs.getDate("invoice_due_date");

        return new Invoice.Builder(rs.getString("invoice_number"), rs.getInt("invoice_user_id"), rs.getInt("invoice_billing_id"))
                .id(rs.getInt("invoice_id"))
                .amount(rs.getFloat("invoice_amount"))
                .status(InvoiceStatus.valueOf(rs.getString("invoice_status")))
                .date(date == null ? null : date.toLocalDate())
                .paymentDate(paymentDate == null ? null : paymentDate.toLocalDate())
                .dueDate(dueDate == null ? null : dueDate.toLocalDate())
                .comment(rs.getString("invoice_comment"))
                .structuredCommunication(rs.getString("invoice_structured_comm"))
                .build();
    }

}
