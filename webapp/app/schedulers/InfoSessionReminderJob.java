/* InfoSessionReminderJob.java
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
import be.ugent.degage.db.dao.InfoSessionDAO;
import be.ugent.degage.db.models.Enrollee;
import be.ugent.degage.db.models.InfoSession;
import be.ugent.degage.db.models.Job;
import notifiers.Notifier;
import play.Logger;

/**
 * Job that sends a reminder of an infosession to all participants
 */
public class InfoSessionReminderJob implements ScheduledJob.Executor {
    @Override
    public void execute(DataAccessContext context, Job job) {
        InfoSessionDAO dao = context.getInfoSessionDAO();
        int sessionId = job.getRefId();
        InfoSession session = dao.getInfoSession(sessionId);
        if (session != null) {
            for (Enrollee er : dao.getEnrollees(sessionId)) {
                Notifier.sendInfoSessionEnrolledMail(context, er.getUser(), session); //TODO: use separate correct notifier
                Logger.debug("Sent infosession reminder mail to " + er.getUser());
            }
        }
    }
}
