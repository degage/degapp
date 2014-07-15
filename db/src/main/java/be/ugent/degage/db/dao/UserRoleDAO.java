package be.ugent.degage.db.dao;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;

/**
 * Data access object for user roles.
 */
public interface UserRoleDAO {
    /**
     * Return the set of roels for a given user.
     */
	public Set<UserRole> getUserRoles(int userId) throws DataAccessException;

    /**
     * All users that have a certain role.
     */
    public List<User> getUsersByRole(UserRole userRole) throws DataAccessException;
	public void addUserRole(int userId, UserRole role) throws DataAccessException;
	public void removeUserRole(int userId, UserRole role) throws DataAccessException;
}
