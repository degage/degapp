/* JDBCDataAccess.java
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

import be.ugent.degage.db.DataAccessProvider;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;

/**
 * Provides methods to obtain a data access provider that connects to a database.
 * <p>
 * To obtain a data access provider use one of two methods
 * <ul>
 * <li>{@link #createDataAccessProvider} in production or development code</li>
 * <li>{@link #createTestDataAccessProvider}, when testing</li>
 * </ul>
 * <p>
 * Note that only MySQL databases are supported, and only for version 5.5 or higher.
 */
public final class JDBCDataAccess {

    /**
     * Create a new data access provider based on the given data source.
     */
    public static DataAccessProvider createDataAccessProvider(DataSource dataSource) {
        return new JDBCDataAccessProvider(false, dataSource);
    }

    private static DataAccessProvider TEST_DATA_ACCESS_PROVIDER;

    /**
     * Retrieve the data access provider for use in tests. This test database uses a fixed configuration, see
     * installation documentation. The object returned is a lazily created singleton.
     */
    public synchronized static DataAccessProvider getTestDataAccessProvider() {
        if (TEST_DATA_ACCESS_PROVIDER == null) {
            TEST_DATA_ACCESS_PROVIDER = createTestDataAccessProvider();
        }
        return TEST_DATA_ACCESS_PROVIDER;
    }


    private static DataAccessProvider createTestDataAccessProvider() {
        MysqlDataSource dataSource  = new MysqlDataSource();

        dataSource.setServerName("localhost");
        dataSource.setPortNumber(3306);
        dataSource.setDatabaseName("degagetest");
        dataSource.setUser("degage");
        dataSource.setPassword("DeGaGe");

        return new JDBCDataAccessProvider(true, dataSource);
    }
}
