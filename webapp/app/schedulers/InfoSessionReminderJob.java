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
    public void execute(Job job) {
        try(DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()){
            InfoSessionDAO dao = context.getInfoSessionDAO();
            InfoSession session = dao.getInfoSession(job.getRefId(), true);
            if(session == null)
                return;

            for(Enrollee er : session.getEnrolled()){
                Notifier.sendInfoSessionEnrolledMail(er.getUser(), session); //TODO: use seperate correct notifier
                Logger.debug("Sent infosession reminder mail to " + er.getUser());
            }
        }
    }
}
