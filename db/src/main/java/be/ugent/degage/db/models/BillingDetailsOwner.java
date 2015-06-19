package be.ugent.degage.db.models;

/**
 * Groups all trip and fuel billing details for an owner or privileged
 * of a car.
 */
public class BillingDetailsOwner {

    private String user;

    private Iterable<BillingDetailsTrip> trips;

    private Iterable<BillingDetailsFuel> refuels;

    public BillingDetailsOwner(String user, Iterable<BillingDetailsTrip> trips, Iterable<BillingDetailsFuel> refuels) {
        this.user = user;
        this.trips = trips;
        this.refuels = refuels;
    }

    public String getUser() {
        return user;
    }

    public Iterable<BillingDetailsTrip> getTrips() {
        return trips;
    }

    public Iterable<BillingDetailsFuel> getRefuels() {
        return refuels;
    }
}
