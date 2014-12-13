/* Addresses.java
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
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package controllers.util;

import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.models.Address;
import controllers.Utils;

import java.util.*;

/**
 */
public class Addresses  {

    public static class EditAddressModel {

        public String city;
        public String num;
        public String street;
        public String zipCode;
        public String country;

        public void populate(Address address) {
            if (address == null) {
                country = DEFAULT_COUNTRY_NAME;
                return;
            }

            city = address.getCity();
            num = address.getNum();
            street = address.getStreet();
            zipCode = address.getZip();
            country = address.getCountry();
        }

        public boolean isEmpty() {
            return nullOrEmpty(zipCode) && nullOrEmpty(city) && nullOrEmpty(street) && nullOrEmpty(num);
        }
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * List of all country names. In Dutch.
     */
    public static Iterable<String> COUNTRY_NAMES;

    public static String DEFAULT_COUNTRY_NAME = Utils.DEFAULT_LOCALE.getDisplayCountry(Utils.DEFAULT_LOCALE);

    static {
        Set<String> countries = new TreeSet<>(); // remove duplicates and sort
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            String name = locale.getDisplayCountry(Utils.DEFAULT_LOCALE);
            if (name != null && !name.isEmpty()) {
                countries.add(name);
            }
        }
        COUNTRY_NAMES = new ArrayList<> (countries);
    }

    /**
     * Modifies, creates or deletes an address in the database based on the provided form data and current address
     *
     * @param model   The submitted form data
     * @param address The already-set address for the user
     * @param dao     The DAO to edit addresses
     * @return The changed or null if deleted
     */
    public static Address modifyAddress(EditAddressModel model, Address address, AddressDAO dao) {
        // TODO: no checks needed for null + modify address always?
        if (address == null) {
            // User entered new address in fields
            address = dao.createAddress(model.country, model.zipCode, model.city, model.street, model.num);
        } else {
            // User changed existing address

            // Only call the database when there's actually some change
            if ((model.country != null && !model.country.equals(address.getCountry())) ||
                    (model.zipCode != null && !model.zipCode.equals(address.getZip())) ||
                    (model.city != null && !model.city.equals(address.getCity())) ||
                    (model.street != null && !model.street.equals(address.getStreet())) ||
                    (model.num != null && !model.num.equals(address.getNum()))
                    ) {
                address.setCountry(model.country);
                address.setZip(model.zipCode);
                address.setCity(model.city);
                address.setStreet(model.street);
                address.setNum(model.num);
                dao.updateAddress(address);
            }
        }
        return address;
    }
}
