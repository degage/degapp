/* JDBCDataAccessProvider.java
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

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.DataAccessProvider;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Implementation of a data access provider based on JDBC.
 */
class JDBCDataAccessProvider implements DataAccessProvider {

    JDBCDataAccessProvider(boolean testDatabase, DataSource dataSource) {
        this.testDatabase = testDatabase;
        this.dataSource = dataSource;
    }

    private boolean testDatabase;

    private DataSource dataSource;

    /**
     * Is this a database used for testing? If so, some additional operations are allowed.
     */
    public boolean isTest() {
        return testDatabase;
    }

    @Override
    public DataAccessContext getDataAccessContext() throws DataAccessException {
        try {
            return new JDBCDataAccessContext(dataSource.getConnection());
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't connect to degage database", e);
        }
    }



}
