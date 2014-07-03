package database.mocking;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.DataAccessProvider;

/**
 * Created by Cedric on 3/7/14.
 */
public class TestDataAccessProvider implements DataAccessProvider {

    private DataAccessContext context;

    public TestDataAccessProvider(){
        this.context = new TestDataAccessContext();
    }

    @Override
    public DataAccessContext getDataAccessContext() throws DataAccessException {
        return context;
    }
}
