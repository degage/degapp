/* ReportGenerationJob.java
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
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.Costs;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.User;
import reports.ReportGeneration;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Job that generates a report
 */
public class ReportGenerationJob implements ScheduledJob.Executor {
    @Override
    public void execute(DataAccessContext context, Job job) {

        /* TODO: verify that these statements do the correct thing
        context.getCarRideDAO().endPeriod();
        context.getRefuelDAO().endPeriod();
        context.getCarCostDAO().endPeriod();
        */

        UserDAO dao = context.getUserDAO();

        List<User> users = dao.getUserList(FilterField.USER_NAME, true, 1, dao.getAmountOfUsers(null), null);

        Costs costInfo = context.getSettingDAO().getCostSettings(Instant.now());
        for (User user : users) {
            ReportGeneration.generateReceipt(context, user, LocalDate.now(), costInfo);
        }
    }
}
