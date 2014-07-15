package providers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.dao.UserRoleDAO;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.models.UserStatus;
import play.cache.Cache;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by Cedric on 3/4/14.
 */
public class UserRoleProvider {

    private static final String ROLES_BY_ID = "role:id:%d";
    private static final UserRole[] ADMIN_ROLES = new UserRole[] { UserRole.CAR_ADMIN, UserRole.INFOSESSION_ADMIN, UserRole.MAIL_ADMIN, UserRole.RESERVATION_ADMIN, UserRole.SUPER_USER };

    private DataAccessProvider provider;
    private UserProvider userProvider;

    public UserRoleProvider(DataAccessProvider provider, UserProvider userProvider) {
        this.provider = provider;
        this.userProvider = userProvider;
    }

    public Set<UserRole> getRoles(int userId) {
        return getRoles(userId, true);
    }

    //TODO: leave this helper function here, or move to the main DataProvider? Decoupling...
    public boolean hasRole(String email, UserRole role) {
        User user = userProvider.getUser(email);
        if (user == null)
            return false;
        else {
            return hasRole(user.getId(), role);
        }
    }

    public boolean hasRole(int id, UserRole role) {
        return hasRole(getRoles(id), role);
    }

    public boolean hasRole(User user, UserRole role) {
        return hasRole(getRoles(user.getId()), role);
    }

    public static boolean hasRole(Set<UserRole> roles, UserRole role) {
        return roles.contains(role) || roles.contains(UserRole.SUPER_USER); // Superuser has all roles!!
    }

    public static boolean hasSomeRole(Set<UserRole> roles, UserRole[] searchFor){
        for(UserRole role : searchFor){
            if(hasRole(roles, role))
                return true;
        }
        return false;
    }

    public boolean hasSomeRole(User user, UserRole[] roles){
        Set<UserRole> l = getRoles(user.getId());
        return hasSomeRole(l, roles);
    }

    public boolean isAdmin(User user){
        return hasSomeRole(user, ADMIN_ROLES);
    }

    public boolean isAdmin(){
        return isAdmin(userProvider.getUser());
    }

    public boolean hasSomeRole(UserRole[] roles){
        User u = userProvider.getUser();
        return hasSomeRole(u, roles);
    }

    public boolean isFullUser(User user) {
        return user.getStatus() == UserStatus.FULL;
    }

    public boolean isFullUser() {
        User user = userProvider.getUser();
        return isFullUser(user);
    }

    public boolean hasRole(UserRole role) {
        User user = userProvider.getUser();
        return hasRole(user, role);
    }

    public Set<UserRole> getRoles(int userId, boolean cached) {

        String key = String.format(ROLES_BY_ID, userId);

        Object obj = null;
        if (cached) {
            obj = Cache.get(key);
        }

        if (obj == null || !(obj instanceof EnumSet)) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                UserRoleDAO dao = context.getUserRoleDAO();
                Set<UserRole> roles = dao.getUserRoles(userId);
                if (roles != null) { // cache and return
                    Cache.set(key, roles);
                    return roles;
                } else {
                    return null;
                }
            } catch (DataAccessException ex) {
                throw ex;
            }
        } else {
            return (EnumSet<UserRole>) obj; //Type erasure problem from Java, works at runtime
        }
    }

    public void invalidateRoles(User user) {
        Cache.remove(String.format(ROLES_BY_ID, user.getId()));
    }
}
