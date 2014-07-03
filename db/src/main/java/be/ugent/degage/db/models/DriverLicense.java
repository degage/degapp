/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

/**
 *
 * @author Laurent
 */
public class DriverLicense {
    
    private String id;
    private FileGroup fileGroup;

    public DriverLicense() {
    }

    public DriverLicense(String id, FileGroup fileGroup) {
        this.id = id;
        this.fileGroup = fileGroup;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FileGroup getFileGroup() {
        return fileGroup;
    }

    public void setFileGroup(FileGroup fileGroup) {
        this.fileGroup = fileGroup;
    }
}
