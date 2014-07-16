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

    private UserProvider userProvider;

    public UserRoleProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

   public boolean isFullUser(User user) {
        return user.getStatus() == UserStatus.FULL;
    }

    public boolean isFullUser() {
        User user = userProvider.getUser();
        return isFullUser(user);
    }
}
