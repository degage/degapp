package be.ugent.degage.db.dao;

import java.util.List;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

/**
 * Created by Cedric on 2/16/14.
 */
public interface UserDAO {
    public User getUser(String email) throws DataAccessException;
    public User getUser(int userId, boolean withRest) throws DataAccessException;
    public void updateUser(User user, boolean withRest) throws DataAccessException;
	public void deleteUser(User user) throws DataAccessException;
    public User createUser(String email, String password, String firstName, String lastName) throws DataAccessException;
    public String getVerificationString(User user, VerificationType type) throws DataAccessException;
    public String createVerificationString(User user, VerificationType type) throws DataAccessException;
    public void deleteVerificationString(User user, VerificationType type) throws DataAccessException;

    public int getAmountOfUsers(Filter filter) throws DataAccessException;
    public List<User> getUserList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
}
