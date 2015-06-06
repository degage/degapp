package be.ugent.degage.db.models;

import java.time.LocalDateTime;

/**
 * Created by kc on 6/6/15.
 */
public class BillingDetails {
    private int reservationId;
    private String carName;
    private int cost; // total cost
    private LocalDateTime time;

    public BillingDetails(String carName, int cost, LocalDateTime time, int reservationId) {
        this.carName = carName;
        this.cost = cost;
        this.time = time;
        this.reservationId = reservationId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public String getCarName() {
        return carName;
    }

    public int getCost() {
        return cost;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
