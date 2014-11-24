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
    private UserHeader user;
    private EnrollementStatus status;

    public Enrollee(UserHeader user, EnrollementStatus status) {
        this.user = user;
        this.status = status;
    }

    public UserHeader getUser() {
        return user;
    }

    public void setUser(UserHeader user) {
        this.user = user;
    }

    public EnrollementStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollementStatus status) {
        this.status = status;
    }
    
    
}
