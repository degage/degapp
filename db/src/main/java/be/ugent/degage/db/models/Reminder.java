
package be.ugent.degage.db.models;

import com.google.gson.annotations.Expose;
import java.time.LocalDate;
import com.google.gson.annotations.Expose;

public class Reminder {

    @Expose
    private final int id;
    @Expose
    private final LocalDate date;
    @Expose
    private final String description;
    @Expose
    private final int invoiceId;
    @Expose
    private final String status;
    @Expose
    private final LocalDate sendDate;


    public static class Builder {
        // Required parameters

        // Optional parameters - initialized to default values
        private int id;
        private LocalDate date;
        private String description;
        private int invoiceId;
        private String status;
        private LocalDate sendDate;

        public Builder() {}

        public Builder(Reminder reminder) {
            this.id = reminder.getId();
            this.date = reminder.getDate();
            this.description = reminder.getDescription();
            this.invoiceId = reminder.getInvoiceId();
            this.status = reminder.getStatus();
            this.sendDate  = reminder.getSendDate();
        }

        public Builder id(int val) {
            id = val;
            return this;
        }
        public Builder date(LocalDate val) {
            date = val;
            return this;
        }
        public Builder description(String val) {
            description = val;
            return this;
        }
        public Builder invoiceId(int val) {
            invoiceId = val;
            return this;
        }
        public Builder status(String val) {
            status = val;
            return this;
        }
        public Builder sendDate(LocalDate val) {
            sendDate = val;
            return this;
        }

        public Reminder build() {
            return new Reminder(this);
        }
    }

    private Reminder(Builder builder) {
        id          = builder.id;
        date        = builder.date;
        description = builder.description;
        invoiceId   = builder.invoiceId;
        status      = builder.status;
        sendDate    = builder.sendDate;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getSendDate() {
        return sendDate;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: " + id)
                .append(", date: " + date)
                .append(", description: " + description)
                .append(", invoiceId: " + invoiceId)
                .append(", sendDate: " + sendDate != null ? sendDate : "not yet sent");
        return sb.toString();
    }

    //used for translating description to Dutch
    public static String translate(String s) {
        String tmp = s.toUpperCase();

        if (tmp == null || tmp == "") {
            return "";
        }

        if ("EERSTE RAPPEL".contains(tmp)) {
            return "FIRST";
        } else if ("TWEEDE RAPPEL".contains(tmp)) {
            return "SECOND";
        } else if ("DERDE RAPPEL".contains(tmp)) {
            return "THIRD";
        } else {
            return s;
        }
    }

    //used for translating status to Dutch
    public static String translateStatus(String s) {
        String tmp = s.toUpperCase();

        if (tmp == null || tmp == "") {
            return "";
        }

        if ("BETAALD".contains(tmp)) {
            return "PAID";
        } else if ("OPEN".contains(tmp)) {
            return "OPEN";
        } else {
            return s;
        }
    }

}
