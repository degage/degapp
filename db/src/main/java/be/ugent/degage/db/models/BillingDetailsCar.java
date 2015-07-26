/* BillingDetailsCar.java
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

package be.ugent.degage.db.models;

/**
 * Contains the (general) billing details for a single car.
 */
public class BillingDetailsCar {

    private int firstKm; // mileage of first km in this period

    private int lastKm; // mileage of last km in this period

    private int totalKm; // total number of kms driven. Need not be the difference between lastKm - firstKm as
                         // some of the trips may be/have been handled in a different period

    private int ownerKm; // total number of kms driven by owner (or privileged person)

    private int depreciationKm; // kms to be taken into account for the depreciation cost

    private int totalFuelCost;

    private int ownerFuelPaid;

    private int depreciationFactor; // in eurocent per 10 km

    private int recuperatedDepreciationCost;

    private int ownerFuelDue;

    private int index;

    private int carCosts;

    private int recuperatedCarCosts;

    public BillingDetailsCar(int firstKm, int lastKm, int totalKm, int ownerKm, int depreciationKm,
                             int totalFuelCost, int ownerFuelPaid, int depreciationFactor,
                             int recuperatedDepreciationCost, int ownerFuelDue, int index,
                             int carCosts, int recuperatedCarCosts) {
        this.firstKm = firstKm;
        this.lastKm = lastKm;
        this.totalKm = totalKm;
        this.ownerKm = ownerKm;
        this.depreciationKm = depreciationKm;
        this.totalFuelCost = totalFuelCost;
        this.ownerFuelPaid = ownerFuelPaid;
        this.depreciationFactor = depreciationFactor;
        this.recuperatedDepreciationCost = recuperatedDepreciationCost;
        this.ownerFuelDue = ownerFuelDue;
        this.index = index;
        this.carCosts = carCosts;
        this.recuperatedCarCosts = recuperatedCarCosts;
    }

    // TODO: no longer used?
    public int getFirstKm() {
        return firstKm;
    }

    public int getLastKm() {
        return lastKm;
    }

    public int getTotalKm() {
        return totalKm;
    }

    public int getOwnerKm() {
        return ownerKm;
    }

    public int getDepreciationKm() {
        return depreciationKm;
    }

    public int getTotalFuelCost() {
        return totalFuelCost;
    }

    /**
     * Return the cost of fuel for 10 km
     */
    public int getFuelCostFactor() {
        if (lastKm == firstKm) {
            return 0;
        } else {
            return totalFuelCost*10/(lastKm-firstKm);
        }
    }

    public int getOwnerFuelPaid() {
        return ownerFuelPaid;
    }

    public int getDepreciationFactor() {
        return depreciationFactor;
    }

    public int getTotalDepreciationCost() {
        return depreciationFactor*depreciationKm/10;
    }

    public int getRecuperatedDepreciationCost() {
        return recuperatedDepreciationCost;
    }

    public int getOwnerFuelDue() {
        return ownerFuelDue;
    }

    public int getIndex() {
        return index;
    }

    public int getCarCosts() {
        return carCosts;
    }

    public int getRecuperatedCarCosts() {
        return recuperatedCarCosts;
    }
}
