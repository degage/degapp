
package be.ugent.degage.db.models;

import java.time.LocalDate;
import com.google.gson.annotations.Expose;

public class InvoiceAndUser {

      @Expose
      private Invoice invoice;
      @Expose
      private User user;

      public InvoiceAndUser(Invoice invoice, User user) {
          this.invoice = invoice;
          this.user = user;
      }

      public Invoice getInvoice() {
          return invoice;
      }

      public User getUser() {
          return user;
      }

  }
