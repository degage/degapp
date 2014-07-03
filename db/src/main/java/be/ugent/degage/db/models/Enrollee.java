/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.models;

/**
 *
 * @author Laurent
 */
public class Enrollee {
    private User user;
    private EnrollementStatus status;

    public Enrollee(User user, EnrollementStatus status) {
        this.user = user;
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EnrollementStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollementStatus status) {
        this.status = status;
    }
    
    
}
