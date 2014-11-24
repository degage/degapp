package be.ugent.degage.db.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import be.ugent.degage.db.models.UserHeader;
import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.UserRoleDAO;

class JDBCUserRoleDAO extends AbstractDAO implements UserRoleDAO {

    public JDBCUserRoleDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private LazyStatement getUserRolesStatement = new LazyStatement(
            "SELECT userrole_role FROM userroles WHERE userrole_userid = ?"
    );

    @Override
    public Set<UserRole> getUserRoles(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getUserRolesStatement.value();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                EnumSet<UserRole> roleSet = EnumSet.of(UserRole.USER); // by default
                while (rs.next()) {
                    roleSet.add(UserRole.valueOf(rs.getString("userrole_role")));
                }
                return roleSet;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get overview", ex);
        }

    }

    private LazyStatement getUsersByRoleStatement = new LazyStatement(
            "SELECT " + JDBCUserDAO.USER_HEADER_FIELDS + " FROM userroles " +
                    "JOIN users ON userrole_userid = user_id " +
                    "WHERE userrole_role = ? OR userrole_role = 'SUPER_USER'"
    );

    @Override
    public Iterable<UserHeader> getUsersByRole(UserRole userRole) throws DataAccessException {
        try {
            PreparedStatement ps = getUsersByRoleStatement.value();
            ps.setString(1, userRole.name());
            try (ResultSet rs = ps.executeQuery()) {
                Collection<UserHeader> userList = new ArrayList<>();
                while (rs.next()) {
                    userList.add(JDBCUserDAO.populateUserHeader(rs));
                }
                return userList;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get overview", ex);
        }
    }

    private LazyStatement insertUserRolesStatement = new LazyStatement(
            "INSERT IGNORE INTO userroles(userrole_userid, userrole_role) VALUES (?,?)"
    );

    @Override
    public void addUserRole(int userId, UserRole role) throws DataAccessException {
        try {
            PreparedStatement ps = insertUserRolesStatement.value();
            ps.setInt(1, userId);
            ps.setString(2, role.name());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not add userrole", ex);
        }
    }

    private LazyStatement removeUserRolesStatement = new LazyStatement(
            "DELETE FROM userroles WHERE userrole_userid=? AND userrole_role=?"
    );

    @Override
    public void removeUserRole(int userId, UserRole role) throws DataAccessException {
        try {
            PreparedStatement ps = removeUserRolesStatement.value();
            ps.setInt(1, userId);
            ps.setString(2, role.name());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not remove userrole", ex);
        }
    }

}
