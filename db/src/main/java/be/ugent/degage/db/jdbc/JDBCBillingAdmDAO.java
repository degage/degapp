/* JDBCBillingAdmDAO.java
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
import be.ugent.degage.db.dao.BillingAdmDAO;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * JDBC implementation of {@link BillingAdmDAO}
 */
class JDBCBillingAdmDAO extends AbstractDAO implements BillingAdmDAO {

    public JDBCBillingAdmDAO(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public void archive(int billingId) {
        try (CallableStatement cs = prepareCall("{call billing_archive(?)}" )) {
            cs.setInt(1,billingId);
            cs.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not archive billing", e);
        }
    }

    @Override
    public void createBilling(String description, String prefix, LocalDate start, LocalDate limit) {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO billing(billing_description,billing_prefix,billing_start,billing_limit) VALUES (?,?,?,?)"
        )) {
            ps.setString(1, description);
            ps.setString(2, prefix);
            ps.setDate(3, Date.valueOf(start));
            ps.setDate(4, Date.valueOf(limit));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not create billing", e);
        }
    }

}
