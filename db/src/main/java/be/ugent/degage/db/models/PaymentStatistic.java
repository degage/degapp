
package be.ugent.degage.db.models;

import com.google.gson.annotations.Expose;
import java.time.LocalDate;
import com.google.gson.annotations.Expose;

public class PaymentStatistic {

    @Expose
    private final int billingId;
    @Expose
    private final String billingDescription;

    @Expose
    private final double percentagePaid;
    @Expose
    private final double percentageOpen;
    @Expose
    private final double percentageOverdue;
    @Expose
    private final double amountToReceive;
    @Expose
    private final double amountReceived;
    @Expose
    private final double amountToPay;
    @Expose
    private final double amountPaid;
    @Expose
    private final double averageTimeToPay;

    public static class Builder {
        // Required parameters
        private int billingId;

        // Optional parameters - initialized to default values
        private String billingDescription;
        private double percentagePaid;
        private double percentageOpen;
        private double percentageOverdue;
        private double amountToReceive;
        private double amountReceived;
        private double amountToPay;
        private double amountPaid;
        private double averageTimeToPay;

        public Builder(int billingId) {
            this.billingId = billingId;
        }

        public Builder billingDescription(String val) {
            billingDescription = val;
            return this;
        }
        public Builder percentagePaid(double val) {
            percentagePaid = val;
            return this;
        }
        public Builder percentageOpen(double val) {
            percentageOpen = val;
            return this;
        }
        public Builder percentageOverdue(double val) {
            percentageOverdue = val;
            return this;
        }
        public Builder amountToReceive(double val) {
            amountToReceive = val;
            return this;
        }
        public Builder amountReceived(double val) {
            amountReceived = val;
            return this;
        }
        public Builder amountToPay(double val) {
            amountToPay = val;
            return this;
        }
        public Builder amountPaid(double val) {
            amountPaid = val;
            return this;
        }
        public Builder averageTimeToPay(double val) {
            averageTimeToPay = val;
            return this;
        }

        public PaymentStatistic build() {
            return new PaymentStatistic(this);
        }
    }

    private PaymentStatistic(Builder builder) {
        billingId           = builder.billingId;

        billingDescription  = builder.billingDescription;
        percentagePaid      = builder.percentagePaid;
        percentageOpen      = builder.percentageOpen;
        percentageOverdue   = builder.percentageOverdue;
        amountToReceive     = builder.amountToReceive;
        amountReceived      = builder.amountReceived;
        amountToPay         = builder.amountToPay;
        amountPaid          = builder.amountPaid;
        averageTimeToPay    = builder.averageTimeToPay;
    }

    public int getBillingId() {
        return billingId;
    }

    public String getBillingDescription() {
        return billingDescription;
    }

    public double getPercentagePaid() {
        return percentagePaid;
    }

    public double getPercentageOpen() {
        return percentageOpen;
    }

    public double getPercentageOverdue() {
        return percentageOverdue;
    }

    public double getAmountToReceive() {
        return amountToReceive;
    }

    public double getAmountReceived() {
        return amountReceived;
    }

    public double getAmountToPay() {
        return amountToPay;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getAverageTimeToPay() {
        return averageTimeToPay;
    }
}
