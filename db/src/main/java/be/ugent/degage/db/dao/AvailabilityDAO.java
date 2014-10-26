package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.CarAvailabilityInterval;

/**
 * DAO for availabilities of cars
 */
public interface AvailabilityDAO {

    public Iterable<CarAvailabilityInterval> getAvailabilities(int carId) throws DataAccessException;

    public void updateAvailabilities(int carId, Iterable<CarAvailabilityInterval> availabilities) throws DataAccessException;
    public void createAvailabilities(int carId, Iterable<CarAvailabilityInterval> availabilities) throws DataAccessException;

    public void deleteAvailabilties(Iterable<Integer> availabilities) throws DataAccessException;

}
