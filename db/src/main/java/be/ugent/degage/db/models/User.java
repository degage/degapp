/* User.java
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

import java.time.LocalDate;

/**
 * Represents a single user
 */
public class User extends UserHeader {

    private Address addressDomicile;
    private Address addressResidence;
    private String license;
    private LocalDate licenseDate;
    private LocalDate dateJoined;
    private Integer deposit;
    private Integer fee;
    private String identityId;
    private String nationalId;
    private String damageHistory;
    private String vatNr;

    // private int profilePictureId; // picture must be retrieved separately

    private boolean agreeTerms;

    public User(int id, String email, String firstName, String lastName, UserStatus status,
                String phone, String cellPhone, Integer degageId) {
        super(id, email, firstName, lastName, status, phone, cellPhone, degageId);
    }

    public Address getAddressDomicile() {
        return addressDomicile;
    }

    public void setAddressDomicile(Address addressDomicile) {
        this.addressDomicile = addressDomicile;
    }

    public Address getAddressResidence() {
        return addressResidence;
    }

    public void setAddressResidence(Address addressResidence) {
        this.addressResidence = addressResidence;
    }
    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getIdentityId() {
        return identityId;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getDamageHistory() {
        return damageHistory;
    }

    public void setDamageHistory(String damageHistory) {
        this.damageHistory = damageHistory;
    }

    public boolean isPayedDeposit() {
        return deposit != null;
    }

    public boolean isAgreeTerms() {
        return agreeTerms;
    }

    public void setAgreeTerms(boolean agreeTerms) {
        this.agreeTerms = agreeTerms;
    }

    public LocalDate getLicenseDate() {
        return licenseDate;
    }

    public void setLicenseDate(LocalDate licenseDate) {
        this.licenseDate = licenseDate;
    }

    public Integer getDeposit() {
        return deposit;
    }

    public void setDeposit(Integer deposit) {
        this.deposit = deposit;
    }

    public Integer getFee() {
        return fee;
    }

    public void setFee(Integer fee) {
        this.fee = fee;
    }

    public LocalDate getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(LocalDate dateJoined) {
        this.dateJoined = dateJoined;
    }

    public String getVatNr() {
        return vatNr;
    }

    public void setVatNr(String vatNr) {
        this.vatNr = vatNr;
    }
}
