package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.JobType;
import org.joda.time.DateTime;

import java.util.List;

public interface JobDAO {
    public List<Job> getUnfinishedBefore(DateTime time) throws DataAccessException;
    public Job createJob(JobType type, int refId, DateTime when) throws DataAccessException;
    public void deleteJob(long jobId) throws DataAccessException;
    public void deleteJob(JobType type, int refId) throws DataAccessException;
    public void setJobStatus(long jobId, boolean finished) throws DataAccessException;
    public Job getLastJobForType(JobType type) throws DataAccessException;
}