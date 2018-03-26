package schedulers.joblessprocesses;

import java.util.HashMap;
import java.util.Map;

import be.ugent.degage.db.DataAccessContext;
import schedulers.joblessprocesses.JoblessProcess;
import schedulers.joblessprocesses.JoblessProcess.JoblessProcessType;

public class JoblessProcessFactory {

    private static final Map<JoblessProcessType, JoblessProcess> joblessProcessMap;

    static{
        joblessProcessMap = new HashMap<>();
        joblessProcessMap.put(JoblessProcessType.MEMBERSHIP_UPDATE, new MembershipUpdateJob());
        joblessProcessMap.put(JoblessProcessType.PAYMENT_REMINDER, new PaymentReminderJob());
    }


    public static JoblessProcess getProcess(JoblessProcessType type){
        return joblessProcessMap.get(type);
    }

}