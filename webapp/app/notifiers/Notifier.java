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
import be.ugent.degage.db.dao.*;
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
import views.html.users.users;

import java.text.MessageFormat;


/**
 *
 */
public class Notifier extends Mailer {

    public static String toFullURL (Call call) {
        return Play.application().configuration().getString("application.hostUrl") + call.url();
    }

    /**
     * Creates a notification for the user and automatically invalidates the cache
     *
     * @param dao
     * @param user
     * @param subject
     * @param mail
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

        createNotificationAndSend (user, "membershipApproved",
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

    public static void sendCarCostApproved(UserHeader user, CarCost carCost) {
        String date = Utils.toLocalizedDateString(carCost.getDate());
        String name = carCost.getCar().getName();
        String amount = carCost.getAmount().toPlainString() + " euro";
        String description = carCost.getDescription();
        createNotificationAndSend(user, "costApproved",
                views.txt.messages.costApproved.render(user, name, description, amount, date),
                views.html.messages.costApproved.render(user, name, description, amount, date)
        );
    }

    public static void sendCarCostRejected(UserHeader user, CarCost carCost) {
        String date = Utils.toLocalizedDateString(carCost.getDate());
        String name = carCost.getCar().getName();
        String amount = carCost.getAmount().toPlainString() + " euro";
        String description = carCost.getDescription();
        createNotificationAndSend(user, "costRejected",
                views.txt.messages.costRejected.render(user, name, description, amount, date),
                views.html.messages.costRejected.render(user, name, description, amount, date)
        );
    }

    public static void sendRefuelApproved(Refuel refuel, String newRemarks) {
        Reservation reservation = refuel.getCarRide().getReservation();
        UserHeader user = reservation.getUser();
        String name = reservation.getCar().getName();
        String amount = EurocentAmount.toString(refuel.getEurocents()) + " euro";
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());

        createNotificationAndSend(user, "refuelApproved",
                views.txt.messages.refuelApproved.render(user, name, amount, newRemarks, from, until),
                views.html.messages.refuelApproved.render(user, name, amount, newRemarks, from, until),
                name, from
        );
    }

    public static void sendRefuelRejected(Refuel refuel, String newRemarks) {
        // TODO: combine with above to reduce code duplication
        Reservation reservation = refuel.getCarRide().getReservation();
        UserHeader user = reservation.getUser();
        String name = reservation.getCar().getName();
        String amount = EurocentAmount.toString(refuel.getEurocents()) + " euro";
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        createNotificationAndSend(user, "refuelRejected",
                views.txt.messages.refuelRejected.render(user, name, amount, newRemarks, from, until),
                views.html.messages.refuelRejected.render(user, name, amount,newRemarks, from, until),
                name, from
        );
    }

    public static void sendCarCostRequest(CarCost carCost) {
        DataAccessContext context = DataAccess.getInjectedContext();
        UserRoleDAO userRoleDAO = context.getUserRoleDAO();
        String date = Utils.toLocalizedDateString(carCost.getDate());
        String carName = carCost.getCar().getName();
        String amount = carCost.getAmount().toPlainString() + " euro";
        String costDescription = carCost.getDescription();
        for (UserHeader u : userRoleDAO.getUsersByRole(UserRole.CAR_ADMIN)) {
            createNotification(context, u, "costRequest",
                    views.html.messages.costRequest.render(u, carName, costDescription, amount, date)
            );
        }

    }

    public static void sendRefuelRequest(UserHeader owner, Reservation reservation, int refuelId, Car car, int eurocents) {
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        String carName = car.getName();
        String amount = EurocentAmount.toString(eurocents) + " euro";
        String url = toFullURL(routes.Refuels.approveOrReject(refuelId));
        UserHeader driver = reservation.getUser();
        createNotificationAndSend(
                owner, "refuelRequest",
                views.txt.messages.refuelRequest.render(owner,driver,carName,amount,url,from,until),
                views.html.messages.refuelRequest.render(owner,driver,carName,amount,url,from,until),
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
        String url = toFullURL(routes.Workflow.approveReservation(reservation.getId()));
        UserHeader driver = reservation.getUser();
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

    public static void sendReservationDetailsProvidedMail(UserHeader user, Reservation reservation, CarRide ride) {
        UserHeader driver = reservation.getUser();
        String url = toFullURL(routes.Drives.details(reservation.getId()));
        String carName = reservation.getCar().getName();
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        int start = ride.getStartKm();
        int end = ride.getEndKm();
        createNotificationAndSend(user, "detailsProvided",
                views.txt.messages.detailsProvided.render(user, driver, carName, url, start, end, from, until),
                views.html.messages.detailsProvided.render(user, driver, carName, url, start, end, from, until),
                carName, from, driver.toString()
        );
    }

    public static void sendReservationApprovedByOwnerMail(DataAccessContext context, String remarks, Reservation reservation) {
        // note: needs extended reservation
        UserHeader user = reservation.getUser();
        Car car = reservation.getCar();
        String carAddress = car.getLocation().toString();
        String carName = car.getName();
        String from = Utils.toLocalizedString(reservation.getFrom());
        String until = Utils.toLocalizedString(reservation.getUntil());
        String url = toFullURL(routes.Drives.details(reservation.getId()));
        UserHeader owner = context.getUserDAO().getUserHeader(reservation.getOwnerId());

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
                context, user, "reservationApproved",
                views.txt.messages.reservationApproved.render(user, from, until, carName, carAddress, url, remarks, contactInfo),
                views.html.messages.reservationApproved.render(user, from, until, carName, carAddress, url, remarks, contactInfo),
                carName, from
        );
    }
    // to be used with injected context
    public static void sendReservationRefusedByOwnerMail(String reason, Reservation reservation) {
        UserHeader driver = reservation.getUser();
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

    public static void sendContractManagerAssignedMail(UserHeader user, Approval approval) {
        UserHeader admin = approval.getAdmin();
        createNotificationAndSend(user, "managerAssigned",
                views.txt.messages.managerAssigned.render(user, admin, 75, 30), // TODO: not hardcoded
                views.html.messages.managerAssigned.render(user, admin, 75, 30)
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
