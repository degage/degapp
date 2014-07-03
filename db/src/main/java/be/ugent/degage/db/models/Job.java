package be.ugent.degage.db.models;
import org.joda.time.DateTime;

public class Job {

    private long id;
    private JobType type;
    private DateTime when;
    private boolean finished;
    private int refId;

    public Job(long id, JobType type, DateTime when, boolean finished, int refId) {
        this.id = id;
        this.type = type;
        this.when = when;
        this.finished = finished;
        this.refId = refId;
    }

    public Job(JobType type, DateTime when, boolean finished, int refId) {
        this(0, type, when, finished, refId);
    }

    public JobType getType() {
        return type;
    }

    public void setType(JobType type) {
        this.type = type;
    }

    public DateTime getWhen() {
        return when;
    }

    public void setWhen(DateTime when) {
        this.when = when;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getRefId() {
        return refId;
    }

    public void setRefId(int refId) {
        this.refId = refId;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString(){
        return String.format("Job type=%s, id=%d, when=%s, finished=%b, data=%d", type.name(), id, when.toString(), finished, refId);
    }

    @Override
    public int hashCode(){
        return (int)getId();
    }

    @Override
    public boolean equals(Object o){
        if(o == null || !(o instanceof  Job))
            return false;

        return ((Job) o).getId() == getId();
    }
}