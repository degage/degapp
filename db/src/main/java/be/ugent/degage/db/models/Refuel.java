package be.ugent.degage.db.models;

import java.sql.Date;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class Refuel {
    private int id;
    private CarRide carRide;
    private File proof;
    private int eurocents;
    private RefuelStatus status;
    private Date billed;

    public Refuel(int id, CarRide carRide, File proof, int eurocents, RefuelStatus status) {
        this.id = id;
        this.carRide = carRide;
        this.proof = proof;
        this.eurocents = eurocents;
        this.status = status;
    }

    public Refuel(int id, CarRide carRide, RefuelStatus status) {
        this.id = id;
        this.carRide = carRide;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CarRide getCarRide() {
        return carRide;
    }

    public void setCarRide(CarRide carRide) {
        this.carRide = carRide;
    }

    public File getProof() {
        return proof;
    }

    public void setProof(File proof) {
        this.proof = proof;
    }

    public int getEurocents() {
        return eurocents;
    }

    public void setEurocents(int eurocents) {
        this.eurocents = eurocents;
    }

    public RefuelStatus getStatus() {
        return status;
    }

    public void setStatus(RefuelStatus status) {
        this.status = status;
    }

    public Date getBilled() { return billed; }

    public void setBilled(Date billed) { this.billed = billed; }
}
