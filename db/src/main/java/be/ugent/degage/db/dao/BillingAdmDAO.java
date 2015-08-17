/* BillingAdmDAO.java
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

import be.ugent.degage.db.models.KmPrice;

import java.time.LocalDate;
import java.util.List;

/**
 * Data access object for billing administration
 */
public interface BillingAdmDAO {

    /**
     * Compute a simulation for the given billing
     */
    public void computeSimulation(int billingId);

    /**
     * Compute the (final) user invoices for this billing
     */
    public void computeUserInvoices(int billingId);

    /**
     * Compute the car invoices for this billing
     */
    public void computeCarInvoices(int billingId);

    /**
     * Finalize a billing period. Puts all related trips, costs and refuels into
     * archive mode.
     */
    public void archive(int billingId);

    /**
     * Create a new billing.
     */
    public void createBilling(String description, String prefix, LocalDate start, LocalDate limit);

    public static class CarInfo {
        public int carId;
        public String carName;
        public boolean included; // whether or not already included in cars_billed
        public boolean nodata; // true if there is nothing to be billed for this car
        public boolean incomplete; // true if the depreciation information for that car is not complete
    }

    /**
     * Return information about cars that are eligible for billing
     */
    public List<CarInfo> listCarBillingInfo(int billingId);

    /**
     * Update the cars billed table. Clears all values for the given billing and includes the cars
     * whose ids are given in the list.
     */
    public void updateCarsBilled(int billingId, Iterable<Integer> carsToInclude);

    /**
     * Update the status of the given billing from CREATED to PREPARING
     */
    public void updateToPreparing(int billingId);


    /**
     * Update the pricing for the given billing. Replaces all existing pricing information
     * for this billing.
     */
    public void updatePricing(int billingId, Iterable<KmPrice> pricing);


    public static class CarBillingInfo {
        public int carId;
        public String carName;
        // amounts are in EUROCENT
        public int fuel;
        public int deprec;
        public int costs;
        public int total;
        public String structuredComment;
    }

    /**
     * Return information about car invoices per car
     */
    public Iterable<CarBillingInfo> listCarBillingOverview (int billingId);

    public static class UserBillingInfo {
        public int userId;
        public String userName;
        // amounts are in EUROCENT
        public int km;
        public int fuel;
        public int total;
        public String structuredComment;
    }

    /**
     * Return information about user invoices, per user
     */
    public Iterable<UserBillingInfo> listUserBillingOverview (int billingId);


}
