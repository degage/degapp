package schedulers;

import be.ugent.degage.db.DataAccessContext;

public class CheckFinishedRidesJob extends RunnableInContext {
    @Override
    public void runInContext(DataAccessContext context) {
            context.getReservationDAO().updateTable();
    }
}
