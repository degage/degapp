package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.Costs;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.User;
import controllers.Receipts;

import java.sql.Date;
import java.time.Instant;
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

        Costs costInfo = context.getSettingDAO().getCostSettings(Instant.now());
        context.commit();
        for (User user : users) {
            Receipts.generateReceipt(user, new Date(Instant.now().toEpochMilli()), costInfo);
        }
    }
}
