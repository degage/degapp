package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.PaymentDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * JDBC implementation of {@link PaymentDAO}
 */
public class JDBCPaymentDAO extends AbstractDAO implements PaymentDAO {

    public static final int SQL_DUPLICATE_ENTRY = 1062;

    private static final String PAYMENT_FIELDS =
            "p.payment_id, p.payment_number, p.payment_date, p.payment_account_number, p.payment_user_id, p.payment_name, p.payment_address," +
            "p.payment_bank, p.payment_amount, p.payment_comment, p.payment_structured_comm, p.payment_status, p.payment_currency, p.payment_filename, p.payment_debit_type, " +
            "p.payment_include_in_balance, p.payment_current_hash, p.payment_previous_hash, p.payment_next_hash";

    private static final String PAYMENT_AND_USER_FIELDS = PAYMENT_FIELDS + ", " +
            "user_id, user_firstname, user_lastname, user_email, user_status, user_phone, user_cellphone, user_degage_id";


    public JDBCPaymentDAO(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public boolean checkUniquePayment(Payment payment) throws DataAccessException {
      StringBuilder sb = new StringBuilder();
      sb.append("SELECT * FROM payments WHERE payment_structured_comm = ? ")
      .append("AND payment_comment = ? AND payment_date = ? ")
      .append("AND payment_account_number = ? AND payment_amount = ? ")
      .append("AND payment_current_hash = ? ");
      if (payment.getPreviousHash() != -1) {
        sb.append("AND (payment_previous_hash = ? OR payment_previous_hash = -1) ");
      }
      if (payment.getNextHash() != -1) {
        sb.append("AND (payment_next_hash = ? OR payment_next_hash = -1) ");
      }
        try (PreparedStatement ps = prepareStatement(sb.toString())) {
            if (payment.getStructuredCommunication() != null) {
                ps.setString(1, payment.getStructuredCommunication());
            } else {
                ps.setString(1, "");
            }
            if (payment.getComment() != null) {
                ps.setString(2, payment.getComment());
            } else {
                ps.setString(2, "");
            }
            ps.setDate(3, payment.getDate() == null ? null : Date.valueOf(payment.getDate()));
            ps.setString(4, payment.getAccountNumber());
            ps.setFloat(5, payment.getAmount());
            ps.setInt(6, payment.getCurrentHash());
            int counter = 7;
            if (payment.getPreviousHash() != -1) {
              ps.setInt(counter, payment.getPreviousHash());
              counter++;
            }
            if (payment.getNextHash() != -1) {
              ps.setInt(counter, payment.getNextHash());
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Error while checking payment.", e);
        }
    }

//    @Override
//    public boolean checkPossibleDuplicatePayment(Payment payment) throws DataAccessException {
//        try (PreparedStatement ps = prepareStatement(
//                "SELECT * FROM payments WHERE payment_structured_comm = ? "+
//                        "AND payment_comment = ? AND payment_date = ? " +
//                        "AND payment_account_number = ? AND payment_amount = ? " //
//        )) {
//            if (payment.getStructuredCommunication() != null) {
//                ps.setString(1, payment.getStructuredCommunication());
//            } else {
//                ps.setString(1, "");
//            }
//            if (payment.getComment() != null) {
//                ps.setString(2, payment.getComment());
//            } else {
//                ps.setString(2, "");
//            }
//            ps.setDate(3, payment.getDate() == null ? null : Date.valueOf(payment.getDate()));
//            ps.setString(4, payment.getAccountNumber());
//            ps.setFloat(5, payment.getAmount());
//
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                return true; //Possible duplicate found
//            }
//            return false; //No possible duplicates found
//        } catch (SQLException e) {
//            throw new DataAccessException("Error while checking payment.", e);
//        }
//    }

    @Override
    public int createPayment(Payment payment) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO payments(payment_number, payment_date, payment_account_number, payment_user_id, payment_name, payment_address, " +
                "payment_bank, payment_amount, payment_comment, payment_structured_comm, payment_status, payment_currency, payment_filename, " +
                "payment_current_hash, payment_previous_hash, payment_next_hash, payment_debit_type) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                "payment_id"
        )) {
            ps.setInt(1, payment.getNumber());
            ps.setDate(2,payment.getDate() == null ? null : Date.valueOf(payment.getDate()));
            ps.setString(3, payment.getAccountNumber());
            if (payment.getUserId() == 0) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, payment.getUserId());
            }
            ps.setString(5, payment.getName());
            ps.setString(6, payment.getAddress());
            ps.setString(7, payment.getBank());
            ps.setFloat(8, payment.getAmount());
            if (payment.getComment() != null) {
                ps.setString(9, payment.getComment());
            } else {
                ps.setString(9, "");
            }
            if (payment.getStructuredCommunication() != null) {
                ps.setString(10, payment.getStructuredCommunication());
            } else {
                ps.setString(10, "");
            }
            ps.setString(11, payment.getStatus() == null ? "CHANGE" : payment.getStatus().name());
            ps.setString(12, payment.getCurrency());
            ps.setString(13, payment.getFilename());
            ps.setInt(14, payment.getCurrentHash());
            ps.setInt(15, payment.getPreviousHash());
            ps.setInt(16, payment.getNextHash());
            ps.setString(17, payment.getDebitType() == null ? "CREDIT" : payment.getDebitType().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                int paymentId = keys.getInt(1);
                return paymentId;
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == SQL_DUPLICATE_ENTRY){
                throw new DataAccessException("Could not create payment because it already exists.", ex);
            } else {
                throw new DataAccessException("Could not create payment.", ex);
            }
        }
    }

    private static Payment populatePayment(ResultSet rs) throws SQLException {

        Payment newPayment = new Payment.Builder(rs.getInt("payment_number"), rs.getDate("payment_date").toLocalDate(), rs.getString("payment_account_number"))
            .id(rs.getInt("payment_id"))
            .userId(rs.getInt("payment_user_id"))
            .name(rs.getString("payment_name"))
            .address(rs.getString("payment_address"))
            .bank(rs.getString("payment_bank"))
            .amount(rs.getFloat("payment_amount"))
            .comment(rs.getString("payment_comment"))
            .structuredCommunication(rs.getString("payment_structured_comm"))
            .currency(rs.getString("payment_currency"))
            .status(PaymentStatus.valueOf(rs.getString("payment_status")))
            .debitType(PaymentDebitType.valueOf(rs.getString("payment_debit_type")))
            .filename(rs.getString("payment_filename"))
            .includeInBalance(rs.getBoolean("payment_include_in_balance"))
            .currentHash(rs.getInt("payment_current_hash"))
            .previousHash(rs.getInt("payment_previous_hash"))
            .nextHash(rs.getInt("payment_next_hash"))
            .build();

            return newPayment;
    }


    private static PaymentAndUser populatePaymentAndUser(ResultSet rs) throws SQLException {

        return new PaymentAndUser(

                  new Payment.Builder(rs.getInt("payment_number"), rs.getDate("payment_date").toLocalDate(), rs.getString("payment_account_number"))
                          .id(rs.getInt("payment_id"))
                          .userId(rs.getInt("payment_user_id"))
                          .name(rs.getString("payment_name"))
                          .address(rs.getString("payment_address"))
                          .bank(rs.getString("payment_bank"))
                          .amount(rs.getFloat("payment_amount"))
                          .comment(rs.getString("payment_comment"))
                          .structuredCommunication(rs.getString("payment_structured_comm"))
                          .status(PaymentStatus.valueOf(rs.getString("payment_status")))
                          .debitType(PaymentDebitType.valueOf(rs.getString("payment_debit_type")))
                          .currency(rs.getString("payment_currency"))
                          .filename(rs.getString("payment_filename"))
                          .includeInBalance(rs.getBoolean("payment_include_in_balance"))
                          .invoiceNumbers(rs.getString("invoice_numbers") == null ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(rs.getString("invoice_numbers").split(","))))
                          .build(),
                    rs.getObject("user_id") == null ? null : new User(
                          rs.getInt("user_id"),
                          rs.getString("user_email"),
                          rs.getString("user_firstname"),
                          rs.getString("user_lastname"),
                          UserStatus.valueOf(rs.getString("user_status")),
                          rs.getString("user_phone"),
                          rs.getString("user_cellphone"),
                          (Integer) rs.getObject("user_degage_id"))
                    );
    }

    @Override
    public void updatePayment(Payment payment) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE payments SET payment_user_id=?, payment_status=?, payment_include_in_balance = ? WHERE payment_id = ?"
        )) {
            ps.setInt(1, payment.getUserId());
            ps.setString(2, payment.getStatus().name());
            ps.setBoolean(3, payment.getIncludeInBalance());
            int paymentId = payment.getId();
            ps.setInt(4, paymentId);

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("No rows were affected when updating payment.");
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update payment.", ex);
        }
    }

    @Override
    public Iterable<Payment> listAllPayments() {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + PAYMENT_FIELDS + " FROM payments p ORDER BY p.payment_id ASC"
        )) {
            return toList(ps, JDBCPaymentDAO::populatePayment);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all payments", ex);
        }
    }

    @Override
    public Iterable<PaymentAndUser> listAllPaymentsAndUsers() {
      String query = "SELECT " + PAYMENT_AND_USER_FIELDS +
              " , group_concat(i.invoice_number SEPARATOR ',') as invoice_numbers " +
              "FROM payments p " +
              " LEFT JOIN users ON p.payment_user_id = user_id " +
              " LEFT JOIN payment_invoice pi ON pi.payment_id = p.payment_id " +
              " LEFT JOIN invoices i on i.invoice_id = pi.invoice_id " +
              " GROUP BY " + PAYMENT_AND_USER_FIELDS +
              " ORDER BY p.payment_id ASC";
        try (PreparedStatement ps = prepareStatement(
          query
        )) {
            return toList(ps, JDBCPaymentDAO::populatePaymentAndUser);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all payments", ex);
        }
    }

    @Override
    public Page<PaymentAndUser> listPaymentsAndUsers(FilterField orderBy, boolean asc, int page, int pageSize, String filter) {
      StringBuilder builder = new StringBuilder(
              "SELECT SQL_CALC_FOUND_ROWS " + PAYMENT_AND_USER_FIELDS +
                      " , group_concat(i.invoice_number SEPARATOR ',') as invoice_numbers " +
                      "FROM payments p " +
                      "LEFT JOIN users ON payment_user_id = user_id " +
                      "LEFT JOIN payment_invoice pi ON pi.payment_id = p.payment_id " +
                      " LEFT JOIN invoices i on i.invoice_id = pi.invoice_id "
      );

      // add filters
      if (filter != null && filter.length() > 0) {
        builder.append(" WHERE ");
        String[] searchStrings = filter.trim().split(" ");
        for (int i = 0; i < searchStrings.length; i++) {
          if (i > 0) {
            builder.append(" AND ");
          }
          builder.append("(");
          StringBuilder filterBuilder = new StringBuilder();
          FilterUtils.appendOrContainsFilter(filterBuilder, "user_lastname", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "user_firstname", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "payment_address", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "payment_structured_comm", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "payment_number", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "payment_comment", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "payment_amount", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "payment_status", searchStrings[i]);
          if (searchStrings[i].toLowerCase().equals("inbalans")) {
            FilterUtils.appendOrBooleanFilter(filterBuilder, "payment_include_in_balance", true);
          } else if (searchStrings[i].toLowerCase().equals("uitbalans")) {
            FilterUtils.appendOrBooleanFilter(filterBuilder, "payment_include_in_balance", false);
          }
          builder.append(filterBuilder).append(" ) ");
        }
      }

      builder.append(" GROUP BY ").append(PAYMENT_AND_USER_FIELDS);

      // add order
      switch (orderBy) {
          case NUMBER:
              builder.append(" ORDER BY payment_number ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case DATE:
              builder.append(" ORDER BY payment_date ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case USER_NAME:
              builder.append(" ORDER BY user_lastname ");
              builder.append(asc ? "ASC" : "DESC");
              builder.append(" , user_firstname ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case BANK:
              builder.append(" ORDER BY payment_bank ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case NAME:
              builder.append(" ORDER BY payment_name ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case AMOUNT:
              builder.append(" ORDER BY payment_amount ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case STATUS:
              builder.append(" ORDER BY payment_status ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case TYPE:
              builder.append(" ORDER BY payment_debit_type ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case FILENAME:
              builder.append(" ORDER BY payment_filename ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case STRUCTURED_COMM:
              builder.append(" ORDER BY payment_structured_comm ");
              builder.append(asc ? "ASC" : "DESC");
              break;
          case PAYMENT_ID:
              builder.append(" ORDER BY payment_id ");
              builder.append(asc ? "ASC" : "DESC");
              break;
      }

      builder.append (" LIMIT ").append (pageSize).append(" OFFSET ").append((page-1)*pageSize);
      try (PreparedStatement ps = prepareStatement(builder.toString())) {

          return toPage(ps, pageSize, JDBCPaymentDAO::populatePaymentAndUser);
      } catch (SQLException ex) {
          throw new DataAccessException(ex);
      }

    }

    @Override
    public Payment getPayment(int paymentId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + PAYMENT_FIELDS + " FROM payments p WHERE p.payment_id = ? "
        )) {
            ps.setInt(1, paymentId);
            return toSingleObject(ps, JDBCPaymentDAO::populatePayment);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get payment", e);
        }
    }

    @Override
    public PaymentAndUser getPaymentAndUser(int paymentId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + PAYMENT_AND_USER_FIELDS +
                " , group_concat(i.invoice_number SEPARATOR ',') as invoice_numbers " +
                "FROM payments p " +
                " LEFT JOIN users ON p.payment_user_id = user_id " +
                " LEFT JOIN payment_invoice pi ON pi.payment_id = p.payment_id " +
                " LEFT JOIN invoices i on i.invoice_id = pi.invoice_id " +
                "WHERE p.payment_id = ?" +
                " GROUP BY " + PAYMENT_AND_USER_FIELDS
        )) {
            ps.setInt(1, paymentId);
            return toSingleObject(ps, JDBCPaymentDAO::populatePaymentAndUser);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get payment and user", e);
        }
    }

    /**
     * @param orderBy  The field you want to order by
     * @param asc      Ascending
     * @param page     The page you want to see
     * @param pageSize The page size
     * @param filter   The filter you want to apply
     * @return List of payments with custom ordering and filtering
     */
    @Override
    public Page<PaymentAndUser> listPaymentsAndUser(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(
                "SELECT SQL_CALC_FOUND_ROWS " + PAYMENT_AND_USER_FIELDS +
                        " , group_concat(i.invoice_number SEPARATOR ',') as invoice_numbers " +
                        "FROM payments p " +
                        "LEFT JOIN users ON payment_user_id = user_id " +
                        "LEFT JOIN payment_invoice pi ON pi.payment_id = p.payment_id " +
                        " LEFT JOIN invoices i on i.invoice_id = pi.invoice_id "
        );

        // add filters
        StringBuilder filterBuilder = new StringBuilder();

        FilterUtils.appendContainsFilter(filterBuilder, "payment_number", filter.getValue(FilterField.NUMBER));
        FilterUtils.appendContainsFilter(filterBuilder, "payment_date", filter.getValue(FilterField.DATE));
        FilterUtils.appendContainsFilter(filterBuilder, "payment_account_number", filter.getValue(FilterField.ACCOUNT_NUMBER));
        FilterUtils.appendContainsFilter(filterBuilder, "payment_name", filter.getValue(FilterField.NAME));
        FilterUtils.appendContainsFilter(filterBuilder, "payment_account_number", filter.getValue(FilterField.ACCOUNT_NUMBER));
        FilterUtils.appendContainsFilter(filterBuilder, "payment_amount", filter.getValue(FilterField.AMOUNT));
        FilterUtils.appendContainsFilter(filterBuilder, "payment_comment", filter.getValue(FilterField.COMMENT));
        FilterUtils.appendContainsFilter(filterBuilder, "payment_structured_comm", filter.getValue(FilterField.STRUCTURED_COMM));
        FilterUtils.appendContainsFilter(filterBuilder, "payment_filename", filter.getValue(FilterField.FILENAME));
        FilterUtils.appendContainsFilter(filterBuilder, "payment_debit_type", filter.getValue(FilterField.TYPE));

        FilterUtils.appendContainsFilter(filterBuilder, "payment_status", PaymentStatus.translate(filter.getValue(FilterField.STATUS)));

        String userId;
        String user = filter.getValue(FilterField.USER_ID);
        if (user.equals("") || user.startsWith("-")) {
            userId = "";
        } else {
            userId = user;
        }
        FilterUtils.appendIntFilter(filterBuilder, "user_id", userId);

        FilterUtils.appendIntFilter(filterBuilder, "p.payment_id", filter.getValue(FilterField.PAYMENT_ID));

        if (filterBuilder.length() > 0) {
            builder.append("WHERE ").append(filterBuilder.substring(4));
        }

        builder.append(" GROUP BY ").append(PAYMENT_AND_USER_FIELDS);

        // add order
        switch (orderBy) {
            case NUMBER:
                builder.append(" ORDER BY payment_number ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case DATE:
                builder.append(" ORDER BY payment_date ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case USER_NAME:
                builder.append(" ORDER BY user_lastname ");
                builder.append(asc ? "ASC" : "DESC");
                builder.append(" , user_firstname ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case BANK:
                builder.append(" ORDER BY payment_bank ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case NAME:
                builder.append(" ORDER BY payment_name ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case AMOUNT:
                builder.append(" ORDER BY payment_amount ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case STATUS:
                builder.append(" ORDER BY payment_status ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case TYPE:
                builder.append(" ORDER BY payment_debit_type ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case FILENAME:
                builder.append(" ORDER BY payment_filename ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case STRUCTURED_COMM:
                builder.append(" ORDER BY payment_structured_comm ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case PAYMENT_ID:
                builder.append(" ORDER BY payment_id ");
                builder.append(asc ? "ASC" : "DESC");
                break;
        }

        builder.append (" LIMIT ").append (pageSize).append(" OFFSET ").append((page-1)*pageSize);
        try (PreparedStatement ps = prepareStatement(builder.toString())) {

            return toPage(ps, pageSize, JDBCPaymentDAO::populatePaymentAndUser);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public Iterable<PaymentAndUser> listSuggestedPaymentsForInvoice(InvoiceAndUser invoiceAndUser) {
        StringBuilder builder = new StringBuilder(
                "SELECT SQL_CALC_FOUND_ROWS " + PAYMENT_AND_USER_FIELDS +
                        " , group_concat(i.invoice_number SEPARATOR ',') as invoice_numbers " +
                        " FROM payments p " +
                        "LEFT JOIN users ON payment_user_id = user_id " +
                        "LEFT JOIN payment_invoice pi ON pi.payment_id = payments.payment_id " +
                        " LEFT JOIN invoices i on i.invoice_id = pi.invoice_id "
        );
        builder.append("WHERE payment_date >= ? ");
        builder.append("AND (payment_user_id = ? ");
        builder.append("OR abs((payment_amount / ?) -1) < 0.02 ");
        builder.append("OR payment_name LIKE ? ");
        builder.append("OR payment_name LIKE ? ");
        builder.append("OR (payment_account_number = ? AND payment_account_number IS NOT NULL) ");
        builder.append("OR (lower(payment_comment) like lower(?) AND payment_comment IS NOT NULL) ");
        builder.append("OR (lower(payment_structured_comm) like lower(?) AND payment_structured_comm IS NOT NULL) ");
        builder.append(") ORDER BY payment_user_id, payment_amount DESC");
        Invoice invoice = invoiceAndUser.getInvoice();
        User user = invoiceAndUser.getUser();
        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            ps.setDate(1, invoiceAndUser.getInvoice().getDate() == null ? null : Date.valueOf(invoiceAndUser.getInvoice().getDate()));
            ps.setInt(2, invoiceAndUser.getInvoice().getUserId());
            ps.setFloat(3, invoiceAndUser.getInvoice().getAmount());
            ps.setString(4, invoiceAndUser.getUser().getLastName());
            ps.setString(5, invoiceAndUser.getUser().getFirstName());
            ps.setString(6, invoiceAndUser.getUser().getAccountNumber());
            ps.setString(7, invoiceAndUser.getInvoice().getComment() == null ? "" : "%" + invoiceAndUser.getInvoice().getComment() + "%");
            ps.setString(8, invoiceAndUser.getInvoice().getStructuredCommunication() == null || invoiceAndUser.getInvoice().getStructuredCommunication() == "" ? "" : "%" + invoiceAndUser.getInvoice().getStructuredCommunication() + "%");
          return toList(ps, JDBCPaymentDAO::populatePaymentAndUser);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot suggest payments for invoice", ex);
        }
    }

    @Override
    public Iterable<Payment> listPaymentsForUser(int userId) {
        try (PreparedStatement ps = prepareStatement(
          "SELECT " + PAYMENT_FIELDS + " FROM payments p " +
                  "WHERE payment_user_id = ? ORDER BY p.payment_id ASC"
          )) {
              ps.setInt(1, userId);
              return toList(ps, JDBCPaymentDAO::populatePayment);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all payments for user", ex);
        }
    }

    @Override
    public LocalDate getLastPaymentDate() throws DataAccessException {
      try (PreparedStatement ps = prepareStatement(
        "SELECT max(payment_date) FROM payments p"
        )) {
          return toSingleDate(ps);
      } catch (SQLException ex) {
          throw new DataAccessException("Cannot list all payments for user", ex);
      }
    }
}
