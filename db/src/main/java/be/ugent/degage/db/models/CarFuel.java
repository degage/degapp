/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

/**
 *
 * @author Laurent
 */
public enum CarFuel {
    PETROL("Benzine"),
    DIESEL("Diesel"),
    BIODIESEL("Biodiesel"),
    GAS("Gas"),
    HYBRID("Hybride"),
    ELECTRIC("Elektrisch");

    // Enum implementation
    private String description;

    private CarFuel(String description) {
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public static CarFuel getFuelFromString(String s) {
        for(CarFuel f : values()) {
            if(f.getDescription().equals(s)) {
                return f;
            }
        }
        return null;
    }
}
