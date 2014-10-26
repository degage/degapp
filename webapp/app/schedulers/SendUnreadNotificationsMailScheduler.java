package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.SchedulerDAO;
import be.ugent.degage.db.models.User;
import notifiers.Notifier;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class SendUnreadNotificationsMailScheduler extends RunnableInContext {

    @Override
    public void runInContext(DataAccessContext context) {

        SchedulerDAO dao = context.getSchedulerDAO();
        //Todo: number_of_unread_messages from system_variable
        List<User> emailList = dao.getReminderEmailList(0);

        for (User user : emailList) {
            Notifier.sendReminderMail(context, user);
        }
    }

}
