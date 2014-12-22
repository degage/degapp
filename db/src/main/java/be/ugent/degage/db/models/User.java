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
public class User {

    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String cellphone;
    private Address addressDomicile;
    private Address addressResidence;
    private UserGender gender;
    private String license;
    private LocalDate licenseDate;
    private Integer degageId;
    private LocalDate dateJoined;
    private Integer deposit;
    private UserStatus status;
    private IdentityCard identityCard;
    private String damageHistory;
    private int profilePictureId; //TODO, review if it's okay practice to -1 = NULL
    private boolean payedDeposit;
    private boolean agreeTerms;

    public User(String email) {
        this(0, email, null, null, UserStatus.REGISTERED);
    }

    public User() {
        this(0, null, null, null, UserStatus.REGISTERED);
    }

    public User(int id, String email, String firstName, String lastName, UserStatus status){
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;

        this.status = status;
        this.profilePictureId = -1;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getProfilePictureId() {
        return profilePictureId;
    }

    public void setProfilePictureId(int profilePictureId) {
        this.profilePictureId = profilePictureId;
    }

    public String getCellphone() {
		return cellphone;
	}

	public void setCellphone(String cellphone) {
		this.cellphone = cellphone;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public UserGender getGender() {
        return gender;
    }

    public void setGender(UserGender gender) {
        this.gender = gender;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public IdentityCard getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(IdentityCard identityCard) {
        this.identityCard = identityCard;
    }

    public String getDamageHistory() {
        return damageHistory;
    }

    public void setDamageHistory(String damageHistory) {
        this.damageHistory = damageHistory;
    }

    public boolean isPayedDeposit() {
        return payedDeposit;
    }

    public void setPayedDeposit(boolean payedDeposit) {
        this.payedDeposit = payedDeposit;
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

    public Integer getDegageId() {
        return degageId;
    }

    public void setDegageId(Integer degageId) {
        this.degageId = degageId;
    }

    public Integer getDeposit() {
        return deposit;
    }

    public void setDeposit(Integer deposit) {
        this.deposit = deposit;
    }

    public LocalDate getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(LocalDate dateJoined) {
        this.dateJoined = dateJoined;
    }

    @Override
    public String toString(){
        return firstName + " " + lastName;
    }

    public String getFullName() {
        return lastName + ", " + firstName;
    }

    /**
     * Has this user status {@link be.ugent.degage.db.models.UserStatus#FULL}?
     * @return
     */
    public boolean hasFullStatus() {
        return status == UserStatus.FULL;
    }

    /**
     * Is this user allowed to login (depends on the user status)?
     */
    public boolean canLogin() {
        return status != UserStatus.BLOCKED && status != UserStatus.DROPPED ;
    }
}
