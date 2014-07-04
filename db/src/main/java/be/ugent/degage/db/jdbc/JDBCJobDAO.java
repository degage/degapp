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
class JDBCJobDAO implements JobDAO {

    private static final String JOB_FIELDS = "job_id, job_type, job_ref_id, job_time, job_finished";

    private Connection connection;
    private PreparedStatement getUnfinishedJobsAfterStatement;
    private PreparedStatement createJobStatement;
    private PreparedStatement deleteJobByTypeStatement;
    private PreparedStatement deleteJobByIdStatement;
    private PreparedStatement updateJobStatement;
    private PreparedStatement getLastJobByTypeStatement;

    public JDBCJobDAO(Connection connection){
        this.connection = connection;
    }

    private PreparedStatement getGetLastJobByTypeStatement() throws SQLException {
        if(getLastJobByTypeStatement == null){
            getLastJobByTypeStatement = connection.prepareStatement("SELECT "+JOB_FIELDS+" FROM jobs WHERE job_type=? ORDER BY job_time DESC LIMIT 1");
        }
        return getLastJobByTypeStatement;
    }

    private PreparedStatement getGetUnfinishedJobsAfterStatement() throws SQLException {
        if(getUnfinishedJobsAfterStatement == null){
            getUnfinishedJobsAfterStatement = connection.prepareStatement("SELECT "+JOB_FIELDS+" FROM jobs WHERE job_finished=0 AND job_time <= ?");
        }
        return getUnfinishedJobsAfterStatement;
    }

    private PreparedStatement getUpdateJobStatement() throws SQLException {
        if(updateJobStatement == null){
            updateJobStatement = connection.prepareStatement("UPDATE jobs SET job_finished=? WHERE job_id=?");
        }
        return updateJobStatement;
    }

    private PreparedStatement getCreateJobStatement() throws SQLException {
        if(createJobStatement == null){
            createJobStatement = connection.prepareStatement("INSERT INTO jobs(job_type, job_ref_id, job_time, job_finished) VALUES(?,?,?,?)", new String[] { "job_id" });
        }
        return createJobStatement;
    }

    private PreparedStatement getDeleteJobByTypeStatement() throws SQLException {
        if(deleteJobByTypeStatement == null){
            deleteJobByTypeStatement = connection.prepareStatement("DELETE FROM jobs WHERE job_type=? AND job_ref_id=? AND job_finished=0");
        }
        return deleteJobByTypeStatement;
    }

    private PreparedStatement getDeleteJobByIdStatement() throws SQLException {
        if(deleteJobByIdStatement == null){
            deleteJobByIdStatement = connection.prepareStatement("DELETE FROM jobs WHERE job_id=?");
        }
        return deleteJobByIdStatement;
    }

    private Job populateJob(ResultSet rs) throws SQLException {
        return new Job(rs.getInt("job_id"), Enum.valueOf(JobType.class, rs.getString("job_type")),
                new DateTime(rs.getTimestamp("job_time").getTime()), rs.getBoolean("job_finished"), rs.getObject("job_ref_id") == null ? -1 : rs.getInt("job_ref_id"));
    }

    @Override
    public List<Job> getUnfinishedBefore(DateTime time) throws DataAccessException {
        try {
            PreparedStatement ps = getGetUnfinishedJobsAfterStatement();
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

    @Override
    public Job createJob(JobType type, int refId, DateTime when) throws DataAccessException {
        try {
            PreparedStatement ps = getCreateJobStatement();
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

    @Override
    public void deleteJob(long jobId) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteJobByIdStatement();
            ps.setLong(1, jobId);
            if(ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to delete job by id. No rows affected.");
        } catch(SQLException ex){
            throw new DataAccessException("Failed to delete job.", ex);
        }
    }

    @Override
    public void deleteJob(JobType type, int refId) throws DataAccessException {
        try {
            PreparedStatement ps = getDeleteJobByTypeStatement();
            ps.setString(1, type.name());
            ps.setInt(2, refId);
            ps.executeUpdate();
        } catch(SQLException ex){
            throw new DataAccessException("Failed to delete job.", ex);
        }
    }

    @Override
    public void setJobStatus(long jobId, boolean finished) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateJobStatement();
            ps.setBoolean(1, finished);
            ps.setLong(2, jobId);

            if(ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to update job status.");
        } catch(SQLException ex){
            throw new DataAccessException("Failed to update job status.", ex);
        }
    }

    @Override
    public Job getLastJobForType(JobType type) throws DataAccessException {
        try {
            PreparedStatement ps = getGetLastJobByTypeStatement();
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
