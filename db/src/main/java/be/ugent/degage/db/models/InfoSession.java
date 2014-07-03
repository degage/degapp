/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Laurent
 */
public class InfoSession {

    public static final List<Enrollee> NO_ENROLLEES = new ArrayList<Enrollee>(0);

    private int id;
    private InfoSessionType type;
    private String typeAlternative;
    private DateTime time;
    private Address address;
    private User host;
    private List<Enrollee> enrolled;
    private int maxEnrollees;
    private int enrolleeCount;
    private String comments;

    public InfoSession(int id, InfoSessionType type, DateTime time, Address address, User host, List<Enrollee> enrolled, int maxEnrollees, String comments) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.address = address;
        this.host = host;
        if(enrolled == null)
            this.enrolled = NO_ENROLLEES;
        else
            this.enrolled = enrolled;
        this.maxEnrollees = maxEnrollees;
        this.comments = comments;
    }

    public InfoSession(int id, InfoSessionType type, DateTime time, Address address, User host, int enrolleeCount, int maxEnrollees, String comments) {
        this(id, type, time, address, host, null, maxEnrollees, comments);
        this.enrolled = null; //TODO: cleanup above constructor not to fix enrolled as null
        this.enrolleeCount = enrolleeCount;
    }

    public InfoSession(int id, InfoSessionType type, DateTime time, Address address, User host, int maxEnrollees, String comments) {
        this(id, type, time, address, host, NO_ENROLLEES, maxEnrollees, comments);
    }

    public EnrollementStatus getEnrollmentStatus(User user){
        if(this.enrolled == NO_ENROLLEES)
            throw new RuntimeException("Please use the fully populated object.");

        for(Enrollee er : this.enrolled){
            if(er.getUser().getId() == user.getId()){
                return er.getStatus();
            }
        }
        return EnrollementStatus.ABSENT;
    }

    /**
     * Gets the current amount of enrollees
     * @return When an enrollee list was provided this returns the size, otherwise it uses a cached count result
     */
    public int getEnrolleeCount(){
        if(this.enrolled == null)
            return enrolleeCount;
        else return enrolled.size();
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

    public List<Enrollee> getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(List<Enrollee> enrolled) {
        this.enrolled = enrolled;
    }

    public void addEnrollee(Enrollee enrollee) {
        if (this.enrolled == NO_ENROLLEES) //lazy loading
            this.enrolled = new ArrayList<Enrollee>();

        this.enrolled.add(enrollee);
    }

    public boolean hasEnrolled(){
        return !this.enrolled.isEmpty();
    }

    public void deleteEnrollee(Enrollee enrollee) {
        this.enrolled.remove(enrollee);
    }

    public int getMaxEnrollees() {
        return maxEnrollees;
    }

    public void setMaxEnrollees(int maxEnrollees) {
        this.maxEnrollees = maxEnrollees;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
