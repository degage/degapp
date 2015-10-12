/* ApprovalListInfo.java
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

import java.time.Instant;

/**
 * Information about approvals and related users for use in lists
 */
public class ApprovalListInfo {

    private int id;
    private String userName;
    private int userId;
    private Approval.ApprovalStatus status;
    private boolean adminAssigned;
    private boolean fullUser;
    private boolean contractSigned;
    private Instant submitted;
    private Integer deposit;
    private Integer fee;

    public ApprovalListInfo(int id, String userName, int userId, Approval.ApprovalStatus status,
                            boolean adminAssigned, boolean fullUser, boolean contractSigned, Instant submitted, Integer deposit, Integer fee) {
        this.id = id;
        this.userName = userName;
        this.userId = userId;
        this.status = status;
        this.adminAssigned = adminAssigned;
        this.fullUser = fullUser;
        this.contractSigned = contractSigned;
        this.submitted = submitted;
        this.deposit = deposit;
        this.fee = fee;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public int getUserId() {
        return userId;
    }

    public Approval.ApprovalStatus getStatus() {
        return status;
    }

    public boolean isAdminAssigned() {
        return adminAssigned;
    }

    public boolean isFullUser() {
        return fullUser;
    }

    public boolean isContractSigned() {
        return contractSigned;
    }

    public Instant getSubmitted() {
        return submitted;
    }

    public Integer getDeposit() {
        return deposit;
    }

    public Integer getFee() {
        return fee;
    }
}