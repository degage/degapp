/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Location;

/**
 *
 * @author Laurent
 */
public interface LocationDAO {
    public Location createLocation(int zip, String location) throws DataAccessException;
    public void updateLocation(Location location) throws DataAccessException;
    public Location getLocation(int zip) throws DataAccessException;
}
