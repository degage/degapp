package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;
import java.sql.SQLException;

/**
 * Used for getting system properties
 */
public interface PropertyDAO {

    public Property getProperty(int propertyId) throws DataAccessException;

    public Property getPropertyByKey(String key) throws DataAccessException;

    public void updateProperty(Property property) throws DataAccessException;

}
