package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.User;
import controllers.Receipts;
import providers.DataProvider;

import java.sql.Date;
import java.util.List;

/**
 * Created by Cedric on 5/3/2014.
 */
public class ReportGenerationJob implements ScheduledJobExecutor {
    @Override
    public void execute(DataAccessContext context, Job job) {

        context.getCarRideDAO().endPeriod();
        context.getRefuelDAO().endPeriod();
        context.getCarCostDAO().endPeriod();

        UserDAO dao = context.getUserDAO();

        List<User> users = dao.getUserList(FilterField.USER_NAME, true, 1, dao.getAmountOfUsers(null), null);
        context.commit();
        for (User user : users) {
            Receipts.generateReceipt(user, new Date(java.util.Calendar.getInstance().getTime().getTime()));
        }
    }
}
