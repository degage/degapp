package be.ugent.degage.db.models;

import org.joda.time.DateTime;

/**
 * Registers damage that occurred during a car ride
 */
public class Damage {
    private Integer id;
    private CarRide carRide;
    private String description;
    private DateTime time;
    private boolean finished;

    public Damage(Integer id, CarRide carRide) {
        this.id = id;
        this.carRide = carRide;
        time = carRide.getReservation().getFrom();
    }

    public Damage(Integer id, CarRide carRide, String description, DateTime time, boolean finished) {
        this.id = id;
        this.carRide = carRide;
        this.description = description;
        this.time = time;
        this.finished = finished;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CarRide getCarRide() {
        return carRide;
    }

    public void setCarRide(CarRide carRide) {
        this.carRide = carRide;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public boolean getFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
