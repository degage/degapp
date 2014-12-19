/* JDBCUserDAO.java
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

package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of @link{UserDAO}
 */
class JDBCUserDAO extends AbstractDAO implements UserDAO {

    static final String USER_HEADER_FIELDS =
            "user_id, user_firstname, user_lastname, user_email, user_status, user_phone, user_cellphone ";

    private static final String USER_FIELDS = USER_HEADER_FIELDS + ", users.user_cellphone, users.user_phone, users.user_gender, " +
            "domicileAddresses.address_id, domicileAddresses.address_country, domicileAddresses.address_city, domicileAddresses.address_zipcode, domicileAddresses.address_street, domicileAddresses.address_number, " +
            "residenceAddresses.address_id, residenceAddresses.address_country, residenceAddresses.address_city, residenceAddresses.address_zipcode, residenceAddresses.address_street, residenceAddresses.address_number,  " +
            "users.user_driver_license_id, users.user_identity_card_id, users.user_identity_card_registration_nr,  " +
            "users.user_damage_history, users.user_payed_deposit, users.user_agree_terms, users.user_image_id";

    private static final String USER_QUERY = "SELECT " + USER_FIELDS + " FROM users " +
            "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
            "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id";

    // TODO: more fields to filter on
    public static final String FILTER_FRAGMENT = " WHERE users.user_firstname LIKE ? AND users.user_lastname LIKE ? " +
            "AND (CONCAT_WS(' ', users.user_firstname, users.user_lastname) LIKE ? OR CONCAT_WS(' ', users.user_lastname, users.user_firstname) LIKE ?)";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if (filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }

