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
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.UserHeader;
import be.ugent.degage.db.models.ReminderAndUserAndInvoice;
import be.ugent.degage.db.models.InvoiceAndUser;
import be.ugent.degage.db.models.InfoSession;
import be.ugent.degage.db.models.Enrollee;
import be.ugent.degage.db.dao.InfoSessionDAO;
import controllers.api.ApiInvoices;
import db.RunnableInContext;
import notifiers.Notifier;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import schedulers.jobprocesses.ScheduledJob;
import schedulers.joblessprocesses.JoblessProcessFactory;
import schedulers.joblessprocesses.JoblessProcess.JoblessProcessType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import controllers.Reminders;
import play.Logger;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

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

      // send notifications by email to users (check once every day
      // Now should also attach relevant pdf to the email too
      schedule(Duration.create(1, TimeUnit.DAYS),
              new RunnableInContext("Send payment reminder mails") {
                  @Override
                  public void runInContext(DataAccessContext context) {                     
                    //   Commented code function replaced by the next schedule block 
                    //   -----------------OLD WORKING CODE-------------------
                    //   Logger.debug("Send payment reminder mail on " + LocalDate.now().toString());
                    //   Reminders.updateReminders(context);
                    //   LocalDate lastPaymentDate = context.getPaymentDAO().getLastPaymentDate();
                    //   for (ReminderAndUserAndInvoice rui : context.getReminderDAO().listUnsentReminders()) {
                    //       Notifier.sendPaymentReminderMail(context, rui, lastPaymentDate);
                    //       Logger.debug("Sent payment reminder mail to " + rui.getUser());
                    //   }
                    if (context.getSchedulerDAO().isCodaUploadReminderToBeSent()){
                        Notifier.sendCodaUploadReminder(context);
                        Logger.debug("Sent coda upload reminder to admin@degage.be");
                    }
                  }
              }
      );

      schedule(Duration.create(1, TimeUnit.DAYS), JoblessProcessFactory.getProcess(JoblessProcessType.PAYMENT_REMINDER));
      schedule(Duration.create(1, TimeUnit.HOURS), JoblessProcessFactory.getProcess(JoblessProcessType.MEMBERSHIP_UPDATE));

        // send membership invoice by email to users who were present at infosessions and have their contract date filled in
        schedule(Duration.create(1, TimeUnit.HOURS),
            new RunnableInContext("Send membership invoice mails") {
                  @Override
                  public void runInContext(DataAccessContext context) {
                      //only run at 3AM
                      if (LocalDateTime.now().getHour() != 3) {
                          return;
                      }
                      System.out.println(LocalDateTime.now() + ": send membership invoices: ");
                      Logger.debug("Send membership invoice mail on " + LocalDateTime.now().toString());
                      for (int userId : context.getInvoiceDAO().listUsersWithoutMembershipInvoices()) {
                          InvoiceAndUser invoiceAndUser = context.getInvoiceDAO().createMembershipInvoiceAndUser(userId);
                          System.out.println(invoiceAndUser.getInvoice());
                          System.out.println(invoiceAndUser.getUser());
                          Notifier.sendMembershipInvoiceMail(context, invoiceAndUser);
                          Logger.debug("Sent membership invoice to " + userId);
                          System.out.println("Sent membership invoice to " + userId);
                      }
                  }
            }
        );

        // send mails to enrollees the day after an infosession
        schedule(Duration.create(1, TimeUnit.HOURS),
            new RunnableInContext("Send infosession mails") {
                @Override
                public void runInContext(DataAccessContext context) {
                    //only run at 4AM
                    if (LocalDateTime.now().getHour() != 3) {
                        return;
                    }
                    System.out.println(LocalDateTime.now() + ": send mail to infosession enrollees after the session");
                    Logger.debug("Send infosession mail on " + LocalDateTime.now().toString());
                    InfoSessionDAO dao = context.getInfoSessionDAO();
                    List<InfoSession> infoSessions = dao.getInfoSessionsOfYesterday();
                    for (InfoSession infoSession : infoSessions) {
                        System.out.println("infosession: " + infoSession);
                        for (Enrollee er : dao.getEnrollees(infoSession.getId())) {
                            Notifier.sendPostInfoSessionMail(context, er, infoSession);
                            System.out.println("send mail to enrollee: " + er);
                            Logger.debug("Sent infosession reminder mail to " + er.getUser());
                        }
                    }
                  }
            }
        );

        // // change ride status from ACCEPTED to REQUEST_DETAILS for every ride with an
        // // end date later than now
        // /* now done by the mysql event scheduler
        // schedule(Duration.create(12, TimeUnit.MINUTES),
        //         new RunnableInContext("Finish rides") {
        //             @Override
        //             public void runInContext(DataAccessContext context) {
        //                 context.getReservationDAO().adjustReservationStatuses();
        //             }
        //         });
        //         */
        // schedule(Duration.create(1, TimeUnit.DAYS), JoblessProcessFactory.getProcess(JoblessProcessType.MEMBERSHIP_UPDATE));


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
    private static void schedule(FiniteDuration repeatDuration, Runnable task) {
        Akka.system().scheduler().schedule(
                Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                repeatDuration,     //Frequency
                task,
                Akka.system().dispatcher()
        );
    }

}
