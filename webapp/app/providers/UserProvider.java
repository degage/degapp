/* UserProvider.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package providers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.User;
import db.CurrentUser;
import play.cache.Cache;

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


    public void invalidateUser(int userId) {
        Cache.remove(String.format(USER_BY_ID, userId));
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
