package be.ugent.degage.db.dao.api;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.api.*;

/**
 *
 */
public interface CarApiDAO {

    /**
     * Returns a list of all car stands, unfiltered and without pagination
     */
    public Iterable<CarStand> listCarStands() throws DataAccessException;

    public int createCar(String name, String email, String brand, String type, int carOwnerUserId, String fuel) throws DataAccessException;

}
