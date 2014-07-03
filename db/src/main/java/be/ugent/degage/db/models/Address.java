package be.ugent.degage.db.models;

/**
 * Created by Cedric on 2/21/14.
 */
public class Address {
    private int id;
    private String country;
    private String zip;
    private String city;
    private String street;
    private String number;
    private String bus;

    public Address(String country, String zip, String city, String street, String number, String bus) {
        this(0, country, zip, city, street, number, bus);
    }

    public Address(int id, String country, String zip, String city, String street, String number, String bus) {
        this.country = country;
        this.id = id;
        this.zip = zip;
        this.city = city;
        this.street = street;
        this.number = number;
        this.bus = bus;
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBus() {
        return bus;
    }

    public void setBus(String bus) {
        this.bus = bus;
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
        if ((street != null && !street.isEmpty()) || (number != null && !number.isEmpty()) || (zip != null && !zip.isEmpty()) || (city != null && !city.isEmpty())) {
            String busString = "";
            if(bus != null && !bus.isEmpty())
                busString = " b" + bus;
            return String.format("%s %s%s, %s %s", street, number, busString, zip, city);
        } else return "/";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (id != address.id) return false;
        if (bus != null ? !bus.equals(address.bus) : address.bus != null) return false;
        if (city != null ? !city.equals(address.city) : address.city != null) return false;
        if (country != null ? !country.equals(address.country) : address.country != null) return false;
        if (number != null ? !number.equals(address.number) : address.number != null) return false;
        if (street != null ? !street.equals(address.street) : address.street != null) return false;
        if (zip != null ? !zip.equals(address.zip) : address.zip != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (zip != null ? zip.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (street != null ? street.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (bus != null ? bus.hashCode() : 0);
        return result;
    }
}
