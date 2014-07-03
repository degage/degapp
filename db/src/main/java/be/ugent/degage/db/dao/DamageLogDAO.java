package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Damage;
import be.ugent.degage.db.models.DamageLog;

import java.util.List;

/**
 * Created by stefaan on 04/05/14.
 */
public interface DamageLogDAO {

    public DamageLog createDamageLog(Damage damage, String description) throws DataAccessException;
    public List<DamageLog> getDamageLogsForDamage(int damageId) throws DataAccessException;
}
