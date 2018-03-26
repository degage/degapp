package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.PaymentStatisticsDAO;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


/**
 * JDBC implementation of {@link PaymentStatisticsDAO}
 */
public class JDBCPaymentStatisticsDAO extends AbstractDAO implements PaymentStatisticsDAO {



    public JDBCPaymentStatisticsDAO (JDBCDataAccessContext context) {
        super(context);
    }

    private final String USER_FIELDS = "user_id, user_email, user_firstname, user_lastname, user_status, user_phone, user_cellphone, user_degage_id";


    private double getPercentage(String status, int billingId) {
        int totalRows = 0;
        int statusRows = 0;

        try (PreparedStatement ps = prepareStatement(
                "SELECT count(*) FROM invoices " +
                        "WHERE invoice_status = ? AND invoice_billing_id = ? " +
                        "AND invoice_amount > ?"
        )) {
            try (PreparedStatement ps2 = prepareStatement(
                    "SELECT count(*) FROM invoices " +
                            "WHERE invoice_billing_id = ? AND invoice_amount > ?"
            )) {
                ps2.setInt(1, billingId);
                ps2.setFloat(2, 0);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) {
                    totalRows = rs2.getInt(1);
                }
            } catch (SQLException ex) {
                throw new DataAccessException("Cannot retrieve the percentages", ex);
            }

            ps.setString(1, status);
            ps.setInt(2, billingId);
            ps.setFloat(3, 0);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                statusRows = rs.getInt(1);
            }

