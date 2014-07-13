package be.ugent.degage.db.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.UserRoleDAO;

class JDBCUserRoleDAO implements UserRoleDAO{

	private Connection connection;
	private PreparedStatement insertUserRolesStatement;
    private PreparedStatement removeUserRolesStatement;
    private PreparedStatement getUserRolesStatement;
    private PreparedStatement getUsersByRoleStatement;
    
	public JDBCUserRoleDAO(Connection connection) {
		this.connection = connection;
	}
	
    private PreparedStatement getRemoveUserRolesStatement() throws SQLException{
    	if(removeUserRolesStatement == null){
    		removeUserRolesStatement = connection.prepareStatement("DELETE FROM userroles WHERE userrole_userid=? AND userrole_role=?");
    	}
    	return removeUserRolesStatement;
    }   

	private PreparedStatement getInsertUserRolesStatement() throws SQLException{
    	if(insertUserRolesStatement == null){
    		insertUserRolesStatement = connection.prepareStatement("INSERT INTO userroles(userrole_userid, userrole_role) VALUES (?,?)");
    	}
    	return insertUserRolesStatement;
    }
    
    private PreparedStatement getUserRolesStatement() throws SQLException {
    	if(getUserRolesStatement == null){
    		getUserRolesStatement = connection.prepareStatement("SELECT userrole_role FROM userroles WHERE userrole_userid = ?");
    	}
    	return getUserRolesStatement;
    }

    private PreparedStatement getGetUsersByRoleStatement() throws SQLException {
        if(getUsersByRoleStatement == null){
            getUsersByRoleStatement = connection.prepareStatement("SELECT * FROM userroles JOIN users ON userrole_userid = userrole_userid WHERE userrole_role = ? OR userrole_role = 'SUPER_USER'");
        }
        return getUsersByRoleStatement;
    }
    
	@Override
	public EnumSet<UserRole> getUserRoles(int userId) throws DataAccessException {
		try {
			PreparedStatement ps = getUserRolesStatement();
			ps.setInt(1, userId);
			EnumSet<UserRole> roleSet = EnumSet.of(UserRole.USER); // by default
			try (ResultSet rs = ps.executeQuery()){
				while(rs.next()){
						roleSet.add(UserRole.valueOf(rs.getString("userrole_role")));
                	}
                return roleSet;
			} catch (SQLException ex){
				throw new DataAccessException("Error reading resultset",ex);
			}			
		} catch (SQLException ex) {
			throw new DataAccessException("Could not get overview",ex);
		}
		
	}

    @Override
    public List<User> getUsersByRole(UserRole userRole) throws DataAccessException {
        try {
            PreparedStatement ps = getGetUsersByRoleStatement();
            ps.setString(1, userRole.name());
            List<User> userList = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    userList.add(JDBCUserDAO.populateUserPartial(rs, "users"));
                }
                return userList;
            } catch (SQLException ex){
                throw new DataAccessException("Error reading resultset",ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get overview",ex);
        }
    }

    @Override
	public void addUserRole(int userId, UserRole role) throws DataAccessException {
		try {
			PreparedStatement ps = getInsertUserRolesStatement();
			ps.setInt(1, userId);
			ps.setString(2, role.name());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when adding user role.");
		} 	catch (SQLException ex) {
			throw new DataAccessException("Could not add userrole",ex);
		}	
	}

	@Override
	public void removeUserRole(int userId, UserRole role) throws DataAccessException {
		try {
			PreparedStatement ps = getRemoveUserRolesStatement();
			ps.setInt(1, userId);
			ps.setString(2, role.name());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when removing user role.");
		} 	catch (SQLException ex) {
			throw new DataAccessException("Could not remove userrole",ex);
		}
	}

}
