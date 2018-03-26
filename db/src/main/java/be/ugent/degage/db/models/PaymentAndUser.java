
package be.ugent.degage.db.models;

import java.time.LocalDate;
import com.google.gson.annotations.Expose;

public class PaymentAndUser {

      @Expose
      private Payment payment;
      @Expose
      private User user;

      public PaymentAndUser(Payment payment, User user) {
          this.payment = payment;
          this.user = user;
      }

      public Payment getPayment() {
          return payment;
      }

      public User getUser() {
          return user;
      }

  }
