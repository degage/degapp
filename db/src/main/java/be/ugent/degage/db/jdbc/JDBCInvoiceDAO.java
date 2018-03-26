package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import java.util.regex.*;
import java.sql.Date;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.time.LocalDate;


/**
 * JDBC implementation of {@link InvoiceDAO}
 */
public class JDBCInvoiceDAO extends AbstractDAO implements InvoiceDAO {

    public static final float AMOUNT_DEVIATION = 2f;

    //patterns to recognize invoice numbers in a string
    private static final Pattern INVOICE_NUMBER_PATTERN = Pattern.compile("(A|E)[0-9]*-[0-9]*-[0-9]*");
    private static final Pattern INVOICE_NUMBER_PATTERN2 = Pattern.compile("(A|E)[0-9]*");
    private static final Pattern STRUCT_COMM_PATTERN = Pattern.compile("\\+\\+\\+[0-9]*/[0-9]*/[0-9]*\\+\\+\\+");
    private static final Pattern STRUCT_COMM_PATTERN2 = Pattern.compile("[0-9]*/[0-9]*/[0-9]*");
    private static final Pattern STRUCT_COMM_PATTERN3 = Pattern.compile("[0-9]*");

    private static final String INVOICE_FIELDS =
            "i.invoice_id, i.invoice_number, i.invoice_date, i.invoice_payment_date, i.invoice_due_date, i.invoice_user_id, " +
            "i.invoice_billing_id, i.invoice_amount, i.invoice_comment, i.invoice_status, i.invoice_type, i.invoice_structured_comm, i.invoice_car_id ";

    private static final String INVOICE_AND_USER_FIELDS = INVOICE_FIELDS + ", user_id, user_email, user_firstname, user_lastname, user_status, " +
         "user_phone, user_cellphone, user_degage_id, user_payment_info, " +
         "domicileAddresses.address_id, domicileAddresses.address_country, domicileAddresses.address_city, " +
         "domicileAddresses.address_zipcode, domicileAddresses.address_street, domicileAddresses.address_number, " +
         "domicileAddresses.address_latitude, domicileAddresses.address_longitude, " +
         "residenceAddresses.address_id, residenceAddresses.address_country, residenceAddresses.address_city, " +
         "residenceAddresses.address_zipcode, residenceAddresses.address_street, residenceAddresses.address_number, " +
         "residenceAddresses.address_latitude, residenceAddresses.address_longitude ";

    public JDBCInvoiceDAO(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public int createInvoice(Invoice invoice) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO invoices(invoice_number, invoice_date, invoice_payment_date, invoice_due_date, invoice_user_id, " +
                "invoice_billing_id, invoice_amount, invoice_comment, invoice_status, invoice_structured_comm, invoice_car_id, invoice_type) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                "invoice_id"
        )) {
            ps.setString(1, invoice.getNumber());
            ps.setDate(2, invoice.getDate() == null ? null : Date.valueOf(invoice.getDate()));
            ps.setDate(3, invoice.getPaymentDate() == null ? null : Date.valueOf(invoice.getPaymentDate()));
            ps.setDate(4, invoice.getDueDate() == null ? null : Date.valueOf(invoice.getDueDate()));
            ps.setInt(5, invoice.getUserId());
            ps.setInt(6, invoice.getBillingId());
            ps.setFloat(7, invoice.getAmount());
            ps.setString(8, invoice.getComment());
            ps.setString(9, invoice.getStatus() == null ? "OPEN" : invoice.getStatus().toString());
            ps.setString(10, invoice.getStructuredCommunication());
            ps.setInt(11, invoice.getCarId());
            ps.setString(12, invoice.getType() == null ? "UNKNOWN" : invoice.getType().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                int invoiceId = keys.getInt(1);
                return invoiceId;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create invoice.", ex);
        }
    }

