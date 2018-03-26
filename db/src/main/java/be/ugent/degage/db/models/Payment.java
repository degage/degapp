
package be.ugent.degage.db.models;

import com.google.gson.annotations.Expose;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import  java.util.ArrayList;


public class Payment {
  @Expose
  private final int paymentId;
  @Expose
  private final int number;
  @Expose
  private final LocalDate date;
  @Expose
  private final String accountNumber;
  @Expose
  private final Integer userId;
  @Expose
  private final String name;
  @Expose
  private final String address;
  @Expose
  private final String bank;
  @Expose
  private final float amount;
  @Expose
  private final String comment;
  @Expose
  private final String structuredCommunication;
  @Expose
  private final PaymentStatus status;
  @Expose
  private final PaymentDebitType debitType;
  @Expose
  private final String currency;
  @Expose
  private final String filename;
  @Expose
  private final boolean includeInBalance;
  @Expose
  private final List<String> invoiceNumbers;
  private int currentHash;
  private int previousHash;
  private int nextHash;

  public static class Builder {
      // Required parameters
      private final int number;
      private final LocalDate date;
      private final String accountNumber;

      // Optional parameters - initialized to default values
      private int paymentId;
      private Integer userId;
      private String name = null;
      private String address = null;
      private String bank = null;
      private float amount = 0;
      private String comment = null;
      private String structuredCommunication = null;
      private PaymentStatus status;
      private PaymentDebitType debitType;
      private String currency = "EUR";
      private String filename = null;
      private List<String> invoiceNumbers = new ArrayList<String>();
      private boolean includeInBalance = true;
      private int currentHash = -1;
      private int previousHash = -1;
      private int nextHash = -1;

      public Builder(int number, LocalDate date, String accountNumber) {
          this.number = number;
          this.date = date;
          this.accountNumber = accountNumber;
      }

      public Builder(Payment payment) {
          this.paymentId = payment.paymentId;
          this.number = payment.number;
          this.date  = payment.date;
          this.accountNumber = payment.accountNumber;
          this.userId = payment.userId;
          this.name = payment.name;
          this.address = payment.address;
          this.bank = payment.bank;
          this.amount = payment.amount;
          this.comment = payment.comment;
          this.structuredCommunication = payment.structuredCommunication;
          this.status = payment.status;
          this.debitType = payment.debitType;
          this.currency = payment.currency;
          this.filename = payment.filename;
          this.invoiceNumbers = payment.invoiceNumbers;
          this.includeInBalance = payment.includeInBalance;
          this.currentHash = payment.currentHash;
          this.previousHash = payment.previousHash;
          this.nextHash = payment.nextHash;
      }

      public Builder id(int val)
          { paymentId = val; return this; }
      public Builder userId(Integer val)
          { userId = val; return this; }
      public Builder name(String val)
          { name = val; return this; }
      public Builder address(String val)
          { address = val; return this; }
      public Builder bank(String val)
          { bank = val; return this; }
      public Builder amount(float val)
          { amount = val; return this; }
      public Builder comment(String val)
          { comment = val; return this; }
      public Builder structuredCommunication(String val)
          { structuredCommunication = val; return this; }
      public Builder status(PaymentStatus val)
          { status = val; return this; }
      public Builder currency(String val)
          { currency = val; return this; }
      public Builder filename(String val)
          { filename = val; return this; }
      public Builder invoiceNumbers(List<String> val)
          { invoiceNumbers = val; return this; }
      public Builder currentHash(int val)
          { currentHash = val; return this; }
      public Builder previousHash(int val)
          { previousHash = val; return this; }
      public Builder nextHash(int val)
          { nextHash = val; return this; }
      public Builder debitType(PaymentDebitType val)
          { debitType = val; return this; }
      public Builder includeInBalance(boolean val)
          { includeInBalance = val; return this; }

