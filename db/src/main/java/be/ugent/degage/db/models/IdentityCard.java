/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

/**
 *
 * @author Laurent
 */
public class IdentityCard {
    
    private String id; // Identiteitskaartnummer

    private String registrationNr; // Rijksregisternummer
    private Integer fileGroupId;

    public IdentityCard() {
    }

    public IdentityCard(String id, String registrationNr, int fileGroup) {
        this.id = id;
        this.registrationNr = registrationNr;
        this.fileGroupId = fileGroup;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public String getRegistrationNr() {
        return registrationNr;
    }

    public void setRegistrationNr(String registrationNr) {
        this.registrationNr = registrationNr;
    }

    public Integer getFileGroupId() {
        return fileGroupId;
    }

    public void setFileGroupId(Integer fileGroupId) {
        this.fileGroupId = fileGroupId;
    }


    
}
