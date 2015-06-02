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

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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


    // TODO: introduce and use more of these (have a look at Apache DBUtils)

    /**
     * Converts the current result set record to an object of the given type
     * @param <T>
     */
    @FunctionalInterface
    interface ResultSetConverter<T> {
        public T convert (ResultSet rs) throws SQLException;
    }

    /**
     * Transforms a resultset into a list
     */
    protected static<T> List<T> toList (ResultSet rs, ResultSetConverter<T> fn) throws SQLException {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(fn.convert(rs));
        }
        return list;
    }

    /**
     * Executes a prepared statement and converts the result into a list
     */
    protected static<T> List<T> toList (PreparedStatement ps, ResultSetConverter<T> fn) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return toList (rs, fn);
        }
    }

    /**
     * Executes a prepared statement and converts the first result into an integer, or 0 if no result.
     */
    protected static int toSingleInt(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        }
    }

     /**
     * Executes a prepared statement and converts the first result into an object, or null if no result.
     */
    protected static<T> T toSingleObject(PreparedStatement ps, ResultSetConverter<T> fn) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return fn.convert(rs);
            } else {
                return null;
            }
        }
    }

}
