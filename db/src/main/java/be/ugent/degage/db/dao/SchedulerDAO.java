package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.User;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 27/04/14.
 */
public interface SchedulerDAO {

    public List<User> getReminderEmailList(int maxMessages) throws DataAccessException;
    public void setReminded(User user) throws DataAccessException;
}
