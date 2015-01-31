/* */

package controllers;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.JobType;
import db.DataAccess;
import play.mvc.Controller;
import play.mvc.Result;
import db.InjectContext;

/**
 * Created by kc on 1/29/15.
 */
public class Diagnostics  extends Controller {

    /**
     * Just returns ok. Used to check whether the application is still alive
     * @return
     */
    public static Result checkApplication() {
        return ok();
    }

    /**
     * Contacts the database and returns OK if there is no exception
     */
    @InjectContext
    public static Result checkDatabase() {
        try {
            DataAccess.getInjectedContext().getJobDAO().existsJobOfType(JobType.DRIVE_FINISH);
            return ok();
        } catch (DataAccessException ex) {
            return internalServerError();
        }
    }
}
