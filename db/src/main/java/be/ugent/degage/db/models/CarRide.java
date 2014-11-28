package be.ugent.degage.db.models;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Created by HannesM on 10/03/14.
 */
public class CarRide {
    private Reservation reservation; // reservation.getId() is CarRide-id
    private boolean approvedByOwner;
    private int startKm;   // 0  when not filled in
    private int endKm;
    private boolean damaged;
    private BigDecimal cost;
    private Date billed;

    private int numberOfRefuels;

    public CarRide(Reservation reservation, int startKm, int endKm, boolean approvedByOwner, boolean damaged, int numberOfRefuels) {
        this.reservation = reservation;
        this.startKm = startKm;
        this.endKm = endKm;
        this.approvedByOwner = approvedByOwner;
        this.damaged = damaged;
        this.numberOfRefuels = numberOfRefuels;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public int getStartKm() {
        return startKm;
    }

    public void setStartKm(int startKm) {
        this.startKm = startKm;
    }

    public int getEndKm() {
        return endKm;
    }

    public void setEndKm(int endKm) {
        this.endKm = endKm;
    }

    public boolean isApprovedByOwner() {
        return approvedByOwner;
    }

    public void setApprovedByOwner(boolean approvedByOwner) {
        this.approvedByOwner = approvedByOwner;
    }

    public int getNumberOfRefuels() {
        return numberOfRefuels;
    }

    public void setNumberOfRefuels(int numberOfRefuels) {
        this.numberOfRefuels = numberOfRefuels;
    }

    public boolean isDamaged() {
        return damaged;
    }

    public void setDamaged(boolean damaged) {
        this.damaged = damaged;
    }

    public BigDecimal getCost() { return cost; }

    public void setCost(BigDecimal cost) { this.cost = cost; }

    public Date getBilled() { return billed; }

    public void setBilled(Date billed) { this.billed = billed; }
}
