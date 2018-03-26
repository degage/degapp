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

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Data access objects for users
 */
public interface UserDAO {

    /**
     * Return the user with the given email address
     */
    public UserHeader getUserByEmail(String email) throws DataAccessException;

    /**
     * Return the user with the given account number
     */
    public UserHeader getUserByAccountNumber(String accountNumber) throws DataAccessException;

    /**
     * Return the user with the given email address and (plain text) password.
     */
    public UserHeader getUserWithPassword(String email, String password) throws DataAccessException;

    /**
     * Change the password of the given user.
     *
     * @return true if and only the old password was correct
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws DataAccessException;

    /**
     * Return the user with the given id
     */
    public User getUser(int userId) throws DataAccessException;

    /**
     * Return partial information on the user with the given id,
     */
    public UserHeader getUserHeader(int userId) throws DataAccessException;

    /**
     * Return partial information on the user with the given id,
     */
    public User getUserHeaderBlocked(int userId) throws DataAccessException;

    /**
     * Update the main profile information about a user, i.e., names and telephone numbers
     */
    public void updateUserMainProfile(User user) throws DataAccessException;

    /**
     * Return the index of the user picture, or 0 if no picture was registered
     */
    public int getUserPicture(int userId);

    /**
     * Update the user picture.
     */
    public void updateUserPicture(int userId, int fileId);

    /**
     * Update the drivers license data for a user
     */
    public void updateUserLicenseData(int userId, String license, LocalDate date, LocalDate expirationDate);

    /**
     * Update the account number data for a user
     */
    public void updateUserAccountNumberData(int userId, String accountNumber);

    /**
     * Update identity data for a user
     */
    public void updateUserIdentityData(int userId, String identityId, String nationalId, LocalDate expirationDate);

    /**
     * Update the user status
     */
    public void updateUserStatus(int userId, UserStatus status);

    /**
     * Update the user status with a reason
     */
    public void updateUserStatusWithReason(int userId, UserStatus status, String reason);

    /**
     * Update the email address of a user.
     *
     * @return true if succeeded, false if mail address existed already
     */
    public boolean updateUserEmail(int userId, String email);

    /**
     * Make user a full member. Creates a new degage id if the user did not have one.
     */
    public void makeUserFull(int userId);


    /**
     * Delete the user with the given id
     */
    public void deleteUser(int userId) throws DataAccessException;

    // TODO: below
    public Page<User> getUserList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    /**
     * Is the given car owner allowed to see the profile of the given user?
     */
    public boolean canSeeProfileAsOwner(int ownerId, int userId);

    /**
     * Is the given user allowed to see the profile of the given owner?
     */
    public boolean canSeeProfileAsUser(int userId, int ownerId);


    /**
     * Return a user list for users with fist and/or last name containing the given string. Used in pickers.
     */
    public Iterable<UserHeaderShort> listUserByName(String str, List<String> status, int limit);

    /**
     * If sendReminder is true reminders for payments should be send (default true)
     */
    public void updateUserPaymentInfo(int userId, boolean sendReminder, String paymentInfo, UserCreditStatus status);


    public UserHeader getUserByInvoice(Invoice invoice);

    public Page<User> findUsers(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException;

}
