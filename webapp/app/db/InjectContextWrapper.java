/* InjectContextWrapper.java
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

package db;

import be.ugent.degage.db.DataAccessContext;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Implements the {@link InjectContext} annotation.
 */
public class InjectContextWrapper extends Action<InjectContext> {

    @Override
    public F.Promise<Result> call(Http.Context httpContext) throws Throwable {

        // create a context, and run the action delegate in that transaction
        try (DataAccessContext dac = DataAccess.getContext()) {
            httpContext.args.put("data-access-context", dac);

            if (configuration.inTransaction()) {
                try {
                    dac.begin();
                    F.Promise<Result> result = delegate.call(httpContext);
                    dac.commit();
                    return result;
                } catch (Exception ex) {
                    dac.rollback();
                    throw ex;
                }
            } else {
                return delegate.call(httpContext);
            }
        }
    }
}
