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
