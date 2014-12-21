/* UserDAO.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

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
     * Register a new user. Returns null if user with the given email address already exists. The status
     * of the newly created user is EMAIL_VALIDATING
     */
    public UserHeader registerUser(String email, String password, String firstName, String lastName) throws DataAccessException;

    // TODO: below
    public int getAmountOfUsers(Filter filter) throws DataAccessException;
    public List<User> getUserList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    public String getVerificationString(int userId, VerificationType type) throws DataAccessException;
    public String createVerificationString(int userId, VerificationType type) throws DataAccessException;
    public void deleteVerificationString(int userID, VerificationType type) throws DataAccessException;

}
