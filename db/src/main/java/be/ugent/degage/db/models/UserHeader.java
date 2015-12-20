/* UserHeader.java
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
 * Represents partial information for a single user. Used in lists or in combination with other data types.
 */
public class UserHeader extends UserHeaderShort {

    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private UserStatus status;

    private String phone;
    private String cellPhone;
    private Integer degageId;

    public UserHeader(int id, String email, String firstName, String lastName, UserStatus status, String phone, String cellPhone, Integer degageId){
        super (id, firstName, lastName);
        this.email = email;
        this.status = status;
        this.phone = phone;
        this.cellPhone = cellPhone;
        this.degageId = degageId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public Integer getDegageId() {
        return degageId;
    }

    /**
     * Has this user status {@link UserStatus#FULL}?
     */
    public boolean hasFullStatus() {
        return status == UserStatus.FULL;
    }

    /**
     * Is this user allowed to login (depends on the user status)?
     */
    public boolean canLogin() {
        return status != UserStatus.BLOCKED && status != UserStatus.DROPPED;
    }

}
