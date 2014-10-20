package be.ugent.degage.db.models;

/**
 * Transfer object for technical car details for a certain car.
 *
 * Created by HannesM on 23/04/14.
 */
public class TechnicalCarDetails {
    private String licensePlate;
    private File registration;
    private String chassisNumber;

    public TechnicalCarDetails(String licensePlate, File registration, String chassisNumber) {
        this.licensePlate = licensePlate;
        this.registration = registration;
        this.chassisNumber = chassisNumber;
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
