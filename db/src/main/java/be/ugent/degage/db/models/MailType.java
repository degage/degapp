/* MailType.java
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
 * Created by stefaan on 04/03/14.
 */
public enum MailType {
    VERIFICATION(1), WELCOME(2), INFOSESSION_ENROLLED(3), RESERVATION_APPROVE_REQUEST(4),
    RESERVATION_APPROVED_BY_OWNER(5), RESERVATION_REFUSED_BY_OWNER(6), PASSWORD_RESET(7),
    TERMS(8), MEMBERSHIP_APPROVED(9), MEMBERSHIP_REFUSED(10), CARCOST_APPROVED(11),
    CARCOST_REFUSED(12), REFUEL_APPROVED(13), REFUEL_REFUSED(14), REMINDER_MAIL(15),
    REFUEL_REQUEST(16), CARCOST_REQUEST(17), CONTRACTMANAGER_ASSIGNED(18), DETAILS_PROVIDED(19);
    private final int key;

    private MailType(final int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
