package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.ReservationDAO;
import providers.DataProvider;

/**
 * Created by Cedric on 5/7/2014.
 */
public class CheckFinishedRidesJob implements Runnable {
    @Override
    public void run() {
        try(DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()){
            ReservationDAO dao = context.getReservationDAO();
            dao.updateTable();
            context.commit();
        }
    }
}
