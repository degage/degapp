package be.ugent.degage.db.models;

import java.io.*;

public enum InvoiceStatus {
    OPEN("Open"),
    PAID("Betaald"),
    OVERDUE("Te laat");

    private String description;

    private InvoiceStatus(String description) {
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

        if ("OPEN".contains(tmp)) {
            return "OPEN";
        } else if ("BETAALD".contains(tmp)) {
            return "PAID";
        } else if ("TE LAAT".contains(tmp)) {
            return "OVERDUE";
        } else {
            return "";
        }
    }
}
