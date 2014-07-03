/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

/**
 *
 * @author Laurent
 */
public class Location {
    
    private int zip;
    private String location;

    public Location(int zip, String location) {
        this.zip = zip;
        this.location = location;
    }
    
    public int getZip() {
        return zip;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    
}
