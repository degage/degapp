package be.ugent.degage.db.models;

/**
 * Contains minimal information about a person: id and name
 */
public class UserHeaderShort {

    private int id;
    private String firstName;
    private String lastName;

    public UserHeaderShort(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString(){
        return firstName + " " + lastName;
    }

    public String getFullName() {
        return lastName + ", " + firstName;
    }
}
