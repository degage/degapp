package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.User;

/**
 * Handles privileged status for cars
 */
public interface PrivilegedDAO {

    public Iterable<User> getPrivileged(int carId) throws DataAccessException;

    public void addPrivileged(int carId, Iterable<User> users) throws DataAccessException;

    public void deletePrivileged(int carId, Iterable<User> users) throws DataAccessException;

}
