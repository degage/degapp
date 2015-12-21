/* Membership.java
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
 * Contains information about membership of a user: contract date, deposit, etc.
 */
public class Membership {

    private int id;
    private int approvalId;

    private String fullName;

    private Integer deposit;
    private Integer fee;
    private int contractAdmin;
    private LocalDate contractDate;
    private MembershipStatus status;

    public Membership(int id, int approvalId, String fullName, Integer deposit, Integer fee, int contractAdmin, LocalDate contractDate, MembershipStatus status) {
        this.id = id;
        this.approvalId = approvalId;
        this.fullName = fullName;
        this.deposit = deposit;
        this.fee = fee;
        this.contractDate = contractDate;
        this.status = status;
        this.contractAdmin = contractAdmin;
    }

    public int getId() {
        return id;
    }

    public int getApprovalId() {
        return approvalId;
    }

    public String getFullName() {
        return fullName;
    }

    public Integer getDeposit() {
        return deposit;
    }

    public Integer getFee() {
        return fee;
    }

    public LocalDate getContractDate() {
        return contractDate;
    }

    public int getContractAdmin() {
        return contractAdmin;
    }

    public boolean canBecomeMember() {
        return contractDate != null && deposit != null && fee != null;
    }

    public MembershipStatus getStatus() {
        return status;
    }
}
