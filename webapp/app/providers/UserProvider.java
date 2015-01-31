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
 * distribution).  If not, see http://www.gnu.org/licenses/.
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

    public void invalidateUser(int userId) {
        Cache.remove(String.format(USER_BY_ID, userId));
    }

    public User getUser() {
        Integer userId = CurrentUser.getId();
        if (userId == null)
            return null;
        else {
            String key = String.format(USER_BY_ID, userId);
            User user =  (User) Cache.get(key);
            if (user == null) {
                // TODO: use injected context for this?
                try (DataAccessContext context = provider.getDataAccessContext()) {
                    UserDAO dao = context.getUserDAO();
                    user = dao.getUser(userId);
                    if (user != null) { // cache and return
                        Cache.set(key, user);
                    }
                } catch (DataAccessException ex) {
                    throw new RuntimeException("Could not get user", ex); //TODO: log
                }
            }
            return user;
        }
    }
}
