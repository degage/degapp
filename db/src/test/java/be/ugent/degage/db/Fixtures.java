package be.ugent.degage.db;

import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.models.Address;

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
}
