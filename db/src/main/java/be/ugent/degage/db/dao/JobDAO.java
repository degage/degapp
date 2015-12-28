/* JobDAO.java
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
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.JobType;

import java.time.Instant;

public interface JobDAO {

    /** Just polls the database connection. Returns an exception when there is something
     * seriously wrong with the database.
     */
    public void ping () throws DataAccessException;

    /** List all jobs that should have been finished by now. */
    public Iterable<Job> listScheduledForNow() throws DataAccessException;

    /** Create a new job for the scheduler to execute at the requested time. */
    public Job createJob(JobType type, Integer refId, Instant when) throws DataAccessException;

    /**
     * Delete the job of the given type and reference id.
     */
    public void deleteJob(JobType type, int refId) throws DataAccessException;

    /**
     * Indicate that the given job is now finished.
     */
    public void finishJob(long jobId) throws DataAccessException;

}
