package providers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserStatus;
import org.mindrot.jbcrypt.BCrypt;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Http;

/**
 * Created by Cedric on 2/20/14.
 */
public class UserProvider {

    private static final String USER_BY_EMAIL = "user:email:%s";

    private DataAccessProvider provider;

    public UserProvider(DataAccessProvider provider) {
        this.provider = provider;
    }

    public User getUser(String email) throws DataAccessException {
        return getUser(email, true);
    }

    /**
     * Returns the user based on session
     * @param cached Whether to use a cached version or not
     * @return The user for current session
     */
    public User getUser(boolean cached){
        return getUser(Controller.session(), cached);
    }

    public User getUser(Http.Session session, boolean cached){
        String email = session.get("email");
        if(email == null || email.isEmpty())
            return null;
        else return getUser(email, cached);
    }

    public static boolean hasValidPassword(User user, String password){
        return user != null && BCrypt.checkpw(password, user.getPassword());
    }

    /**
     * Hashes a password using the BCRYPT iteration hashing method including a salt.
     *
     * @param password The password to be hashed
     * @return The hashed password including the salt
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public User getUser() {
        return getUser(true);
    }

    public void createUserSession(User user){
        Controller.session("email", user.getEmail());
    }

    public void invalidateUser(User user) {
        Cache.remove(String.format(USER_BY_EMAIL, user.getEmail()));
    }

    public static boolean isBlocked(User user){
        return user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.DROPPED || user.getStatus() == UserStatus.EMAIL_VALIDATING;
    }

    public User getUser(String email, boolean cached) throws DataAccessException {
        if (email == null) {
            return null;
        }

        String key = String.format(USER_BY_EMAIL, email);

        Object obj = null;
        if (cached) {
            obj = Cache.get(key);
        }

        if (obj == null || !(obj instanceof User)) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                UserDAO dao = context.getUserDAO();
                User user = dao.getUser(email);
                //user.setPassword(""); // We wipe the password in RAM, all classes using this in controllers shouldn't use cache!
                if (user != null) { // cache and return
                    Cache.set(key, user);
                    return user;
                } else {
                    return null;
                }
            } catch (DataAccessException ex) {
                throw ex; //TODO: log
            }
        } else {
            return (User) obj;
        }
    }
}
