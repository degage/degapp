package controllers.util;

import be.ugent.degage.db.models.*;

public class InvoicePickerLine extends PickerLine {

    private float amount;
    private UserHeader user;

    public InvoicePickerLine(String value, String search, int id, float amount, UserHeader user) {
        super(value, search, id);
        this.amount = amount;
        this.user = user;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getStrong() {
        return strong;
    }

    public int getId() {
        return id;
    }

    public float getAmount() {
        return amount;
    }

    public UserHeader getUser() {
        return user;
    }
}