        ps.setString(start, filter.getValue(FilterField.USER_FIRSTNAME));
        ps.setString(start + 1, filter.getValue(FilterField.USER_LASTNAME));
        ps.setString(start + 2, filter.getValue(FilterField.USER_NAME));
        ps.setString(start + 3, filter.getValue(FilterField.USER_NAME));
    }

    public JDBCUserDAO(JDBCDataAccessContext context) {
        super(context);
    }

    // TODO: move to separate verification dao?

    private LazyStatement deleteVerificationStatement = new LazyStatement(
            "DELETE FROM verifications WHERE verification_user_id = ? AND verification_type = ?"
    );

    @Override
    public void deleteVerificationString(int userId, VerificationType type) throws DataAccessException {
        try {
            PreparedStatement ps = deleteVerificationStatement.value();
            ps.setInt(1, userId);
            ps.setString(2, type.name());
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Verification delete operation affected 0 rows.");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete verification.", ex);
        }
    }

    private LazyStatement createVerificationStatement = new LazyStatement(
            "INSERT INTO verifications(verification_ident, verification_user_id, verification_type) VALUES(UUID(),?, ?)"
    );

    @Override
    public String createVerificationString(int userId, VerificationType type) throws DataAccessException {
        try {
            PreparedStatement ps = createVerificationStatement.value();
            ps.setInt(1, userId);
            ps.setString(2, type.name());
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("Verification string creation failed. Zero rows affected");

            return getVerificationString(userId, type);

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create verification string.", ex);
        }
    }

    private LazyStatement getVerificationStatement = new LazyStatement(
            "SELECT verification_ident FROM verifications WHERE verification_user_id = ? AND verification_type = ?"
    );

    @Override
    public String getVerificationString(int userId, VerificationType type) throws DataAccessException {
        try {
            PreparedStatement ps = getVerificationStatement.value();
            ps.setInt(1, userId);
            ps.setString(2, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                else
                    return rs.getString("verification_ident");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get verification string.", ex);
        }
    }


    public static User populateUser(ResultSet rs) throws SQLException {
        return populateUser(rs, "users");
    }

    public static UserHeader populateUserHeader(ResultSet rs) throws SQLException {
        return new UserHeader(
                rs.getInt("user_id"),
                rs.getString("user_email"),
                rs.getString("user_firstname"),
                rs.getString("user_lastname"),
                UserStatus.valueOf(rs.getString("user_status")),
                rs.getString ("user_phone"),
                rs.getString ( "user_cellphone")
        );
    }

    public static UserHeader populateUserHeader(ResultSet rs, String tableName) throws SQLException {
        return new UserHeader(
                rs.getInt(tableName+".user_id"),
                rs.getString(tableName + ".user_email"),
                rs.getString(tableName+".user_firstname"),
                rs.getString(tableName+".user_lastname"),
                UserStatus.valueOf(rs.getString(tableName + ".user_status")),
                rs.getString(tableName + ".user_phone"),
                rs.getString(tableName+".user_cellphone")
        );
    }

    public static User populateUser(ResultSet rs, String tableName) throws SQLException {
        User user = new User(
                rs.getInt("users.user_id"),
                rs.getString("users.user_email"),
                rs.getString("users.user_firstname"),
                rs.getString("users.user_lastname"),
                UserStatus.valueOf(rs.getString("users.user_status")
                )
        );

        user.setAddressDomicile(JDBCAddressDAO.populateAddress(rs, "domicileAddresses"));
        user.setAddressResidence(JDBCAddressDAO.populateAddress(rs, "residenceAddresses"));
        user.setCellphone(rs.getString(tableName + ".user_cellphone"));
        user.setPhone(rs.getString(tableName + ".user_phone"));
        user.setGender(UserGender.valueOf(rs.getString(tableName + ".user_gender")));
        user.setDamageHistory(rs.getString(tableName + ".user_damage_history"));
        user.setPayedDeposit(rs.getBoolean(tableName + ".user_payed_deposit"));
        user.setAgreeTerms(rs.getBoolean(tableName + ".user_agree_terms"));

        if (rs.getObject(tableName + ".user_image_id") != null) {
            user.setProfilePictureId(rs.getInt(tableName + ".user_image_id"));
        }

        user.setLicense(rs.getString(tableName + ".user_driver_license_id"));

        IdentityCard identityCard = new IdentityCard();
        boolean identityCardNotNull = false;
        String identityCardId = rs.getString(tableName + ".user_identity_card_id");
        if (!rs.wasNull()) {
            identityCardNotNull = true;
            identityCard.setId(identityCardId);
        }
        String identityCardRegistrationNr = rs.getString(tableName + ".user_identity_card_registration_nr");
        if (!rs.wasNull()) {
            identityCardNotNull = true;
            identityCard.setRegistrationNr(identityCardRegistrationNr);
        }
        if (identityCardNotNull)
            user.setIdentityCard(identityCard);
        else
            user.setIdentityCard(null);

        return user;
    }


    private LazyStatement getUserByEmailStatement = new LazyStatement(
            "SELECT " + USER_HEADER_FIELDS + " FROM users WHERE user_email = ?"
    );

    @Override
    public UserHeader getUserByEmail(String email) {
        if (email == null || email.isEmpty())
            return null;

        try {
            PreparedStatement ps = getUserByEmailStatement.value();
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return populateUserHeader(rs);
                else
                    return null;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by email.", ex);
        }
    }

    private LazyStatement getUserByPasswordStatement = new LazyStatement(
            "SELECT " + USER_HEADER_FIELDS + ", user_password FROM users WHERE user_email = ?"
    );


    @Override
    public UserHeader getUserWithPassword(String email, String password) throws DataAccessException {
        try {
            PreparedStatement ps = getUserByPasswordStatement.value();
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserHeader result = populateUserHeader(rs);
                    if (BCrypt.checkpw(password, rs.getString("user_password"))) {
                        return result;
                    } else {
                        return null;
                    }
                } else
                    return null;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by email.", ex);
        }
    }


    private LazyStatement getPasswordByIdStatement = new LazyStatement(
            "SELECT user_password FROM users WHERE user_id = ?"
    );

    private LazyStatement updatePasswordStatement = new LazyStatement(
            "UPDATE users SET user_password = ? WHERE user_id = ?"
    );

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws DataAccessException {
        try {
            PreparedStatement ps = getPasswordByIdStatement.value();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !BCrypt.checkpw(oldPassword, rs.getString("user_password"))) {
                    return false;
                }
            }

            ps = updatePasswordStatement.value();
            ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
            ps.setInt (2, userId);
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Could not change password");
            }
            return true;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not change password", ex);
        }
    }

    @Override public void updatePassword (int userId, String newPassword) throws DataAccessException {
        try {
            PreparedStatement ps = updatePasswordStatement.value();
            ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
            ps.setInt (2, userId);
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Could not update password");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not update password", ex);
        }
    }

    private LazyStatement getUserByIdStatement = new LazyStatement(
            USER_QUERY + " WHERE users.user_id = ?"
    );

    @Override
    public User getUser(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getUserByIdStatement.value();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return populateUser(rs);
                } else
                    return null;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by id.", ex);
        }

    }

    private LazyStatement smallGetUserByIdStatement = new LazyStatement(
            "SELECT " + USER_HEADER_FIELDS + " FROM users WHERE user_id = ?"
    );

    @Override
    public UserHeader getUserHeader(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = smallGetUserByIdStatement.value();
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return populateUserHeader(rs);
                } else
                    return null;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by id.", ex);
        }

    }


    private LazyStatement createUserStatement = new LazyStatement(
            "INSERT INTO users(user_email, user_password, user_firstname, user_lastname, user_status) VALUES (?,?,?,?,?)",
            "user_id"
    );

    @Override
    public UserHeader createUser(String email, String password, String firstName, String lastName,
                                 UserStatus status, String phone, String cellPhone) throws DataAccessException {
        try {
            PreparedStatement ps = createUserStatement.value();
            ps.setString(1, email);
            ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt(12)));
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, status.name());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new UserHeader(keys.getInt(1), email, firstName, lastName, status, phone, cellPhone);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to commit new user transaction.", ex);
        }
    }



    private LazyStatement updateUserStatusStatement = new LazyStatement(
            "UPDATE users SET user_status=? WHERE user_id = ?"
    );

    public void updateUserStatus(int userId, UserStatus status) throws DataAccessException {
        try {
            PreparedStatement ps = updateUserStatusStatement.value();
            ps.setString(1, status.name());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user status", ex);
        }
    }
    private LazyStatement updateUserStatement = new LazyStatement(
            "UPDATE users SET user_email=?, user_firstname=?, user_lastname=?, user_status=?, " +
                    "user_gender=?, user_phone=?, user_cellphone=?, user_damage_history=?, user_payed_deposit=?, " +
                    "user_agree_terms=?, user_image_id = ?, user_driver_license_id=?,  " +
                    "user_identity_card_id=?, user_identity_card_registration_nr=? " +
                    "WHERE user_id = ?"
    );

    @Override
    public void updateUser(User user) throws DataAccessException {
        try {
            PreparedStatement ps = updateUserStatement.value();
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());

            ps.setString(4, user.getStatus().name());
            ps.setString(5, user.getGender().name());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getCellphone());

            ps.setString(8, user.getDamageHistory());

            ps.setBoolean(9, user.isPayedDeposit());
            ps.setBoolean(10, user.isAgreeTerms());

            if (user.getProfilePictureId() != -1) { // TODO: use NULL here
                ps.setInt(11, user.getProfilePictureId());
            }
            else {
                ps.setNull(11, Types.INTEGER);
            }

            ps.setString(12, user.getLicense());

            if (user.getIdentityCard() == null) {
                ps.setString(13, null);
                ps.setString(14, null);
            } else {
                ps.setString(13, user.getIdentityCard().getId());
                ps.setString(14, user.getIdentityCard().getRegistrationNr());
            }

            ps.setInt(17, user.getId());

            if (ps.executeUpdate() == 0)
                throw new DataAccessException("User update affected 0 rows.");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user", ex);
        }
    }

