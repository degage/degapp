package be.ugent.degage.db.models;

import java.util.List;

/**
 * Information needed for cost calculations
 */
public class Costs {

    private double[] amounts;

    private int[] limits;

    private double deprecation;

    public Costs (String deprecationString, List<String> amountStrings, List<String> limitStrings) {
        this.deprecation = Double.parseDouble(deprecationString);
        amounts = new double[amountStrings.size()];
        limits = new int[amounts.length - 1];

        for (int i = 0; i < amounts.length; i++) {
            amounts[i] = Double.parseDouble(amountStrings.get(i));
        }
        for (int i = 0; i < limits.length; i++) {
            limits[i] = Integer.parseInt(limitStrings.get(i));
        }
    }

    public int getLevels () {
        return amounts.length;
    }

    public double getCost(int level) {
        return amounts[level];
    }

    public double getDeprecation () {
        return deprecation;
    }

    public int getLimit(int level) {
        return limits[level];
    }


}
