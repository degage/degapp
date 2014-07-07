package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.User;
import controllers.Receipts;
import providers.DataProvider;

import java.sql.Date;
import java.util.List;

public class GenerateReceiptsScheduler extends RunnableInContext {

    @Override
    public void runInContext(DataAccessContext context) {
        context.getCarRideDAO().endPeriod();
        context.getRefuelDAO().endPeriod();
        context.getCarCostDAO().endPeriod();

        UserDAO dao = context.getUserDAO();

        List<User> users = dao.getUserList(FilterField.USER_NAME, true, 1, dao.getAmountOfUsers(null), null);

        for (User user : users) {
            Receipts.generateReceipt(user, new Date(java.util.Calendar.getInstance().getTime().getTime()));
        }
    }


}
