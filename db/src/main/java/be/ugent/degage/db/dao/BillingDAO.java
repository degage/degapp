package be.ugent.degage.db.dao;

import be.ugent.degage.db.models.*;

/**
 * Data access object related to billing.
 */
public interface BillingDAO {

    /**
     * Return a list of all billings
     */
    public Iterable<Billing> listAllBillings ();

    /**
     * Return a list of billings relevant to a given user
     */
    public Iterable<Billing> listBillingsForUser (int userId);

    /**
     * Retreive the billing with the given id
     */
    public Billing getBilling (int id);

    /**
     * Retreive the user information for the given billng and user
     */
    public BillingDetailsUser getUserDetails (int billingId, int userId);

    /**
     * Retreive price information for the given billing
     */
    // NOT USED ? public Iterable<KmPrice> listKmPrices (int billingId);

    public KmPriceDetails getKmPriceDetails (int billingId);

    /**
     * Retreive all trip billing details for a given billing and user (except those with a zero or negative nr of kms)
     */
    public Iterable<BillingDetailsTrip> listTripDetails (int billingId, int userId, boolean privileged);

    /**
     * Retreive all fuel billing details for a given billing and user
     */
    public Iterable<BillingDetailsFuel> listFuelDetails (int billingId, int userId, boolean privileged);

    /*
     * Retreive all trip and billing details for the owner and privileged users of a given car
     */
    public Iterable<BillingDetailsOwner> listOwnerDetails (int billingId, int carId);

}
