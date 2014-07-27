package providers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserStatus;
import db.CurrentUser;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Http;

/**
 * Created by Cedric on 2/20/14.
 */
public class UserProvider {

    private static final String USER_BY_ID = "user:id:%d";

    private DataAccessProvider provider;

    public UserProvider(DataAccessProvider provider) {
        this.provider = provider;
    }

    public User getUser() {
        return getUser(true);
    }
    /**
     * Returns the user based on session
     * @param cached Whether to use a cached version or not
     * @return The user for current session
     */
    public User getUser(boolean cached) {
        Integer userId = CurrentUser.getId();
        if (userId == null)
            return null;
        else
            return getUser(userId, cached);
    }

    public User getUserFromHttpSession(Http.Session session){
        // TODO: make this obsolete
        String idString = Controller.session("id");
        if (idString == null)
            return null;
        else
            return getUser(Integer.parseInt(idString), true);
    }


    public void invalidateUser(User user) {
        Cache.remove(String.format(USER_BY_ID, user.getId()));
    }

    public User getUser(Integer userId) throws DataAccessException {
        return getUser(userId, true);
    }

    public User getUser(int userId, boolean cached) throws DataAccessException {

        String key = String.format(USER_BY_ID, userId);
        User user = cached ? (User)Cache.get(key) : null;
        if (user == null) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                UserDAO dao = context.getUserDAO();
                user = dao.getUser(userId);
                if (user != null) { // cache and return
                    Cache.set(key, user);
                }
            } catch (DataAccessException ex) {
                throw new RuntimeException ("Could not get user", ex); //TODO: log
            }
        }
        return user;
    }
}
