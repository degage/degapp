package be.ugent.degage.db;

import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.dao.SettingDAO;
import be.ugent.degage.db.models.Address;

import java.time.Instant;

/**
 * Provides data for several of the tests.
 */
public final class Fixtures {

    public static Address[] createAddresses (DataAccessContext context) {
        AddressDAO dao = context.getAddressDAO();

        return new Address[]{
                dao.createAddress("België", "9000", "Gent", "Krijgslaan", "281", "S9"),
                dao.createAddress("België", "8500", "Kortrijk", "Lange steenstraat", "11", null)
        };
    }

    public static void createSettings (DataAccessContext context) {
        SettingDAO dao = context.getSettingDAO();

        dao.createSettingAfterDate("setting_1", "value_1", Instant.now().minusSeconds(1));
        dao.createSettingAfterDate("setting_2", "value_new", Instant.now().plusSeconds(60));
        dao.createSettingAfterDate("setting_2", "value_old", Instant.now().minusSeconds(60));
        dao.createSettingAfterDate("setting_2", "value_recent", Instant.now().minusSeconds(20));

    }
}
