/* Application.java
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

package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.dao.CarCostDAO;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.dao.ReminderDAO;
import be.ugent.degage.db.dao.CheckDAO;
import be.ugent.degage.db.dao.BillingDAO;
import be.ugent.degage.db.models.*;
import controllers.routes.javascript;
import data.ProfileCompleteness;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.dashboardFullUser;
import views.html.dashboardOwner;
import views.html.dashboardRegistered;

import java.time.LocalDate;
import java.util.*;

public class Application extends Controller {

    /**
     * Redirects to a specific page depending on user status (logged in? full user? ...)
     */
    @InjectContext
    public static Result index() {

        if (!CurrentUser.isValid()) {
            // login page if not logged in
            return redirect(routes.Login.login(null));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            String headerHtml = context.getAnnouncementDAO().getAnnouncement("header").getHtml();
            UserDAO userDAO = context.getUserDAO();
            User user = userDAO.getUser(CurrentUser.getId());

            CarCostDAO carCostDAO = context.getCarCostDAO();
            CarDAO carDAO = context.getCarDAO();
            List<Integer> carCosts = new ArrayList<>();
            Iterable<CarHeader> cars = carDAO.listCarsOfUser(CurrentUser.getId());
            for (CarHeader car: cars) {
                for (CarCost cost: carCostDAO.listCostsOfCar(car.getId())) {
                    if (cost.getCommentCarAdmin() != null) {
                        carCosts.add(cost.getId());
                    }
                }
            }

            ProfileCompleteness pc = new ProfileCompleteness(
                    user,
                    context.getFileDAO().hasFileByType(CurrentUser.getId(), FileDAO.FileType.LICENSE),
                    userDAO.getUserPicture(CurrentUser.getId()) > 0
            );

            int nbrOfMissingTripInfo = 0;
            int nbrOfMissingRefuelInfo = 0;
            int firstCarId = 0;
            LocalDate billingPeriodStartDate = LocalDate.now();
            if (CurrentUser.hasRole(UserRole.CAR_OWNER)) {
                BillingDAO billingDAO = context.getBillingDAO();
                Billing latestBilling = billingDAO.listAllBillings().iterator().next();
                billingPeriodStartDate = latestBilling.getStart();
                CheckDAO checkDAO = context.getCheckDAO();
                for (CarHeader car: cars) {
                    firstCarId = car.getId();
                    Iterable<CheckDAO.TripAnomaly> tripAnomalies = checkDAO.getTripAnomalies(latestBilling.getId(), car.getId());
                    Iterator it = tripAnomalies.iterator();
                    while (it.hasNext()) {
                        nbrOfMissingTripInfo++;
                        it.next();
                    }
                    Iterable<CheckDAO.RefuelAnomaly> refuelAnomalies = checkDAO.getUnReviewedRefuels(latestBilling.getId(), car.getId());
                    it = refuelAnomalies.iterator();
                    while (it.hasNext()) {
                        nbrOfMissingRefuelInfo++;
                        it.next();
                    }
                }
            }

            ReminderDAO reminderDAO = context.getReminderDAO();
            Iterable<Reminder> tmp = reminderDAO.listRemindersForUser(CurrentUser.getId());
            Boolean emptyReminders = true;
            ArrayList<Reminder> reminders = new ArrayList();
            if (user.getSendReminder()) { //only show reminders when true
                ArrayList<Integer> addedInvoices = new ArrayList();
                ArrayList<Integer> priorities = new ArrayList();
                for (Reminder r : tmp) { // only show latest reminder per invoice
                    int priority;
                    emptyReminders = false;
                    if (r.getDescription().equals("FIRST")) {
                        priority = 1;
                    } else if (r.getDescription().equals("SECOND")) {
                        priority = 2;
                    } else { // "THIRD"
                        priority = 3;
                    }

                    if (addedInvoices.contains(r.getInvoiceId())) {
                        int index = addedInvoices.indexOf(r.getInvoiceId());
                        if (priorities.get(index) < priority) { //remove old and add new
                            reminders.remove(index);
                            addedInvoices.remove(index);
                            priorities.remove(index);

                            reminders.add(r);
                            addedInvoices.add(r.getInvoiceId());
                            priorities.add(priority);
                        } else {
                            //leave old and ignore new
                        }
                    } else {
                        reminders.add(r);
                        addedInvoices.add(r.getInvoiceId());
                        priorities.add(priority);
                    }
                }
            }

            if (CurrentUser.hasFullStatus() || CurrentUser.hasRole(UserRole.SUPER_USER)) {
                // normal dashboard if user has full status
                if (CurrentUser.hasRole(UserRole.CAR_OWNER)) {
                    List<Calendars.OverviewForCar> ofclist = new ArrayList<>();
                    for (CarHeader car : context.getCarDAO().listCarsOfUser(CurrentUser.getId())) {
                        if (car.isActive()) {
                            Calendars.CarDateData data = new Calendars.CarDateData();
                            data.carId = car.getId();
                            data.carIdAsString = car.getName();
                            data.date = Utils.toDateString(LocalDate.now());
                            data.period = "week";
                            ofclist.add(Calendars.getOverviewForCar(data));
                        }
                    }
                    return ok(dashboardOwner.render(
                            headerHtml, pc,
                            nbrOfMissingTripInfo, nbrOfMissingRefuelInfo, billingPeriodStartDate, firstCarId,
                            carCosts, carCosts.size()==0, reminders, emptyReminders, ofclist));
                } else {
                    return ok(dashboardFullUser.render(headerHtml, pc, carCosts, carCosts.size()==0,
                            reminders, emptyReminders));
                }
            } else if (CurrentUser.isBlocked()) {
                return redirect(routes.Billings.list(CurrentUser.getId()));
            } else {
                // reduced dashboard
                Boolean didUserGoToInfoSession = InfoSessions.didUserGoToInfoSession();
                InfoSession futureInfoSession = null;
                if (!didUserGoToInfoSession) {
                    futureInfoSession = DataAccess.getInjectedContext().getInfoSessionDAO().getAttendingInfoSession(CurrentUser.getId());
                }
                return ok(
                        dashboardRegistered.render(
                                headerHtml,
                                user.getFirstName(),
                                didUserGoToInfoSession,
                                futureInfoSession,
                                context.getApprovalDAO().membershipRequested(CurrentUser.getId()),
                                pc,
                                userDAO.getUserPicture(CurrentUser.getId()) > 0
                        )
                );
            }
        }
    }

    /**
     * Javascript controllers.routes allowing the calling of actions on the server from
     * Javascript as if they were invoked directly in the script.
     */
    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("myJsRoutes",
                        // Routes
                        javascript.Cars.updatePrivileged(),
                        javascript.Cars.getCarInfoModal(),
                        javascript.Damages.editDamage(),
                        javascript.Damages.addStatus(),
                        javascript.Damages.addProof(),
                        javascript.InfoSessions.enrollSession(),
                        javascript.Maps.getMap(),
                        javascript.Calendars.availabilityCar(),
                        javascript.CalendarEvents.getEventForReservation(),
                        javascript.CalendarEvents.getEventsForCar()
                )
        );
    }

    public static Result paginationRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("paginationJsRoutes",
                        // Routes
                        javascript.Approvals.pendingApprovalListPaged(),
                        javascript.Calendars.listAvailableCarsPage(),
                        javascript.Contracts.showContractsPage(),
                        javascript.Costs.showCostsPage(),
                        javascript.Cars.showCarsPage(),
                        javascript.InfoSessions.showSessionsPage(),
                        javascript.Assistances.showAllAssistancesPage(),
                        javascript.Parkingcards.showAllParkingcardsPage(),
                        javascript.Damages.showDamagesPage(),
                        javascript.Damages.showDamagesPageOwner(),
                        javascript.Damages.showDamagesPageAdmin(),
                        javascript.Trips.showTripsPage(),
                        javascript.Trips.showTripsAdminPage(),
                        javascript.Messages.showReceivedMessagesPage(),
                        javascript.Messages.showSentMessagesPage(),
                        javascript.Notifications.showNotificationsPage(),
                        javascript.Refuels.showUserRefuelsPage(),
                        javascript.Refuels.showOwnerRefuelsPage(),
                        javascript.Refuels.showAllRefuelsPage(),
                        javascript.UserRoles.showUsersPage(),
                        javascript.Users.showUsersPage(),
                        javascript.Invoices.showInvoicesPage(),
                        javascript.Payments.showPaymentsPage(),
                        javascript.Codas.showCodasPage(),
                        javascript.Reminders.showRemindersPage(),
                        javascript.PaymentStatistics.showUserStatsPage()
                )
        );
    }
}
