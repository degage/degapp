/* InfoSession.java
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
 * Information about an information session
 */
public class InfoSession {

    private int id;
    private InfoSessionType type;
    private Instant time;
    private Address address;
    private int hostId;
    private String hostName;
    private int maxEnrollees;
    private int enrolleeCount;
    private int membershipCount;
    private String comments;

    public InfoSession(int id, InfoSessionType type, Instant time, Address address, int hostId, String hostName, int maxEnrollees, String comments) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.address = address;
        this.hostId = hostId;
        this.hostName = hostName;
        this.maxEnrollees = maxEnrollees;
        this.comments = comments;
        this.membershipCount = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public InfoSessionType getType() {
        return type;
    }

    public void setType(InfoSessionType type) {
        this.type = type;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getHostId() {
        return hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public int getMaxEnrollees() {
        return maxEnrollees;
    }

    public void setMaxEnrollees(int maxEnrollees) {
        this.maxEnrollees = maxEnrollees;
    }

    public int getEnrolleeCount() {
        return enrolleeCount;
    }

    public void setEnrolleeCount(int enrolleeCount) {
        this.enrolleeCount = enrolleeCount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Number of enrollees for this infosession that are already a member.
     * Not always filled in.
     */
    public int getMembershipCount() {
        return membershipCount;
    }

    public void setMembershipCount(int membershipCount) {
        this.membershipCount = membershipCount;
    }
}
