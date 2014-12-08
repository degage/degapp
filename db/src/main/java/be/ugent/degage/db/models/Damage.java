package be.ugent.degage.db.models;

import java.time.LocalDate;

/**
 * Registers damage that occurred during a car ride
 */
public class Damage {

    private Integer id;

    private int carId;

    private int driverId;

    private String description;
    private LocalDate date;
    private boolean finished;

    private String carName; // not always filled in
    private String driverName; // not always filled in

    private Reservation reservation; // only partially filled in

    public Damage(Integer id, int carId, int driverId, Reservation reservation,
                  String description, LocalDate date, boolean finished) {
        this.id = id;
        this.carId = carId;
        this.driverId = driverId;
        this.description = description;
        this.date = date;
        this.finished = finished;
        this.reservation = reservation;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCarId() {
        return carId;
    }

    public int getDriverId() {
        return driverId;
    }

    public int getReservationId() {
        return reservation.getId();
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }


    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean getFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
