package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.JobType;
import play.Logger;

import java.util.EnumMap;
import java.util.Map;

/**
 * Runnable that puts a certain job in a queue
 */
public class ScheduledJob extends RunnableInContext {

    public static interface Executor {
        public void execute(DataAccessContext context, Job job);
    }

    private static final Map<JobType, Executor> EXECUTORS;

    static {
        EXECUTORS = new EnumMap<>(JobType.class);
        EXECUTORS.put(JobType.IS_REMINDER, new InfoSessionReminderJob());
        EXECUTORS.put(JobType.REPORT, new ReportGenerationJob());
        EXECUTORS.put(JobType.RESERVE_ACCEPT, new ReservationAutoAcceptJob());
    }

    private Job job;

    public ScheduledJob(Job job) {
        super(job.toString());
        this.job = job;
    }

    @Override
    public void runInContext(DataAccessContext context) {
        Executor executor = EXECUTORS.get(job.getType());
        if (executor == null) {
            Logger.error("No executor for type: " + job.getType());
        } else {
            executor.execute(context, job);
            context.getJobDAO().finishJob(job.getId());
        }
    }

}
