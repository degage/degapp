/* Refuel.java
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
 * Created by Stefaan Vermassen on 26/04/14.
 */
public class Refuel {
    private int id;
    private CarRide carRide;
    private File proof;
    private int eurocents;
    private RefuelStatus status;
    private LocalDate billed;

    public Refuel(int id, CarRide carRide, File proof, int eurocents, RefuelStatus status) {
        this.id = id;
        this.carRide = carRide;
        this.proof = proof;
        this.eurocents = eurocents;
        this.status = status;
    }

    public Refuel(int id, CarRide carRide, RefuelStatus status) {
        this.id = id;
        this.carRide = carRide;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CarRide getCarRide() {
        return carRide;
    }

    public void setCarRide(CarRide carRide) {
        this.carRide = carRide;
    }

    public File getProof() {
        return proof;
    }

    public void setProof(File proof) {
        this.proof = proof;
    }

    public int getEurocents() {
        return eurocents;
    }

    public void setEurocents(int eurocents) {
        this.eurocents = eurocents;
    }

    public RefuelStatus getStatus() {
        return status;
    }

    public void setStatus(RefuelStatus status) {
        this.status = status;
    }

    public LocalDate getBilled() { return billed; }

    public void setBilled(LocalDate billed) { this.billed = billed; }
}
