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

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.ApprovalDAO;
import be.ugent.degage.db.models.Approval;
import be.ugent.degage.db.models.ApprovalListInfo;
import be.ugent.degage.db.models.MembershipStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * JDBC implementation of {@link ApprovalDAO}
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

    private static final String APPROVAL_FIELDS_SHORT = "approval_id, approval_user, approval_admin, approval_status, " +
            "approval_infosession, approval_user_message, approval_admin_message, " +
            "infosession_id, infosession_type, infosession_timestamp, infosession_max_enrollees, infosession_comments ";

    private static final String APPROVAL_QUERY_SHORT = "SELECT " + APPROVAL_FIELDS_SHORT + " FROM approvals " +
            "LEFT JOIN infosessions AS ses ON approval_infosession = infosession_id ";

    public JDBCApprovalDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private LazyStatement getCreateApprovalStatement = new LazyStatement(
            "INSERT INTO approvals(approval_user, approval_status, approval_infosession, approval_user_message) " +
                    "VALUES(?,?,?,?)",
            "approval_id"
    );

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

    @Override
    public Iterable<ApprovalListInfo> getApprovals(FilterField orderBy, boolean asc, int page, int pageSize) throws DataAccessException {
        String sql =
                "SELECT approval_id, approval_submission, approval_status, approval_admin IS NOT NULL as has_admin, " +
                        "approval_user, user_lastname, user_firstname, user_deposit, user_fee, " +
                        "user_status = 'FULL' as full_user, user_contract IS NOT NULL as contract_signed " +
                        "FROM approvals JOIN users ON approval_user = user_id " +
                        "ORDER BY ";
        String ascString = asc ? "asc" : "desc";
        switch (orderBy) {
            case USER_NAME:
                sql += "user_lastname " + ascString + ", user_firstname " + ascString;
                break;
            default: // should be 'instant'
                sql += "approval_submission " + ascString;
                break;
        }
        sql += " LIMIT ? OFFSET ?";
        try (PreparedStatement ps = prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            return toList(ps, rs -> new ApprovalListInfo(
                            rs.getInt("approval_id"),
                            rs.getString("user_lastname") + ", " + rs.getString("user_firstname"),
                            rs.getInt("approval_user"),
                            MembershipStatus.valueOf(rs.getString("approval_status")),
                            rs.getBoolean("has_admin"),
                            rs.getBoolean("full_user"),
                            rs.getBoolean("contract_signed"),
                            rs.getTimestamp("approval_submission").toInstant(),
                            (Integer) rs.getObject("user_deposit"),
                            (Integer) rs.getObject("user_fee")
                    )
            );
        } catch (
                SQLException ex
                )

        {
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
        try (PreparedStatement ps = prepareStatement(
                APPROVAL_QUERY_SHORT + "WHERE approval_id = ?"
        )) {
            ps.setInt(1, approvalId);
            return toSingleObject(ps, rs ->
                    new Approval(
                            approvalId,
                            rs.getInt("approval_user"),
                            rs.getInt("approval_admin"), // 0 means null
                            rs.getInt("approval_infosession") == 0 ? null : JDBCInfoSessionDAO.populateInfoSessionPartial(rs),
                            MembershipStatus.valueOf(rs.getString("approval_status")),
                            rs.getString("approval_user_message"),
                            rs.getString("approval_admin_message")
                    ));
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get approval info for user.", ex);
        }
    }

    /**
     * Creates a new PENDING approval with submission time as current time. Also
     * changes the user status to pending.
     */
    @Override
    public void createApproval(int userId, int sessionId, String userMessage) {
        // TODO: use trigger or stored procedure to combine both statements
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO approvals(approval_user, approval_status, approval_infosession, approval_user_message) " +
                        "VALUES(?,'PENDING',?,?)"

        )) {
            ps.setInt(1, userId);
            ps.setInt(2, sessionId);
            ps.setString(3, userMessage);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create approval request", ex);
        }

        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_status = 'FULL_VALIDATING' WHERE user_id = ?"
        )) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not change user status", ex);
        }

    }

    /**
     * Updates user, admin, infosession and status
     * Sets review time to now, does NOT update submission time
     */
    @Override
    public void updateApproval(Approval approval) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE approvals SET approval_user=?, approval_admin=?, " +
                        "approval_date=NOW(), approval_status=?, approval_infosession=?," +
                        "approval_user_message=?,approval_admin_message=? WHERE approval_id = ?"
        )) {
            ps.setInt(1, approval.getUserId());
            if (approval.getAdminId() == 0) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, approval.getAdminId());
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
