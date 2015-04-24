package be.ugent.degage.db.models;

/**
 * Extends {@link CarHeader} with an owner field.
 */
public class CarHeaderWithOwner extends CarHeader {

    private UserHeader owner;

    /**
     * Create an object of this type.
     */
    public CarHeaderWithOwner(int id, String name, String brand, String type, String email, boolean active) {
        super(id, name, brand, type, email, active);
    }

    public UserHeader getOwner() {
        return owner;
    }

    public void setOwner(UserHeader owner) {
        this.owner = owner;
    }


}
