package be.ugent.degage.db;

/**
 * Created by Cedric on 2/16/14.
 */
public class DataAccessException extends RuntimeException {

    private Exception innerException;

    public DataAccessException(String desc, Exception exception){
        super(desc);
        this.innerException = exception;
    }

    public DataAccessException(String desc){
        this(desc, null);
    }

    public Exception getInnerException() {
        return innerException;
    }

    @Override
    public String getMessage(){
        return super.getMessage() + " -- " + (innerException != null ? innerException.getMessage() : "");
    }
}