            if (totalRows == 0) { //no invoices so 100% of invoices is paid
                return 100;
            } else {
                return (double) (statusRows * 100) / totalRows;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot retrieve the percentages", ex);
        }
    }

    @Override
    public double getPercentagePaid(int billingId) throws DataAccessException {
        return getPercentage("PAID", billingId);
    }


    @Override
    public double getPercentageOverdue(int billingId) throws DataAccessException {
        return getPercentage("OVERDUE", billingId);

    }


    @Override
    public double getPercentageOpen(int billingId) throws DataAccessException {
        return getPercentage("OPEN", billingId);

    }


    @Override
    public double getAmountReceived(int billingId) throws DataAccessException {
        double res = 0;

        try (PreparedStatement ps = prepareStatement(
                "SELECT sum(invoice_amount) as amount FROM invoices " +
                        "WHERE invoice_status = ?  AND invoice_billing_id = ? " +
                        "AND invoice_amount > ?"
        )) {
            ps.setString(1, "PAID");
            ps.setInt(2, billingId);
            ps.setFloat(3, 0);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                res = rs.getFloat("amount");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot retrieve the amount that is received", ex);
        }

        return res;
    }

    @Override
    public double getAmountToReceive(int billingId) throws DataAccessException {
        double res = 0;

        try (PreparedStatement ps = prepareStatement(
                "SELECT sum(invoice_amount) as amount FROM invoices " +
                        "WHERE invoice_status != ?  AND invoice_billing_id = ? " +
                        "AND invoice_amount > ?"
        )) {
            ps.setString(1, "PAID");
            ps.setInt(2, billingId);
            ps.setFloat(3, 0);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                res = rs.getFloat("amount");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot retrieve the amount to receive", ex);
        }

        return res;
    }

    @Override
    public double getAmountPaid(int billingId) throws DataAccessException {
        double res = 0;

        try (PreparedStatement ps = prepareStatement(
                "SELECT sum(invoice_amount) as amount FROM invoices " +
                        "WHERE invoice_status = ?  AND invoice_billing_id = ? " +
                        "AND invoice_amount < ?"
        )) {
            ps.setString(1, "PAID");
            ps.setInt(2, billingId);
            ps.setFloat(3, 0);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                res = rs.getFloat("amount") * (-1);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot retrieve the amount that is paid", ex);
        }

        return res;
    }

    @Override
    public double getAmountToPay(int billingId) throws DataAccessException {
        double res = 0;

        try (PreparedStatement ps = prepareStatement(
                "SELECT sum(invoice_amount) as amount FROM invoices " +
                        "WHERE invoice_status != ?  AND invoice_billing_id = ? " +
                        "AND invoice_amount < ?"
        )) {
            ps.setString(1, "PAID");
            ps.setInt(2, billingId);
            ps.setFloat(3, 0);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                res = rs.getFloat("amount") * (-1);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot retrieve the amount that has to be paid", ex);
        }

        return res;
    }

    @Override
    public double getAverageTimeToPay(int billingId) throws DataAccessException {
        int res = 0;
        int counter = 0;
        try (PreparedStatement ps = prepareStatement(
                "SELECT * FROM invoices " +
                        "WHERE invoice_status = ?  AND invoice_billing_id = ? " +
                        "AND invoice_amount > ?"
        )) {
            ps.setString(1, "PAID");
            ps.setInt(2, billingId);
            ps.setInt(3, 0);
            ResultSet rs = ps.executeQuery();
            Iterable<Invoice> invoices = toList(ps, JDBCInvoiceDAO::populateInvoice);

            for (Invoice invoice : invoices) {
                if (invoice.getDate() != null && invoice.getPaymentDate() != null) {
                  int days = (int) ChronoUnit.DAYS.between(invoice.getDate(), invoice.getPaymentDate());
                  res += days;
                  counter++;
                }
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Cannot retrieve the average paying time", ex);
        }

        return counter == 0 ? 0 : (double)res / (double)counter;
    }

    public Iterable<PaymentUserStatistic> getStatsPerUser() throws DataAccessException {
        Map<Integer, PaymentUserStatistic.Builder> theMap = new HashMap<Integer, PaymentUserStatistic.Builder>();

        try {
            PreparedStatement ps = prepareStatement(
                "SELECT " + USER_FIELDS +
                        ", SUM(invoice_amount) as amount1 FROM users " +
                        "LEFT JOIN invoices ON users.user_id = invoices.invoice_user_id " +
                        "GROUP BY " + USER_FIELDS);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                theMap.put(Integer.valueOf(rs.getInt("user_id")), populateUserStatisticBuilder(rs));
            }
            PreparedStatement ps2 = prepareStatement("SELECT user_id, SUM(payment_amount) as amount2 FROM users " +
                            "LEFT JOIN payments ON users.user_id = payments.payment_user_id " +
                            "WHERE payments.payment_include_in_balance = 1 " +
                            "GROUP BY user_id");
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                theMap.put(Integer.valueOf(rs2.getInt("user_id")), theMap.get(Integer.valueOf(rs2.getInt("user_id"))).amountPaid(rs2.getFloat("amount2")));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot retrieve the average paying time", ex);
        }
        List<PaymentUserStatistic> l = new ArrayList<PaymentUserStatistic>();
        for (Integer key : theMap.keySet()) {
            l.add(theMap.get(key).build());
        }
        return l;
    }

    public static PaymentUserStatistic.Builder populateUserStatisticBuilder(ResultSet rs) throws SQLException {

        return new PaymentUserStatistic.Builder(rs.getObject("user_id") == null ? null : new User(
                        rs.getInt("user_id"),
                        rs.getString("user_email"),
                        rs.getString("user_firstname"),
                        rs.getString("user_lastname"),
                        UserStatus.valueOf(rs.getString("user_status")),
                        rs.getString("user_phone"),
                        rs.getString("user_cellphone"),
                        (Integer) rs.getObject("user_degage_id"))
                )
                .amountToPay(rs.getFloat("amount1"));
    }

    public static PaymentUserStatistic populateUserStatistic(ResultSet rs) throws SQLException {

        return new PaymentUserStatistic.Builder(rs.getObject("user_id") == null ? null : new User(
                        rs.getInt("user_id"),
                        rs.getString("user_email"),
                        rs.getString("user_firstname"),
                        rs.getString("user_lastname"),
                        UserStatus.valueOf(rs.getString("user_status")),
                        rs.getString("user_phone"),
                        rs.getString("user_cellphone"),
                        (Integer) rs.getObject("user_degage_id"))
                )
                .amountToPay(rs.getFloat("amount1"))
                .amountPaid(rs.getFloat("amount2"))
                .build();
    }

    private static void appendFilter(StringBuilder builder, String filter, boolean firstWhereClause) {
      // add filters
      if (filter != null && filter.length() > 0) {
        builder.append(firstWhereClause ? " WHERE " : " AND ");
        String[] searchStrings = filter.trim().split(" ");
        for (int i = 0; i < searchStrings.length; i++) {
          if (i > 0) {
            builder.append(" AND ");
          }
          builder.append("(");
          StringBuilder filterBuilder = new StringBuilder();
          FilterUtils.appendOrContainsFilter(filterBuilder, "user_lastname", searchStrings[i]);
          FilterUtils.appendOrContainsFilter(filterBuilder, "user_firstname", searchStrings[i]);
          builder.append(filterBuilder).append(")");
        }
      }
    }

    @Override
    public Page<PaymentUserStatistic> listUserStats(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException {

        try {
            List<PaymentUserStatistic.Builder> theList = new ArrayList<PaymentUserStatistic.Builder>();
            StringBuilder builder = new StringBuilder(
                    "SELECT SQL_CALC_FOUND_ROWS " + USER_FIELDS +
                        ", SUM(invoice_amount) as amount1 FROM users " +
                        "LEFT JOIN invoices ON users.user_id = invoices.invoice_user_id ");
            boolean firstWhereClause = true;
            appendFilter(builder, filter, firstWhereClause);
            builder.append(" GROUP BY ").append(USER_FIELDS);
                   // add order
            switch (orderBy) {
                case NAME:
                  builder.append(" ORDER BY user_lastname ").append(asc ? "ASC" : "DESC")
                    .append(", user_firstname ").append(asc ? "ASC" : "DESC");
                    break;
                default:
                  builder.append(" ORDER BY user_lastname ").append(asc ? "ASC" : "DESC")
                    .append(", user_firstname ").append(asc ? "ASC" : "DESC");
            }
            builder.append (" LIMIT ").append (pageSize).append(" OFFSET ").append((page-1)*pageSize);
            PreparedStatement ps = prepareStatement(builder.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                theList.add(populateUserStatisticBuilder(rs));
            }
            PreparedStatement ps1 = prepareStatement("SELECT FOUND_ROWS()");
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            int fullSize = rs1.getInt(1);

            builder = new StringBuilder("SELECT user_id, SUM(payment_amount) as amount2 FROM users " +
                            "LEFT JOIN payments ON users.user_id = payments.payment_user_id " +
                            "WHERE payments.payment_include_in_balance = 1 ");
            builder.append("AND user_id in (");
            for (int i = 0; i < theList.size(); i++){
                builder.append(i > 0 ? "," : "").append(theList.get(i).build().getUser().getId());
            }
            builder.append(") ");
            builder.append(" GROUP BY user_id");
            PreparedStatement ps2 = prepareStatement(builder.toString());
            ResultSet rs2 = ps2.executeQuery();
            List<PaymentUserStatistic> l = new ArrayList<PaymentUserStatistic>();
            Map<Integer, Float> paymentMap = new HashMap<Integer, Float>();
            while (rs2.next()) {
                paymentMap.put(rs2.getInt("user_id"), rs2.getFloat("amount2"));
            }
            for (int i = 0; i < theList.size(); i++){
                int userId = theList.get(i).build().getUser().getId();
                l.add(theList.get(i).amountPaid(paymentMap.get(userId) != null ? paymentMap.get(userId) : 0f).build());
            }
            return toPage(l, pageSize, fullSize);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public PaymentUserStatistic getPaymentStatsByUser(int userId) throws DataAccessException {
      try {
        StringBuilder builder = new StringBuilder(
                "SELECT SQL_CALC_FOUND_ROWS " + USER_FIELDS +
                    ", SUM(invoice_amount) as amount1 FROM users " +
                    "LEFT JOIN invoices ON users.user_id = invoices.invoice_user_id " +
                    " WHERE invoice_user_id = ?");
        PreparedStatement ps = prepareStatement(builder.toString());
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        PaymentUserStatistic.Builder statBuilder = toSingleObject(ps, JDBCPaymentStatisticsDAO::populateUserStatisticBuilder);

        builder = new StringBuilder("SELECT user_id, SUM(payment_amount) as amount2 FROM users " +
                        "LEFT JOIN payments ON users.user_id = payments.payment_user_id ");
        builder.append(" WHERE payments.payment_include_in_balance = 1 ");
        builder.append(" AND payment_user_id = ?");
        PreparedStatement ps2 = prepareStatement(builder.toString());
        ps2.setInt(1, userId);
        ResultSet rs2 = ps2.executeQuery();
        return statBuilder.amountPaid(rs2.next() ? rs2.getFloat("amount2") : 0).build();
      } catch (SQLException ex) {
          throw new DataAccessException(ex);
      }
    }

}
