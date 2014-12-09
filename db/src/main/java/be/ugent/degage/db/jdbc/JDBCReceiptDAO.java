package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.*;
import be.ugent.degage.db.dao.ReceiptDAO;
import be.ugent.degage.db.models.Receipt;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.File;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class JDBCReceiptDAO extends AbstractDAO implements ReceiptDAO {


    // TODO: more fields to filter on
    public static final String USER_FRAGMENT =	" receipts.receipt_userID LIKE ? ";

    public static final String DATE_FRAGMENT1 = " receipts.receipt_date BETWEEN ? AND ? ";

    public static final String DATE_FRAGMENT2 = " receipts.receipt_date <= ? ";

    private static final String RECEIPT_FIELDS = " receipt_id, receipt_name, receipt_date, receipt_fileID, receipt_userID ";

    private static final String RECEIPT_QUERY = "SELECT * FROM receipts " +
            " LEFT JOIN files ON files.file_id = receipts.receipt_fileID " +
            " LEFT JOIN users ON users.user_id = receipts.receipt_userID ";

    public static final String FILTER_FRAGMENT =
        " WHERE "+ USER_FRAGMENT + " AND "+ DATE_FRAGMENT2;

    public JDBCReceiptDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private LazyStatement getGetAmountOfReceiptsStatement = new LazyStatement(
            "SELECT COUNT(receipt_id) AS amount_of_receipts FROM receipts " + FILTER_FRAGMENT
    );
	    //getGetAmountOfReceiptsStatement = connection.prepareStatement("SELECT COUNT(receipt_id) AS amount_of_receipts FROM Receipts WHERE receipts.receipt_date<=?;");
    /**
     * @param filterDate Date to use for filtering
     * @return The amount of filtered cars
     * @throws be.ugent.degage.db.DataAccessException
     */
    @Override
    public int getAmountOfReceipts(LocalDate filterDate, User user) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfReceiptsStatement.value();
            fillFragment(ps, filterDate, user);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_receipts");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of receipts", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of receipts", ex);
        }
    }

    private LazyStatement getReceiptsListStatement = new LazyStatement(
            RECEIPT_QUERY + FILTER_FRAGMENT + " ORDER BY receipts.receipt_date asc LIMIT ?, ? "
    );

    public List<Receipt> getReceiptsList(FilterField orderBy, boolean asc, int page, int pageSize, LocalDate filterDate, User user){
        try {
            PreparedStatement ps = getReceiptsListStatement.value();
            fillFragment(ps, filterDate, user);
            int first = (page-1)*pageSize;
            ps.setInt(3, first);
            ps.setInt(4, pageSize);
            return getReceipts(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of receipts", ex);
        }
    }

    private List<Receipt> getReceipts(PreparedStatement ps) {
        List<Receipt> receipts = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                receipts.add(populateReceipt(rs, true, true));
            }
            return receipts;
        } catch (SQLException ex) {
            throw new DataAccessException("Error reading receipts resultset", ex);
        }
    }

    private void fillFragment(PreparedStatement ps, LocalDate filterDate, User user) throws SQLException {

        ps.setInt(1, user.getId());

        if (filterDate == null) {
            filterDate = LocalDate.now();
        }
        ps.setDate (2, Date.valueOf(filterDate));
    }

    public static Receipt populateReceipt(ResultSet rs, boolean withDate, boolean withFiles) throws SQLException {
        return populateReceipt(rs, withDate, withFiles, "receipts");
    }

    public static Receipt populateReceipt(ResultSet rs, boolean withDate, boolean withFiles, String tableName) throws SQLException {
        Receipt receipt = new Receipt(
		rs.getInt(tableName + ".receipt_id"), 
		rs.getString(tableName + ".receipt_name"),
		rs.getBigDecimal(tableName + ".receipt_price"));

	//receipt.setUser(JDBCUserDAO.populateUser(rs, false, false));
        if(withFiles) {
	    receipt.setFiles(JDBCFileDAO.populateFile(rs, "files"));
	}
        if (withDate) {
            Date receiptDate = rs.getDate(tableName + ".receipt_date");
            if (receiptDate != null) {
                receipt.setDate(receiptDate.toLocalDate());
            }
        }
        return receipt;
    }

    private LazyStatement getCreateReceiptStatement = new LazyStatement(
            "INSERT INTO receipts (receipt_name, receipt_date ,receipt_fileID,receipt_userID,receipt_price) VALUES (?,?,?,?,?)",
            "receipt_id"
    );

    @Override
    public Receipt createReceipt(String name, LocalDate date, File file, User user, BigDecimal price) throws DataAccessException {
	//(InfoSessionType type, String typeAlternative, User host, Address address, DateTime time, int maxEnrollees, String comments)
	if(name==null){
	    throw new DataAccessException("Tried to create receipt without a name");
	}
        if (user.getId() == 0){
            throw new DataAccessException("Tried to create receipt without receipt user");
	}
        if (file.getId() == 0){
            throw new DataAccessException("Tried to create receipt without receipt file");
	}
        try {
            PreparedStatement ps = getCreateReceiptStatement.value();
            ps.setString(1, name);
            ps.setDate(2, Date.valueOf(date));
	        ps.setInt(3, file.getId());
            ps.setInt(4, user.getId());
            ps.setBigDecimal(5, price);

            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating infosession.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Receipt(keys.getInt(1), name, file, date, user, price);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new receipt.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create receipt.", ex);
        }
    }
}
