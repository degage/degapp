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
    private FileGroup fileGroup;

    public IdentityCard() {
    }

    public IdentityCard(String id, String registrationNr, FileGroup fileGroup) {
        this.id = id;
        this.registrationNr = registrationNr;
        this.fileGroup = fileGroup;
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

    public FileGroup getFileGroup() {
        return fileGroup;
    }

    public void setFileGroup(FileGroup fileGroup) {
        this.fileGroup = fileGroup;
    }


    
}
