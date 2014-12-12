/* AbstractDAO.java
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Common superclass of the data access objects in this package.
 */
class AbstractDAO {

    protected final JDBCDataAccessContext context;

    /*
     * Inner class for simplifying lazy creation of prepared statements
     */
    protected class LazyStatement {
        private final String sql;

        private PreparedStatement preparedStatement;

        private String[] primaryKeys;

        public LazyStatement (String sql) {
            this.sql = sql;
            this.primaryKeys = null;
        }

        public LazyStatement (String sql, String... primaryKeys) {
            this.sql = sql;
            this.primaryKeys = primaryKeys;
        }

        public PreparedStatement value() throws SQLException {
            if (preparedStatement == null) {
                if (primaryKeys == null) {
                    preparedStatement = prepareStatement(sql);
                } else {
                    preparedStatement = prepareStatement(sql, primaryKeys);
                }
            }
            return preparedStatement;
        }
    }

    public AbstractDAO(JDBCDataAccessContext context) {
        this.context = context;
    }

    /**
     * Convenience method that returns the connection from the context
     */
    protected Connection getConnection() {
        return context.getConnection();
    }

    /**
     * Convenience method for creating a  statement in the current context
     */
    protected Statement createStatement() throws SQLException {
        return context.getConnection().createStatement();
    }

    /**
     * Convenience method for creating a prepared statement in the current context
     */
    protected PreparedStatement prepareStatement(String sql) throws SQLException {
        return context.getConnection().prepareStatement(sql);
    }

    /**
     * Convenience method for creating a prepared statement in the current context, with an auto generated primary key
     */
    protected PreparedStatement prepareStatement(String sql, String... primaryKeys) throws SQLException {
        return context.getConnection().prepareStatement(sql, primaryKeys);
    }
}
