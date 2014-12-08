/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;


import java.time.LocalDateTime;

/**
 * Contains the most essential information for reservations.
 * @see be.ugent.degage.db.models.Reservation
 */
public class ReservationHeader {

    private int id;
    private ReservationStatus status;
    private int carId;
    private int userId;
    private LocalDateTime from;
    private LocalDateTime until;
    private String message;
    private boolean privileged;

    public ReservationHeader(int id, int carId, int userId, LocalDateTime from, LocalDateTime until, String message) {
        this.id = id;
        this.carId = carId;
        this.userId = userId;
        this.from = from;
        this.until = until;
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

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getUntil() {
        return until;
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
