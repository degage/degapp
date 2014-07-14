/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

import org.joda.time.DateTime;

/**
 * Information about an information session
 */
public class InfoSession {

    private int id;
    private InfoSessionType type;
    private String typeAlternative;
    private DateTime time;
    private Address address;
    private User host;
    private int maxEnrollees;
    private int enrolleeCount;
    private String comments;

    public InfoSession(int id, InfoSessionType type, DateTime time, Address address, User host, int maxEnrollees, String comments) {
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

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
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
