/* JDBCVerificationDAO.java
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
import be.ugent.degage.db.dao.VerificationDAO;
import be.ugent.degage.db.models.VerificationType;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * JDBC implementation of {@link be.ugent.degage.db.dao.VerificationDAO}
 */
class JDBCVerificationDAO  extends AbstractDAO implements VerificationDAO {

    public JDBCVerificationDAO (JDBCDataAccessContext context) {
        super(context);
    }

    private LazyStatement deleteVerificationStatement = new LazyStatement(
            "DELETE FROM verifications WHERE verification_email = ?"
    );

    private void deleteVerifications(String email) throws SQLException {
        PreparedStatement ps = deleteVerificationStatement.value();
        ps.setString(1, email.trim().toLowerCase());
        ps.executeUpdate();
    }

    private LazyStatement createTokenStatement = new LazyStatement(
            "INSERT INTO verifications(verification_ident, verification_email, verification_type) VALUES(?,?, ?)",
            "verification_ident"
    );

    @Override
    public String createToken(String email, VerificationType type) throws DataAccessException {
        try {
            String uuid = UUID.randomUUID().toString();
            PreparedStatement ps = createTokenStatement.value();
            ps.setString(1, uuid);
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, type.name());
            ps.executeUpdate();
            return uuid;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create verification token.", ex);
        }
    }

    private LazyStatement createUserStatement = new LazyStatement(
            "INSERT users(user_email, user_password, user_firstname, user_lastname) " +
                    "SELECT verification_email,?,?,? FROM verifications WHERE verification_email = ? AND verification_ident=?"
    );

    @Override
    public Result createUserForToken(String token, String email, String password, String firstName, String lastName) throws DataAccessException {
        try {
            PreparedStatement ps = createUserStatement.value();
            String emailNormalized = email.trim().toLowerCase();
            ps.setString(1, BCrypt.hashpw(password, BCrypt.gensalt(12)));
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, emailNormalized);
            ps.setString(5, token);

            if (ps.executeUpdate() == 0) {
                return Result.INVALID_PAIR; // invalid token-email pair
            }
            deleteVerifications(emailNormalized);
            return Result.OK;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == MYSQL_ERROR_DUPLICATE_ENTRY) {
                return Result.ALREADY_EXISTS; // unique key violation
            } else {
                throw new DataAccessException("Failed to create user.", ex);
            }
        }
    }

    private LazyStatement updatePasswordStatement = new LazyStatement(
            "UPDATE users JOIN verifications ON user_email = verification_email " +
                    "SET user_password = ? WHERE user_email = ? AND verification_ident = ?"
    );

    @Override
    public Result changePasswordForToken (String token, String email, String newPassword) throws DataAccessException {
        try {
            PreparedStatement ps = updatePasswordStatement.value();
            String emailNormalized = email.trim().toLowerCase();
            ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
            ps.setString(2, emailNormalized);
            ps.setString(3, token);
            if (ps.executeUpdate() == 0) {
                return Result.INVALID_PAIR; // invalid token-email pair
            }
            deleteVerifications(emailNormalized);
            return Result.OK;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not change password", ex);
        }
    }


}
