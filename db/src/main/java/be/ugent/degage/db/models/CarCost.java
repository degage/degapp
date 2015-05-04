/* CarCost.java
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
 * Information on costs made for a certain car
 */
public class CarCost {

    // TODO: use CarHeaderShort

    private int id;
    private int carId;
    private String carName;
    private int amount; // in eurocent
    private LocalDate date;
    private int km;
    private String description;
    private ApprovalStatus status;
    private int proofId;
    private LocalDate billed;
    private CarCostCategory category;
    private int spread; // see getter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getKm() {
        return km;
    }

    public void setKm(int km) {
        this.km = km;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public int getProofId() {
        return proofId;
    }

    public void setProofId(int proofId) {
        this.proofId = proofId;
    }

    public LocalDate getBilled() { return billed; }

    public void setBilled(LocalDate billed) { this.billed = billed; }

    public int getCarId() {
        return carId;
    }

    public String getCarName() {
        return carName;
    }

    /**
     * The number of months over which this cost should be spread out. By default 1. A value of 0 indicates that
     * this cost will be payed back outside the system.
     */
    public int getSpread() {
        return spread;
    }

    public CarCostCategory getCategory() {
        return category;
    }

    public CarCost(int id, int amount, int km, String description, LocalDate date, int proofId,
                   int carId, String carName, CarCostCategory category, int spread){
        this.id = id;
        this.amount = amount;
        this.km = km;
        this.description = description;
        this.date = date;
        this.status = ApprovalStatus.REQUEST;
        this.proofId = proofId;
        this.carId = carId;
        this.carName = carName;
        this.category = category;
        this.spread = spread;
    }
}
