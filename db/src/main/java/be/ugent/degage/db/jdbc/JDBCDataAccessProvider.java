package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.DatabaseConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCDataAccessProvider implements DataAccessProvider {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    private DatabaseConfiguration configuration;

    public JDBCDataAccessProvider(DatabaseConfiguration configuration){
        this.configuration = configuration;
    }

    @Override
    public DataAccessContext getDataAccessContext() throws DataAccessException {
        Connection conn;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(getDatabaseUrl(configuration), configuration.getUsername(), configuration.getPassword());
            return new JDBCDataAccessContext(conn);
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("Couldn't find jdbc driver", e);
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't connect to degage database", e);
        }
    }

    private static String getDatabaseUrl(DatabaseConfiguration configuration){
        return String.format("jdbc:mysql://%s:%d/%s", configuration.getServer(), configuration.getPort(), configuration.getDatabase());
    }

}
