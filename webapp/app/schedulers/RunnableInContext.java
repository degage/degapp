package schedulers;

import be.ugent.degage.db.DataAccessContext;
import db.DataAccess;
import play.Logger;

/**
 * Runnable that runs within a database context
 */
public abstract class RunnableInContext implements Runnable {

    public abstract void runInContext (DataAccessContext context);

    private String msg;

    public RunnableInContext (String msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        try (DataAccessContext dac = DataAccess.getContext()) {
            try {
                dac.begin();
                runInContext(dac);
                Logger.info("Finished job: " + msg);
                dac.commit();
            } catch (Exception ex) {
                dac.rollback();
                Logger.error("Error during job: " + msg, ex);
                throw ex;
            }
        }
    }
}
