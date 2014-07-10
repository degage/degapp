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
    private Integer fileGroupId;

    public DriverLicense() {
    }

    public DriverLicense(String id, Integer fileGroupId) {
        this.id = id;
        this.fileGroupId = fileGroupId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getFileGroupId() {
        return fileGroupId;
    }

    public void setFileGroupId(Integer fileGroupId) {
        this.fileGroupId = fileGroupId;
    }
}
