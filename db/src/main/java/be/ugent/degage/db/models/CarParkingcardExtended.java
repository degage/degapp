/* CarParkingcardExtended.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 *
 * This file is part of the Degage Web Application
 *
 * Corresponding author (see also AUTHORS.txt)
 *
 * Emmanuel Isebaert
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
import com.google.gson.annotations.Expose;

/**
 * Extends {@link CarParkingcard} with context information (car/owner)
 */
public class CarParkingcardExtended extends CarParkingcard {

    @Expose
    private String carName;
    @Expose
    private int carId;
    @Expose
    private String carLicensePlate;

    public CarParkingcardExtended(String city, LocalDate expiration, String zones, String contractNr, int fileId, String carName, int carId, String carLicensePlate) {
        super(city, expiration, zones, contractNr, fileId);
        this.carName = carName;
        this.carId = carId;
        this.carLicensePlate = carLicensePlate;
    }

    public String getCarName() {
        return carName;
    }

    public int getCarId() {
        return carId;
    }

    public String getCarLicensePlate() {
        return carLicensePlate;
    }
}
