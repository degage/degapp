/* ScheduledJob.java
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

package schedulers.jobprocesses;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.JobType;
import db.RunnableInContext;
import play.Logger;

import java.util.EnumMap;
import java.util.Map;

/**
 * Runnable that puts a certain job in a queue
 */
public class ScheduledJob extends RunnableInContext {

    public static interface Executor {
        public void execute(DataAccessContext context, Job job);
    }

    private static final Map<JobType, Executor> EXECUTORS;

    static {
        EXECUTORS = new EnumMap<>(JobType.class);
        EXECUTORS.put(JobType.IS_REMINDER, new InfoSessionReminderJob()); 
        // EXECUTORS.put(JobType.IS_POST_INFOSESSION, new PostInfoSessionJob());
    }

    private Job job;

    public ScheduledJob(Job job) {
        super(job.toString());
        this.job = job;
    }

    @Override
    public void runInContext(DataAccessContext context) {
        Executor executor = EXECUTORS.get(job.getType());
        if (executor == null) {
            Logger.error("No executor for type: " + job.getType());
        } else {
            executor.execute(context, job);
            context.getJobDAO().finishJob(job.getId());
        }
    }

}
