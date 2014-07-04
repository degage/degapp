package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.*;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 2/16/14.
 */
class JDBCUserDAO implements UserDAO {

    private static final String[] AUTO_GENERATED_KEYS = {"user_id"};

    private static final String SMALL_USER_FIELDS = "users.user_id, users.user_password, users.user_firstname, users.user_lastname, users.user_email";

    private static final String SMALL_USER_QUERY = "SELECT " + SMALL_USER_FIELDS + " FROM users";

    private static final String USER_FIELDS = SMALL_USER_FIELDS + ", users.user_cellphone, users.user_phone, users.user_status, users.user_gender, " +
            "domicileAddresses.address_id, domicileAddresses.address_country, domicileAddresses.address_city, domicileAddresses.address_zipcode, domicileAddresses.address_street, domicileAddresses.address_street_number, domicileAddresses.address_street_bus, " +
            "residenceAddresses.address_id, residenceAddresses.address_country, residenceAddresses.address_city, residenceAddresses.address_zipcode, residenceAddresses.address_street, residenceAddresses.address_street_number, residenceAddresses.address_street_bus, " +
            "users.user_driver_license_id, users.user_driver_license_file_group_id, users.user_identity_card_id, users.user_identity_card_registration_nr, users.user_identity_card_file_group_id, " +
            "users.user_damage_history, users.user_payed_deposit, users.user_agree_terms, users.user_image_id";

    private static final String USER_QUERY = "SELECT " + USER_FIELDS + " FROM users " +
            "LEFT JOIN addresses as domicileAddresses on domicileAddresses.address_id = user_address_domicile_id " +
            "LEFT JOIN addresses as residenceAddresses on residenceAddresses.address_id = user_address_residence_id";

