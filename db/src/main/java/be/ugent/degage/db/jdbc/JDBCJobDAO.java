package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.JobDAO;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.JobType;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 5/3/2014.
 */
class JDBCJobDAO extends AbstractDAO implements JobDAO {

    private static final String JOB_FIELDS = "job_id, job_type, job_ref_id, job_time, job_finished";


    public JDBCJobDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private Job populateJob(ResultSet rs) throws SQLException {
        return new Job(rs.getInt("job_id"), Enum.valueOf(JobType.class, rs.getString("job_type")),
                new DateTime(rs.getTimestamp("job_time").getTime()), rs.getBoolean("job_finished"), rs.getObject("job_ref_id") == null ? -1 : rs.getInt("job_ref_id"));
    }

    private LazyStatement getUnfinishedJobsAfterStatement = new LazyStatement(
            "SELECT "+JOB_FIELDS+" FROM jobs WHERE job_finished=0 AND job_time <= ?"
    );

    @Override
    public List<Job> getUnfinishedBefore(DateTime time) throws DataAccessException {
        try {
            PreparedStatement ps = getUnfinishedJobsAfterStatement.value();
            ps.setTimestamp(1, new Timestamp(time.getMillis()));
            List<Job> jobs = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    jobs.add(populateJob(rs));
                }
                return jobs;
            } catch(SQLException ex){
                throw new DataAccessException("Failed to read job resultset.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to fetch unfinished jobs.", ex);
        }
    }

    private LazyStatement createJobStatement = new LazyStatement(
            "INSERT INTO jobs(job_type, job_ref_id, job_time, job_finished) VALUES(?,?,?,?)",
            "job_id"
    );

    @Override
    public Job createJob(JobType type, int refId, DateTime when) throws DataAccessException {
        try {
            PreparedStatement ps = createJobStatement.value();
            ps.setString(1, type.name());
            ps.setInt(2, refId);
            ps.setTimestamp(3, new Timestamp(when.getMillis()));
            ps.setBoolean(4, false);

            if (ps.executeUpdate() != 1)
                throw new DataAccessException("New job record failed. No rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next())
                    throw new DataAccessException("Failed to read keys for new job record.");
                return new Job(keys.getInt(1), type, when, false, refId);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for new job.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to create job.", ex);
        }
    }

    private LazyStatement deleteJobByIdStatement = new LazyStatement("DELETE FROM jobs WHERE job_id=?");

    @Override
    public void deleteJob(long jobId) throws DataAccessException {
        try {
            PreparedStatement ps = deleteJobByIdStatement.value();
            ps.setLong(1, jobId);
            if(ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to delete job by id. No rows affected.");
        } catch(SQLException ex){
            throw new DataAccessException("Failed to delete job.", ex);
        }
    }

    private LazyStatement deleteJobByTypeStatement = new LazyStatement(
            "DELETE FROM jobs WHERE job_type=? AND job_ref_id=? AND job_finished=0"
    );

    @Override
    public void deleteJob(JobType type, int refId) throws DataAccessException {
        try {
            PreparedStatement ps = deleteJobByTypeStatement.value();
            ps.setString(1, type.name());
            ps.setInt(2, refId);
            ps.executeUpdate();
        } catch(SQLException ex){
            throw new DataAccessException("Failed to delete job.", ex);
        }
    }

    private LazyStatement updateJobStatement = new LazyStatement(
            "UPDATE jobs SET job_finished=? WHERE job_id=?"
    );

    @Override
    public void setJobStatus(long jobId, boolean finished) throws DataAccessException {
        try {
            PreparedStatement ps = updateJobStatement.value();
            ps.setBoolean(1, finished);
            ps.setLong(2, jobId);

            if(ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to update job status.");
        } catch(SQLException ex){
            throw new DataAccessException("Failed to update job status.", ex);
        }
    }

    private LazyStatement getLastJobByTypeStatement = new LazyStatement(
            "SELECT "+JOB_FIELDS+" FROM jobs WHERE job_type=? ORDER BY job_time DESC LIMIT 1"
    );

    @Override
    public Job getLastJobForType(JobType type) throws DataAccessException {
        try {
            PreparedStatement ps = getLastJobByTypeStatement.value();
            ps.setString(1, type.name());

            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next())
                    return null;
                else return populateJob(rs);
            } catch(SQLException ex){
                throw new DataAccessException("Failed to read job resultset.", ex);
            }
        } catch(SQLException ex){
            throw new DataAccessException("Failed to get last job by type.", ex);
        }
    }
}
