/* RunnableInContext.java
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

package schedulers;

import be.ugent.degage.db.DataAccessContext;
import db.DataAccess;
import play.Logger;

/**
 * Runnable that runs within a database context
 */
public abstract class RunnableInContext implements Runnable {

    public abstract void runInContext (DataAccessContext context);

    private String msg;

    public RunnableInContext (String msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        try (DataAccessContext dac = DataAccess.getContext()) {
            try {
                dac.begin();
                runInContext(dac);
                Logger.info("Finished job: " + msg);
                dac.commit();
            } catch (Exception ex) {
                dac.rollback();
                Logger.error("Error during job: " + msg, ex);
                throw ex;
            }
        }
    }
}
