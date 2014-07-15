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
     * Return the set of roles for a given user. (Every registered user gets role 'USER' by default.)
     */
	public Set<UserRole> getUserRoles(int userId) throws DataAccessException;

    /**
     * All users that have a certain role, including all super users. (Only partial information filled in.)
     */
    public Iterable<User> getUsersByRole(UserRole userRole) throws DataAccessException;

    /**
     * Add a role to a user (unless that user already has that role)
     */
	public void addUserRole(int userId, UserRole role) throws DataAccessException;

    /**
     * Remove a role from a user (unless the user did not have that role)
     */
	public void removeUserRole(int userId, UserRole role) throws DataAccessException;
}
