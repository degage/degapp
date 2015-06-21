/* Billing.java
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
 * Information about a single 'billing'
 */
public class Billing {

    private int id;
    private String description;
    private String prefix;
    private LocalDate start;
    private LocalDate limit;
    private BillingStatus status;
    private LocalDate simulationDate;
    private LocalDate driversDate;
    private LocalDate ownersDate;

    public Billing(int id, String description, String prefix, LocalDate start, LocalDate limit,
                   BillingStatus status, LocalDate simulationDate, LocalDate driversDate, LocalDate ownersDate) {
        this.id = id;
        this.description = description;
        this.prefix = prefix;
        this.start = start;
        this.limit = limit;
        this.status = status;
        this.simulationDate = simulationDate;
        this.driversDate = driversDate;
        this.ownersDate = ownersDate;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getPrefix() {
        return prefix;
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

    public LocalDate getSimulationDate() {
        return simulationDate;
    }

    public LocalDate getDriversDate() {
        return driversDate;
    }

    public LocalDate getOwnersDate() {
        return ownersDate;
    }
}
