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
 * Job that generates a report
 */
public class ReportGenerationJob implements ScheduledJob.Executor {
    @Override
    public void execute(DataAccessContext context, Job job) {

        /* TODO: verify that these statements do the correct thing
        context.getCarRideDAO().endPeriod();
        context.getRefuelDAO().endPeriod();
        context.getCarCostDAO().endPeriod();
        */

        UserDAO dao = context.getUserDAO();

        List<User> users = dao.getUserList(FilterField.USER_NAME, true, 1, dao.getAmountOfUsers(null), null);

        Costs costInfo = context.getSettingDAO().getCostSettings(Instant.now());
        for (User user : users) {
            Receipts.generateReceipt(user, new Date(Instant.now().toEpochMilli()), costInfo);
        }
    }
}
