package be.ugent.degage.db.models;

/**
 * Represents an address stored in the database
 */
public class Address {
    private int id;
    private String country;
    private String zip;
    private String city;
    private String street;
    private String num;

    public Address(String country, String zip, String city, String street, String num) {
        this(0, country, zip, city, street, num);
    }

    public Address(int id, String country, String zip, String city, String street, String num) {
        this.country = country;
        this.id = id;
        this.zip = zip;
        this.city = city;
        this.street = street;
        this.num = num;
    }

    public int getId() {
        return id;
    }

    public String getZip() {
        return zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        if ((street != null && !street.isEmpty()) || (num != null && !num.isEmpty()) || (zip != null && !zip.isEmpty()) || (city != null && !city.isEmpty())) {
            return String.format("%s %s, %s %s", street, num, zip, city);
        } else return "/";
    }

}
