/* Approval.java
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
 * Created by Cedric on 3/30/2014.
 */
public class Approval {

    public enum ApprovalStatus {
        PENDING,
        ACCEPTED,
        DENIED
    }

    private int id;
    private UserHeader user;
    private UserHeader admin;
    private Instant submitted;
    private Instant reviewed;
    private InfoSession session;
    private ApprovalStatus status;
    private String userMessage;
    private String adminMessage;

    public Approval(int id, UserHeader user, UserHeader admin, Instant submitted, Instant reviewed, InfoSession session, ApprovalStatus status, String userMessage, String adminMessage) {
        this.id = id;
        this.user = user;
        this.admin = admin;
        this.submitted = submitted;
        this.reviewed = reviewed;
        this.session = session;
        this.status = status;
        this.userMessage = userMessage;
        this.adminMessage = adminMessage;
    }

    public int getId(){
        return id;
    }

    public String getUserMessage() {
        return userMessage;
    }
    public String getAdminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }


    public UserHeader getUser() {
        return user;
    }

    public void setUser(UserHeader user) {
        this.user = user;
    }

    public UserHeader getAdmin() {
        return admin;
    }

    public void setAdmin(UserHeader admin) {
        this.admin = admin;
    }
//
//    public Instant getSubmitted() {
//        return submitted;
//    }

    public InfoSession getSession() {
        return session;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
}
