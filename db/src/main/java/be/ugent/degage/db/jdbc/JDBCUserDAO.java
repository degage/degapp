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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;

/**
 * JDBC implementation of @link{UserDAO}
 */
class JDBCUserDAO extends AbstractDAO implements UserDAO {

    static final String USER_HEADER_SHORT_FIELDS =
            "user_id, user_firstname, user_lastname ";

    static final String USER_HEADER_FIELDS =
            USER_HEADER_SHORT_FIELDS + ", user_email, user_status, user_phone, user_cellphone, user_degage_id ";

    private static final String USER_QUERY =
            "SELECT SQL_CALC_FOUND_ROWS " + USER_HEADER_FIELDS + ",  " +
                    "domicileAddresses.address_id, domicileAddresses.address_country, domicileAddresses.address_city, " +
                    "domicileAddresses.address_zipcode, domicileAddresses.address_street, domicileAddresses.address_number, " +
                    "residenceAddresses.address_id, residenceAddresses.address_country, residenceAddresses.address_city, " +
                    "residenceAddresses.address_zipcode, residenceAddresses.address_street, residenceAddresses.address_number,  " +
                    "users.user_driver_license_id, users.user_identity_card_id, users.user_identity_card_registration_nr,  " +
                    "users.user_damage_history, users.user_agree_terms,  " +
                    "users.user_date_joined, users.user_driver_license_date, users.user_vat " +
                    "FROM users " +
                    "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
                    "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id ";

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

