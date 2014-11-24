package be.ugent.degage.db.models;

/**
 * Represents partial information for a single user. Used in lists or in combination with other data types.
 */
public class UserHeader {

    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private UserStatus status;

    private String phone;
    private String cellPhone;

    public UserHeader(int id, String email, String firstName, String lastName, UserStatus status, String phone, String cellPhone){
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.phone = phone;
        this.cellPhone = cellPhone;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    @Override
    public String toString(){
        return firstName + " " + lastName;
    }

    public String getFullName() {
        return lastName + ", " + firstName;
    }

    /**
     * Has this user status {@link UserStatus#FULL}?
     * @return
     */
    public boolean hasFullStatus() {
        return status == UserStatus.FULL;
    }

    /**
     * Is this user allowed to login (depends on the user status)?
     */
    public boolean canLogin() {
        return status != UserStatus.BLOCKED && status != UserStatus.DROPPED && status != UserStatus.EMAIL_VALIDATING;
    }

}