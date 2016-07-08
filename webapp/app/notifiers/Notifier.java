/* Notifier.java
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

package notifiers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.NotificationDAO;
import be.ugent.degage.db.models.*;
import com.google.common.base.Strings;
import controllers.Utils;
import controllers.routes;
import data.EurocentAmount;
import db.DataAccess;
import play.Play;
import play.api.mvc.Call;
import play.i18n.Messages;
import play.twirl.api.Html;
import play.twirl.api.Txt;
import providers.DataProvider;

import java.text.MessageFormat;
import java.time.LocalDate;


/**
 *
 */
public class Notifier extends Mailer {

    public static String toFullURL (Call call) {
        return Play.application().configuration().getString("application.hostUrl") + call.url();
    }

    /**
     * Creates a notification for the user and automatically invalidates the cache
     */
    private static void createNotification(NotificationDAO dao, UserHeader user, String subject, String mail) {
        dao.createNotification(user, subject, mail);
        DataProvider.getCommunicationProvider().invalidateNotifications(user.getId());
        DataProvider.getCommunicationProvider().invalidateNotificationNumber(user.getId());
    }


    private static void createNotification(DataAccessContext context, UserHeader user, String subjectKey, Html mail) {
        createNotification(context.getNotificationDAO(), user, Messages.get("subject." + subjectKey), mail.body().trim());
    }

    private static void createNotificationAndSend(UserHeader user, String subjectKey,
                                                  Txt text, Html html, Object... args) {
        createNotificationAndSend(DataAccess.getInjectedContext(), user, subjectKey, text, html, args);
    }

    private static void createNotificationAndSend(DataAccessContext context, UserHeader user, String subjectKey,
                                                  Txt text, Html html, Object... args) {
        String subject = Messages.get("subject." + subjectKey);
        if (args.length > 0) {
            subject = MessageFormat.format(subject, args);
        }
        createNotification(context.getNotificationDAO(), user, subject, html.body().trim());
        sendMail(user.getEmail(), subject, text, html);
    }

    public static void sendVerificationMail(String email, String verificationUrl) {
        String url = toFullURL(routes.Login.registerVerification(verificationUrl));
        sendMailWithSubjectKey(email, "verification",
                views.txt.messages.verification.render(url),
                views.html.messages.verification.render(url)
        );
    }

    public static void sendMembershipApproved(UserHeader user, String comment) {
        String url = toFullURL(routes.Application.index());

        createNotificationAndSend(user, "membershipApproved",
                views.txt.messages.membershipApproved.render(user, comment, url),
                views.html.messages.membershipApproved.render(user, comment, url),
                user.getFirstName(), user.getLastName()
        );
    }

    public static void sendMembershipRejected(UserHeader user, String comment) {
        createNotificationAndSend (user, "membershipRejected",
                views.txt.messages.membershipRejected.render(user, comment),
                views.html.messages.membershipRejected.render(user, comment)
        );
    }

    public static void sendCarCostApproved(UserHeader owner, CarCost carCost, int newSpread) {
        String date = Utils.toLocalizedDateString(carCost.getDate());
        String name = carCost.getCarName();
        String amount = EurocentAmount.toString(carCost.getAmount()) + " euro";
        String description = carCost.getDescription();
        String category = carCost.getCategory().getDescription();
        createNotificationAndSend(owner, "costApproved",
                views.txt.messages.costApproved.render(owner, name, description, amount, date, category, newSpread),
                views.html.messages.costApproved.render(owner, name, description, amount, date, category, newSpread)
        );
    }

    public static void sendCarCostRejected(UserHeader owner, CarCost carCost, String comment) {
        String date = Utils.toLocalizedDateString(carCost.getDate());
        String name = carCost.getCarName();
        String amount = EurocentAmount.toString(carCost.getAmount()) + " euro";
        String description = carCost.getDescription();
        String category = carCost.getCategory().getDescription();
        createNotificationAndSend(owner, "costRejected",
                views.txt.messages.costRejected.render(owner, name, description, amount, date, category, comment),
                views.html.messages.costRejected.render(owner, name, description, amount, date, category, comment)
        );
    }

    public static void sendRefuelApproved(UserHeader user, RefuelExtended refuel) {
        String name = refuel.getCarName();
        String amount = EurocentAmount.toString(refuel.getEurocents()) + " euro";
        String from = Utils.toLocalizedString(refuel.getReservationFrom());
        String until = Utils.toLocalizedString(refuel.getReservationUntil());

        createNotificationAndSend(user, "refuelApproved",
                views.txt.messages.refuelApproved.render(user, name, amount, from, until),
                views.html.messages.refuelApproved.render(user, name, amount, from, until),
                name, from
        );
    }

