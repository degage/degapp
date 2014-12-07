package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.JobType;

import java.time.Instant;

public interface JobDAO {

    /** List all jobs that should have been finished by now. */
    public Iterable<Job> listScheduledForNow() throws DataAccessException;

    /** Create a new job for the scheduler to execute at the requested time. */
    public Job createJob(JobType type, Integer refId, Instant when) throws DataAccessException;

    /**
     * Delete the job of the given type and reference id.
     * @param type
     * @param refId
     * @throws DataAccessException
     */
    public void deleteJob(JobType type, int refId) throws DataAccessException;


    /**
     * Indicate that the given job is now finished.
     * @param jobId
     * @throws DataAccessException
     */
    public void finishJob(long jobId) throws DataAccessException;

    /**
     * Does a job of this type exist in the database?
     * @param type
     * @return
     * @throws DataAccessException
     */
    public boolean existsJobOfType(JobType type) throws DataAccessException;
}