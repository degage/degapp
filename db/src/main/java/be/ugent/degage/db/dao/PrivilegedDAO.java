package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.UserHeader;

/**
 * Handles privileged status for cars
 */
public interface PrivilegedDAO {

    /**
     * List all users with privileges for a certain car
     */
    public Iterable<UserHeader> getPrivileged(int carId) throws DataAccessException;

    /**
     * Add new privileged users for the given car
     */
    public void addPrivileged(int carId, Iterable<Integer> userIds) throws DataAccessException;

    /**
     * Remove a number of privileges for the given car
     */
    public void deletePrivileged(int carId, Iterable<Integer> userIds) throws DataAccessException;

    /**
     * Is the given user privileged to use the given car (or owner of that car)
     */
    public boolean isOwnerOrPrivileged (int carId, int userId);

}
