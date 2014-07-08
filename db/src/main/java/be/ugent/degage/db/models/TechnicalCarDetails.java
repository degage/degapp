package be.ugent.degage.db.models;

/**
 * Created by HannesM on 23/04/14.
 */
public class TechnicalCarDetails {
    private Integer id = null;
    private String licensePlate;
    private File registration;
    private String chassisNumber;

    public TechnicalCarDetails(String licensePlate, File registration, String chassisNumber) {
        this(null, licensePlate, registration, chassisNumber);
    }

    public TechnicalCarDetails(Integer id, String licensePlate, File registration, String chassisNumber) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.registration = registration;
        this.chassisNumber = chassisNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public File getRegistration() {
        return registration;
    }

    public void setRegistration(File registration) {
        this.registration = registration;
    }

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }
}
