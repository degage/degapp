/* ProfileCompleteness.java
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

package data;

import be.ugent.degage.db.models.Address;
import be.ugent.degage.db.models.User;

import java.util.EnumSet;
import java.util.Set;

import static controllers.Utils.isBlank;

/**
 * Represents in how far the data stored for a user is complete
 */
public class ProfileCompleteness {
    
    public enum Item {
        FIRST_NAME ("Voornaam"),
        LAST_NAME ("Familienaam"),
        ADDRESS_DOMICILE ("Domicilieadres"),
        ADDRESS_RESIDENCE ("Verblijfsadres"),
        EMAIL_ADDRESS ("Geldig e-mailadres"),
        PHONE("Telefoon- en/of GSM-nummer"),
        ID_CARD_NUMBER ("Nummer identiteitskaart"),
        NATIONAL_NUMBER ("Rijksregisternummer"),
        LICENSE_NUMBER ("Rijbewijsnummer"),
        LICENSE_SCAN ("Scan/foto van rijbewijs"),
        PROFILE_PICTURE ("Profielfoto");

        private final String description;

        Item (String description) {
            this.description = description;
        }
    }
    
    private Set<Item> set;


    /**
     * Return the set of profile items which are already complete.
     */
    public Set<Item> getSet() {
        return set;
    }

    //
    private static boolean isComplete (Address address) {
        return address != null && 
                !isBlank(address.getStreet()) &&
                !isBlank(address.getNum()) &&
                !isBlank(address.getCity()) &&
                !isBlank(address.getZip());         
    }
    
    //
    private static boolean isValidEmail (String email) {
        return email != null && 
                ! (email.startsWith("degage.") && email.endsWith("@gmail.com"));
    }
    
    /**
     * Compute how much the profile is complete for the given user
     * @param user Fully initialized user record
     */
    public ProfileCompleteness (User user, boolean hasLicenseFile) {
        set = EnumSet.noneOf(Item.class);
        if (!isBlank(user.getFirstName())) {
            set.add(Item.FIRST_NAME);
        }
        if (!isBlank(user.getLastName())) {
            set.add(Item.LAST_NAME);
        }
        if (isComplete(user.getAddressDomicile())) {
            set.add(Item.ADDRESS_DOMICILE);
        }
        if (isComplete(user.getAddressResidence())) {
            set.add(Item.ADDRESS_RESIDENCE);
        }
        if (!isValidEmail(user.getEmail())) {
            set.add(Item.EMAIL_ADDRESS);
        }
        if (!isBlank(user.getCellphone()) || !isBlank (user.getPhone())) {
            set.add(Item.PHONE);
        }

        if (!isBlank(user.getIdentityId())) {
            set.add(Item.ID_CARD_NUMBER);
        }
        if (!isBlank(user.getNationalId())) {
            set.add(Item.NATIONAL_NUMBER);
        }
        if (!isBlank(user.getLicense())) {
            set.add(Item.LICENSE_NUMBER);
        }
        if (user.getProfilePictureId() != 0) {
            set.add(Item.PROFILE_PICTURE);
        }
        if (hasLicenseFile) {
            set.add(Item.LICENSE_SCAN);
        }
    }

    /**
     * The (approximate) percentage of the profile which is complete
     */
    public int getPercentage() {
        return 100*set.size() / Item.values().length;
    }
}
