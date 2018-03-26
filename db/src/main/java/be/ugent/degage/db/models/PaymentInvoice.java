
package be.ugent.degage.db.models;

public class PaymentInvoice {

      private Payment payment;
      private Invoice invoice;

      public PaymentInvoice(Payment payment, Invoice invoice) {
          this.payment = payment;
          this.invoice = invoice;
      }

      public Payment getPayment() {
          return payment;
      }

      public Invoice getInvoice() {
          return invoice;
      }

  }
