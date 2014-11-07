package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.InfoSessionDAO;
import be.ugent.degage.db.models.Enrollee;
import be.ugent.degage.db.models.InfoSession;
import be.ugent.degage.db.models.Job;
import notifiers.Notifier;
import play.Logger;

/**
 * Job that sends a reminder of an infosession to all participants
 */
public class InfoSessionReminderJob implements ScheduledJob.Executor {
    @Override
    public void execute(DataAccessContext context, Job job) {
        InfoSessionDAO dao = context.getInfoSessionDAO();
        int sessionId = job.getRefId();
        InfoSession session = dao.getInfoSession(sessionId);
        if (session != null) {
            for (Enrollee er : dao.getEnrollees(sessionId)) {
                Notifier.sendInfoSessionEnrolledMail(context, er.getUser(), session); //TODO: use separate correct notifier
                Logger.debug("Sent infosession reminder mail to " + er.getUser());
            }
        }
    }
}
