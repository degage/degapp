/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

import org.joda.time.LocalTime;

import java.util.List;

/**
 *
 * @author Laurent
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
    private User owner;
    private String comments;
    private boolean active;
    private File photo;

    public Car() {
        this(0, null, null, null, null, null, null, null, false, false, false, null, null, null, null, null, null, null, null);
    }

    public Car(int id, String name, String brand, String type, Address location, Integer seats, Integer doors, Integer year, boolean manual, boolean gps, boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm, TechnicalCarDetails technicalCarDetails, CarInsurance insurance, User owner, String comments) {
        this.id = id;
        this.name = name;
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Car car = (Car) o;

        if (gps != car.gps) return false;
        if (hook != car.hook) return false;
        if (id != car.id) return false;
        if (brand != null ? !brand.equals(car.brand) : car.brand != null) return false;
        if (comments != null ? !comments.equals(car.comments) : car.comments != null) return false;
        if (doors != null ? !doors.equals(car.doors) : car.doors != null) return false;
        if (estimatedValue != null ? !estimatedValue.equals(car.estimatedValue) : car.estimatedValue != null)
            return false;
        if (fuel != car.fuel) return false;
        if (fuelEconomy != null ? !fuelEconomy.equals(car.fuelEconomy) : car.fuelEconomy != null) return false;
        if (location != null ? !location.equals(car.location) : car.location != null) return false;
        if (name != null ? !name.equals(car.name) : car.name != null) return false;
        if (owner != null ? owner.getId() != car.owner.getId() : car.owner != null) return false;
        if (ownerAnnualKm != null ? !ownerAnnualKm.equals(car.ownerAnnualKm) : car.ownerAnnualKm != null) return false;
        if (seats != null ? !seats.equals(car.seats) : car.seats != null) return false;
        if (technicalCarDetails != null ? !technicalCarDetails.equals(car.technicalCarDetails) : car.technicalCarDetails != null)
            return false;
        if (insurance != null ? !insurance.equals(car.insurance) : car.insurance != null)
            return false;
        if (type != null ? !type.equals(car.type) : car.type != null) return false;
        if (year != null ? !year.equals(car.year) : car.year != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (brand != null ? brand.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (seats != null ? seats.hashCode() : 0);
        result = 31 * result + (doors != null ? doors.hashCode() : 0);
        result = 31 * result + (year != null ? year.hashCode() : 0);
        result = 31 * result + (gps ? 1 : 0);
        result = 31 * result + (hook ? 1 : 0);
        result = 31 * result + (fuel != null ? fuel.hashCode() : 0);
        result = 31 * result + (fuelEconomy != null ? fuelEconomy.hashCode() : 0);
        result = 31 * result + (estimatedValue != null ? estimatedValue.hashCode() : 0);
        result = 31 * result + (ownerAnnualKm != null ? ownerAnnualKm.hashCode() : 0);
        result = 31 * result + (technicalCarDetails != null ? technicalCarDetails.hashCode() : 0);
        result = 31 * result + (insurance != null ? insurance.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.getId() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        return result;
    }
}
