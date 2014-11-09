package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Damage;
import be.ugent.degage.db.models.DamageLog;

import java.util.List;

/**
 *
 */
public interface DamageLogDAO {

    /**
     * Add a message to the log for the given damage
     */
    public void addDamageLog(int damageId, String message) throws DataAccessException;

    public Iterable<DamageLog> getDamageLogsForDamage(int damageId) throws DataAccessException;

}
