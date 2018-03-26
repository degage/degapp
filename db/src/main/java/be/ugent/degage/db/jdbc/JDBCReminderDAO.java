package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.ReminderDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.regex.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

/**
 * JDBC implementation of {@link ReminderDAO}
 */
public class JDBCReminderDAO extends AbstractDAO implements ReminderDAO {


    private static final String REMINDER_FIELDS =
            "reminder_id, reminder_date, reminder_description, reminder_invoice_id, reminder_status, reminder_send_date ";


    public JDBCReminderDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static Reminder populateReminder(ResultSet rs) throws SQLException {

        Reminder r = new Reminder.Builder()
                .id(rs.getInt("reminder_id"))
                .date(rs.getDate("reminder_date").toLocalDate())
                .description(rs.getString("reminder_description"))
                .invoiceId(rs.getInt("reminder_invoice_id"))
                .status(rs.getString("reminder_status"))
                .sendDate(rs.getDate("reminder_send_date") != null ? rs.getDate("reminder_send_date").toLocalDate() : null)
                .build();

        return r;
    }

    /*
     * Creates a reminder in the db
     * Only if there is no reminder yet with the same invoice and description!
     * Returns -1 when no reminder was created
     */
    private int createReminder(Invoice invoice, String description) {
        if (existsReminder(invoice, description)) {
            //do nothing, reminder already send
            return -1;
        }
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO reminders(reminder_date, reminder_description, reminder_invoice_id) " +
                        "VALUES (?,?,?)",
                "reminder_id"
        )) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setString(2, description);
            ps.setInt(3, invoice.getId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                int reminderId = keys.getInt(1);
                return reminderId;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create reminder.", ex);
        }
    }


    @Override
    public void updateReminder(Reminder reminder) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE reminders SET reminder_date = ?, reminder_description = ?, reminder_invoice_id = ?, " +
                "reminder_status = ?, reminder_send_date = ? WHERE reminder_id = ?"
        )) {
            ps.setDate(1, Date.valueOf(reminder.getDate()));
            ps.setString(2, reminder.getDescription());
            ps.setInt(3, reminder.getInvoiceId());
            ps.setString(4, reminder.getStatus().toString());
            ps.setDate(5, reminder.getSendDate() != null ? Date.valueOf(reminder.getSendDate()) : null);
            ps.setInt(6, reminder.getId());

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("No rows were affected when updating reminder.");
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update reminder.", ex);
        }
    }

    //returns true if the reminder already exists
    private boolean existsReminder(Invoice invoice, String description) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT * FROM reminders WHERE reminder_invoice_id = ? AND reminder_description = ? "
        )) {
            ps.setInt(1, invoice.getId());
            ps.setString(2, description);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Error while checking reminder.", e);
        }
    }

    @Override
    public Iterable<Reminder> listAllReminders() throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + REMINDER_FIELDS + " FROM reminders ORDER BY reminder_id ASC"
        )) {
            return toList(ps, JDBCReminderDAO::populateReminder);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all reminders", ex);
        }
    }

    @Override
    public int createInitialReminder(Invoice invoice)throws DataAccessException{
        return createReminder(invoice, "INITIAL");
    }

    @Override
    public int createFirstReminder(Invoice invoice) throws DataAccessException {
        return createReminder(invoice, "FIRST");
    }

    @Override
    public int createSecondReminder(Invoice invoice) throws DataAccessException {
        return createReminder(invoice, "SECOND");
    }

    @Override
    public int createThirdReminder(Invoice invoice) throws DataAccessException {
        return createReminder(invoice, "THIRD");
    }


    @Override
    public Iterable<Reminder> listRemindersForUser(int userId) throws DataAccessException {

        try (PreparedStatement ps = prepareStatement(
                "SELECT * FROM reminders " +
                        "INNER JOIN invoices on reminder_invoice_id = invoice_id " +
                        "WHERE invoice_user_id = ? ORDER BY invoice_date ASC "
        )) {
            ps.setInt(1, userId);
            return toList(ps, JDBCReminderDAO::populateReminder);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all reminders for user", ex);
        }
    }

    @Override
    public List<Reminder> listRemindersForInvoice(int invoiceId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT * FROM reminders WHERE reminder_invoice_id = ? ORDER BY reminder_description ASC"
        )) {
            ps.setInt(1, invoiceId);
            return toList(ps, JDBCReminderDAO::populateReminder);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all reminders for invoice", ex);
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
    public Page<ReminderAndUserAndInvoice> listReminderAndUserAndInvoice(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(
                "SELECT SQL_CALC_FOUND_ROWS * FROM reminders " +
                "INNER JOIN invoices ON reminder_invoice_id = invoice_id " +
                "INNER JOIN users ON invoice_user_id = user_id "
        );

        // add filters
        StringBuilder filterBuilder = new StringBuilder();

        FilterUtils.appendContainsFilter(filterBuilder, "invoice_number", filter.getValue(FilterField.INVOICE_NUMBER));
        FilterUtils.appendContainsFilter(filterBuilder, "reminder_description", Reminder.translate(filter.getValue(FilterField.REMINDER_DESCRIPTION)));
        FilterUtils.appendContainsFilter(filterBuilder, "reminder_date", filter.getValue(FilterField.REMINDER_DATE));
        FilterUtils.appendContainsFilter(filterBuilder, "reminder_status", Reminder.translateStatus(filter.getValue(FilterField.REMINDER_STATUS)));

        String userId;
        String user = filter.getValue(FilterField.USER_ID);
        if (user.equals("") || user.startsWith("-")) {
            userId = "";
        } else {
            userId = user;
        }
        FilterUtils.appendIntFilter(filterBuilder, "user_id", userId);

        if (filterBuilder.length() > 0) {
            builder.append(" WHERE ").append(filterBuilder.substring(4));
        }

        // add order
        switch (orderBy) {
            case REMINDER_DATE:
                builder.append(" ORDER BY reminder_date ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case USER_NAME:
                builder.append(" ORDER BY user_lastname ");
                builder.append(asc ? "ASC" : "DESC");
                builder.append(" , user_firstname ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            default: //default open first
                builder.append(" ORDER BY reminder_status ASC");
                break;
        }

        builder.append(" LIMIT ").append(pageSize).append(" OFFSET ").append((page-1)*pageSize);
        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            return toPage(ps, pageSize, rs -> new ReminderAndUserAndInvoice(
                    new Reminder.Builder()
                            .id(rs.getInt("reminder_id"))
                            .date(rs.getDate("reminder_date").toLocalDate())
                            .description(rs.getString("reminder_description"))
                            .invoiceId(rs.getInt("reminder_invoice_id"))
                            .status(rs.getString("reminder_status"))
                            .build(),
                    rs.getObject("user_id") == null ? null : new User(
                            rs.getInt("user_id"),
                            rs.getString("user_email"),
                            rs.getString("user_firstname"),
                            rs.getString("user_lastname"),
                            UserStatus.valueOf(rs.getString("user_status")),
                            rs.getString("user_phone"),
                            rs.getString("user_cellphone"),
                            (Integer) rs.getObject("user_degage_id")),
                    new Invoice.Builder(rs.getString("invoice_number"), rs.getInt("invoice_user_id"), rs.getInt("invoice_billing_id"))
                            .id(rs.getInt("invoice_id"))
                            .amount(rs.getFloat("invoice_amount"))
                            .status(InvoiceStatus.valueOf(rs.getString("invoice_status")))
                            .type(InvoiceType.valueOf(rs.getString("invoice_type")))
                            .date(rs.getDate("invoice_date") == null ? null : rs.getDate("invoice_date").toLocalDate())
                            .paymentDate(rs.getDate("invoice_payment_date") == null ? null : rs.getDate("invoice_payment_date").toLocalDate())
                            .dueDate(rs.getDate("invoice_due_date") == null ? null : rs.getDate("invoice_due_date").toLocalDate())
                            .comment(rs.getString("invoice_comment"))
                            .structuredCommunication(rs.getString("invoice_structured_comm"))
                            .carId(rs.getInt("invoice_car_id"))
                            .build()
            ));
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    /**
     * @param orderBy  The field you want to order by
     * @param asc      Ascending
     * @param page     The page you want to see
     * @param pageSize The page size
     * @param filter   The filter you want to apply
     * @return List of invoices with custom ordering and filtering
     */
    @Override
    public Page<ReminderAndUserAndInvoice> listReminderAndUserAndInvoice(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder(
                "SELECT SQL_CALC_FOUND_ROWS reminder_description, reminder_id, reminder_date, reminder_status, reminder_send_date, " +
                        "reminder_invoice_id, user_id, user_email, user_firstname, user_lastname, user_status, " +
                        "user_phone, user_cellphone, user_degage_id, user_payment_info, " +
                        "invoice_number, invoice_id, invoice_amount, invoice_status, invoice_date, " +
                        "invoice_payment_date, invoice_due_date, invoice_comment, invoice_structured_comm, " +
                        "invoice_car_id, invoice_user_id, invoice_billing_id, invoice_type " +
                        "FROM reminders " +
                        "LEFT JOIN invoices ON invoice_id = reminder_invoice_id " +
                        "JOIN users ON invoice_user_id = user_id "
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
            FilterUtils.appendOrContainsFilter(filterBuilder, "reminder_description", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "reminder_status", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "reminder_id", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_structured_comm", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_number", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_comment", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_amount", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_status", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "reminder_send_date", searchStrings[i]);

            builder.append(filterBuilder).append(")");
          }
        }

        // add order
        switch (orderBy) {
            case NUMBER:
                builder.append(" ORDER BY invoice_number ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case DATE:
                builder.append(" ORDER BY invoice_date ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case PAYMENT_DATE:
                builder.append(" ORDER BY invoice_payment_date ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case DUE_DATE:
                builder.append(" ORDER BY invoice_due_date ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case NAME:
                builder.append(" ORDER BY user_lastname ");
                builder.append(asc ? "ASC" : "DESC");
                builder.append(" , user_firstname ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case AMOUNT:
                builder.append(" ORDER BY invoice_amount ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case STATUS:
                builder.append(" ORDER BY invoice_status ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case SENT_ON:
                builder.append(" ORDER BY reminder_send_date ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            default:
                builder.append(" ORDER BY invoice_date DESC ");
                break;
        }

        builder.append(" LIMIT ").append (pageSize).append(" OFFSET ").append((page-1)*pageSize);
        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            return toPage(ps, pageSize, JDBCReminderDAO::populateReminderAndUserAndInvoice);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public List<ReminderAndUserAndInvoice> listUnsentReminders() throws DataAccessException {
      StringBuilder builder = new StringBuilder(
              "SELECT " + REMINDER_FIELDS + ", user_id, user_email, user_firstname, user_lastname, user_status, " +
              "user_phone, user_cellphone, user_degage_id, user_payment_info, " +
              "invoice_number, invoice_id, invoice_amount, invoice_status, invoice_date, " +
              "invoice_payment_date, invoice_due_date, invoice_comment, invoice_structured_comm, " +
              "invoice_car_id, invoice_user_id, invoice_billing_id, invoice_type " +
              "FROM reminders " +
              "LEFT JOIN invoices ON invoice_id = reminder_invoice_id " +
              "JOIN users ON invoice_user_id = user_id "
      );
      builder.append(" WHERE reminder_status = 'OPEN' ")
      .append(" AND users.user_send_reminder = true")
      .append(" AND invoice_date > '2017-11-01'")
      .append(" AND reminder_send_date is null");
      try (PreparedStatement ps = prepareStatement(builder.toString())) {
          return toList(ps, JDBCReminderDAO::populateReminderAndUserAndInvoice);
      } catch (SQLException ex) {
          throw new DataAccessException(ex);
      }
    }

    private void setReminderStatus(Reminder reminder, String status) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE reminders SET reminder_status = ? WHERE reminder_id = ? "
        )) {
            ps.setString(1, status);
            ps.setInt(2, reminder.getId());

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("No rows were affected when updating reminder.");
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update reminder.", ex);
        }
    }

    @Override
    public void setInvoiceRemindersPaid(Invoice invoice) throws DataAccessException {
        Iterable<Reminder> reminders = listRemindersForInvoice(invoice.getId());

        for (Reminder r : reminders) {
            setReminderStatus(r, "PAID");
        }

    }

    private static ReminderAndUserAndInvoice populateReminderAndUserAndInvoice(ResultSet rs) throws SQLException {
        return new ReminderAndUserAndInvoice(
            new Reminder.Builder()
                    .id(rs.getInt("reminder_id"))
                    .date(rs.getDate("reminder_date").toLocalDate())
                    .description(rs.getString("reminder_description"))
                    .invoiceId(rs.getInt("reminder_invoice_id"))
                    .status(rs.getString("reminder_status"))
                    .sendDate(rs.getDate("reminder_send_date") != null ? rs.getDate("reminder_send_date").toLocalDate() : null)
                    .build(),
            rs.getObject("user_id") == null ? null : new User(
                    rs.getInt("user_id"),
                    rs.getString("user_email"),
                    rs.getString("user_firstname"),
                    rs.getString("user_lastname"),
                    UserStatus.valueOf(rs.getString("user_status")),
                    rs.getString("user_phone"),
                    rs.getString("user_cellphone"),
                    (Integer) rs.getObject("user_degage_id")),
            new Invoice.Builder(rs.getString("invoice_number"), rs.getInt("invoice_user_id"), rs.getInt("invoice_billing_id"))
                    .id(rs.getInt("invoice_id"))
                    .amount(rs.getFloat("invoice_amount"))
                    .status(InvoiceStatus.valueOf(rs.getString("invoice_status")))
                    .type(InvoiceType.valueOf(rs.getString("invoice_type")))
                    .date(rs.getDate("invoice_date") == null ? null : rs.getDate("invoice_date").toLocalDate())
                    .paymentDate(rs.getDate("invoice_payment_date") == null ? null : rs.getDate("invoice_payment_date").toLocalDate())
                    .dueDate(rs.getDate("invoice_due_date") == null ? null : rs.getDate("invoice_due_date").toLocalDate())
                    .comment(rs.getString("invoice_comment"))
                    .structuredCommunication(rs.getString("invoice_structured_comm"))
                    .carId(rs.getInt("invoice_car_id"))
                    .build()
        );
    }

    @Override
    public ReminderAndUserAndInvoice getReminderAndUserAndInvoice(int reminderId) throws DataAccessException {
      StringBuilder builder = new StringBuilder(
          "SELECT SQL_CALC_FOUND_ROWS * FROM reminders " +
          "INNER JOIN invoices ON reminder_invoice_id = invoice_id " +
          "INNER JOIN users ON invoice_user_id = user_id " +
          "WHERE reminder_id = ?"
      );

      try (PreparedStatement ps = prepareStatement(builder.toString())) {
        ps.setInt(1, reminderId);
        return toSingleObject(ps, JDBCReminderDAO::populateReminderAndUserAndInvoice);
      } catch (SQLException ex) {
          throw new DataAccessException(ex);
      }

    }

    @Override
    public void setInvoiceRemindersPaid(ArrayList<Integer> invoiceIds) throws DataAccessException {
        if (invoiceIds.size() == 0) return;

        StringBuilder sb = new StringBuilder("UPDATE reminders SET reminder_status = ? WHERE reminder_invoice_id in (");
        for (int i = 0; i < invoiceIds.size(); i++){
            sb.append("?");
            if (i < (invoiceIds.size() - 1)) {
                sb.append(", ");
            }
        }
        sb.append(") AND reminder_status != 'PAID'");
        try (PreparedStatement ps = prepareStatement(
            sb.toString()
        )) {
            ps.setString(1, "PAID");
            for (int i = 0; i < invoiceIds.size(); i++){
                ps.setInt((2+i), invoiceIds.get(i));
            }
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update reminder.", ex);
        }

    }



}
