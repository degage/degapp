/* UserpickerData.java
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

import be.ugent.degage.db.models.UserHeader;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.Collections;
import java.util.List;

/**
 * Data for a form that uses a userpicker.
 */
public class UserpickerData {
    public Integer userId;

    @Constraints.Required
    public String userIdAsString;

    public List<ValidationError> validate() {
        if (userId == null || userId <= 0) {
            // needed for those cases where a string is input which does not correspond with a real person
            return Collections.singletonList(new ValidationError("userId", "Gelieve een bestaande persoon te selecteren"));
        } else {
            return null;
        }
    }

    public void populate(UserHeader user) {
        if (user != null) {
            userId = user.getId();
            userIdAsString = user.getFullName();
        }
    }

}
