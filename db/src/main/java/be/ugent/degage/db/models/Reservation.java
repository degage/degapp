package be.ugent.degage.db.models;

import java.time.LocalDateTime;

/**
 *
 */
public class Reservation {
    
    private int id;
    private ReservationStatus status;
    private Car car;
    private UserHeader user;
    private LocalDateTime from;
    private LocalDateTime until;
    private String message;
    private boolean privileged;

    public Reservation(int id, Car car, UserHeader user, LocalDateTime from, LocalDateTime until, String message) {
        this.id = id;
        this.car = car;
        this.user = user;
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

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Car getCar() {
        return car;
    }

    public UserHeader getUser() {
        return user;
    }

    public void setUser(UserHeader user) {
        this.user = user;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getUntil() {
        return until;
    }

    public void setUntil(LocalDateTime until) {
        this.until = until;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }
}