    public static void sendRefuelRejected(UserHeader user, RefuelExtended refuel, String newRemarks) {
        // TODO: combine with above to reduce code duplication
        String name = refuel.getCarName();
        String amount = EurocentAmount.toString(refuel.getEurocents()) + " euro";
        String from = Utils.toLocalizedString(refuel.getReservationFrom());
        String until = Utils.toLocalizedString(refuel.getReservationUntil());
        createNotificationAndSend(user, "refuelRejected",
                views.txt.messages.refuelRejected.render(user, name, amount, newRemarks, from, until),
                views.html.messages.refuelRejected.render(user, name, amount,newRemarks, from, until),
                name, from
        );
    }

    public static void sendDetailsRejected(UserHeader driver, TripAndCar trip, String remarks) {
        String url = toFullURL(routes.WFTrip.tripInfo(trip.getId()));
        String carName = trip.getCar().getName();
        String from = Utils.toLocalizedString(trip.getFrom());
        String until = Utils.toLocalizedString(trip.getUntil());
        int start = trip.getStartKm();
        int end = trip.getEndKm();
        createNotificationAndSend(driver, "detailsRejected",
                views.txt.messages.detailsRejected.render(driver, carName, url, start, end, remarks, from, until),
                views.html.messages.detailsRejected.render(driver, carName, url, start, end, remarks, from, until),
                carName, from
        );

    }

    public static void sendCarCostRequest(LocalDate date, String carName, EurocentAmount amount, String costDescription, String category) {
        DataAccessContext context = DataAccess.getInjectedContext();
        String dateString = Utils.toLocalizedDateString(date);
        String amountString = amount.toString() + " euro";
        for (UserHeader u : context.getUserRoleDAO().getUsersByRole(UserRole.CAR_ADMIN)) {
            createNotification(context, u, "costRequest",
                    views.html.messages.costRequest.render(u, carName, costDescription, amountString, dateString, category)
            );
        }

    }

    public static void sendRefuelRequest(UserHeader owner, TripAndCar reservation, int refuelId, int eurocents) {
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        String carName = reservation.getCar().getName();
        String amount = EurocentAmount.toString(eurocents) + " euro";
        String url = toFullURL(routes.RefuelApprove.approveOrReject(refuelId, false));
        String driverName = reservation.getDriverName();
        createNotificationAndSend(
                owner, "refuelRequest",
                views.txt.messages.refuelRequest.render(owner,driverName,carName,amount,url,from,until),
                views.html.messages.refuelRequest.render(owner,driverName,carName,amount,url,from,until),
                carName
        );
    }

    public static void sendInfoSessionEnrolledMail(DataAccessContext context, UserHeader user, InfoSession infoSession) {
        String date = Utils.toLocalizedString(infoSession.getTime());
        String address = infoSession.getAddress().toString();
        createNotificationAndSend(context,
                user, "infosessionEnrolled",
                views.txt.messages.infosessionEnrolled.render(user, date, address),
                views.html.messages.infosessionEnrolled.render(user, date, address)
        );

    }

    public static void sendReservationApproveRequestMail(UserHeader owner, Reservation reservation, String carName) {
        String url = toFullURL(routes.WFApprove.approveReservation(reservation.getId()));
        UserHeader driver = reservation.getDriver();
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        createNotificationAndSend (
                owner, "reservationRequest",
                views.txt.messages.reservationRequest.render(
                        owner, driver, carName, from,  until, url, reservation.getMessage()),
                views.html.messages.reservationRequest.render(
                        owner, driver, carName, from,  until, url, reservation.getMessage()),
                carName, from
        );
    }

    public static void sendRemindOwner(UserHeader owner, Reservation reservation, String carName) {
        String url = toFullURL(routes.WFApprove.approveReservation(reservation.getId()));
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        createNotificationAndSend (
                owner, "remindOwner",
                views.txt.messages.remindOwner.render( owner, carName, from,  until, url),
                views.html.messages.remindOwner.render( owner, carName, from,  until, url),
                carName, from
        );
    }


    public static void sendReservationDetailsProvidedMail(UserHeader owner, TripAndCar reservation) {
        String driverName = reservation.getDriverName();
        String url = toFullURL(routes.Trips.details(reservation.getId()));
        String carName = reservation.getCar().getName();
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        int start = reservation.getStartKm();
        int end = reservation.getEndKm();
        createNotificationAndSend(owner, "detailsProvided",
                views.txt.messages.detailsProvided.render(owner, driverName, carName, url, start, end, from, until),
                views.html.messages.detailsProvided.render(owner, driverName, carName, url, start, end, from, until),
                carName, from, driverName
        );
    }

