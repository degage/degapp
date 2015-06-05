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
     * Retreive price information for the given billing, ordered by starting km
     */
    public Iterable<KmPrice> listKmPrices (int billingId);

    /**
     * Retreive all trip billing details for a given billing and user
     */
    public Iterable<BillingDetailsTrip> listTripDetails (int billingId, int userId, boolean privileged);

    /**
     * Retreive all trip billing details for a given billing and user
     */
    public Iterable<BillingDetailsFuel> listFuelDetails (int billingId, int userId, boolean privileged);



}
