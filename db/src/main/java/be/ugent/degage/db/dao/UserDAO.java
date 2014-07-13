package be.ugent.degage.db.dao;

import java.util.List;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

/**
 * Data access objects for users
 */
public interface UserDAO {

    /**
     * Return the user with the given email address
     */
    public User getUser(String email) throws DataAccessException;

    /**
     * Return the user with the given id
     */
    public User getUser(int userId) throws DataAccessException;

    /**
     * Return partial information on the user with the given id,
     */
    public User getUserPartial(int userId) throws DataAccessException;

    /**
     * Update information about the given user
     */
    public void updateUser(User user) throws DataAccessException;

    /**
     * Update partial information about the given user
     */
    public void updateUserPartial(User user) throws DataAccessException;


    /**
     * Delete the user with the given id
     */
	public void deleteUser(int userId) throws DataAccessException;

    /**
     * Create a new user
     */
    public User createUser(String email, String password, String firstName, String lastName) throws DataAccessException;

    // TODO: below
    public int getAmountOfUsers(Filter filter) throws DataAccessException;
    public List<User> getUserList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    public String getVerificationString(User user, VerificationType type) throws DataAccessException;
    public String createVerificationString(User user, VerificationType type) throws DataAccessException;
    public void deleteVerificationString(User user, VerificationType type) throws DataAccessException;

}
