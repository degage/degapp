/* JDBCPrivilegedDAO.java
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
import be.ugent.degage.db.dao.PrivilegedDAO;
import be.ugent.degage.db.models.UserHeader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * JDBC implementation of {@link be.ugent.degage.db.dao.PrivilegedDAO}
 */
public class JDBCPrivilegedDAO extends AbstractDAO implements PrivilegedDAO {

    public JDBCPrivilegedDAO(JDBCDataAccessContext context) {
        super(context);
    }

    // TODO: replace * by actual fields
    private LazyStatement getPrivilegedStatement = new LazyStatement (
            "SELECT " + JDBCUserDAO.USER_HEADER_FIELDS +
            " FROM carprivileges INNER JOIN users ON user_id = car_privilege_user_id WHERE car_privilege_car_id=?"
    );

    @Override
    public Iterable<UserHeader> getPrivileged(int carId) throws DataAccessException {
        try {
            PreparedStatement ps = getPrivilegedStatement.value();
            ps.setInt(1, carId);
            Collection<UserHeader> users = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(JDBCUserDAO.populateUserHeader(rs));
                }
                return users;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of privileged", ex);
        }
    }

    private LazyStatement createPrivilegedStatement = new LazyStatement (
            "INSERT INTO carprivileges(car_privilege_user_id, car_privilege_car_id) VALUES (?,?) " +
                    "ON DUPLICATE KEY UPDATE car_privilege_car_id=car_privilege_car_id"  // = do nothing
    );

    @Override
    public void addPrivileged(int carId, Iterable<Integer> userIds) throws DataAccessException {
        try {
            for(int userId : userIds) {
                PreparedStatement ps = createPrivilegedStatement.value();
                ps.setInt(1, userId);
                ps.setInt(2, carId);
                ps.executeUpdate();
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create new privileged");
        }
    }

    private LazyStatement deletePrivilegedStatement = new LazyStatement (
        "DELETE FROM carprivileges WHERE car_privilege_user_id = ? AND car_privilege_car_id=?"
    );

    @Override
    public void deletePrivileged(int carId, Iterable<Integer> userIds) throws DataAccessException {
        try {
            for(int userId : userIds) {
                PreparedStatement ps = deletePrivilegedStatement.value();
                ps.setInt(1, userId);
                ps.setInt(2, carId);
                ps.executeUpdate();
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to delete privileged");
        }
    }

    private LazyStatement isOwnerOrPrivilegedStatement = new LazyStatement(
            " SELECT 1 FROM cars WHERE car_id = ? AND car_owner_user_id = ? " +
            " UNION " +
            " SELECT 1 FROM carprivileges WHERE car_privilege_car_id = ? AND car_privilege_user_id = ?"
    );

    @Override
    public boolean isOwnerOrPrivileged(int carId, int userId) {
        try {
            PreparedStatement ps = isOwnerOrPrivilegedStatement.value();
            ps.setInt (1, carId);
            ps.setInt (2, userId);
            ps.setInt (3, carId);
            ps.setInt (4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next ();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not determine car onwerschip or privileges");
        }

    }

}
