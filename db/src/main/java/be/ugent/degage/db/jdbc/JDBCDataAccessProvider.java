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
