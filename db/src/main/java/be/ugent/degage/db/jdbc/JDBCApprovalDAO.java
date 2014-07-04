package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.dao.ApprovalDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Approval;
import be.ugent.degage.db.models.InfoSession;
import be.ugent.degage.db.models.User;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 3/30/2014.
 */
class JDBCApprovalDAO implements ApprovalDAO {

    private static final String APPROVAL_FIELDS = "approval_id, approval_user, approval_admin, approval_submission, approval_date, approval_status, approval_infosession, approval_user_message, approval_admin_message, " +
            "users.user_id, users.user_password, users.user_firstname, users.user_lastname, users.user_email, " +
            "admins.user_id, admins.user_password, admins.user_firstname, admins.user_lastname, admins.user_email, " +
            "infosession_id, infosession_type, infosession_timestamp, infosession_max_enrollees, infosession_type_alternative, infosession_comments ";

    private static final String APPROVAL_QUERY = "SELECT " + APPROVAL_FIELDS + " FROM approvals " +
            "LEFT JOIN users users ON approval_user = users.user_id " +
            "LEFT JOIN users admins ON approval_admin = admins.user_id " +
            "LEFT JOIN infosessions ON approval_infosession = infosession_id ";

    private Connection connection;

    private PreparedStatement createApprovalStatement;
    private PreparedStatement getApprovalByIdStatement;
    private PreparedStatement getApprovalByUserStatement;
    private PreparedStatement getPendingUserApprovalStatement;
    private PreparedStatement getPendingApprovalStatement;
    private PreparedStatement updateApprovalStatement;
    private PreparedStatement getPagedApprovalsStatement;
    private PreparedStatement countApprovalsStatement;
    private PreparedStatement setApprovalAdminStatement;

    public JDBCApprovalDAO(Connection connection){
        this.connection = connection;
    }

    private PreparedStatement getSetApprovalAdminStatement() throws SQLException {
        if(setApprovalAdminStatement == null){
            setApprovalAdminStatement = connection.prepareStatement("UPDATE approvals SET approval_admin=? WHERE approval_id=?");
        }
        return setApprovalAdminStatement;
    }

    private PreparedStatement getCountApprovalsStatement() throws SQLException {
        if(countApprovalsStatement == null){
            countApprovalsStatement = connection.prepareStatement("SELECT COUNT(*) FROM approvals");
        }
        return countApprovalsStatement;
    }
    private PreparedStatement getGetPagedApprovalsStatement() throws SQLException {
        if(getPagedApprovalsStatement == null){
            getPagedApprovalsStatement = connection.prepareStatement(APPROVAL_QUERY + " ORDER BY approval_submission DESC LIMIT ? OFFSET ?");
        }
        return getPagedApprovalsStatement;
    }

    private PreparedStatement getGetPendingApprovalStatement() throws SQLException {
        if (getPendingApprovalStatement == null) {
            getPendingApprovalStatement = connection.prepareStatement(APPROVAL_QUERY + "WHERE approval_status = 'PENDING'");
        }
        return getPendingApprovalStatement;
    }

    private PreparedStatement getGetApprovalByUserStatement() throws SQLException {
        if(getApprovalByUserStatement == null){
            getApprovalByUserStatement = connection.prepareStatement(APPROVAL_QUERY + "WHERE approval_user = ?");
        }
        return getApprovalByUserStatement;
    }

    private PreparedStatement getGetPendingUserApprovalStatement() throws SQLException {
        if(getPendingUserApprovalStatement == null){
            getPendingUserApprovalStatement = connection.prepareStatement(APPROVAL_QUERY + "WHERE approval_user = ? AND approval_status = 'PENDING'");
        }
        return getPendingUserApprovalStatement;
    }

    private PreparedStatement getGetApprovalByIdStatement() throws SQLException {
        if(getApprovalByIdStatement == null){
            getApprovalByIdStatement = connection.prepareStatement(APPROVAL_QUERY + "WHERE approval_id = ?");
        }
        return getApprovalByIdStatement;
    }

    private PreparedStatement getCreateApprovalStatement() throws SQLException {
        if(createApprovalStatement == null){
            createApprovalStatement = connection.prepareStatement("INSERT INTO approvals(approval_user, approval_status, approval_infosession, approval_user_message, approval_submission) " +
                    "VALUES(?,?,?,?,?)", new String[]{"approval_id"});
        }
        return createApprovalStatement;
    }

    private PreparedStatement getUpdateApprovalStatement() throws SQLException {
        if(updateApprovalStatement == null){
            updateApprovalStatement = connection.prepareStatement("UPDATE Approvals SET approval_user=?, approval_admin=?, " +
                    "approval_date=?, approval_status=?, approval_infosession=?,approval_user_message=?,approval_admin_message=? WHERE approval_id = ?");
        }
        return updateApprovalStatement;
    }

