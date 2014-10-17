package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.*;
import be.ugent.degage.db.dao.ReceiptDAO;
import be.ugent.degage.db.models.Receipt;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.File;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class JDBCReceiptDAO implements ReceiptDAO {

    private Connection connection;
    public JDBCReceiptDAO(Connection connection) {
        this.connection = connection;
    }

    // TODO: more fields to filter on
    public static final String USER_FRAGMENT = 
	" receipts.receipt_userID LIKE ? ";

    public static final String DATE_FRAGMENT1 =
        " receipts.receipt_date BETWEEN ? AND ? ";

    public static final String DATE_FRAGMENT2 =
        " receipts.receipt_date <= ? ";

    private static final String RECEIPT_FIELDS = " receipt_id, receipt_name, receipt_date, receipt_fileID, receipt_userID ";

    private static final String RECEIPT_QUERY = "SELECT * FROM receipts " +
            " LEFT JOIN files ON files.file_id = receipts.receipt_fileID " +
            " LEFT JOIN users ON users.user_id = receipts.receipt_userID ";

    public static final String FILTER_FRAGMENT =
        " WHERE "+ USER_FRAGMENT + " AND "+ DATE_FRAGMENT2;

    private PreparedStatement getGetAmountOfReceiptsStatement;
    private PreparedStatement getReceiptsListStatement;
    private PreparedStatement createReceiptStatement;


    private PreparedStatement getGetAmountOfReceiptsStatement() throws SQLException {
        if(getGetAmountOfReceiptsStatement == null) {
            getGetAmountOfReceiptsStatement = connection.prepareStatement("SELECT COUNT(receipt_id) AS amount_of_receipts FROM receipts " + FILTER_FRAGMENT+ ";");
	    //getGetAmountOfReceiptsStatement = connection.prepareStatement("SELECT COUNT(receipt_id) AS amount_of_receipts FROM Receipts WHERE receipts.receipt_date<=?;");
        }
        return getGetAmountOfReceiptsStatement;
    }
    
    private PreparedStatement getReceiptsListStatement() throws SQLException {
        if(getReceiptsListStatement == null) {
            getReceiptsListStatement = connection.prepareStatement(RECEIPT_QUERY + FILTER_FRAGMENT + " ORDER BY receipts.receipt_date asc LIMIT ?, ? ;");
        }
        return getReceiptsListStatement;
    }

    private PreparedStatement getCreateReceiptStatement() throws SQLException {
        if (createReceiptStatement == null) {
            createReceiptStatement = connection.prepareStatement("INSERT INTO receipts (receipt_name, receipt_date ,receipt_fileID,receipt_userID,receipt_price) VALUES (?,?,?,?,?);",
                    new String[]{"receipt_id"});
        }
        return createReceiptStatement;
    }


    /**
     * @param filter The filter to apply to
     * @return The amount of filtered cars
     * @throws be.ugent.degage.db.DataAccessException
     */
    @Override
    public int getAmountOfReceipts(Filter filter, User user) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfReceiptsStatement();
            fillFragment(ps, filter, 1, user);

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

    public List<Receipt> getReceiptsList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter, User user){
        try {
            PreparedStatement ps = getReceiptsListStatement();
            fillFragment(ps, filter, 1, user);
            int first = (page-1)*pageSize;
            ps.setInt(3, first);
            ps.setInt(4, pageSize);
            return getReceipts(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of reciepts", ex);
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

    private void fillFragment(PreparedStatement ps, Filter filter, int start, User user) throws SQLException {
	
	ps.setInt(start, user.getId());        

	if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
	String string_date=filter.getValue(FilterField.RECEIPT_DATE);
	//string_date="2036-10-29 11:42:32";
        Timestamp date;
	if(string_date.equals("")){
	    java.util.Date now= new java.util.Date();
	    date = new Timestamp(now.getTime());
	    //date=DateTime.now().toString();
	} else{
            date= Timestamp.valueOf(string_date);
        }
        ps.setTimestamp(start+1, date);
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
        if(withDate) {
	    Object date = rs.getObject(tableName + ".receipt_date");
            if(date!=null){
                receipt.setDate(new DateTime(date));
            }
        }
        return receipt;
    }

    @Override
    public Receipt createReceipt(String name, DateTime date, File file, User user, BigDecimal price) throws DataAccessException {
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
            PreparedStatement ps = getCreateReceiptStatement();
            ps.setString(1, name);
            ps.setTimestamp(2, new Timestamp(date.getMillis())); //TODO: timezones?? convert to datetime see below
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
