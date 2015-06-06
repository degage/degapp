package be.ugent.degage.db.models;

/**
 * Gathers information about kilometer prices for a certain billing
 */
public class KmPriceDetails {

    private int[] froms;
    private int[] prices;

    public KmPriceDetails(int[] froms, int[] prices) {
        this.froms = froms;
        this.prices = prices;
    }

    public int[] getFroms() {
        return froms;
    }

    public int[] getPrices() {
        return prices;
    }
}
