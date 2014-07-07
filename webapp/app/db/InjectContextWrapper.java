/* InjectContextWrapper.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright (C) 2013 Universiteit Gent
 * 
 * This file is part of the Rasbeb project, an interactive web
 * application for the Belgian version of the international Bebras
 * competition.
 * 
 * Corresponding author:
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License can be found in the file
 * LICENSE.txt provided with the source distribution of this program (see
 * the META-INF directory in the source jar). This license can also be
 * found on the GNU website at http://www.gnu.org/licenses/gpl.html.
 * 
 * If you did not receive a copy of the GNU General Public License along
 * with this program, contact the lead developer, or write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
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
