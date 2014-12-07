/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

import java.time.Instant;

/**
 * Information about an information session
 */
public class InfoSession {

    private int id;
    private InfoSessionType type;
    private String typeAlternative;
    private Instant time;
    private Address address;
    private UserHeader host;
    private int maxEnrollees;
    private int enrolleeCount;
    private String comments;

    public InfoSession(int id, InfoSessionType type, Instant time, Address address, UserHeader host, int maxEnrollees, String comments) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.address = address;
        this.host = host;
        this.maxEnrollees = maxEnrollees;
        this.comments = comments;
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

    public String getTypeAlternative() {
        return typeAlternative;
    }

    public void setTypeAlternative(String typeAlternative) {
        this.typeAlternative = typeAlternative;
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

    public UserHeader getHost() {
        return host;
    }

    public void setHost(UserHeader host) {
        this.host = host;
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
}
