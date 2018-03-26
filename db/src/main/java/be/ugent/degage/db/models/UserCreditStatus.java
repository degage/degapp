package be.ugent.degage.db.models;

public enum UserCreditStatus {
    REGULAR("Normaal"),
    PAYMENT_PLAN("Betalingsplan");

    // Enum definition
    private String description;

    private UserCreditStatus(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