    @Override
    public boolean checkUniqueInvoice(Invoice invoice) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT * FROM invoices WHERE invoice_number = ? "
        )) {
            ps.setString(1, invoice.getNumber());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Error while checking invoice.", e);
        }
    }

  

    @Override
    public boolean checkExistingInvoice(int userId, InvoiceType invoiceType) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT * FROM invoices WHERE invoice_user_id = ? and invoice_type = ? "
        )) {
            ps.setInt(1, userId);
            ps.setString(2, invoiceType.toString());

            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new DataAccessException("Error while checking invoice.", e);
        }
    }

    public static User populateUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt("users.user_id"),
                rs.getString("users.user_email"),
                rs.getString("users.user_firstname"),
                rs.getString("users.user_lastname"),
                UserStatus.valueOf(rs.getString("users.user_status")),
                rs.getString("users.user_phone"),
                rs.getString("users.user_cellphone"),
                (Integer) rs.getObject("users.user_degage_id")
        );

        user.setAddressDomicile(JDBCAddressDAO.populateAddress(rs, "domicileAddresses"));
        user.setAddressResidence(JDBCAddressDAO.populateAddress(rs, "residenceAddresses"));
        user.setPaymentInfo(rs.getString("users.user_payment_info"));
        return user;
    }


    public static Invoice populateInvoice(ResultSet rs) throws SQLException {
        Date date = rs.getDate("invoice_date");
        Date paymentDate = rs.getDate("invoice_payment_date");
        Date dueDate = rs.getDate("invoice_due_date");
        InvoiceStatus status = InvoiceStatus.valueOf(rs.getString("invoice_status"));
        InvoiceType type = InvoiceType.valueOf(rs.getString("invoice_type"));

        return new Invoice.Builder(rs.getString("invoice_number"), rs.getInt("invoice_user_id"), rs.getInt("invoice_billing_id"))
                .id(rs.getInt("invoice_id"))
                .amount(rs.getFloat("invoice_amount"))
                .status(status == null ? InvoiceStatus.OPEN : status)
                .type(type == null ? InvoiceType.UNKNOWN : type)
                .date(date == null ? null : date.toLocalDate())
                .paymentDate(paymentDate == null ? null : paymentDate.toLocalDate())
                .dueDate(dueDate == null ? null : dueDate.toLocalDate())
                .comment(rs.getString("invoice_comment"))
                .structuredCommunication(rs.getString("invoice_structured_comm"))
                .carId(rs.getInt("invoice_car_id"))
                .build();
    }

    private static List<Integer> asListOfIntegers(String s) {
      List<String> paymentIdStrings = (s == null ? new ArrayList<String>() :
            new ArrayList<String>(Arrays.asList(s.split(","))));
      Set<Integer> paymentIdSet = new HashSet<Integer>();
      for (String paymentId : paymentIdStrings) {
        paymentIdSet.add(Integer.parseInt(paymentId));
      }
      return new ArrayList<Integer>(paymentIdSet);
    }

    public static InvoiceAndUser populateInvoiceAndUser(ResultSet rs) throws SQLException {
        return new InvoiceAndUser(
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
                    .paymentIds(asListOfIntegers(rs.getString("payment_ids")))
                    .paidAmount(rs.getFloat("paid_amount"))
                    .build(),
                    rs.getObject("user_id") == null ? null : populateUser(rs)
        );
    }

    @Override
    public Iterable<Invoice> listAllInvoices() {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + INVOICE_FIELDS + "FROM invoices i ORDER BY invoice_id ASC"
        )) {
            return toList(ps, JDBCInvoiceDAO::populateInvoice);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all invoices", ex);
        }
    }

    @Override
    public List<InvoiceAndUser> listAllInvoicesAndUsers() {
      String query =   "SELECT " + INVOICE_AND_USER_FIELDS +
         ", group_concat(pi.payment_id SEPARATOR ',') as payment_ids " +
         ", sum(p.payment_amount) as paid_amount " +
         "FROM invoices i " +
         "LEFT JOIN users ON invoice_user_id = user_id " +
         "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
         "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id " +
         "LEFT JOIN payment_invoice pi on pi.invoice_id = i.invoice_id " +
         "LEFT JOIN payments p on p.payment_id = pi.payment_id " +
         " GROUP BY " + INVOICE_AND_USER_FIELDS;
        try (PreparedStatement ps = prepareStatement(query)) {
            return toList(ps, JDBCInvoiceDAO::populateInvoiceAndUser);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all invoices and users", ex);
        }
    }

      /**
     * Queries 'degage' database for invoices with the given type and status filter. 
     * 
     * All users statisfying the status filter will be returned together with the
     * invoices of the given type. 
     */
    @Override
    public Iterable<InvoiceAndUser> listInvoicesAndUsersOnType(InvoiceType type, Set<UserStatus> statusFilter){
        List<String> statusList = new ArrayList<>();
        for(UserStatus status : statusFilter){
            statusList.add("user_status = " + "'" + status.name() + "'");
        }
        String filter = String.join(" OR ", statusList);

        String query = "SELECT "+ INVOICE_AND_USER_FIELDS + 
            ", group_concat(pi.payment_id SEPARATOR ',') as payment_ids " +
            ", sum(p.payment_amount) as paid_amount " +
            " FROM ( SELECT * FROM  invoices WHERE invoice_type = ? ) as i " + 
            "JOIN ( SELECT * FROM users WHERE " + filter + ") as users ON user_id = invoice_user_id " + 
            "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
            "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id " +
            "LEFT JOIN payment_invoice pi on pi.invoice_id = i.invoice_id " +
            "LEFT JOIN payments p on p.payment_id = pi.payment_id " +
            " GROUP BY " + INVOICE_AND_USER_FIELDS;
        


        try(PreparedStatement ps = prepareStatement(query)){
            int counter = 1 ; 
            ps.setString(counter ++ , type.name());
            return toList(ps, JDBCInvoiceDAO :: populateInvoiceAndUser); 

        }catch (SQLException e){
            System.err.println("\n" + e.getMessage());
            throw new DataAccessException("Error while getting invoices and users by type.", e);
        }
    }

    @Override
    public Iterable<InvoiceAndUser> listInvoicesAndUsers(String user) {
      String[] searchStrings = user.trim().split(" ");
      StringBuilder builder = new StringBuilder();
      builder.append("SELECT ").append(INVOICE_AND_USER_FIELDS)
        .append(", group_concat(pi.payment_Id SEPARATOR ',') as payment_ids ")
        .append(", sum(p.payment_amount) as paid_amount ")
        .append("FROM invoices i ")
        .append("LEFT JOIN users ON invoice_user_id = user_id ")
        .append("LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id ")
        .append("LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id ")
        .append("LEFT JOIN payment_invoice pi on pi.invoice_id = i.invoice_id ")
        .append("LEFT JOIN payments p on p.payment_id = pi.payment_id ")
        .append("WHERE ")
        ;
      for (int i = 0; i < searchStrings.length; i++) {
        if (i > 0) {
          builder.append(" AND ");
        }
        builder.append("(");
        StringBuilder filterBuilder = new StringBuilder();
        FilterUtils.appendOrContainsFilter(filterBuilder, "user_lastname", searchStrings[i]);
        FilterUtils.appendOrContainsFilter(filterBuilder, "user_firstname", searchStrings[i]);
        FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_structured_comm", searchStrings[i]);
        FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_number", searchStrings[i]);
        FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_comment", searchStrings[i]);
        FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_amount", searchStrings[i]);
        FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_status", searchStrings[i]);
        builder.append(filterBuilder).append(")");
      }
      builder.append(" GROUP BY ").append(INVOICE_AND_USER_FIELDS);
        try (PreparedStatement ps = prepareStatement(
          builder.toString()
        )) {
          return toList(ps, JDBCInvoiceDAO::populateInvoiceAndUser);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all invoices and users", ex);
        }
    }

    @Override
    public Invoice getInvoice(int invoiceId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + INVOICE_FIELDS + " FROM invoices i WHERE invoice_id = ? "
        )) {
            ps.setInt(1, invoiceId);
            return toSingleObject(ps, JDBCInvoiceDAO::populateInvoice);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get invoice", e);
        }
    }

    @Override
    public InvoiceAndUser getInvoiceAndUserByNumber(String invoiceNumber) throws DataAccessException {
      try (PreparedStatement ps = prepareStatement(
             "SELECT " + INVOICE_AND_USER_FIELDS +
             ", group_concat(pi.payment_Id SEPARATOR ',') as payment_ids " +
             ", sum(p.payment_amount) as paid_amount " +
              "FROM invoices i " +
              "LEFT JOIN users ON invoice_user_id = user_id " +
              "LEFT JOIN payment_invoice pi on pi.invoice_id = i.invoice_id " +
              "LEFT JOIN payments p on p.payment_id = pi.payment_id " +
              "WHERE invoice_number = ? " +
              " GROUP BY " + INVOICE_AND_USER_FIELDS
      )) {
          ps.setString(1, invoiceNumber);
          return toSingleObject(ps, JDBCInvoiceDAO::populateInvoiceAndUser);
      } catch (SQLException e) {
          throw new DataAccessException("Unable to get invoice", e);
      }
    }

    @Override
    public InvoiceAndUser getInvoiceAndUser(int invoiceId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
               "SELECT " + INVOICE_AND_USER_FIELDS +
               ", group_concat(pi.payment_Id SEPARATOR ',') as payment_ids " +
               ", sum(p.payment_amount) as paid_amount " +
                "FROM invoices i " +
                "LEFT JOIN users ON invoice_user_id = user_id " +
                "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
                "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id " +
                "LEFT JOIN payment_invoice pi on pi.invoice_id = i.invoice_id " +
                "LEFT JOIN payments p on p.payment_id = pi.payment_id " +
                "WHERE i.invoice_id = ? " +
                " GROUP BY " + INVOICE_AND_USER_FIELDS
        )) {
            ps.setInt(1, invoiceId);
            return toSingleObject(ps, JDBCInvoiceDAO::populateInvoiceAndUser);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get invoice", e);
        }
    }

    @Override
    public Invoice findInvoiceByComment(String comment) throws DataAccessException {
        if (comment == null) {
            return null;
        }

        Invoice res = null;

        Matcher m = INVOICE_NUMBER_PATTERN.matcher(comment);
        res = lookForInvoice(m);
        if (res != null) {
            return res;
        }

        m = INVOICE_NUMBER_PATTERN2.matcher(comment);
        res = lookForInvoice(m);
        if (res != null) {
            return res;
        }

        m = STRUCT_COMM_PATTERN.matcher(comment);
        res = lookForStructComm(m);
        if (res != null) {
            return res;
        }

        m = STRUCT_COMM_PATTERN2.matcher(comment);
        if (m.find()) {
            String structComm = m.group(0); // whole matched expression
            structComm = "+++" + structComm + "+++";

            try (PreparedStatement ps = prepareStatement(
                    "SELECT " + INVOICE_FIELDS + " FROM invoices i WHERE invoice_structured_comm = ? "
            )) {
                ps.setString(1, structComm);
                return toSingleObject(ps, JDBCInvoiceDAO::populateInvoice);
            } catch (SQLException e) {

            }
        }

        m = STRUCT_COMM_PATTERN3.matcher(comment);
        if (m.find()) {
            String structComm = m.group(0); // whole matched expression
            if (structComm.length() == 12) { // 3+4+5
                structComm = "+++" + structComm.substring(0, 3) + "/" + structComm.substring(3, 7)
                        + "/" + structComm.substring(7) + "+++";

                try (PreparedStatement ps = prepareStatement(
                        "SELECT " + INVOICE_FIELDS + " FROM invoices i WHERE invoice_structured_comm = ? "
                )) {
                    ps.setString(1, structComm);
                    return toSingleObject(ps, JDBCInvoiceDAO::populateInvoice);
                } catch (SQLException e) {

                }
            }
        }

        return null;
    }

    private Invoice lookForInvoice(Matcher m) {
        if (m.find()) {
            String invoiceNumber = m.group(0); // whole matched expression

            try (PreparedStatement ps = prepareStatement(
                    "SELECT " + INVOICE_FIELDS + " FROM invoices i WHERE invoice_number = ? "
            )) {
                ps.setString(1, invoiceNumber);
                return toSingleObject(ps, JDBCInvoiceDAO::populateInvoice);
            } catch (SQLException e) {

            }
        }

        return null;
    }

    private Invoice lookForStructComm(Matcher m) {
        if (m.find()) {
            String structComm = m.group(0); // whole matched expression

            try (PreparedStatement ps = prepareStatement(
                    "SELECT " + INVOICE_FIELDS + " FROM invoices i WHERE invoice_structured_comm = ? "
            )) {
                ps.setString(1, structComm);
                return toSingleObject(ps, JDBCInvoiceDAO::populateInvoice);
            } catch (SQLException e) {

            }
        }

        return null;
    }

    @Override
    public Invoice getInvoiceByStructComm(String structuredCommunication) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + INVOICE_FIELDS + " FROM invoices i WHERE invoice_structured_comm = ? "
        )) {
            ps.setString(1, structuredCommunication);
            return toSingleObject(ps, JDBCInvoiceDAO::populateInvoice);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to fetch invoice by structured communication.", e);
        }
    }

    @Override
    public void updateInvoice(Invoice invoice) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE invoices SET invoice_payment_date=?, invoice_status=? WHERE invoice_id = ?"
        )) {
            ps.setDate(1, invoice.getPaymentDate() == null ? null : Date.valueOf(invoice.getPaymentDate()));
            ps.setString(2, invoice.getStatus().name());
            int invoiceId = invoice.getId();
            ps.setInt(3, invoiceId);


            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("No rows were affected when updating invoice.");
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update invoice.", ex);
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
                "SELECT SQL_CALC_FOUND_ROWS user_id, user_email, user_firstname, user_lastname, user_status, " +
                        "user_phone, user_cellphone, user_degage_id, user_payment_info, " +
                        "invoice_number, i.invoice_id, invoice_amount, invoice_status, invoice_date, invoice_type, " +
                        "invoice_payment_date, invoice_due_date, invoice_comment, invoice_structured_comm, " +
                        "invoice_car_id, invoice_user_id, invoice_billing_id, " +
                        "group_concat(p.payment_id SEPARATOR ',') as payment_ids, " +
                        "sum(p.payment_amount) as paid_amount " +
                        "FROM invoices i " +
                        "JOIN users ON invoice_user_id = user_id " +
                        "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
                        "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id " +
                        "LEFT JOIN payment_invoice pi ON pi.invoice_id = i.invoice_id " +
                        "LEFT JOIN payments p on p.payment_id = pi.payment_id "
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
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_structured_comm", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_number", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_comment", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_amount", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_status", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "invoice_type", searchStrings[i]);
            builder.append(filterBuilder).append(")");
          }
        }


        builder.append(" GROUP BY i.invoice_id, " +
                "user_id, user_email, user_firstname, user_lastname, user_status, " +
                "user_phone, user_cellphone, user_degage_id, user_payment_info, invoice_billing_id, " +
                "invoice_number, invoice_amount, invoice_status, invoice_date, invoice_payment_date, invoice_type, " +
                "invoice_due_date, invoice_comment, invoice_structured_comm, invoice_car_id, invoice_user_id "
        );

        // String str = Reminder.translate(filter.getValue(FilterField.REMINDER_DESCRIPTION));
        //
        // if (str != null && ! str.isEmpty()) {
        //     builder.append(" HAVING ")
        //             .append("MAX(reminder_description)")
        //             .append(" LIKE '%") ;
        //     FilterUtils.appendEscapedString(builder, str);
        //     builder.append("%'");
        // }

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
            default:
                builder.append(" ORDER BY invoice_date DESC ");
                break;
        }

        builder.append(" LIMIT ").append (pageSize).append(" OFFSET ").append((page-1)*pageSize);
        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            return toPage(ps, pageSize, rs -> new ReminderAndUserAndInvoice(
                    null,
                    rs.getObject("user_id") == null ? null : new User(
                            rs.getInt("user_id"),
                            rs.getString("user_email"),
                            rs.getString("user_firstname"),
                            rs.getString("user_lastname"),
                            UserStatus.valueOf(rs.getString("user_status")),
                            rs.getString("user_phone"),
                            rs.getString("user_cellphone"),
                            (Integer) rs.getObject("user_degage_id"))
                            .setPaymentInfoBuilder(rs.getString("user_payment_info")),
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
                            .paymentIds(asListOfIntegers(rs.getString("payment_ids")))
                            .paidAmount(rs.getFloat("paid_amount"))
                            .build()
            ));
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public Iterable<InvoiceAndUser> listSuggestedInvoicesForPayment(Payment payment) {
        String query = "SELECT " + INVOICE_AND_USER_FIELDS +
                        ", group_concat(pi.payment_Id SEPARATOR ',') as payment_ids " +
                        ", sum(p.payment_amount) as paid_amount " +
                        "FROM invoices i " +
                        "LEFT JOIN users ON invoice_user_id = user_id " +
                        "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
                        "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id " +
                        "LEFT JOIN payment_invoice pi on pi.invoice_id = i.invoice_id " +
                        "LEFT JOIN payments p on p.payment_id = pi.payment_id " +
          "WHERE invoice_date <= ? " +
          "AND (invoice_user_id = ? " +
          "OR abs((invoice_amount / ?) -1) < 0.02 " +
          "OR soundex(concat(user_lastname, ' ', user_firstname)) = soundex(?) " +
          "OR soundex(concat(user_firstname, ' ', user_lastname)) = soundex(?) " +
          "OR soundex(user_lastname) = soundex(?) " +
          "OR soundex(user_lastname) = soundex(?) " +
          "OR (user_accountnumber = ? AND user_accountnumber IS NOT NULL) " +
          "OR (lower(invoice_comment) like lower(?) AND invoice_comment IS NOT NULL) " +
          "OR (lower(invoice_structured_comm) like lower(?) AND invoice_structured_comm IS NOT NULL) " +
          ") GROUP BY " + INVOICE_AND_USER_FIELDS +
          "ORDER BY invoice_user_id, invoice_amount DESC";
        try (PreparedStatement ps = prepareStatement(query)) {
            ps.setDate(1, payment.getDate() == null ? null : Date.valueOf(payment.getDate()));
            ps.setInt(2, payment.getUserId());
            ps.setFloat(3, payment.getAmount());
            ps.setString(4, payment.getName());
            ps.setString(5, payment.getName());
            ps.setString(6, payment.getName().split("-")[0].trim());
            ps.setString(7, payment.getName().split("-").length > 1 ? payment.getName().split("-")[1].trim() : "");
            ps.setString(8, payment.getAccountNumber());
            ps.setString(9, payment.getComment() == null ? "" : "%" + payment.getComment() + "%");
            ps.setString(10, payment.getStructuredCommunication() == null || payment.getStructuredCommunication() == "" ? "" : "%" + payment.getStructuredCommunication() + "%");
          return toList(ps, JDBCInvoiceDAO::populateInvoiceAndUser);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all invoices for user", ex);
        }
    }

    @Override
    public List<InvoiceAndUser> listInvoicesForUser(int userId) {
      try (PreparedStatement ps = prepareStatement(
        "SELECT " + INVOICE_AND_USER_FIELDS +
        ", group_concat(pi.payment_Id SEPARATOR ',') as payment_ids " +
        ", sum(p.payment_amount) as paid_amount " +
        "FROM invoices i " +
        "LEFT JOIN users ON invoice_user_id = user_id " +
        "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
        "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id " +
        "LEFT JOIN payment_invoice pi on pi.invoice_id = i.invoice_id " +
        "LEFT JOIN payments p on p.payment_id = pi.payment_id " +
        "WHERE invoice_user_id = ? " +
        " GROUP BY " + INVOICE_AND_USER_FIELDS +
        " ORDER BY invoice_date DESC"
        )) {
            ps.setInt(1, userId);
            return toList(ps, JDBCInvoiceDAO::populateInvoiceAndUser);
      } catch (SQLException ex) {
          throw new DataAccessException("Cannot list all invoices for user", ex);
      }
    }

    @Override
    public List<InvoiceAndUser> listUnpaidInvoicesByUser(int userId) {
        try (PreparedStatement ps = prepareStatement(
          "SELECT " + INVOICE_AND_USER_FIELDS +
          ", group_concat(pi.payment_Id SEPARATOR ',') as payment_ids " +
          ", sum(p.payment_amount) as paid_amount " +
          "FROM invoices i " +
          "LEFT JOIN users ON invoice_user_id = user_id " +
          "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
          "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id " +
          "LEFT JOIN payment_invoice pi on pi.invoice_id = i.invoice_id " +
          "LEFT JOIN payments p on p.payment_id = pi.payment_id " +
          "WHERE invoice_user_id = ? " +
          " AND invoice_amount > 2 " +
          " AND invoice_status != 'PAID' " +
          " GROUP BY " + INVOICE_AND_USER_FIELDS +
          " ORDER BY invoice_date DESC"
          )) {
              ps.setInt(1, userId);
              return toList(ps, JDBCInvoiceDAO::populateInvoiceAndUser);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all unpaid invoices for user", ex);
        }
    }

    @Override
    public int checkDueDate() {
        List<InvoiceAndUser> invoicesAndUsers = listAllInvoicesAndUsers();

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        int numChanged = 0;

        for (InvoiceAndUser invoiceAndUser : invoicesAndUsers) {
            Invoice invoice = invoiceAndUser.getInvoice();
            String status = "OPEN";
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(Date.valueOf(invoice.getDueDate()));
            //invoices of 0 EUR
            if (invoice.getAmount() == 0) {
                updateInvoiceStatus(invoice.getInvoiceId(), "PAID");
            //invoices without linked payments
            } else if ((invoice.getPaymentIds() == null || invoice.getPaymentIds().size() == 0) && today.after(dueDate)) {
                updateInvoiceStatus(invoice.getInvoiceId(), "OVERDUE");
            //unpaid invoices with linked payments
            } else if (invoice.getPaymentIds() != null && invoice.getPaymentIds().size() > 0 && invoice.getStatus() != InvoiceStatus.PAID) {
                if (invoice.getPaidAmount() >= invoice.getAmount() - AMOUNT_DEVIATION) {
                    updateInvoiceStatus(invoice.getInvoiceId(), "PAID");
                } else if (today.after(dueDate)) {
                    updateInvoiceStatus(invoice.getInvoiceId(), "OVERDUE");
                }
            }
        }
        return numChanged;
    }

    public void updateInvoiceStatus(int invoiceId, String status) {
        try (PreparedStatement ps = prepareStatement(
            "UPDATE invoices SET invoice_status = ? WHERE invoice_id = ?"
        )) {
            ps.setString(1, status);
            ps.setInt(2, invoiceId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not update invoice.", ex);
        }
    }


    @Override
    public float sumOfPaymentsForInvoice(int invoiceId) {
      try (PreparedStatement ps = prepareStatement(
              "SELECT SUM(payment_amount) FROM payments p " +
              "JOIN payment_invoice pi on pi.payment_id = p.payment_id " +
              "JOIN invoices i on pi.invoice_id = i.invoice_id " +
              "WHERE i.invoice_id = ?"
      )) {
          ps.setInt(1, invoiceId);
          return toSingleFloat(ps);
      } catch (SQLException ex) {
          throw new DataAccessException(ex);
      }
    }

    @Override
    public Iterable<Invoice> listInvoiceByNumber(String str, int limit) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + INVOICE_FIELDS + "FROM invoices " +
                        "WHERE invoice_number LIKE CONCAT ('%', ?, '%') " +
                        "ORDER BY invoice_number ASC " +
                        "LIMIT ?"
        )) {
            ps.setString(1, str);
            ps.setInt(2, limit);
            return toList(ps, JDBCInvoiceDAO::populateInvoice);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public String getLastMembershipInvoiceNumber(String year) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT invoice_number FROM invoices " +
                "WHERE invoice_number LIKE 'L%' " +
                "AND invoice_number LIKE ? " +
                "ORDER BY invoice_id DESC LIMIT 1"
        )) {
            ps.setString(1, "%-" + year + "-%");
            return toSingleString(ps);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }        
    }

    public static Integer populateInteger(ResultSet rs) throws SQLException {
        return rs.getInt(1);
    }

    @Override
    public List<Integer> listUsersWithoutMembershipInvoices() {
        try (PreparedStatement ps = prepareStatement(
            "SELECT user_id FROM users u " +
            "WHERE u.user_contract IS NOT NULL AND " +
            "u.user_status = 'REGISTERED' AND " +
            "u.user_id NOT IN (select invoice_user_id FROM invoices where invoice_type = 'CAR_MEMBERSHIP')"
        )) {
            return toList(ps, JDBCInvoiceDAO::populateInteger);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public InvoiceAndUser createMembershipInvoiceAndUser(int userId) throws DataAccessException {
        if (checkExistingInvoice(userId, InvoiceType.CAR_MEMBERSHIP)) {
            throw new DataAccessException("Invoice already exists for user " + userId);
        }
        // attach invoice to the latest quarter
        int billingId = context.getBillingDAO().getLatestBillingId();
        String year = String.valueOf(LocalDate.now().getYear() - 2000);
        LocalDate invoiceDate = LocalDate.now();
        String lastInvoiceNumber = getLastMembershipInvoiceNumber(year);
        String invoiceNumber = String.format("L-%s-%04d", year, lastInvoiceNumber == null ? 0 : (Integer.parseInt(lastInvoiceNumber.substring(5,9)) + 1));
        float amount = 110f;
    
        Invoice i =  new Invoice.Builder(invoiceNumber, userId, billingId)
            .date(invoiceDate)
            .dueDate(invoiceDate.plusWeeks(2))
            .amount(amount)
            .status(InvoiceStatus.OPEN)
            .type(InvoiceType.CAR_MEMBERSHIP)
            .structuredCommunication(structuredComment(billingId, (int)amount, userId))
            .build();
    
        
        if (checkUniqueInvoice(i)) {
            int id = createInvoice(i);
            return getInvoiceAndUser(id);
        } else {
            throw new DataAccessException("Invoice already exists for user " + userId);
        }
    
    }

    public String structuredComment(int first, int second, int third) {
        int pre = (3 * first) % 1000;
        int mid = (second % 10000);
        int end = (third % 1000);

        int mod = (10000000 * pre + 1000 * mid + end) % 97;
        if (mod == 0) {
            mod = 97;
        }
        return String.format("+++%03d/%04d/%03d%02d+++", pre, mid, end, mod);
    }

}
