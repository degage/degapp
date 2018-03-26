
package be.ugent.degage.db.models;

import java.time.LocalDate;
import com.google.gson.annotations.Expose;

public class ReminderAndUserAndInvoice {

    @Expose
    private Reminder reminder;
    @Expose
    private User user;
    @Expose
    private Invoice invoice;

    public ReminderAndUserAndInvoice(Reminder reminder, User user, Invoice invoice) {
        this.reminder = reminder;
        this.user = user;
        this.invoice = invoice;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public User getUser() {
        return user;
    }

    public Invoice getInvoice() {
        return invoice;
    }
}
