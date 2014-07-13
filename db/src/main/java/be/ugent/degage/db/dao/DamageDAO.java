package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.CarRide;
import be.ugent.degage.db.models.Damage;
import be.ugent.degage.db.models.File;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 02/05/14.
 */
public interface DamageDAO {

    public Damage createDamage(CarRide carRide) throws DataAccessException;
    public Damage getDamage(int damageId) throws DataAccessException;
    public void updateDamage(Damage damage) throws DataAccessException;
    public void deleteDamage(int damageId);
    public int getAmountOfOpenDamages(int userId) throws DataAccessException;
    public int getAmountOfDamages(Filter filter) throws DataAccessException;
    public List<Damage> getDamages(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;


}

