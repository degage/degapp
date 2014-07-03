package controllers.util;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.models.File;
import be.ugent.degage.db.models.User;
import play.mvc.Result;

import java.io.IOException;

/**
 * Created by Cedric on 4/15/2014.
 */
public interface FileAction {
    public Result process(File file, FileDAO dao, DataAccessContext context) throws IOException, DataAccessException;
    public File getFile(int fileId, User user, FileDAO dao, DataAccessContext context) throws DataAccessException;
    public Result failAction(User user);
}
