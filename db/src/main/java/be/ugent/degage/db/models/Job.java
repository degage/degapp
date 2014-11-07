package be.ugent.degage.db.models;

/**
 * Keeps track of jobs that need to be run at regular intervals by the scheduler
 */
public class Job {

    private long id;
    private JobType type;
    private Integer refId;

    public Job(long id, JobType type, Integer refId) {
        this.id = id;
        this.type = type;
        this.refId = refId;
    }

    public JobType getType() {
        return type;
    }

    public Integer getRefId() {
        return refId;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString(){
        return String.format("Job type=%s, id=%d, data=%d", type.name(), id, refId);
    }
}