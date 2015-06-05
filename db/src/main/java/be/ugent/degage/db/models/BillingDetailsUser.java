package be.ugent.degage.db.models;

/**
 * Contains user specific billing details
 */
public class BillingDetailsUser {

    private int userId;

    private int index;

    public int getUserId() {
        return userId;
    }

    public int getIndex() {
        return index;
    }

    public BillingDetailsUser(int userId, int index) {

        this.userId = userId;
        this.index = index;
    }
}
