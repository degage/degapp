/* JDBCJobDAO.java
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

package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.JobDAO;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.JobType;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Cedric on 5/3/2014.
 */
class JDBCJobDAO extends AbstractDAO implements JobDAO {


    public JDBCJobDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private LazyStatement getListUnfinishedStatement = new LazyStatement(
            "SELECT job_id, job_type, job_ref_id, job_time, job_finished " +
                    "FROM jobs WHERE NOT job_finished AND job_time <= now()"
    );

    @Override
    public Iterable<Job> listScheduledForNow() throws DataAccessException {
        try {
            PreparedStatement ps = getListUnfinishedStatement.value();
            Collection<Job> jobs = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jobs.add(new Job(
                            rs.getLong("job_id"),
                            Enum.valueOf(JobType.class, rs.getString("job_type")),
                            (Integer)rs.getObject("job_ref_id")
                    ));
                }
                return jobs;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch unfinished jobs.", ex);
        }
    }

    private LazyStatement createJobStatement = new LazyStatement(
            "INSERT INTO jobs(job_type, job_ref_id, job_time, job_finished) " +
                    "VALUES(?,?,?,false)",
            "job_id"
    );

    /**
     * Create a new job for the scheduler to be executed at the requested time.
     */
    @Override
    public Job createJob(JobType type, Integer refId, Instant when) throws DataAccessException {
        try {
            PreparedStatement ps = createJobStatement.value();
            ps.setString(1, type.name());
            ps.setObject(2, refId, Types.INTEGER);
            ps.setTimestamp(3, Timestamp.from(when));

            if (ps.executeUpdate() != 1) {
                throw new DataAccessException("New job record failed. No rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException("Failed to read keys for new job record.");
                } else {
                    return new Job(keys.getLong(1), type, refId);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create job.", ex);
        }
    }

    private LazyStatement deleteJobByTypeStatement = new LazyStatement(
            "DELETE FROM jobs WHERE job_type=? AND job_ref_id=?"
    );

    @Override
    public void deleteJob(JobType type, int refId) throws DataAccessException {
        try {
            PreparedStatement ps = deleteJobByTypeStatement.value();
            ps.setString(1, type.name());
            ps.setObject(2, refId, Types.INTEGER);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete job.", ex);
        }
    }

    private LazyStatement updateJobStatement = new LazyStatement(
            "UPDATE jobs SET job_finished=true WHERE job_id=?"
    );

    @Override
    public void finishJob(long jobId) throws DataAccessException {
        try {
            PreparedStatement ps = updateJobStatement.value();
            ps.setLong(1, jobId);

            if (ps.executeUpdate() != 1) {
                throw new DataAccessException("Failed to update job status.");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update job status.", ex);
        }
    }

    private LazyStatement getLastJobByTypeStatement = new LazyStatement(
            "SELECT 1 FROM jobs WHERE job_type=?"
    );

    @Override
    public boolean existsJobOfType(JobType type) throws DataAccessException {
        try {
            PreparedStatement ps = getLastJobByTypeStatement.value();
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get last job by type.", ex);
        }
    }
}
