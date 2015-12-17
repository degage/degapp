package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.MembershipDAO;
import be.ugent.degage.db.models.Membership;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

/**
 * JDBC implmentation fo @link{MembershipDAO}
 */
public class JDBCMembershipDAO extends AbstractDAO implements MembershipDAO {

    public JDBCMembershipDAO(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public Membership getMembership(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT user_lastname, user_firstname, user_deposit, user_fee, user_contract " +
                        "FROM users WHERE user_id = ?"
        )) {
            ps.setInt(1, userId);
            return toSingleObject(ps, rs -> {
                Date contractDate = rs.getDate("user_contract");
                return new Membership(userId,
                        rs.getString("user_lastname") + ", " + rs.getString("user_firstname"),
                        (Integer) rs.getObject("user_deposit"),
                        (Integer) rs.getObject("user_fee"),
                        contractDate == null ? null : contractDate.toLocalDate());
            });
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

}
