/* TechnicalCarDetails.java
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
 * Transfer object for technical car details for a certain car.
 *
 * Created by HannesM on 23/04/14.
 */
public class TechnicalCarDetails {
    private String licensePlate;
    private int registrationId;
    private String chassisNumber;

    private Integer ecoScore;
    private String euroNorm;
    private LocalDate startDate;
    private Integer kiloWatt;

    public TechnicalCarDetails(String licensePlate, int registrationId, String chassisNumber, Integer ecoScore, String euroNorm, LocalDate startDate, Integer kiloWatt) {
        this.licensePlate = licensePlate;
        this.registrationId = registrationId;
        this.chassisNumber = chassisNumber;
        this.ecoScore = ecoScore;
        this.euroNorm = euroNorm;
        this.startDate = startDate;
        this.kiloWatt = kiloWatt;
    }

    public TechnicalCarDetails(String licensePlate, int registrationId, String chassisNumber) {
        this.licensePlate = licensePlate;
        this.registrationId = registrationId;
        this.chassisNumber = chassisNumber;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }

    public Integer getEcoScore() {
        return ecoScore;
    }

    public void setEcoScore(Integer ecoScore) {
        this.ecoScore = ecoScore;
    }

    public String getEuroNorm() {
        return euroNorm;
    }

    public void setEuroNorm(String euroNorm) {
        this.euroNorm = euroNorm;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getKiloWatt() {
        return kiloWatt;
    }

    public void setKiloWatt(Integer kiloWatt) {
        this.kiloWatt = kiloWatt;
    }
}
