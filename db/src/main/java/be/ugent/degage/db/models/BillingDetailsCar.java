package be.ugent.degage.db.models;

/**
 * Contains the (general) billing details for a single car.
 */
public class BillingDetailsCar {

    private int firstKm; // mileage of first km in this period

    private int lastKm; // mileage of last lm in this period

    private int ownerKm; // total number of kms driven by owner (or privileged person)

    private int deprecationKm; // kms to be taken into account for the deprecation cost

    private int totalFuelCost;

    private int ownerFuelPaid;

    private int deprecationFactor; // in eurocent per 10 km

    private int recuperatedDeprecationCost;

    private int ownerFuelDue;

    private int index;

    public BillingDetailsCar(int firstKm, int lastKm, int ownerKm, int deprecationKm,
                             int totalFuelCost, int ownerFuelPaid, int deprecationFactor,
                             int recuperatedDeprecationCost, int ownerFuelDue, int index) {
        this.firstKm = firstKm;
        this.lastKm = lastKm;
        this.ownerKm = ownerKm;
        this.deprecationKm = deprecationKm;
        this.totalFuelCost = totalFuelCost;
        this.ownerFuelPaid = ownerFuelPaid;
        this.deprecationFactor = deprecationFactor;
        this.recuperatedDeprecationCost = recuperatedDeprecationCost;
        this.ownerFuelDue = ownerFuelDue;
        this.index = index;
    }

    public int getFirstKm() {
        return firstKm;
    }

    public int getLastKm() {
        return lastKm;
    }

    /**
     * Total number of kms for this period
     */
    public int getKm() {
        return lastKm - firstKm;
    }

    public int getOwnerKm() {
        return ownerKm;
    }

    public int getDeprecationKm() {
        return deprecationKm;
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

    public int getDeprecationFactor() {
        return deprecationFactor;
    }

    public int getTotalDeprecationCost() {
        return deprecationFactor*deprecationKm/10;
    }

    public int getRecuperatedDeprecationCost() {
        return recuperatedDeprecationCost;
    }

    public int getOwnerFuelDue() {
        return ownerFuelDue;
    }

    public int getIndex() {
        return index;
    }
}
