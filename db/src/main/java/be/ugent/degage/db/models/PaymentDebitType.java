
package be.ugent.degage.db.models;

public enum PaymentDebitType {
    CREDIT("CREDIT"),
    DEBIT("DEBIT");

    private String description;

    private PaymentDebitType(String description) {
        this.description = description;
    }

    public String toString(){
        return description;
    }
}
