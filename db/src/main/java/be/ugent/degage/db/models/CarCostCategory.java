package be.ugent.degage.db.models;

/**
 * Category to which a car cost can belong
 */
public class CarCostCategory {

    private int id;

    private String description;

    public CarCostCategory(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
