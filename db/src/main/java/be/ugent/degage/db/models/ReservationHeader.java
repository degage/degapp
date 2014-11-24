/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

import org.joda.time.DateTime;

/**
 * Contains the most essential information for reservations.
 * @see be.ugent.degage.db.models.Reservation
 */
public class ReservationHeader {

    private int id;
    private ReservationStatus status;
    private int carId;
    private int userId;
    private DateTime from;
    private DateTime to;
    private String message;
    private boolean privileged;

    public ReservationHeader(int id, int carId, int userId, DateTime from, DateTime to, String message) {
        this.id = id;
        this.carId = carId;
        this.userId = userId;
        this.from = from;
        this.to = to;
        this.message = message;

    }    
    
    public int getId() {
        return id;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public int getCarId() {
        return carId;
    }

    public int getUserId() {
        return userId;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public void setStatus(ReservationStatus status) {
            this.status = status;
        }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }
}
