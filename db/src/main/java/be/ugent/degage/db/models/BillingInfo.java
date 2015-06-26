package be.ugent.degage.db.models;

import java.time.LocalDate;

/**
 * Contains information about which kind of billings are available for a given user, including
 * billings for the cars of which his is owner
 */
public class BillingInfo {

    private int id;
    private String description;
    private LocalDate start;
    private LocalDate limit;
    private BillingStatus status;

    private int carId; // 0 for a users billing, otherwise car id

    private String carName; // null for users billing

    public BillingInfo(int id, String description, LocalDate start, LocalDate limit, BillingStatus status, int carId, String carName) {
        this.id = id;
        this.description = description;
        this.start = start;
        this.limit = limit;
        this.status = status;
        this.carId = carId;
        this.carName = carName;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getLimit() {
        return limit;
    }

    public BillingStatus getStatus() {
        return status;
    }

    public int getCarId() {
        return carId;
    }

    public String getCarName() {
        return carName;
    }
}
