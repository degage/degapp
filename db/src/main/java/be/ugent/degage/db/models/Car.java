/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

/**
 * Represents information about a certain car.
 */
public class Car {
    
    private int id;
    private String name;
    private String brand;
    private String type;
    private Address location;
    private Integer seats;
    private Integer doors;
    private Integer year;
    private boolean manual;
    private boolean gps;
    private boolean hook;
    private CarFuel fuel;
    private Integer fuelEconomy;
    private Integer estimatedValue;
    private Integer ownerAnnualKm;
    private TechnicalCarDetails technicalCarDetails;
    private CarInsurance insurance;
    private UserHeader owner;
    private String comments;
    private boolean active;
    private File photo;

    public Car(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Car(int id, String name, String brand, String type, Address location, Integer seats, Integer doors, Integer year, boolean manual, boolean gps, boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm, TechnicalCarDetails technicalCarDetails, CarInsurance insurance, UserHeader owner, String comments) {
        this(id, name);
        this.brand = brand;
        this.type = type;
        this.location = location;
        this.seats = seats;
        this.doors = doors;
        this.year = year;
        this.manual = manual;
        this.gps = gps;
        this.hook = hook;
        this.fuel = fuel;
        this.fuelEconomy = fuelEconomy;
        this.estimatedValue = estimatedValue;
        this.ownerAnnualKm = ownerAnnualKm;
        this.technicalCarDetails = technicalCarDetails;
        this.insurance = insurance;
        this.owner = owner;
        this.comments = comments;
    }
   
    public int getId() {
        return id;
    }
    
    public void setId(int id){
        this.id=id;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Address getLocation() {
        return location;
    }

    public void setLocation(Address location) {
        this.location = location;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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

    public Integer getFuelEconomy() {
        return fuelEconomy;
    }

    public void setFuelEconomy(Integer fuelEconomy) {
        this.fuelEconomy = fuelEconomy;
    }

    public Integer getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(Integer estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public Integer getOwnerAnnualKm() {
        return ownerAnnualKm;
    }

    public void setOwnerAnnualKm(Integer ownerAnnualKm) {
        this.ownerAnnualKm = ownerAnnualKm;
    }

    public TechnicalCarDetails getTechnicalCarDetails() {
        return technicalCarDetails;
    }

    public void setTechnicalCarDetails(TechnicalCarDetails technicalCarDetails) {
        this.technicalCarDetails = technicalCarDetails;
    }

    public CarInsurance getInsurance() {
        return insurance;
    }

    public void setInsurance(CarInsurance insurance) {
        this.insurance = insurance;
    }

    public UserHeader getOwner() {
        return owner;
    }

    public void setOwner(UserHeader owner) {
        this.owner = owner;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public File getPhoto() {
        return photo;
    }

    public void setPhoto(File photo) {
        this.photo = photo;
    }

}
