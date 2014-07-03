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
