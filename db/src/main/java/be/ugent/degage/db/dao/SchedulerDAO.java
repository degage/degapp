package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.UserHeader;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 27/04/14.
 */
public interface SchedulerDAO {

    public Iterable<UserHeader> getReminderEmailList(int maxMessages) throws DataAccessException;
    public void setReminded(int userId) throws DataAccessException;
}
