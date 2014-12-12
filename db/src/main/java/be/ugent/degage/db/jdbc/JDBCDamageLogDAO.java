/* JDBCDamageLogDAO.java
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

import be.ugent.degage.db.dao.DamageLogDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.DamageLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Stefaan Vermassen on 04/05/14.
 */
class JDBCDamageLogDAO extends AbstractDAO implements DamageLogDAO {

    public JDBCDamageLogDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private LazyStatement createDamageLogStatement = new LazyStatement(
            "INSERT INTO damagelogs (damage_log_damage_id, damage_log_description) VALUES(?, ?)"
    );

    @Override
    public void addDamageLog(int damageId, String message) throws DataAccessException {
        try {
            PreparedStatement ps = createDamageLogStatement.value();
            ps.setInt(1, damageId);
            ps.setString(2, message);
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("No rows were affected when creating damagelog.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create damagelog", e);
        }
    }

    private LazyStatement getDamageLogsStatement = new LazyStatement(
            "SELECT damage_log_description, damage_log_created_at FROM damagelogs " +
                    "WHERE damage_log_damage_id = ? ORDER BY damage_log_created_at DESC"
    );

    @Override
    public Iterable<DamageLog> getDamageLogsForDamage(int damageId) throws DataAccessException {
        try {
            PreparedStatement ps = getDamageLogsStatement.value();
            ps.setInt(1, damageId);
            Collection<DamageLog> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new DamageLog(
                                    rs.getString("damage_log_description"),
                                    rs.getTimestamp("damage_log_created_at").toInstant()
                            )
                    );
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve the list of damagelogs.", e);
        }
    }

}
