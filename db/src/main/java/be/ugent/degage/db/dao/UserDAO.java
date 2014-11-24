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
    public UserHeader getUserByEmail(String email) throws DataAccessException;

    /**
     * Return the user with the given email address and (plain text) password.
     */
    public UserHeader getUserWithPassword (String email, String password) throws DataAccessException;

    /**
     * Change the password of the given user.
     * @return true if and only the old password was correct
     */
    public boolean changePassword (int userId, String oldPassword, String newPassword) throws DataAccessException;

    /**
     * Give a new password to the user. (User {@link #changePassword} by preference.)
     */
    public void updatePassword (int userId, String newPassword) throws DataAccessException;

    /**
     * Return the user with the given id
     */
    public User getUser(int userId) throws DataAccessException;

    /**
     * Return partial information on the user with the given id,
     */
    public UserHeader getUserHeader(int userId) throws DataAccessException;

    /**
     * Update information about the given user
     */
    public void updateUser(User user) throws DataAccessException;

    /**
     * Update partial information about the given user
     */
    //public void updateUserPartial(User user) throws DataAccessException;

    /**
     * Update the user status
     */
    public void updateUserStatus(int userId, UserStatus status);


    /**
     * Delete the user with the given id
     */
	public void deleteUser(int userId) throws DataAccessException;

    /**
     * Create a new user.
     * @param password plain text password
     */
    public UserHeader createUser(String email, String password, String firstName, String lastName,
                                 UserStatus status, String phone, String cellPhone) throws DataAccessException;

    // TODO: below
    public int getAmountOfUsers(Filter filter) throws DataAccessException;
    public List<User> getUserList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    public String getVerificationString(int userId, VerificationType type) throws DataAccessException;
    public String createVerificationString(int userId, VerificationType type) throws DataAccessException;
    public void deleteVerificationString(int userID, VerificationType type) throws DataAccessException;

}
