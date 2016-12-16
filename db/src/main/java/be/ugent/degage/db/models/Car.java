/* Car.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.models;

import java.time.LocalDate;

/**
 * Represents information about a certain car.
 */
public class Car extends CarHeaderLong {

    private Integer year;
    private Integer fuelEconomy;
    private Integer estimatedValue;
    private Integer ownerAnnualKm;
    private TechnicalCarDetails technicalCarDetails;
    private CarInsurance insurance;
    private CarAssistance assistance;
    private CarParkingcard parkingcard;
    private LocalDate createdAt;

    public Car(int id, String name, String email, String brand, String type,
               Integer seats, Integer doors, Integer year, boolean manual, boolean gps, boolean hook,
               CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm,
               String comments, boolean active, UserHeader owner) {
        super(id, name, brand, type, email, active,
                seats, doors, manual, gps, hook, fuel, comments, year, "");
        if (owner != null) {
            setOwner(owner);
        }
        this.year = year;
        this.fuelEconomy = fuelEconomy;
        this.estimatedValue = estimatedValue;
        this.ownerAnnualKm = ownerAnnualKm;
    }

    public Car(int id, String name, String email, String brand, String type,
               Integer seats, Integer doors, Integer year, boolean manual, boolean gps, boolean hook,
               CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm,
               String comments, boolean active, LocalDate createdAt, UserHeader owner, String licensePlate) {
        super(id, name, brand, type, email, active,
                seats, doors, manual, gps, hook, fuel, comments, year, licensePlate);
        if (owner != null) {
            setOwner(owner);
        }
        this.year = year;
        this.fuelEconomy = fuelEconomy;
        this.estimatedValue = estimatedValue;
        this.ownerAnnualKm = ownerAnnualKm;
        this.createdAt = createdAt;
    }


    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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

    public CarAssistance getAssistance() {
        return assistance;
    }

    public void setAssistance(CarAssistance assistance) {
        this.assistance = assistance;
    }

    public CarParkingcard getParkingcard() {
        return parkingcard;
    }

    public void setParkingcard(CarParkingcard parkingcard) {
        this.parkingcard = parkingcard;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

}
