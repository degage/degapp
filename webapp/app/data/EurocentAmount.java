package data;

import java.util.regex.Pattern;

/**
 * Contains an amount of eurocents. Provides parsing and formatting methods
 */
public class EurocentAmount {

    private int value;

    /**
     * Return the amount of eurocents
     */
    public int getValue() {
        return value;
    }

    public EurocentAmount (int euro, int cent) {
        if (cent >= 0 && cent < 100) {
            if (euro >= 0)
                value = 100*euro + cent;
            else
                value = 100*euro - cent;
        } else {
            throw new IllegalArgumentException("Cent part must be in the range 0..99: " + cent);
        }
    }

    /**
     * Output in the form 15,20 0,03 ...
     */
    public String toString() {
        return toString(value);
    }

    public static String toString (int eurocents) {
        if (eurocents >= 0) {
            return String.format("%d,%02d", eurocents / 100, eurocents % 100);
        } else {

            return String.format("-%d,%02d", (-eurocents) / 100, (-eurocents) % 100);
        }
    }

    public static final Pattern PATTERN = Pattern.compile("-?[0-9]+[,.][0-9][0-9]");

    /**
     * Parse a string into an amount of eurocents. The string *must* contain a decimal point or comma followed by exactly two digits.
     * @throws java.lang.NumberFormatException
     */
    public static EurocentAmount parse (String str) {
        if (PATTERN.matcher(str).matches()) {
            int len = str.length();
            return new EurocentAmount(
               Integer.parseInt(str.substring(0, len-3)),
               Integer.parseInt(str.substring(len-2, len))
            );
        } else {
            throw new NumberFormatException("Incorrect format for EurocentAmount");
        }
    }
}
