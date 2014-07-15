package providers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.dao.UserRoleDAO;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.models.UserStatus;
import db.CurrentUser;
import play.cache.Cache;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by Cedric on 3/4/14.
 */
public class UserRoleProvider {

    private static final String ROLES_BY_ID = "role:id:%d";

    private DataAccessProvider provider;
    private UserProvider userProvider;

    public UserRoleProvider(DataAccessProvider provider, UserProvider userProvider) {
        this.provider = provider;
        this.userProvider = userProvider;
    }

    public Set<UserRole> getRoles(int userId) {
        return getRoles(userId, true);
    }

    public boolean hasRole(User user, UserRole role) {
        return hasRole(getRoles(user.getId()), role);
    }

    public static boolean hasRole(Set<UserRole> roles, UserRole role) {
        return roles.contains(role) || roles.contains(UserRole.SUPER_USER); // Superuser has all roles!!
    }

    public static boolean hasSomeRole(Set<UserRole> roles, UserRole role1, UserRole role2){
        return roles.contains(UserRole.SUPER_USER) || roles.contains(role1) || roles.contains(role2);
    }

    public boolean isFullUser(User user) {
        return user.getStatus() == UserStatus.FULL;
    }

    public boolean isFullUser() {
        User user = userProvider.getUser();
        return isFullUser(user);
    }

    public boolean isAdmin (User user) {
        // TODO: only needed because admins can change profiles too
        Set<UserRole> roleSet = getRoles(user.getId());
        return roleSet.contains (UserRole.SUPER_USER) ||
                roleSet.contains (UserRole.CAR_ADMIN) ||
                roleSet.contains (UserRole.INFOSESSION_ADMIN) ||
                roleSet.contains (UserRole.MAIL_ADMIN) ||
                roleSet.contains (UserRole.RESERVATION_ADMIN);
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
