package schedulers.joblessprocesses;

import java.lang.reflect.Constructor;
import java.util.Map;

import be.ugent.degage.db.DataAccessContext;
import db.RunnableInContext;
/**
 * These are processes which will be called in the scheduler without having an entry in the job table since these 
 * processes need to be run frequently : if job -> too much entries in job table. 
 * 
 * 
 * TEMPORARY PATCH UP TO FIX HARD CODING IN SCHEDULER.
 * PLEASE REFRACTOR IT ONCE MORE IF POSSIBLE SO THAT JOBLESS- AND JOB-PROCESSES ARE UNDER ONE SUPER CLASS.
 */
public abstract class JoblessProcess extends RunnableInContext {

    private String name; 
    public enum JoblessProcessType {
        MEMBERSHIP_UPDATE,
        PAYMENT_REMINDER
    }

    public JoblessProcess(String ricName, String name){
        super(ricName);

        if(name == null ||name.isEmpty()){
            throw new IllegalArgumentException("Jobless process's name is empty or null: " + name);
        }
        this.name = name;
    }
    public void runProcess(DataAccessContext context){
        this.runInContext(context);
    }
}