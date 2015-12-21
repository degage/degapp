/* JDBCMembershipDAO.java
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
import be.ugent.degage.db.dao.MembershipDAO;
import be.ugent.degage.db.models.Membership;
import be.ugent.degage.db.models.MembershipStatus;
import be.ugent.degage.db.models.Page;

import java.sql.*;
import java.time.LocalDate;

/**
 * JDBC implmentation fo @link{MembershipDAO}
 */
public class JDBCMembershipDAO extends AbstractDAO implements MembershipDAO {

    public JDBCMembershipDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static Membership populateMembership(ResultSet rs) throws SQLException {
        Date contractDate = rs.getDate("user_contract");

        String approval_status = rs.getString("approval_status");
        return new Membership(
                rs.getInt("user_id"),
                rs.getInt("approval_id"),
                rs.getString("user_lastname") + ", " + rs.getString("user_firstname"),
                (Integer) rs.getObject("user_deposit"),
                (Integer) rs.getObject("user_fee"),
                contractDate == null ? null : contractDate.toLocalDate(),
                approval_status == null ? null : MembershipStatus.valueOf(approval_status)
        );
    }

    @Override
    public Membership getMembership(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT user_id, user_lastname, user_firstname, user_deposit, user_fee, user_contract, approval_id, approval_status " +
                        "FROM users LEFT JOIN approvals ON approval_user = user_id WHERE user_id = ?"
        )) {
            ps.setInt(1, userId);
            return toSingleObject(ps, JDBCMembershipDAO::populateMembership);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by id.", ex);
        }
    }

    @Override
    public void updateUserMembership(int userId, Integer deposit, Integer fee) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_deposit = ?, user_fee = ? WHERE user_id = ?"
        )) {
            ps.setObject(1, deposit, Types.INTEGER);
            ps.setObject(2, fee, Types.INTEGER);
            ps.setInt(3, userId);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user membership information", ex);
        }
    }

    @Override
    public void updateUserContract(int userId, LocalDate contract) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_contract = ? WHERE user_id = ?"
        )) {
            if (contract == null) {
                ps.setNull(1, Types.DATE);
            } else {
                ps.setDate(1, Date.valueOf(contract));
            }
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user contract information", ex);
        }
    }

    @Override
    public boolean isContractAdminOf(int adminId, int userId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT 1 FROM approvals WHERE approval_admin = ? AND approval_user = ?"
        )) {
            ps.setInt(1, adminId);
            ps.setInt(2, userId);
            return isNonEmpty(ps);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public Page<Membership> getContractees(int adminId, int type , int page, int pageSize) {
        StringBuilder builder = new StringBuilder (
                "SELECT SQL_CALC_FOUND_ROWS user_id, user_lastname, user_firstname, user_deposit, user_fee, user_contract, approval_id, approval_status " +
                "FROM approvals JOIN users ON approval_user = user_id " +
                "WHERE approval_admin = ? AND ");
        switch (type) {
            case 0:
                builder.append ("user_contract IS NULL");
                break;
            case 1:
                builder.append ("user_contract IS NOT NULL AND approval_status = 'PENDING'");
                break;
            default: // 2
                builder.append ("user_contract IS NOT NULL AND approval_status != 'PENDING'");
                break;
        }
        builder.append (" ORDER BY user_id DESC LIMIT ?,?");
        //System.err.println("SQL = " + builder.toString());
        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            ps.setInt(1, adminId);
            ps.setInt(2, (page - 1) * pageSize);
            ps.setInt(3, pageSize);
            return toPage(ps, pageSize, JDBCMembershipDAO::populateMembership);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}
