/* SettingDAO.java
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

package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.CostSettings;
import be.ugent.degage.db.models.Setting;


import java.time.Instant;

/**
 * Data access object for system settings
 */
public interface SettingDAO {

    /**
     * Create a new setting which will be valid after the given date and time. Also use this method
     * for 'updating' the settings, because a new value will also correspond to a new instant.
     */
    public void createSettingAfterDate(String name, String value, Instant after) throws DataAccessException;

    /**
     * Retrieve the current value of a setting.
     */
    public String getSettingForNow(String name) throws DataAccessException;

    /**
     * Retrieve the value of a setting valid at a given instant
     */
    public String getSettingForDate(String name, Instant instant) throws DataAccessException;
    // TODO: do we really need the above? (ask customer - for bills?)

    /**
     * Return a list of all settings with their newest value.
     */
    public Iterable<Setting> getSettings() throws DataAccessException;

}
