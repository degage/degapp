package schedulers;

import be.ugent.degage.db.DataAccessContext;
import db.DataAccess;

/**
 * Runnable job
 */
public abstract class RunnableInContext implements Runnable {

    public abstract void runInContext (DataAccessContext context);

    @Override
    public void run() {
        try (DataAccessContext dac = DataAccess.getContext()) {
            try {
                dac.begin();
                runInContext(dac);
                dac.commit();
            } catch (Exception ex) {
                dac.rollback();
                throw ex;
            }
        }
    }
}
