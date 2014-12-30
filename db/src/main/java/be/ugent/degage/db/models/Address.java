/* Address.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

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
            return String.format("%s %s, %s %s (%s)", street, num, zip, city, country);
        } else {
            return "(onbekend)";
        }
    }

}
