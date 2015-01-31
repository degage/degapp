/* Scheduler.java
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

package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.JobDAO;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.JobType;
import be.ugent.degage.db.models.UserHeader;
import db.DataAccess;
import db.RunnableInContext;
import notifiers.Notifier;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Utility methods for jobs that are run repeatedly or in a future time
 */
public final class Scheduler {

    private static final ExecutorService CACHED_POOL = Executors.newCachedThreadPool();

    public static void stop() {
        CACHED_POOL.shutdown();
    }

    /**
     * Start the scheduler and launch periodic tasks (runnables = periodic, jobs = once, persistent)
     */
    public static void start() {

        // send notifications by email to users (check once every hour
        schedule(Duration.create(1, TimeUnit.HOURS),
                new RunnableInContext("Send reminder mails") {
                    @Override
                    public void runInContext(DataAccessContext context) {
                        for (UserHeader user : context.getSchedulerDAO().getReminderEmailList(0)) {
                            Notifier.sendReminderMail(context, user);
                        }
                    }
                }
        );

        // change ride status to finished for every ride with an end date later than now
        // TODO: the above is not entirely correct: only accepted -> request_details
        // TODO: avoid this by looking at the date when querying the database
        schedule(Duration.create(12, TimeUnit.MINUTES),
                new RunnableInContext("Finish rides") {
                    @Override
                    public void runInContext(DataAccessContext context) {
                        context.getReservationDAO().adjustReservationStatuses();
                    }
                });

        // make sure that at least one report job is planned
        // TODO: checkInitialReports();

        // schedule 'jobs' to be run at a fixed interval (standard: every five minutes)
        schedule(Duration.create(db.Utils.getSchedulerInterval(), TimeUnit.SECONDS),
                new RunnableInContext("Job scheduler") {
                    @Override
                    public void runInContext(DataAccessContext context) {
                        for (Job job : context.getJobDAO().listScheduledForNow()) {
                            CACHED_POOL.submit(new ScheduledJob(job));
                        }
                    }
                }
        );
    }

    /**
     * Checks if there's a report already scheduled, and if not, schedule one
     */
    private static void checkInitialReports() {
        // TODO: currently not used
        new RunnableInContext("Launch initial reports") {
            @Override
            public void runInContext(DataAccessContext context) {
                JobDAO dao = context.getJobDAO();
                if (!dao.existsJobOfType(JobType.REPORT)) {
                    dao.createJob(JobType.REPORT, 0,
                            // TODO: bug
                            // Unable to obtain Instant from TemporalAccessor: 2015-02-01T00:00 of type java.time.LocalDateTime
                            Instant.from(LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth()).atStartOfDay())
                    );
                }
            }
        }.run();
    }

    private static void schedule(FiniteDuration repeatDuration, Runnable task) {
        Akka.system().scheduler().schedule(
                Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                repeatDuration,     //Frequency
                task,
                Akka.system().dispatcher()
        );
    }

}


