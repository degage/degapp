package schedulers.jobprocesses;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.InfoSessionDAO;
import be.ugent.degage.db.models.Enrollee;
import be.ugent.degage.db.models.InfoSession;
import be.ugent.degage.db.models.Job;
import notifiers.Notifier;
import play.Logger;

/**
 * Sends post info session email to users who have attended the info session.
 * Mails will be composed accordingly to the users attendance.
 */
public class PostInfoSessionJob implements ScheduledJob.Executor {

    @Override
    public void execute(DataAccessContext context, Job job){

        InfoSessionDAO dao = context.getInfoSessionDAO();
        int sessionId = job.getRefId();
        InfoSession session = dao.getInfoSession(sessionId);
        
        if (session != null) {
            for (Enrollee er : dao.getEnrollees(sessionId)) {      
                Notifier.sendPostInfoSessionMail(context, er, session); 
            }
        }
    }
}