package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.models.Damage;
import be.ugent.degage.db.models.Reservation;
import org.joda.time.DateTime;

/**
 * Created by Stefaan Vermassen on 02/05/14.
 */
public interface DamageDAO {

    public Damage createDamage(Reservation reservation) throws DataAccessException;
    public Damage getDamage(int damageId) throws DataAccessException;

    public void updateDamageFinished(int damageId, boolean finished) throws DataAccessException;
    public void updateDamageDetails (int damageId, String description, DateTime time) throws DataAccessException;

    public int getAmountOfOpenDamages(int userId) throws DataAccessException;

    /**
     * Retrieve a list of damages for the given driver
     */
    public Iterable<Damage> listDamagesForDriver (int driverId) throws DataAccessException;

    /**
     * Retrieve a list of damages for the given owner
     */
    public Iterable<Damage> listDamagesForOwner (int driverId) throws DataAccessException;

    /**
     * Retrieve a filtered list of damages
     */
    public Iterable<Damage> getDamages(int page, int pageSize, Filter filter) throws DataAccessException;

    /**
     * Size of the filtered list as produced by {@link #getDamages}.
     */
    public int getAmountOfDamages(Filter filter) throws DataAccessException;


}

