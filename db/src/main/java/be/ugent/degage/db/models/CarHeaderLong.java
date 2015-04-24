package be.ugent.degage.db.models;

/**
 * Extends {@link CarHeader} with further information. Location and owner
 * can be left null.
 */
public class CarHeaderLong extends CarHeaderWithOwner {


    private Integer seats;
    private Integer doors;

    private boolean manual;
    private boolean gps;
    private boolean hook;
    private CarFuel fuel;


    /**
     * Create an object containing the given information.
     */
    public CarHeaderLong(int id, String name, String brand, String type, String email,
                         boolean active, Integer seats, Integer doors,
                         boolean manual, boolean gps, boolean hook, CarFuel fuel,
                         String comments) {
        super(id, name, brand, type, email, active);
        this.seats = seats;
        this.doors = doors;
        this.manual = manual;
        this.gps = gps;
        this.hook = hook;
        this.fuel = fuel;
        this.comments = comments;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public Integer getDoors() {
        return doors;
    }

    public void setDoors(Integer doors) {
        this.doors = doors;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public boolean isGps() {
        return gps;
    }

    public void setGps(boolean gps) {
        this.gps = gps;
    }

    public boolean isHook() {
        return hook;
    }

    public void setHook(boolean hook) {
        this.hook = hook;
    }

    public CarFuel getFuel() {
        return fuel;
    }

    public void setFuel(CarFuel fuel) {
        this.fuel = fuel;
    }

    private String comments;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
