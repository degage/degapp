/* JDBCApprovalDAO.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.dao.ApprovalDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Cedric on 3/30/2014.
 */
class JDBCApprovalDAO extends AbstractDAO implements ApprovalDAO {

    private static final String USERS_USER_HEADER_FIELDS =
            "users.user_id, users.user_firstname, users.user_lastname, users.user_email, " +
            "       users.user_status, users.user_phone, users.user_cellphone, users.user_degage_id ";

    private static final String ADMINS_USER_HEADER_FIELDS =
            "admins.user_id, admins.user_firstname, admins.user_lastname, admins.user_email, " +
            "       admins.user_status, admins.user_phone, admins.user_cellphone, admins.user_degage_id ";


    private static final String APPROVAL_FIELDS = "approval_id, approval_user, approval_admin, approval_submission, " +
            "approval_date, approval_status, approval_infosession, approval_user_message, approval_admin_message, " +
            USERS_USER_HEADER_FIELDS + "," + ADMINS_USER_HEADER_FIELDS + "," +
            "infosession_id, infosession_type, infosession_timestamp, infosession_max_enrollees, infosession_comments ";

    private static final String APPROVAL_QUERY = "SELECT " + APPROVAL_FIELDS + " FROM approvals " +
            "LEFT JOIN users users ON approval_user = users.user_id " +
            "LEFT JOIN users admins ON approval_admin = admins.user_id " +
            "LEFT JOIN infosessions AS ses ON approval_infosession = infosession_id ";

    public JDBCApprovalDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private LazyStatement getApprovalByIdStatement = new LazyStatement(
            APPROVAL_QUERY + "WHERE approval_id = ?"
    );

    private LazyStatement getCreateApprovalStatement = new LazyStatement(
            "INSERT INTO approvals(approval_user, approval_status, approval_infosession, approval_user_message) " +
                    "VALUES(?,?,?,?)",
            "approval_id"
    );

    private LazyStatement getUpdateApprovalStatement = new LazyStatement(
            "UPDATE approvals SET approval_user=?, approval_admin=?, " +
                    "approval_date=NOW(), approval_status=?, approval_infosession=?," +
                    "approval_user_message=?,approval_admin_message=? WHERE approval_id = ?"
    );

    private Approval populateApprovalPartial(ResultSet rs) throws SQLException {
        // note that admin can be null

        UserHeader admin =
                rs.getString ("admins.user_status") == null ?
                        null :
                        JDBCUserDAO.populateUserHeader(rs, "admins");
        return new Approval(
                rs.getInt("approval_id"),
                JDBCUserDAO.populateUserHeader(rs, "users"),
                admin,
                rs.getTimestamp("approval_submission").toInstant(),
                null,
                null,
                Approval.ApprovalStatus.valueOf(rs.getString("approval_status")),
                null,
                null
        );
    }


    private LazyStatement hasApprovalPendingStatement = new LazyStatement(
            "SELECT 1 FROM approvals WHERE approval_user = ? AND approval_status = 'PENDING'"
    );

    @Override
    public boolean hasApprovalPending(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = hasApprovalPendingStatement.value();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to check pending approvals for user.", ex);
        }
    }

    private LazyStatement getPagedApprovalsStatement = new LazyStatement(
            "SELECT approval_id, approval_submission, approval_status, " +
                    USERS_USER_HEADER_FIELDS + ","  + ADMINS_USER_HEADER_FIELDS +
            "FROM approvals " +
            "LEFT JOIN users users ON approval_user = users.user_id " +
            "LEFT JOIN users admins ON approval_admin = admins.user_id " +
            "ORDER BY approval_submission DESC LIMIT ? OFFSET ?"
    );

    @Override
    public Iterable<Approval> getApprovals(int page, int pageSize) throws DataAccessException {
        try {
            PreparedStatement ps = getPagedApprovalsStatement.value();
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                Collection<Approval> approvals = new ArrayList<>();
                while (rs.next()) {
                    approvals.add(populateApprovalPartial(rs));
                }
                return approvals;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get paged approvals for user.", ex);
        }
    }

    private LazyStatement countApprovalsStatement = new LazyStatement("SELECT COUNT(*) FROM approvals");

    @Override
    public int getApprovalCount() throws DataAccessException {
        try {
            PreparedStatement ps = countApprovalsStatement.value();
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new DataAccessException("Failed to get approval count. Empty resultset.");
                }

            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get approval count.", ex);
        }
    }

    private LazyStatement setApprovalAdminStatement = new LazyStatement(
            "UPDATE approvals SET approval_admin=? WHERE approval_id=?"
    );

    @Override
    public void setApprovalAdmin(int approvalId, int adminId) throws DataAccessException {
        try {
            PreparedStatement ps = setApprovalAdminStatement.value();
            ps.setInt(1, adminId);
            ps.setInt(2, approvalId);
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Failed to update approval admin, no rows affected.");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to prepare change approval admin query.", ex);
        }
    }

    @Override
    public Approval getApproval(int approvalId) throws DataAccessException {
        try {
            PreparedStatement ps = getApprovalByIdStatement.value();
            ps.setInt(1, approvalId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp approvalDate = rs.getTimestamp("approval_date");
                    return new Approval(
                            rs.getInt("approval_id"),
                            JDBCUserDAO.populateUserHeader(rs, "users"),
                            JDBCUserDAO.populateUserHeader(rs, "admins"),
                            rs.getTimestamp("approval_submission").toInstant(),
                            approvalDate == null ? null : approvalDate.toInstant(),
                            rs.getObject("infosession_type") == null ? null : JDBCInfoSessionDAO.populateInfoSessionPartial(rs),
                            Approval.ApprovalStatus.valueOf(rs.getString("approval_status")),
                            rs.getString("approval_user_message"),
                            rs.getString("approval_admin_message")
                    );
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get approvals for user.", ex);
        }
    }

    /**
     * Creates a new PENDING approval with submission time as current time
     */
    @Override
    public void createApproval(int userId, int sessionId, String userMessage) {
        // TODO: change user into userId, session into sessionId
        try {
            PreparedStatement ps = getCreateApprovalStatement.value();

            ps.setInt(1, userId);
            ps.setString(2, Approval.ApprovalStatus.PENDING.name());
            ps.setInt(3, sessionId);
            ps.setString(4, userMessage);

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("No rows were affected when creating approval request.");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create approval request", ex);
        }
    }

    /**
     * Updates user, admin, infosession and status
     * Sets review time to now, does NOT update submission time
     */
    @Override
    public void updateApproval(Approval approval) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateApprovalStatement.value();
            ps.setInt(1, approval.getUser().getId());
            if (approval.getAdmin() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, approval.getAdmin().getId());
            }
            ps.setString(3, approval.getStatus().name());
            if (approval.getSession() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, approval.getSession().getId());
            }
            ps.setString(5, approval.getUserMessage());
            ps.setString(6, approval.getAdminMessage());

            ps.setInt(7, approval.getId());

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Approval update affected 0 rows.");
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update approval", ex);
        }
    }
}
