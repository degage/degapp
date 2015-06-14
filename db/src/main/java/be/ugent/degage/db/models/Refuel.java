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

/**
 * Contains information about a single refuel.
 */
public class Refuel {
    private int id;
    private int proofId;
    private int eurocents;
    private ApprovalStatus status;
    private int km;
    private String amount; // Amount of fuel (free format)
    private String message;

    public Refuel(int id, int proofId, int eurocents, ApprovalStatus status,
                  int km, String amount, String message) {
        this.id = id;
        this.proofId = proofId;
        this.eurocents = eurocents;
        this.status = status;
        this.km = km;
        this.amount = amount;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProofId() {
        return proofId;
    }

    public int getEurocents() {
        return eurocents;
    }

    public int getKm() {
        return km;
    }

    /**
     * Amount of fuel (free format)
     * @return
     */
    public String getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }

    public void setEurocents(int eurocents) {
        this.eurocents = eurocents;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
}
