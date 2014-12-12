/* SettingDAOTest.java
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

package be.ugent.degage.db;

import be.ugent.degage.db.dao.SettingDAO;
import be.ugent.degage.db.models.Costs;
import be.ugent.degage.db.models.Setting;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

/**
 * Test for {@link SettingDAO}
 */
public class SettingDAOTest extends DAOTest {

    // Note that the test database is already initialized with certain settings.

    private SettingDAO dao;


    @Before
    public void getDAOAndFixtures() {
        dao = context.getSettingDAO();
        Fixtures.createSettings(context);
        Fixtures.createCostSettings(context);
    }

    @Test
    public void getSettingForNowTest() {
        assertEquals("value_1", dao.getSettingForNow("setting_1"));
        assertEquals("value_recent", dao.getSettingForNow("setting_2"));
    }

    @Test
    public void getSettingForDateTest() {
        assertEquals("value_1", dao.getSettingForDate("setting_1", Instant.now().plusSeconds(20)));
        assertEquals("value_new", dao.getSettingForDate("setting_2", Instant.now().plusSeconds(120)));
    }

    @Test
    public void getSettingsTest() {
        Iterable<Setting> list = dao.getSettings();
        assertEquals ("value_1",
                Iterables.find(list, s -> s.getName().equals("setting_1")).getValue());
        assertEquals ("value_new",
                Iterables.find(list, s -> s.getName().equals("setting_2")).getValue());
    }

    @Test
    public void getCostSettingsTest() {
        Costs costs = dao.getCostSettings(Instant.now().plusSeconds(30));
        assertEquals (4, costs.getLevels());
        assertEquals (0.31, costs.getCost(1), 0.001);
        assertEquals (132, costs.getLimit(2));
        assertEquals (0.85, costs.getDeprecation(), 0.001);
    }

}