    public static User populateUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt("users.user_id"),
                rs.getString("users.user_email"),
                rs.getString("users.user_firstname"),
                rs.getString("users.user_lastname"),
                UserStatus.valueOf(rs.getString("users.user_status")),
                rs.getString("users.user_phone"),
                rs.getString("users.user_cellphone"),
                (Integer) rs.getObject("users.user_degage_id")
        );

        user.setAddressDomicile(JDBCAddressDAO.populateAddress(rs, "domicileAddresses"));
        user.setAddressResidence(JDBCAddressDAO.populateAddress(rs, "residenceAddresses"));
        user.setDamageHistory(rs.getString("users.user_damage_history"));
        user.setAgreeTerms(rs.getBoolean("users.user_agree_terms"));

        user.setLicense(rs.getString("users.user_driver_license_id"));

        user.setIdentityId(rs.getString("users.user_identity_card_id"));
        user.setNationalId(rs.getString("users.user_identity_card_registration_nr"));

        Date dateJoined = rs.getDate("users.user_date_joined");
        user.setDateJoined(dateJoined == null ? null : dateJoined.toLocalDate());

        Date dateLicense = rs.getDate("users.user_driver_license_date");
        user.setLicenseDate(dateLicense == null ? null : dateLicense.toLocalDate());

        user.setVatNr(rs.getString("users.user_vat"));


        return user;
    }

    public static UserHeader populateUserHeader(ResultSet rs) throws SQLException {
        return new UserHeader(
                rs.getInt("user_id"),
                rs.getString("user_email"),
                rs.getString("user_firstname"),
                rs.getString("user_lastname"),
                UserStatus.valueOf(rs.getString("user_status")),
                rs.getString("user_phone"),
                rs.getString("user_cellphone"),
                (Integer) rs.getObject("user_degage_id")
        );
    }

    public static UserHeaderShort populateUserHeaderShort(ResultSet rs, String tableName) throws SQLException {
        int id = rs.getInt(tableName + ".user_id");
        if (rs.wasNull()) {
            return null;
        } else {
            return new UserHeaderShort(
                    id,
                    rs.getString(tableName + ".user_firstname"),
                    rs.getString(tableName + ".user_lastname")
            );
        }
    }

    public static UserHeaderShort populateUserHeaderShort(ResultSet rs) throws SQLException {
        return new UserHeaderShort(
                rs.getInt("user_id"),
                rs.getString("user_firstname"),
                rs.getString("user_lastname")
        );
    }

    public static UserHeader populateUserHeader(ResultSet rs, String tableName) throws SQLException {
        return new UserHeader(
                rs.getInt(tableName + ".user_id"),
                rs.getString(tableName + ".user_email"),
                rs.getString(tableName + ".user_firstname"),
                rs.getString(tableName + ".user_lastname"),
                UserStatus.valueOf(rs.getString(tableName + ".user_status")),
                rs.getString(tableName + ".user_phone"),
                rs.getString(tableName + ".user_cellphone"),
                (Integer) rs.getObject(tableName + ".user_degage_id")
        );
    }


    @Override
    public UserHeader getUserByEmail(String email) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + USER_HEADER_FIELDS + " FROM users WHERE user_email = ?"
        )) {
            ps.setString(1, email.trim().toLowerCase());
            return toSingleObject(ps, JDBCUserDAO::populateUserHeader);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by email.", ex);
        }
    }

    @Override
    public UserHeader getUserWithPassword(String email, String password) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + USER_HEADER_FIELDS + ", user_password FROM users WHERE user_email = ?"
        )) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserHeader result = populateUserHeader(rs);
                    if (BCrypt.checkpw(password, rs.getString("user_password"))) {
                        return result;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by password.", ex);
        }
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT user_password FROM users WHERE user_id = ?"
        )) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !BCrypt.checkpw(oldPassword, rs.getString("user_password"))) {
                    return false;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retreive password", ex);
        }

        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_password = ? WHERE user_id = ?"
        )) {
            ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not change password", ex);
        }
    }

    @Override
    public User getUser(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                USER_QUERY + " WHERE users.user_id = ?"
        )) {
            ps.setInt(1, userId);
            return toSingleObject(ps, JDBCUserDAO::populateUser);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by id.", ex);
        }

    }

    @Override
    public UserHeader getUserHeader(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + USER_HEADER_FIELDS + " FROM users WHERE user_id = ?"
        )) {
            ps.setInt(1, userId);
            return toSingleObject(ps, JDBCUserDAO::populateUserHeader);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by id.", ex);
        }
    }

    @Override
    public void updateUserStatus(int userId, UserStatus status) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement("UPDATE users SET user_status=? WHERE user_id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user status", ex);
        }
    }

    public void makeUserFull(int userId) {
        // note: there is no easy way in MySQL (apart from using a stored procedure) to do
        // what is done below with a single call to the database. But this not important
        // because this procedure will not be called a lot.

        // first retrieve new Degage id
        int newDegageId;
        try (PreparedStatement ps = prepareStatement(
                "SELECT max(user_degage_id) AS id FROM users WHERE user_degage_id > ?"
        )) {
            int yearValue = (Year.now().getValue() - 2000) * 10000;
            ps.setInt (1, yearValue);
            newDegageId = toSingleInt(ps) + 1;
            if (newDegageId == 1) { // first for this year
                newDegageId = yearValue + 1;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to retreive degage id", ex);
        }

        // then try to register user as full (for the first time)
        int nrOfUpdates;
        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_status='FULL', user_date_joined=NOW(), user_degage_id = ? " +
                        "WHERE user_id = ? AND user_degage_id IS NULL")) {
            ps.setInt(1, newDegageId);
            ps.setInt(2, userId);
            nrOfUpdates = ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to make full user", ex);
        }

        // if the user already existed, only update the status
        if (nrOfUpdates == 0) {
            updateUserStatus(userId, UserStatus.FULL);
        }
    }

    @Override
    public void updateUserMainProfile(User user) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_firstname=?, user_lastname=?,  user_phone=?, user_cellphone=? WHERE user_id = ?"
        )) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getCellPhone());
            ps.setInt(5, user.getId());

            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user main profile", ex);
        }
    }

    @Override
    public int getUserPicture(int userId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT user_image_id FROM users WHERE user_id = ?"
        )) {
            ps.setInt(1, userId);
            return toSingleInt(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get user picture", ex);
        }
    }

    @Override
    public void updateUserPicture(int userId, int fileId) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_image_id = ? WHERE user_id = ?"
        )) {
            ps.setInt(1, fileId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user picture", ex);
        }
    }

    @Override
    public void updateUserLicenseData(int userId, String license, LocalDate date) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_driver_license_id = ?, user_driver_license_date = ? WHERE user_id = ?"
        )) {
            ps.setString(1, license);
            ps.setDate(2, date == null ? null : Date.valueOf(date));
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user license data", ex);
        }
    }

    @Override
    public void updateUserIdentityData(int userId, String identityId, String nationalId) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_identity_card_id = ?, user_identity_card_registration_nr = ? WHERE user_id = ?"
        )) {
            ps.setString(1, identityId);
            ps.setString(2, nationalId);
            ps.setInt(3, userId);

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user identity data", ex);
        }
    }

    @Override
    public void deleteUser(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_status = 'DROPPED' WHERE user_id = ?"
        )) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not delete user", ex);
        }
    }

    // TODO: refactor filters

    @Override
    public Page<User> getUserList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        StringBuilder builder = new StringBuilder();
        builder.append(USER_QUERY);
        builder.append(FILTER_FRAGMENT);

        // add order
        switch (orderBy) {
            case NAME:
                builder.append(" ORDER BY user_lastname ");
                builder.append(asc ? "ASC" : "DESC");
                builder.append(" , user_firstname ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case DATE:
                builder.append(" ORDER BY user_date_joined ");
                builder.append(asc ? "ASC" : "DESC");
                break;
        }

        builder.append(" LIMIT ?, ?");

        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            fillFragment(ps, filter, 1);
            ps.setInt(5, (page - 1) * pageSize);
            ps.setInt(6, pageSize);
            return toPage(ps, pageSize, JDBCUserDAO::populateUser);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of users", ex);
        }
    }

    @Override
    public boolean updateUserEmail(int userId, String email) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE users SET user_email = ? WHERE user_id = ?"
        )) {
            ps.setString(1, email);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == MYSQL_ERROR_DUPLICATE_ENTRY) {
                return false;
            } else {
                throw new DataAccessException("Could not update user email", ex);
            }
        }
    }

    @Override
    public boolean canSeeProfileAsOwner(int ownerId, int userId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT 1 FROM reservations " +
                        "JOIN cars ON reservation_car_id = car_id " +
                        "WHERE car_owner_user_id = ? AND reservation_user_id = ? " +
                        "AND (reservation_from + INTERVAL 120 DAY) > NOW() LIMIT 1"
        )) {
            ps.setInt(1, ownerId);
            ps.setInt(2, userId);
            return isNonEmpty(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not privacy information", ex);
        }
    }

    @Override
    public boolean canSeeProfileAsUser(int userId, int ownerId) {
        // TODO? Only ownerIds with correct role
        try (PreparedStatement ps = prepareStatement(
                "SELECT 1 FROM reservations " +
                        "JOIN cars ON reservation_car_id = car_id " +
                        "WHERE car_owner_user_id = ? AND reservation_user_id = ? " +
                        "AND reservation_status > 4 " + // [ENUM INDEX] = at least accepted
                        "AND (reservation_from + INTERVAL 120 DAY) > NOW() LIMIT 1"
        )) {
            ps.setInt(1, ownerId);
            ps.setInt(2, userId);
            return isNonEmpty(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not privacy information", ex);
        }
    }

    @Override
    public Iterable<UserHeaderShort> listUserByName(String str, int limit) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT user_id, user_firstname, user_lastname FROM users " +
                        "WHERE CONCAT(user_lastname, ', ', user_firstname) LIKE CONCAT ('%', ?, '%')" +
                        "ORDER BY user_lastname ASC, user_firstname ASC " +
                        "LIMIT ?"
        )) {
            ps.setString(1, str);
            ps.setInt(2, limit);
            return toList(ps, JDBCUserDAO::populateUserHeaderShort);
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}
