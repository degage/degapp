package schedulers.joblessprocesses;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.models.ReminderAndUserAndInvoice;
import play.Logger;
import play.Play;
import notifiers.Notifier;
import controllers.Reminders;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * Sends payment reminder emails with the invoice as attachment to users. 
 * 
 */
public class PaymentReminderJob extends JoblessProcess{


    public PaymentReminderJob () {
        super("Send payment reminders to users", JoblessProcessType.PAYMENT_REMINDER.name());
    }

    @Override 
    public void runInContext(DataAccessContext context){
        Logger.debug("Send payment reminder mail on " + LocalDate.now().toString());
        Reminders.updateReminders(context);
        LocalDate lastPaymentDate = context.getPaymentDAO().getLastPaymentDate();
        if (Play.isDev()){
            System.err.println(lastPaymentDate);
        }
        for (ReminderAndUserAndInvoice rui : context.getReminderDAO().listUnsentReminders()) {            
            Notifier.sendPaymentReminderMail(context, rui, lastPaymentDate);
            Logger.debug("Sent payment reminder mail to " + rui.getUser());
        }
    }
}