    // TODO: more fields to filter on
    public static final String FILTER_FRAGMENT = " WHERE users.user_firstname LIKE ? AND users.user_lastname LIKE ? " +
            "AND (CONCAT_WS(' ', users.user_firstname, users.user_lastname) LIKE ? OR CONCAT_WS(' ', users.user_lastname, users.user_firstname) LIKE ?)";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }

        ps.setString(start, filter.getValue(FilterField.USER_FIRSTNAME));
        ps.setString(start+1, filter.getValue(FilterField.USER_LASTNAME));
        ps.setString(start+2, filter.getValue(FilterField.USER_NAME));
        ps.setString(start+3, filter.getValue(FilterField.USER_NAME));
    }

    private Connection connection;
    private PreparedStatement getUserByEmailStatement;
    private PreparedStatement smallGetUserByIdStatement;
    private PreparedStatement getUserByIdStatement;
    private PreparedStatement createUserStatement;
    private PreparedStatement smallUpdateUserStatement;
    private PreparedStatement updateUserStatement;
    private PreparedStatement deleteUserStatement;
    private PreparedStatement createVerificationStatement;
    private PreparedStatement getVerificationStatement;
    private PreparedStatement deleteVerificationStatement;
    private PreparedStatement getGetUserListPageByNameAscStatement;
    private PreparedStatement getGetUserListPageByNameDescStatement;
    private PreparedStatement getGetAmountOfUsersStatement;


    public JDBCUserDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getDeleteVerificationStatement() throws SQLException {
        if(deleteVerificationStatement == null){
            deleteVerificationStatement = connection.prepareStatement("DELETE FROM verifications WHERE verification_user_id = ? AND verification_type = ?");
        }
        return deleteVerificationStatement;
    }

    private PreparedStatement getCreateVerificationStatement() throws SQLException {
        if(createVerificationStatement == null){
            createVerificationStatement = connection.prepareStatement("INSERT INTO verifications(verification_ident, verification_user_id, verification_type) VALUES(UUID(),?, ?)");
        }
        return createVerificationStatement;
    }

    private PreparedStatement getGetVerificationStatement() throws SQLException {
        if(getVerificationStatement == null){
            getVerificationStatement = connection.prepareStatement("SELECT verification_ident FROM verifications WHERE verification_user_id = ? AND verification_type = ?");
        }
        return getVerificationStatement;
    }
    
    private PreparedStatement getDeleteUserStatement() throws SQLException {
    	if(deleteUserStatement == null){
    		deleteUserStatement = connection.prepareStatement("UPDATE users SET user_status = 'DROPPED' WHERE user_id = ?");
    	}
    	return deleteUserStatement;
    }
    
    private PreparedStatement getUserByEmailStatement() throws SQLException {
        if (getUserByEmailStatement == null) {
            getUserByEmailStatement = connection.prepareStatement(USER_QUERY + " WHERE users.user_email = ?");
        }
        return getUserByEmailStatement;
    }

    private PreparedStatement getSmallGetUserByIdStatement() throws SQLException {
        if(smallGetUserByIdStatement == null){
            smallGetUserByIdStatement = connection.prepareStatement(SMALL_USER_QUERY + " WHERE user_id = ?");
        }
        return smallGetUserByIdStatement;
    }

    private PreparedStatement getGetUserByIdStatement() throws SQLException {
        if(getUserByIdStatement == null){
            getUserByIdStatement = connection.prepareStatement(USER_QUERY + " WHERE users.user_id = ?");
        }
        return getUserByIdStatement;
    }

    private PreparedStatement getCreateUserStatement() throws SQLException {
        if (createUserStatement == null) {
            createUserStatement = connection.prepareStatement("INSERT INTO users(user_email, user_password, user_firstname, user_lastname) VALUES (?,?,?,?)", AUTO_GENERATED_KEYS);
        }
        return createUserStatement;
    }

    private PreparedStatement getSmallUpdateUserStatement() throws SQLException {
        if (smallUpdateUserStatement == null){
            smallUpdateUserStatement = connection.prepareStatement("UPDATE users SET user_email=?, user_password=?, user_firstname=?, user_lastname=? WHERE user_id = ?");
        }
        return smallUpdateUserStatement;
    }

    private PreparedStatement getUpdateUserStatement() throws SQLException {
    	if (updateUserStatement == null){
    		updateUserStatement = connection.prepareStatement("UPDATE users SET user_email=?, user_password=?, user_firstname=?, user_lastname=?, user_status=?, " +
                    "user_gender=?, user_phone=?, user_cellphone=?, user_address_domicile_id=?, user_address_residence_id=?, user_damage_history=?, user_payed_deposit=?, " +
                    "user_agree_terms=?, user_image_id = ?, user_driver_license_id=?, user_driver_license_file_group_id=?, " +
                    "user_identity_card_id=?, user_identity_card_registration_nr=?, user_identity_card_file_group_id=? " +
                    "WHERE user_id = ?");
    	}
    	return updateUserStatement;
    }

    private PreparedStatement getGetUserListPageByNameAscStatement() throws SQLException {
        if(getGetUserListPageByNameAscStatement == null) {
            getGetUserListPageByNameAscStatement = connection.prepareStatement(USER_QUERY + FILTER_FRAGMENT + "ORDER BY users.user_firstname asc, users.user_lastname asc LIMIT ?, ?");
        }
        return getGetUserListPageByNameAscStatement;
    }

    private PreparedStatement getGetUserListPageByNameDescStatement() throws SQLException {
        if(getGetUserListPageByNameDescStatement == null) {
            getGetUserListPageByNameDescStatement = connection.prepareStatement(USER_QUERY + FILTER_FRAGMENT +"ORDER BY users.user_firstname desc, users.user_lastname desc LIMIT ?, ?");
        }
        return getGetUserListPageByNameDescStatement;
    }

    private PreparedStatement getGetAmountOfUsersStatement() throws SQLException {
        if(getGetAmountOfUsersStatement == null) {
            getGetAmountOfUsersStatement = connection.prepareStatement("SELECT COUNT(user_id) AS amount_of_users FROM users" + FILTER_FRAGMENT);
        }
        return getGetAmountOfUsersStatement;
    }


    public static User populateUser(ResultSet rs, boolean withPassword, boolean withRest) throws SQLException {
        return populateUser(rs, withPassword, withRest, "users");
    }

    public static User populateUser(ResultSet rs, boolean withPassword, boolean withRest, String tableName) throws SQLException {
        if(rs.getObject(tableName + ".user_id") == null || rs.getInt(tableName + ".user_id") == 0){ //Fix for left join not returning nullable int
            return null;
        }

        User user = new User(rs.getInt(tableName + ".user_id"), rs.getString(tableName + ".user_email"), rs.getString(tableName + ".user_firstname"), rs.getString(tableName + ".user_lastname"),
                withPassword ? rs.getString(tableName + ".user_password") : null);

        if(withRest) {
            user.setAddressDomicile(JDBCAddressDAO.populateAddress(rs, "domicileAddresses"));
            user.setAddressResidence(JDBCAddressDAO.populateAddress(rs, "residenceAddresses"));
            user.setCellphone(rs.getString(tableName + ".user_cellphone"));
            user.setPhone(rs.getString(tableName + ".user_phone"));
            user.setGender(UserGender.valueOf(rs.getString(tableName + ".user_gender")));
            user.setDamageHistory(rs.getString(tableName + ".user_damage_history"));
            user.setPayedDeposit(rs.getBoolean(tableName + ".user_payed_deposit"));
            user.setAgreeTerms(rs.getBoolean(tableName + ".user_agree_terms"));

            if(rs.getObject(tableName + ".user_image_id") != null){
                user.setProfilePictureId(rs.getInt(tableName + ".user_image_id"));
            }

            DriverLicense driverLicense = new DriverLicense();
            boolean driverLicenseNotNull = false;
            String driverLicenseId = rs.getString(tableName + ".user_driver_license_id");
            if(!rs.wasNull()) {
                driverLicenseNotNull = true;
                driverLicense.setId(driverLicenseId);
            }
            int driverLicenseFileGroupId = rs.getInt(tableName + ".user_driver_license_file_group_id");
            if(!rs.wasNull()) {
                driverLicenseNotNull = true;
                FileGroup driverLicenseFileGroup = new FileGroup(driverLicenseFileGroupId);
                driverLicense.setFileGroup(driverLicenseFileGroup);
            }
            if(driverLicenseNotNull)
                user.setDriverLicense(driverLicense);
            else
                user.setDriverLicense(null);

            IdentityCard identityCard = new IdentityCard();
            boolean identityCardNotNull = false;
            String identityCardId = rs.getString(tableName + ".user_identity_card_id");
            if(!rs.wasNull()) {
                identityCardNotNull = true;
                identityCard.setId(identityCardId);
            }
            String identityCardRegistrationNr = rs.getString(tableName + ".user_identity_card_registration_nr");
            if(!rs.wasNull()) {
                identityCardNotNull = true;
                identityCard.setRegistrationNr(identityCardRegistrationNr);
            }
            int identityCardFileGroupId = rs.getInt(tableName + ".user_identity_card_file_group_id");
            if(!rs.wasNull()) {
                identityCardNotNull = true;
                FileGroup identityCardFileGroup = new FileGroup(identityCardFileGroupId);
                identityCard.setFileGroup(identityCardFileGroup);
            }
            if(identityCardNotNull)
                user.setIdentityCard(identityCard);
            else
                user.setIdentityCard(null);

            user.setStatus(UserStatus.valueOf(rs.getString(tableName + ".user_status")));

        }

        return user;
    }


    @Override
    public User getUser(String email) {
        if(email == null || email.isEmpty())
            return null;

        try {
            PreparedStatement ps = getUserByEmailStatement();
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return populateUser(rs, true, true);
                else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading user resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by email.", ex);
        }
    }

    @Override
    public User getUser(int userId, boolean withRest) throws DataAccessException {
        try {
            PreparedStatement ps;
            if(withRest) {
                ps = getGetUserByIdStatement();
            } else {
                ps = getSmallGetUserByIdStatement();
            }

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return populateUser(rs, true, withRest);
                }
                else return null;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading user resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch user by id.", ex);
        }

    }

    @Override
    public User createUser(String email, String password, String firstName, String lastName) throws DataAccessException {
        try {
            PreparedStatement ps = getCreateUserStatement();
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, firstName);
            ps.setString(4, lastName);

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating user.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new User(keys.getInt(1), email, firstName, lastName, password); //TODO: extra constructor
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new user.", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to commit new user transaction.", ex);
        }
    }

    @Override
    public void updateUser(User user, boolean withRest) throws DataAccessException {
        try {
            PreparedStatement ps;
            if(withRest) {
                ps = getUpdateUserStatement();
            } else {
                ps = getSmallUpdateUserStatement();
            }
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());

            if(!withRest) {
                ps.setInt(5, user.getId());
            } else {
                ps.setString(5, user.getStatus().name());
                ps.setString(6, user.getGender().name());
                if(user.getPhone()==null) ps.setNull(7, Types.VARCHAR);
                else ps.setString(7, user.getPhone());
                if(user.getCellphone()==null) ps.setNull(8, Types.VARCHAR);
                else ps.setString(8, user.getCellphone());
                if(user.getAddressDomicile() == null) ps.setNull(9, Types.INTEGER);
                else ps.setInt(9, user.getAddressDomicile().getId());
                if(user.getAddressResidence() == null) ps.setNull(10, Types.INTEGER);
                else ps.setInt(10, user.getAddressResidence().getId());
                if(user.getDamageHistory()==null) ps.setNull(11, Types.VARCHAR);
                else ps.setString(11, user.getDamageHistory());
                ps.setBoolean(12, user.isPayedDeposit());
                ps.setBoolean(13, user.isAgreeTerms());

                if(user.getProfilePictureId() != -1) ps.setInt(14, user.getProfilePictureId());
                else ps.setNull(14, Types.INTEGER);
                if(user.getDriverLicense() == null) {
                    ps.setNull(15, Types.VARCHAR);
                    ps.setNull(16, Types.INTEGER);
                } else {
                    if(user.getDriverLicense().getId() == null) ps.setNull(15, Types.VARCHAR);
                    else ps.setString(15, user.getDriverLicense().getId());
                    if(user.getDriverLicense().getFileGroup() == null) ps.setNull(16, Types.INTEGER);
                    else ps.setInt(16, user.getDriverLicense().getFileGroup().getId());
                }

                if(user.getIdentityCard() == null) {
                    ps.setNull(17, Types.VARCHAR);
                    ps.setNull(18, Types.VARCHAR);
                    ps.setNull(19, Types.INTEGER);
                } else {
                    if(user.getIdentityCard().getId() == null) ps.setNull(17, Types.VARCHAR);
                    else ps.setString(17, user.getIdentityCard().getId());
                    if(user.getIdentityCard().getRegistrationNr() == null) ps.setNull(18, Types.VARCHAR);
                    else ps.setString(18, user.getIdentityCard().getRegistrationNr());
                    if(user.getIdentityCard().getFileGroup() == null) ps.setNull(19, Types.INTEGER);
                    else ps.setInt(19, user.getIdentityCard().getFileGroup().getId());
                }


                ps.setInt(20, user.getId());
            }

            if(ps.executeUpdate() == 0)
                throw new DataAccessException("User update affected 0 rows.");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update user", ex);
        }
    }

    @Override
    public void deleteUser(User user) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteUserStatement();
            ps.setInt(1, user.getId());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when deleting (=updating to DROPPED) user.");
        } catch (SQLException ex){
            throw new DataAccessException("Could not delete user",ex);
        }

    }

    @Override
    public String getVerificationString(User user, VerificationType type) throws DataAccessException {
        try {
            PreparedStatement ps = getGetVerificationStatement();
            ps.setInt(1, user.getId());
            ps.setString(2, type.name());
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next())
                    return null;
                else return rs.getString("verification_ident");
            } catch(SQLException ex){
                throw new DataAccessException("Failed to read verification resultset.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to get verification string.", ex);
        }
    }

    @Override
    public String createVerificationString(User user, VerificationType type) throws DataAccessException {
        try {
            PreparedStatement ps = getCreateVerificationStatement();
            ps.setInt(1, user.getId());
            ps.setString(2, type.name());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Verification string creation failed. Zero rows affected");

            return getVerificationString(user, type); //TODO: this might throw an exception about 2 open connections?

        } catch(SQLException ex){
            throw new DataAccessException("Failed to create verification string.", ex);
        }
    }

    @Override
    public void deleteVerificationString(User user, VerificationType type) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteVerificationStatement();
            ps.setInt(1, user.getId());
            ps.setString(2, type.name());
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("Verification delete operation affected 0 rows.");

        } catch(SQLException ex){
            throw new DataAccessException("Failed to delete verification.", ex);
        }
    }

    /**
     * @param filter The filter to apply to
     * @return The amount of filtered cars
     * @throws DataAccessException
     */
    @Override
    public int getAmountOfUsers(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getGetAmountOfUsersStatement();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_users");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of users", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of users", ex);
        }
    }

    @Override
    public List<User> getUserList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = null;
            switch(orderBy) {
                case USER_NAME:
                    ps = asc ? getGetUserListPageByNameAscStatement() : getGetUserListPageByNameDescStatement();
                    break;
            }
            if(ps == null) {
                throw new DataAccessException("Could not create getUserList statement");
            }

            fillFragment(ps, filter, 1);
            int first = (page-1)*pageSize;
            ps.setInt(5, first);
            ps.setInt(6, pageSize);
            return getUsers(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of users", ex);
        }
    }

    private List<User> getUsers(PreparedStatement ps) {
        List<User> users = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(populateUser(rs, false, true));
            }
            return users;
        } catch (SQLException ex) {
            throw new DataAccessException("Error reading users resultset", ex);
        }
    }
}
