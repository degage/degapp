/* RefuelDAO.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.models.Refuel;
import be.ugent.degage.db.models.RefuelExtended;
import be.ugent.degage.db.models.ApprovalStatus;

/**
 * DAO for managing refuel information
 */
public interface RefuelDAO {

    public int createRefuel(int reservationId, int eurocents, int fileId, ApprovalStatus status,
                            int km, String amount) throws DataAccessException;

    /**
     * Update the status of the refuel record (but not the message)
     */
    public void updateRefuelStatus(ApprovalStatus status, int refuelId) throws DataAccessException;

    /**
     * Set the status to refused and store the reason why
     */
    public void rejectRefuel(int refuelId, String message) throws DataAccessException;

    public Refuel getRefuel(int refuelId) throws DataAccessException; // TODO: not used?

    public RefuelExtended getRefuelExtended(int refuelId) throws DataAccessException;

    // note: only shows refuels that are not archived
    public Page<RefuelExtended> getRefuels(int page, int pageSize, Filter filter) throws DataAccessException;

    public Page<RefuelExtended> getRefuels(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    // note: only shows refuels that are not archived
    public Iterable<Refuel> getRefuelsForCarRide(int reservationId) throws DataAccessException;

    /**
     * Shows the number of outstanding refuel requests for a reservation with the given owner.
     */
    public int numberOfRefuelRequests(int ownerId) throws DataAccessException;

}
