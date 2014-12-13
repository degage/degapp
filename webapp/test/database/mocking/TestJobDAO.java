/* TestJobDAO.java
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
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package database.mocking;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.JobDAO;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.JobType;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by HannesM on 9/05/14.
 */
public class TestJobDAO implements JobDAO {
    @Override
    public List<Job> getUnfinishedBefore(DateTime dateTime) throws DataAccessException {
        return null;
    }

    @Override
    public Job createJob(JobType jobType, int i, DateTime dateTime) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteJob(long l) throws DataAccessException {

    }

    @Override
    public void deleteJob(JobType jobType, int i) throws DataAccessException {

    }

    @Override
    public void setJobStatus(long l, boolean b) throws DataAccessException {

    }

    @Override
    public Job getLastJobForType(JobType jobType) throws DataAccessException {
        return null;
    }
}
