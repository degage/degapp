/* DataAccess.java
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

package db;/* db.DataAccess.java
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

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.jdbc.JDBCDataAccess;
import play.mvc.Http;

import javax.sql.DataSource;

/**
 * Provides access to the current {@link DataAccessProvider} which was initialized at application start.
 * Also provides some shortcuts for database request that are required often
 */
public class DataAccess {

    private static DataAccessProvider provider;


    /**
     * The current data access provider.
     */
    public static DataAccessProvider getProvider() {
        return provider;
    }

    /**
     * Obtain a data access context from the current data access provider.
     */
    static DataAccessContext getContext() {
        return provider.getDataAccessContext();
    }

    /**
     * Used during initialization to install the data access provider. Do not call this in your own
     * programs.
     */
    public static void setProviderFromDataSource(DataSource datasource) {
        if (provider == null) {
            DataAccess.provider = JDBCDataAccess.createDataAccessProvider(datasource);
        }
    }

    /**
     * Used during initialization to install the data access provider. Do not call this in your own
     * programs.
     */
    public static void setProviderForTesting() {
        if (provider == null) {
            DataAccess.provider = JDBCDataAccess.getTestDataAccessProvider();
        }
    }

    /**
     * Return the data access context that was injected by annotating the current action
     * with {@link InjectContext}.
     */
    public static DataAccessContext getInjectedContext() {
        return (DataAccessContext) Http.Context.current().args.get("data-access-context");
    }

}
