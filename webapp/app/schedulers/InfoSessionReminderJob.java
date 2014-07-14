package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.InfoSessionDAO;
import be.ugent.degage.db.models.Enrollee;
import be.ugent.degage.db.models.InfoSession;
import be.ugent.degage.db.models.Job;
import notifiers.Notifier;
import play.Logger;
import providers.DataProvider;

/**
 * Created by Cedric on 5/3/2014.
 */
public class InfoSessionReminderJob implements ScheduledJobExecutor {
    @Override
    public void execute(DataAccessContext context, Job job) {
        InfoSessionDAO dao = context.getInfoSessionDAO();
        int sessionId = job.getRefId();
        InfoSession session = dao.getInfoSession(sessionId);
        if (session == null)
            return;

        for (Enrollee er : dao.getEnrollees(sessionId)) {
            Notifier.sendInfoSessionEnrolledMail(context, er.getUser(), session); //TODO: use separate correct notifier
            Logger.debug("Sent infosession reminder mail to " + er.getUser());
        }
    }
}
