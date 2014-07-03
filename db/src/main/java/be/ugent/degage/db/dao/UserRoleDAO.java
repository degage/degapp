package be.ugent.degage.db.dao;

import java.util.EnumSet;
import java.util.List;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;

public interface UserRoleDAO {
	public EnumSet<UserRole> getUserRoles(int userId) throws DataAccessException;
    public List<User> getUsersByRole(UserRole userRole) throws DataAccessException;
	public void addUserRole(int userId, UserRole role) throws DataAccessException;
	public void removeUserRole(int userId, UserRole role) throws DataAccessException;
}
