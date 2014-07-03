package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.SchedulerDAO;
import be.ugent.degage.db.models.User;
import notifiers.Notifier;
import providers.DataProvider;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class SendUnreadNotificationsMailScheduler implements Runnable {

    @Override
    public void run() {
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            SchedulerDAO dao = context.getSchedulerDAO();
            //Todo: number_of_unread_messages from system_variable
            List<User> emailList = dao.getReminderEmailList(0);
            context.commit();
            for(User user : emailList){
                Notifier.sendReminderMail(user);
            }
        }catch(DataAccessException ex) {
            throw ex;
        }
    }
}