    public static void sendOldReservationApproved (Reservation reservation) {
        UserHeader driver = reservation.getDriver();
        String carName = reservation.getCar().getName();
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        createNotificationAndSend(
                driver, "oldReservationApproved",
                views.txt.messages.oldReservationApproved.render(driver, carName, from, until),
                views.html.messages.oldReservationApproved.render(driver, carName, from, until),
                carName
        );


    }

    public static void sendReservationApprovedByOwnerMail(UserHeader driver, UserHeader owner, String remarks, TripAndCar reservation) {
        // note: needs location of car in header
        CarHeader car = reservation.getCar();
        String carAddress = car.getLocation().toString();
        String carName = car.getName();
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        String url = toFullURL(routes.Trips.details(reservation.getId()));

        String contactInfo = car.getEmail();
        if (!Strings.isNullOrEmpty(owner.getCellPhone())) {
            contactInfo += " - ";
            contactInfo += owner.getCellPhone();
        }
        if (!Strings.isNullOrEmpty(owner.getPhone())) {
            contactInfo += " - ";
            contactInfo += owner.getPhone();
        }

        createNotificationAndSend(
                driver, "reservationApproved",
                views.txt.messages.reservationApproved.render(driver, from, until, carName, carAddress, url, remarks, contactInfo),
                views.html.messages.reservationApproved.render(driver, from, until, carName, carAddress, url, remarks, contactInfo),
                carName, from
        );
    }
    public static void sendReservationRefusedByOwnerMail(UserHeader driver, String reason, TripAndCar reservation) {
        String carName = reservation.getCar().getName();
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        createNotificationAndSend(
                driver, "reservationRejected",
                views.txt.messages.reservationRejected.render(driver, carName, from, until, reason),
                views.html.messages.reservationRejected.render(driver, carName, from, until, reason),
                carName
        );
    }

    /**
     * Sent when a reservation is cancelled although it was already accepted
     */
    public static void sendReservationCancelled(UserHeader owner, TripAndCar trip) {
        String from = Utils.toLocalizedString(trip.getFrom());
        String until = Utils.toLocalizedString(trip.getUntil());
        String carName = trip.getCar().getName();
        String driverName = trip.getDriverName();
        createNotificationAndSend (
                owner, "reservationCancelled",
                views.txt.messages.reservationCancelled.render(
                        owner, driverName, carName, from,  until, trip.getMessage()),
                views.html.messages.reservationCancelled.render(
                        owner, driverName, carName, from,  until, trip.getMessage()),
                carName, from
        );
    }

    /**
     * Sent when a trip is marked as not having taken place
     */
    public static void sendLateCancel(UserHeader driver, TripAndCar trip) {
        String carName = trip.getCar().getName();
        String from = Utils.toLocalizedString(trip.getFrom());
        String until = Utils.toLocalizedString(trip.getUntil());
        String message = trip.getMessage();
        createNotificationAndSend(
                driver, "lateCancel",
                views.txt.messages.lateCancel.render(driver, carName, from, until, message),
                views.html.messages.lateCancel.render(driver, carName, from, until, message),
                carName
        );
    }


    public static void sendContractManagerAssignedMail(UserHeader user, UserHeader admin) {
        String loginURL = toFullURL(routes.Login.login(null));
        createNotificationAndSend(user, "managerAssigned",
                views.txt.messages.managerAssigned.render(user, admin, 75, 35, loginURL), // TODO: not hardcoded
                views.html.messages.managerAssigned.render(user, admin, 75, 35, loginURL)
        );
    }

    public static void sendPasswordResetMail(UserHeader user, String verificationUrl) {
        String url = toFullURL(routes.Login.resetPassword(verificationUrl));
        sendMailWithSubjectKey(user.getEmail(), "passwordReset",
                views.txt.messages.passwordReset.render(user, url),
                views.html.messages.passwordReset.render(user, url)
        );
    }

    public static void sendReminderMail(DataAccessContext context, UserHeader user) {
        context.getSchedulerDAO().setReminded(user.getId());
        String url = toFullURL(routes.Application.index());
        sendMailWithSubjectKey(user.getEmail(), "reminder",
                views.txt.messages.reminder.render(user, url),
                views.html.messages.reminder.render(user, url)
        );
    }

}
