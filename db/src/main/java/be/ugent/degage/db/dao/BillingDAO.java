/* BillingDAO.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.dao;

import be.ugent.degage.db.models.*;

/**
 * Data access object related to billing (reports).
 */
public interface BillingDAO {

    /**
     * Return a list of all billings
     */
    public Iterable<Billing> listAllBillings ();

    /**
     * Return a list of billings relevant to a given user
     */
    public Iterable<BillingInfo> listBillingsForUser (int userId);

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

    /**
     * Retreive general billing details for a car
     */
    public BillingDetailsCar getCarDetails (int billingId, int carId);

}
