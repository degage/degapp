package controllers.util;

import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.models.Address;

import java.util.*;

/**
 * Created by HannesM on 21/04/14.
 */
public class Addresses  {

    private static List<String> COUNTRIES;
    private static final Locale COUNTRY_LANGUAGE = new Locale("nl", "BE");

    public static class EditAddressModel {

        public String city;
        public String num;
        public String street;
        public String zipCode;
        public String country;

        public void populate(Address address) {
            if (address == null) {
                country = COUNTRY_LANGUAGE.getDisplayCountry(COUNTRY_LANGUAGE);
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
     * Lazy loads a country list in current configured locale
     *
     * @return A list of all countries enabled in the Java locale
     */
    public static List<String> getCountryList() {
        if (COUNTRIES == null) {

            Set<String> countries = new HashSet<>(); // remove duplicates
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale obj : locales) {
                if ((obj.getDisplayCountry() != null) && (!"".equals(obj.getDisplayCountry()))) {
                    countries.add(obj.getDisplayCountry(COUNTRY_LANGUAGE));
                }
            }

            COUNTRIES = new ArrayList<>(countries);
            Collections.sort(COUNTRIES);
        }
        return COUNTRIES;
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
