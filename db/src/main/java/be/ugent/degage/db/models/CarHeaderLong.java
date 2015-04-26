/* CarHeaderLong.java
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

/**
 * Extends {@link CarHeader} with further information. Location and owner
 * can be left null.
 */
public class CarHeaderLong extends CarHeaderAndOwner {


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
