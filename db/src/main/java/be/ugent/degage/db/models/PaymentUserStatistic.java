
package be.ugent.degage.db.models;

import com.google.gson.annotations.Expose;
import java.time.LocalDate;
import be.ugent.degage.db.models.*;

public class PaymentUserStatistic {

    @Expose
    private final User user;

    @Expose
    private final double amountToPay;
    @Expose
    private final double amountPaid;

    public static class Builder {
        // Required parameters
        private User user;

        // Optional parameters - initialized to default values
        private double amountToPay;
        private double amountPaid;

        public Builder(User user) {
            this.user = user;
        }

        public Builder amountToPay(double val) {
            amountToPay = val;
            return this;
        }
        public Builder amountPaid(double val) {
            amountPaid = val;
            return this;
        }

        public PaymentUserStatistic build() {
            return new PaymentUserStatistic(this);
        }
    }

    private PaymentUserStatistic(Builder builder) {
        user                = builder.user;

        amountToPay         = builder.amountToPay;
        amountPaid          = builder.amountPaid;
    }

    public User getUser() {
        return user;
    }

    public double getAmountToPay() {
        return amountToPay;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

}
