package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.ReservationDAO;
import providers.DataProvider;

public class CheckFinishedRidesJob extends RunnableInContext {
    @Override
    public void runInContext(DataAccessContext context) {
            context.getReservationDAO().updateTable();
    }
}
