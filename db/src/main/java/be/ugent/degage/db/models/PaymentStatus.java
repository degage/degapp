
package be.ugent.degage.db.models;

public enum PaymentStatus {
    OK("OK"),
    CHANGE("Te bekijken"),
    UNASSIGNED("Geen factuur");

    private String description;

    private PaymentStatus(String description) {
        this.description = description;
    }

    public String toString(){
        return description;
    }

    //used for translating Dutch
    public static String translate(String s) {
        String tmp = s.toUpperCase();

        if (tmp == null || tmp == "") {
            return "";
        }

        if ("OK".contains(tmp)) {
            return "OK";
        } else if ("TE BEKIJKEN".contains(tmp)) {
            return "CHANGE";
        } else if ("GEEN FACTUUR".contains(tmp)) {
            return "UNASSIGNED";
        } else {
            return "";
        }
    }

}
