/* JDBCCarPreferencesDAO.java
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
import be.ugent.degage.db.dao.CarPreferencesDAO;
import be.ugent.degage.db.models.CarPreference;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC Implementation of {@link be.ugent.degage.db.dao.CarPreferencesDAO}
 */
public class JDBCCarPreferencesDAO extends  AbstractDAO implements CarPreferencesDAO {

    public JDBCCarPreferencesDAO(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public List<CarPreference> listPreferences(int userId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT cars.car_id, car_name, user_id " +
                        "FROM cars LEFT JOIN carpreferences on cars.car_id=carpreferences.car_id AND user_id = ? " +
                        "WHERE cars.car_active ORDER BY car_name"
        )) {
            ps.setInt(1, userId);
            return toList (ps, rs -> new CarPreference(
                    rs.getInt ("car_id"),
                    rs.getString("car_name"),
                    rs.getInt("user_id") == userId
            ));
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public void updatePreferences(int userId, Iterable<Integer> carIds) {
        // first delete existing preferences
        try (PreparedStatement ps = prepareStatement(
                "DELETE carpreferences FROM carpreferences JOIN cars USING (car_id) " +
                "WHERE user_id = ? AND car_active"
        )) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }

        // now included all new preferences
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO carpreferences(user_id,car_id) VALUES (?,?)"
        )) {
            for (Integer carId : carIds) {
                ps.setInt(1, userId);
                ps.setInt(2, carId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }

    }
}