//    private LazyStatement smallUpdateUserStatement = new LazyStatement(
//            "UPDATE users SET user_email=?, user_firstname=?, user_lastname=? WHERE user_id = ?"
//    );
//
//    @Override
//    public void updateUserPartial(User user) throws DataAccessException {
//        try {
//            PreparedStatement ps = smallUpdateUserStatement.value();
//
//            ps.setString(1, user.getEmail());
//            ps.setString(2, user.getFirstName());
//            ps.setString(3, user.getLastName());
//
//            ps.setInt(4, user.getId());
//            if (ps.executeUpdate() == 0)
//                throw new DataAccessException("User update affected 0 rows.");
//
//        } catch (SQLException ex) {
//            throw new DataAccessException("Failed to update user", ex);
//        }
//    }

    private LazyStatement deleteUserStatement = new LazyStatement(
            "UPDATE users SET user_status = 'DROPPED' WHERE user_id = ?"
    );

    @Override
    public void deleteUser(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = deleteUserStatement.value();
            ps.setInt(1, userId);
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting (=updating to DROPPED) user.");
        } catch (SQLException ex) {
            throw new DataAccessException("Could not delete user", ex);
        }

    }

    // TODO: refactor filters

    private LazyStatement getUserListPageByNameAscStatement = new LazyStatement(
            USER_QUERY + FILTER_FRAGMENT + "ORDER BY users.user_firstname asc, users.user_lastname asc LIMIT ?, ?"
    );

    private LazyStatement getUserListPageByNameDescStatement = new LazyStatement(
            USER_QUERY + FILTER_FRAGMENT + "ORDER BY users.user_firstname desc, users.user_lastname desc LIMIT ?, ?"
    );

    private LazyStatement getAmountOfUsersStatement = new LazyStatement(
            "SELECT COUNT(user_id) AS amount_of_users FROM users" + FILTER_FRAGMENT
    );


    /**
     * @param filter The filter to apply to
     * @return The amount of filtered cars
     * @throws DataAccessException
     */
    @Override
    public int getAmountOfUsers(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfUsersStatement.value();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("amount_of_users");
                else
                    return 0;
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of users", ex);
        }
    }

    @Override
    public List<User> getUserList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = null;
            switch (orderBy) {
                case USER_NAME:
                    ps = asc ? getUserListPageByNameAscStatement.value() : getUserListPageByNameDescStatement.value();
                    break;
            }
            if (ps == null) {
                throw new DataAccessException("Could not create getUserList statement");
            }

            fillFragment(ps, filter, 1);
            int first = (page - 1) * pageSize;
            ps.setInt(5, first);
            ps.setInt(6, pageSize);
            List<User> users = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(populateUser(rs));
                }
                return users;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of users", ex);
        }
    }

}