    private Approval populateApproval(ResultSet rs) throws SQLException {
        return new Approval(rs.getInt("approval_id"), JDBCUserDAO.populateUser(rs, false, false, "users"), JDBCUserDAO.populateUser(rs, false, false, "admins"),
                new DateTime(rs.getTimestamp("approval_submission").getTime()),
                rs.getTimestamp("approval_date") != null ? new DateTime(rs.getTimestamp("approval_date").getTime()) : null,
                rs.getObject("infosession_type") == null ? null : JDBCInfoSessionDAO.populateInfoSession(rs, false, false), Approval.ApprovalStatus.valueOf(rs.getString("approval_status")),
                rs.getString("approval_user_message"), rs.getString("approval_admin_message"));
    }

    private List<Approval> getApprovalList(PreparedStatement ps){
        try(ResultSet rs = ps.executeQuery()) {
            List<Approval> approvals = new ArrayList<>();
            while(rs.next()){
                approvals.add(populateApproval(rs));
            }
            return approvals;
        } catch(SQLException ex){
            throw new DataAccessException("Failed to read approval resultset.", ex);
        }
    }

    @Override
    public List<Approval> getApprovals(User user) throws DataAccessException {
        try {
            PreparedStatement ps = getGetApprovalByUserStatement();
            ps.setInt(1, user.getId());
            return getApprovalList(ps);
        } catch(SQLException ex){
            throw new DataAccessException("Failed to get approvals for user.", ex);
        }
    }

    @Override
    public List<Approval> getPendingApprovals(User user) throws DataAccessException {
        try {
            PreparedStatement ps = getGetPendingUserApprovalStatement();
            ps.setInt(1, user.getId());

            return getApprovalList(ps);
        } catch(SQLException ex){
            throw new DataAccessException("Failed to get pending approvals for user.", ex);
        }
    }

    @Override
    public List<Approval> getPendingApprovals() throws DataAccessException {
        try {
            PreparedStatement ps = getGetPendingApprovalStatement();
            return getApprovalList(ps);
        } catch(SQLException ex){
            throw new DataAccessException("Failed to get approvals for user.", ex);
        }
    }

    @Override
    public List<Approval> getApprovals(int page, int pageSize) throws DataAccessException {
        try {
            PreparedStatement ps = getGetPagedApprovalsStatement();
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            return getApprovalList(ps);
        } catch(SQLException ex){
            throw new DataAccessException("Failed to get paged approvals for user.", ex);
        }
    }

    @Override
    public int getApprovalCount() throws DataAccessException {
        try {
            PreparedStatement ps = getCountApprovalsStatement();
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next())
                    return rs.getInt(1);
                else
                    throw new DataAccessException("Failed to get approval count. Empty resultset.");

            } catch(SQLException ex) {
                throw new DataAccessException("Failed to get approval count.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to prepare count query.", ex);
        }
    }

    @Override
    public void setApprovalAdmin(Approval approval, User admin) throws DataAccessException {
        try {
            PreparedStatement ps = getSetApprovalAdminStatement();
            ps.setInt(1, admin.getId());
            ps.setInt(2, approval.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Failed to update approval admin, no rows affected.");
        } catch(SQLException ex){
            throw new DataAccessException("Failed to prepare change approval admin query.", ex);
        }
    }

    @Override
    public Approval getApproval(int approvalId) throws DataAccessException {
        try {
            PreparedStatement ps = getGetApprovalByIdStatement();
            ps.setInt(1, approvalId);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateApproval(rs);
                else return null;
            } catch(SQLException ex){
                throw new DataAccessException("Failed to read approval resultset.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to get approvals for user.", ex);
        }
    }

    /**
     * Creates a new PENDING approval with submission time as current time
     * @param user
     * @param session
     * @return
     */
    @Override
    public Approval createApproval(User user, InfoSession session, String userMessage) {
        try {
            DateTime date = new DateTime();

            PreparedStatement ps = getCreateApprovalStatement();
            ps.setInt(1, user.getId());
            ps.setString(2, Approval.ApprovalStatus.PENDING.name());

            if(session == null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, session.getId());

            ps.setString(4, userMessage);

            ps.setDate(5, new java.sql.Date(date.getMillis()));

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating approval request.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new Approval(keys.getInt(1), user, null, date, null, session, Approval.ApprovalStatus.PENDING, userMessage, null);
            } catch(SQLException ex){
                throw new DataAccessException("Failed to create approval.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to create approval request", ex);
        }
    }

    /**
     * Updates user, admin, reviewed time, infosession and status
     * Does NOT update submission time
     * @param approval
     * @throws DataAccessException
     */
    @Override
    public void updateApproval(Approval approval) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateApprovalStatement();
            ps.setInt(1, approval.getUser().getId());
            if(approval.getAdmin() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, approval.getAdmin().getId());
            if(approval.getReviewed() == null) ps.setNull(3, Types.TIMESTAMP);
            else ps.setTimestamp(3, new Timestamp(approval.getReviewed().getMillis()));
            ps.setString(4, approval.getStatus().toString());
            if(approval.getSession() == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, approval.getSession().getId());
            ps.setString(6, approval.getUserMessage());
            ps.setString(7, approval.getAdminMessage());

            ps.setInt(8, approval.getId());

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Approval update affected 0 rows.");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update approval", ex);
        }
    }
}