      public int hashResult() {
        ZoneId zoneId = ZoneId.of("Europe/Oslo");
        int result = 13;
        result = 31 * result + (date != null ? (int)(date.atStartOfDay(zoneId).toEpochSecond()) : 0);
        result = 31 * result + (accountNumber != null ? accountNumber.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (bank != null ? bank.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (structuredCommunication!= null ? structuredCommunication.hashCode() : 0);
        result = 31 * result + (debitType != null ? debitType.toString().hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (amount != +0.0f ? Float.floatToIntBits(amount) : 0);

        return result;
      }


      public Payment build() {
          this.currentHash = this.hashResult();
          return new Payment(this);
      }
  }

  private Payment(Builder builder) {
      paymentId               = builder.paymentId;
      number                  = builder.number;
      date                    = builder.date;
      accountNumber           = builder.accountNumber;
      userId                  = builder.userId;
      name                    = builder.name;
      address                 = builder.address;
      bank                    = builder.bank;
      amount                  = builder.amount;
      comment                 = builder.comment;
      structuredCommunication = builder.structuredCommunication;
      status                  = builder.status;
      debitType               = builder.debitType;
      currency                = builder.currency;
      filename                = builder.filename;
      invoiceNumbers          = builder.invoiceNumbers;
      includeInBalance        = builder.includeInBalance;
      currentHash             = builder.currentHash;
      previousHash            = builder.previousHash;
      nextHash                = builder.nextHash;
  }

  public int getId() { return paymentId; }
  public int getNumber() { return number; }
  public LocalDate getDate() { return date; }
  public String getAccountNumber() { return accountNumber; }
  public int getUserId() { return userId; }
  public String getName() { return name; }
  public String getAddress() { return address; }
  public String getBank() { return bank; }
  public float getAmount() { return amount; }
  public String getComment() { return comment; }
  public String getStructuredCommunication() { return structuredCommunication; }
  public PaymentStatus getStatus() { return status; }
  public PaymentDebitType getDebitType() { return debitType; }
  public String getCurrency() { return currency; }
  public String getFilename() { return filename; }
  public List<String> getInvoiceNumbers() { return invoiceNumbers; }
  public boolean getIncludeInBalance() { return includeInBalance; }
  public int getCurrentHash() { return currentHash; }
  public int getPreviousHash() { return previousHash; }
  public int getNextHash() { return nextHash; }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("PAYMENT: ")
      .append("id:")
      .append(paymentId)
      .append(", number:")
      .append(number)
      .append(", userID:")
      .append(userId)
      .append(", date:")
      .append(date)
      .append(", rek nr:")
      .append(accountNumber)
      .append(", name:")
      .append(name)
      .append(", adres:")
      .append(address)
      .append(", bank:")
      .append(bank)
      .append(", bedrag:")
      .append(amount)
      .append(", comment:")
      .append(comment)
      .append(", struct comm:")
      .append(structuredCommunication)
      .append(", status:")
      .append(status)
      .append(", debitType:")
      .append(debitType)
      .append(", currency:")
      .append(currency)
      .append(", filename:")
      .append(filename)
      .append(", invoiceNumbers:")
      .append(invoiceNumbers)
      .append(", currentHash:")
      .append(currentHash)
      .append(", previousHash:")
      .append(previousHash)
      .append(", nextHash:")
      .append(nextHash);
    return sb.toString();
  }

  public boolean equals(Object object) {
      if (this == object) return true;
      if (object == null || getClass() != object.getClass()) return false;
      if (!super.equals(object)) return false;

      Payment payment = (Payment) object;

      if (paymentId != payment.paymentId) return false;
      if (getNumber() != payment.getNumber()) return false;
      if (getUserId() != payment.getUserId()) return false;
      if (Float.compare(payment.getAmount(), getAmount()) != 0) return false;
      if (getDate() != null ? !getDate().equals(payment.getDate()) : payment.getDate() != null) return false;
      if (getAccountNumber() != null ? !getAccountNumber().equals(payment.getAccountNumber()) : payment.getAccountNumber() != null)
          return false;
      if (getName() != null ? !getName().equals(payment.getName()) : payment.getName() != null) return false;
      if (getAddress() != null ? !getAddress().equals(payment.getAddress()) : payment.getAddress() != null)
          return false;
      if (getBank() != null ? !getBank().equals(payment.getBank()) : payment.getBank() != null) return false;
      if (getComment() != null ? !getComment().equals(payment.getComment()) : payment.getComment() != null)
          return false;
      if (getStructuredCommunication() != null ? !getStructuredCommunication().equals(payment.getStructuredCommunication()) : payment.getStructuredCommunication() != null)
          return false;
      if (getStatus() != null ? !getStatus().equals(payment.getStatus()) : payment.getStatus() != null) return false;
      if (getDebitType() != null ? !getDebitType().equals(payment.getDebitType()) : payment.getDebitType() != null) return false;
      if (getCurrency() != null ? !getCurrency().equals(payment.getCurrency()) : payment.getCurrency() != null)
          return false;
      if (getFilename() != null ? !getFilename().equals(payment.getFilename()) : payment.getFilename() != null)
          return false;
      if (getCurrentHash() != payment.getCurrentHash()) return false;
      if (getPreviousHash() != payment.getPreviousHash()) return false;
      if (getNextHash() != payment.getNextHash()) return false;

      return true;
  }

  public int hashCode() {
      int result = 13;
      result = 31 * result + paymentId;
      result = 31 * result + getNumber();
      result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
      result = 31 * result + (getAccountNumber() != null ? getAccountNumber().hashCode() : 0);
      result = 31 * result + getUserId();
      result = 31 * result + (getName() != null ? getName().hashCode() : 0);
      result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
      result = 31 * result + (getBank() != null ? getBank().hashCode() : 0);
      result = 31 * result + (getAmount() != +0.0f ? Float.floatToIntBits(getAmount()) : 0);
      result = 31 * result + (getComment() != null ? getComment().hashCode() : 0);
      result = 31 * result + (getStructuredCommunication() != null ? getStructuredCommunication().hashCode() : 0);
      result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
      result = 31 * result + (getDebitType() != null ? getDebitType().hashCode() : 0);
      result = 31 * result + (getCurrency() != null ? getCurrency().hashCode() : 0);
      result = 31 * result + (getFilename() != null ? getFilename().hashCode() : 0);
      return result;
  }

}
