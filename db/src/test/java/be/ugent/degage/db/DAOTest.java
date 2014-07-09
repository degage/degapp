package be.ugent.degage.db;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.jdbc.JDBCDataAccess;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Common super class for most tests. Contains methods that (automatically) open
 * and close connections and transactions before and after the tests.
 */
public class DAOTest {

    protected static DataAccessContext context;

    @BeforeClass
    public static void getContext() {
        context = JDBCDataAccess.getTestDataAccessProvider().getDataAccessContext();
    }

    @AfterClass
    public static void closeContext() {
        context.close();
    }


    @Before
    public void begin() {
        context.begin();
    }

    @After
    public void rollback() {
        context.rollback();
    }
}
