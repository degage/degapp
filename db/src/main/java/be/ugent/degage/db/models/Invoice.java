
package be.ugent.degage.db.models;

import com.google.gson.annotations.Expose;
import java.time.LocalDate;
import java.util.List;
import  java.util.ArrayList;

public class Invoice {
  @Expose
  private final int invoiceId;
  @Expose
  private final String number;
  @Expose
  private final float amount;
  @Expose
  private final InvoiceStatus status;
  @Expose
  private final InvoiceType type;

  @Expose
  private final int userId;
  @Expose
  private final int billingId;
  @Expose
  private final int carId;

  @Expose
  private final String comment;
  @Expose
  private final String structuredCommunication;

  @Expose
  private final LocalDate date;
  @Expose
  private final LocalDate paymentDate;
  @Expose
  private final LocalDate dueDate;
  @Expose
  private final List<Integer> paymentIds;
  @Expose
  private final float paidAmount;

  public static class Builder {
      // Required parameters
      private final int userId;
      private final int billingId;
      private final String number;

      // Optional parameters - initialized to default values
      private int invoiceId;
      private float amount = 0;
      private InvoiceStatus status;
      private InvoiceType type;
      private LocalDate date = null;
      private LocalDate paymentDate = null;
      private LocalDate dueDate = null;
      private String comment;
      private String structuredCommunication;
      private int carId;
      private List<Integer> paymentIds;
      private float paidAmount;

      public Builder(Invoice invoice) {
          this.number = invoice.getNumber();
          this.userId = invoice.getUserId();
          this.billingId = invoice.getBillingId();
          this.invoiceId = invoice.getId();
          this.amount = invoice.getAmount();
          this.status  = invoice.getStatus();
          this.type  = invoice.getType();
          this.date = invoice.getDate();
          this.paymentDate = invoice.getPaymentDate();
          this.dueDate = invoice.getDueDate();
          this.structuredCommunication = invoice.getStructuredCommunication();
          this.comment = invoice.getComment();
          this.carId = invoice.getCarId();
          this.paymentIds = invoice.getPaymentIds();
          this.paidAmount = invoice.getPaidAmount();
      }

      public Builder(String number, int userId, int billingId) {
          this.number = number;
          this.userId = userId;
          this.billingId = billingId;
      }

      public Builder id(int val)
          { invoiceId = val;      return this; }
      public Builder amount(float val)
          { amount = val;           return this; }
        public Builder status(InvoiceStatus val)
            { status = val;  return this; }
        public Builder type(InvoiceType val)
            { type = val;  return this; }
      public Builder date(LocalDate val)
          { date = val; return this; }
      public Builder paymentDate(LocalDate val)
          { paymentDate = val; return this; }
      public Builder dueDate(LocalDate val)
          { dueDate = val; return this; }
      public Builder structuredCommunication(String val)
          { structuredCommunication = val; return this; }
      public Builder comment(String val)
          { comment = val; return this; }
      public Builder carId(int val)
          { carId = val; return this; }
      public Builder paymentIds(List<Integer> val)
          { paymentIds = val; return this; }
      public Builder paidAmount(float val)
          { paidAmount = val; return this; }

      public Invoice build() {
          return new Invoice(this);
      }
  }

  private Invoice(Builder builder) {
      invoiceId     = builder.invoiceId;
      number        = builder.number;
      amount        = builder.amount;
      status        = builder.status;
      type          = builder.type;
      date          = builder.date;
      paymentDate   = builder.paymentDate;
      dueDate       = builder.dueDate;
      userId        = builder.userId;
      billingId     = builder.billingId;
      comment       = builder.comment;
      structuredCommunication = builder.structuredCommunication;
      carId         = builder.carId;
      paymentIds    = builder.paymentIds;
      paidAmount    = builder.paidAmount;
  }

  public int getId() { return invoiceId; }
  public int getInvoiceId() { return invoiceId; }
  public String getNumber() { return number; }
  public LocalDate getDate() { return date; }
  public LocalDate getPaymentDate() { return paymentDate; }
  public LocalDate getDueDate() { return dueDate; }
  public int getUserId() { return userId; }
  public int getBillingId() { return billingId; }
  public float getAmount() { return amount; }
  public String getComment() { return comment; }
  public InvoiceStatus getStatus() { return status; }
  public InvoiceType getType() { return type; }
  public String getStructuredCommunication() { return structuredCommunication; }
  public int getCarId() { return carId; }
  public List<Integer> getPaymentIds() { return paymentIds; }
  public float getPaidAmount() { return paidAmount; }

  //returns true is invoice starts with 'E' -> owner invoice
  public boolean isOwnerInvoice() {
      return getNumber().substring(0, 1).equals("E");
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("number:")
      .append(number)
      .append(", date:")
      .append(date)
      .append(", paymentdate:")
      .append(paymentDate)
      .append(", duedate:")
      .append(dueDate)
      .append(", status:")
      .append(status)
      .append(", type:")
      .append(type)
      .append(", bedrag:")
      .append(amount)
      .append(", userid:")
      .append(userId)
      .append(", paymentIds:")
      .append(paymentIds)
      .append(", paidAmount:")
      .append(paidAmount);
    return sb.toString();
  }
}
