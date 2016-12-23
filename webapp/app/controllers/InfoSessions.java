/* InfoSessions.java
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
import be.ugent.degage.db.dao.InfoSessionDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import controllers.util.Addresses;
import controllers.util.UserpickerData;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.infosession.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cedric on 2/21/14.
 */
public class InfoSessions extends Controller {

    public static class InfoSessionData {

        public Integer userId;

        @Constraints.Required
        public String userIdAsString;

        @Constraints.Required
        public Instant time;
        public Integer max_enrollees;

        public String type;

        public String comments;

        public String submit; // "default" or "copyOwnerAddress"

        public Addresses.EditAddressModel address = new Addresses.EditAddressModel();

        public List<ValidationError> validate() {
            List<ValidationError> errors = new ArrayList<>();
            if (userId == null || userId <= 0) {
                // needed for those cases where a string is input which does not correspond with a real person
                errors.add (new ValidationError("userId","Gelieve een gastvrouw/gastheer te selecteren"));
            }
            if (errors.isEmpty())
                return null;
            else
                return errors;
        }

        public void populate(InfoSession i) {

            userId = i.getHostId();
            userIdAsString = i.getHostName();

            time = i.getTime();
            max_enrollees = i.getMaxEnrollees();
            type = i.getType().name();
            comments = i.getComments();

            address.populate(i.getAddress());
        }

    }

    /**
     * Method: GET
     *
     * @return An infosession form
     */
    @AllowRoles({UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result newSession() {

        InfoSessionData data = new InfoSessionData();
        data.userId = CurrentUser.getId();
        data.userIdAsString = CurrentUser.getFullName();
        data.address.populate(
                DataAccess.getInjectedContext().getUserDAO().getUser(CurrentUser.getId()).getAddressResidence()
        );
        data.type = "NORMAL";
        return ok(addinfosession.render(
                Form.form(InfoSessionData.class).fill(data))
        );
    }

    /**
     * Method: GET
     *
     * @param sessionId SessionId to edit
     * @return An infosession form for given id
     */
    @AllowRoles({UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result editSession(int sessionId) {
        InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();
        InfoSession is = dao.getInfoSession(sessionId);
        if (is == null) {
            flash("danger", "Infosessie met ID=" + sessionId + " bestaat niet.");
            return redirect(routes.InfoSessions.showUpcomingSessions());
        } else {
            InfoSessionData data = new InfoSessionData();
            data.populate(is);

            Form<InfoSessionData> editForm = Form.form(InfoSessionData.class).fill(data);
            return ok(editinfosession.render(editForm, sessionId));
        }
    }

    /**
     * Method: GET
     *
     * @param sessionId SessionID to remove
     * @return A result redirect whether delete was successfull or not.
     */
    @AllowRoles({UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result removeSession(int sessionId) {
        DataAccessContext context = DataAccess.getInjectedContext();

        context.getInfoSessionDAO().deleteInfoSession(sessionId);

        // Delete the reminder
        context.getJobDAO().deleteJob(JobType.IS_REMINDER, sessionId);

        flash("success", "De infosessie werd verwijderd.");
        return redirect(routes.InfoSessions.showUpcomingSessions());
    }

    /**
     * Method: POST
     * Edits the session for given ID, based on submitted form data
     *
     * @param sessionId SessionID to edit
     * @return Redirect to edited session, or the form if errors occurred
     */
    @AllowRoles({UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result editSessionPost(int sessionId) {
        Form<InfoSessionData> editForm = Form.form(InfoSessionData.class).bindFromRequest();
        if (editForm.hasErrors()) {
            return badRequest(editinfosession.render(editForm, sessionId));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            UserDAO udao = context.getUserDAO();
            InfoSessionData data = editForm.get();
            if ("copyAddress".equals(data.submit)) {
                User host = udao.getUser(data.userId);
                data.address.populate(host.getAddressResidence());
                return ok(addinfosession.render (Form.form(InfoSessionData.class).fill(data)));
            } else {
                InfoSessionDAO dao = context.getInfoSessionDAO();

                // check if amountOfAttendees < new max
                int amountOfAttendees = dao.getAmountOfAttendees(sessionId);
                if (data.max_enrollees != 0 && data.max_enrollees < amountOfAttendees) {
                    editForm.reject("max_enrollees", "Het nieuwe maximum mag niet kleiner zijn dan het aantal huidige inschrijvingen");
                    return badRequest(editinfosession.render(editForm, sessionId));
                }

                // reschedule the reminder
                context.getJobDAO().updateJobTime(
                        JobType.IS_REMINDER,
                        sessionId,
                        data.time.minusSeconds(60 * Integer.parseInt(context.getSettingDAO().getSettingForNow("infosession_reminder")))
                );

                dao.updateInfoSession(sessionId, InfoSessionType.valueOf(data.type), data.max_enrollees, data.time,
                        data.userId, data.comments, data.address.toAddress());
                flash("success", "De wijzigingen werden met succes toegepast.");
                return redirect(routes.InfoSessions.detail(sessionId));
            }
        }
    }

    /**
     * Method: GET
     * Unenrolls the user for his subscribed infosession.
     *
     * @return A redirect to the overview page with message if unenrollment was successfull.
     */
    @AllowRoles({})
    @InjectContext
    public static Result unenrollSession() {
        InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();

        InfoSession alreadyAttending = dao.getAttendingInfoSession(CurrentUser.getId());
        if (alreadyAttending == null) {
            flash("danger", "Je bent niet ingeschreven voor een toekomstige infosessie.");
            return redirect(routes.InfoSessions.showUpcomingSessions());
        } else {
            dao.unregisterUser(alreadyAttending.getId(), CurrentUser.getId());

            flash("success", "Je bent uitgeschreven uit deze infosessie.");
            return redirect(routes.InfoSessions.showUpcomingSessions());
        }
    }

    // TODO: reintegrate the Map (using a promise instead of a result?)
    @AllowRoles({})
    @InjectContext
    public static Result detail(int sessionId) {
        InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();
        InfoSession infoSession = dao.getInfoSession(sessionId);
        return ok(detail.render(
            infoSession,
            Form.form(UserpickerData.class),
            dao.getEnrollees(sessionId), 
            new Maps.MapDetails(infoSession.getAddress().getLat(), infoSession.getAddress().getLng(), 14, "Infosessie"))
        );
    }


    /*
    @RoleSecured.RoleAuthenticated()                    detail.render
    @InjectContext
    public static F.Promise<Result> detail(int sessionId) {
        final User user = DataProvider.getUserProvider().getUser();
        final InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();
        final InfoSession session = dao.getInfoSession(sessionId);
        final Iterable<Enrollee> enrollees = dao.getEnrollees(sessionId);

        if (session == null) {
            return F.Promise.promise(new F.Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    return badRequest("Sessie id bestaat niet.");
                }
            });
        } else {
            final InfoSession enrolled = dao.getAttendingInfoSession(user);
            if (DataProvider.getSettingProvider().getBooleanOrDefault("show_maps", true)) {
                return Maps.getLatLongPromise(session.getAddress().getId()).map(
                        new F.Function<F.Tuple<Double, Double>, Result>() {
                            public Result apply(F.Tuple<Double, Double> coordinates) {
                                return ok(detail.render(session, enrolled, enrollees,
                                        coordinates == null ? null : new Maps.MapDetails(coordinates._1, coordinates._2, 14, "Afspraak op " + session.getTime().toString("dd-MM-yyyy") + " om " + session.getTime().toString("HH:mm"))));
                            }
                        }
                );
            } else {
                return F.Promise.promise(new F.Function0<Result>() {
                    @Override
                    public Result apply() throws Throwable {
                        return ok(detail.render(session, enrolled, null));
                    }
                });
            }
        }
    }
    */

    /**
     * Method: GET
     *
     * @param sessionId Id of the session the user has to be removed from
     * @param userId    Userid of the user to be removed
     * @return Status of the operation page
     */
    @AllowRoles(UserRole.INFOSESSION_ADMIN)
    @InjectContext
    public static Result removeUserFromSession(int sessionId, int userId) {

        DataAccess.getInjectedContext().getInfoSessionDAO().unregisterUser(sessionId, userId);

        flash("success", "De gebruiker werd uitgeschreven uit de infosessie.");
        return redirect(routes.InfoSessions.detail(sessionId));
    }

    /**
     * Method: GET
     *
     * @param sessionId SessionID to change userstatus on
     * @param userId    UserID of the user to change status of
     * @param status    New status of the user.
     * @return Redirect to the session detail page if successful.
     */
    @AllowRoles(UserRole.INFOSESSION_ADMIN)
    @InjectContext
    public static Result setUserSessionStatus(int sessionId, int userId, String status) {
        DataAccess.getInjectedContext().getInfoSessionDAO().setUserEnrollmentStatus(
                sessionId,
                userId,
                Enum.valueOf(EnrollementStatus.class, status)
        );
        flash("success", "De gebruikersstatus werd aangepast.");
        return redirect(routes.InfoSessions.detail(sessionId));
    }

    /**
     * Method: POST
     * Adds a user to the given infosession
     *
     * @param sessionId
     * @return
     */
    @AllowRoles(UserRole.INFOSESSION_ADMIN)
    @InjectContext
    public static Result addUserToSession(int sessionId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        InfoSessionDAO idao = context.getInfoSessionDAO();
        Form<UserpickerData> form = Form.form(UserpickerData.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(detail.render(
                    idao.getInfoSession(sessionId),
                    form,
                    idao.getEnrollees(sessionId), null));
        } else {
            if (idao.registerUser(sessionId, form.get().userId)) {
                flash("success", "De gebruiker werd toegevoegd aan deze infosessie.");
            } else {
                flash("danger", "De gebruiker is reeds ingeschreven voor deze sessie.");
            }
            return redirect(routes.InfoSessions.detail(sessionId));
        }
    }

    /**
     * Method: GET
     *
     * @param sessionId The sessionId to enroll to
     * @return A redirect to the detail page to which the user has subscribed
     */
    @AllowRoles({})
    @InjectContext
    public static Result enrollSession(int sessionId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        InfoSessionDAO dao = context.getInfoSessionDAO();

        InfoSession alreadyAttending = dao.getAttendingInfoSession(CurrentUser.getId());
        InfoSession session = dao.getInfoSession(sessionId);
        int numberOfAttendees = dao.getAmountOfAttendees(sessionId);

        if (session.getMaxEnrollees() != 0 && numberOfAttendees >= session.getMaxEnrollees()) {
            flash("danger", "Deze infosessie zit reeds vol.");
            return redirect(routes.InfoSessions.showUpcomingSessions());
        } else {
            if (alreadyAttending != null && alreadyAttending.getTime().isAfter(Instant.now())) {
                dao.unregisterUser(alreadyAttending.getId(), CurrentUser.getId());
            }
            if (dao.registerUser(sessionId, CurrentUser.getId())) {
                flash("success",
                        (alreadyAttending == null ?
                                "Je bent ingeschreven voor de infosessie op " :
                                "Je bent van infosessie veranderd naar ")
                                + Utils.toLocalizedString(session.getTime()) + ".");

                // TODO: avoid this?
                UserHeader user = context.getUserDAO().getUserHeader(CurrentUser.getId());
                Notifier.sendInfoSessionEnrolledMail(context, user, session);
            } else {
                flash("warning", "Je was reeds ingeschreven voor deze infosessie");
            }
            return redirect(routes.InfoSessions.showUpcomingSessions());
        }
    }


    /**
     * Method: POST
     * Creates a new infosession based on submitted form data
     *
     * @return A redirect to the newly created infosession, or the infosession edit page if the form contains errors.
     */
    @AllowRoles(UserRole.INFOSESSION_ADMIN)
    @InjectContext
    public static Result createNewSession() {
        Form<InfoSessionData> createForm = Form.form(InfoSessionData.class).bindFromRequest();
        if (createForm.hasErrors()) {
            return badRequest(addinfosession.render(createForm));
        } else {
            InfoSessionData data = createForm.get();
            DataAccessContext context = DataAccess.getInjectedContext();
            UserDAO udao = context.getUserDAO();
            if ("copyAddress".equals(data.submit)) {
                User host = udao.getUser(data.userId);
                data.address.populate(host.getAddressResidence());
                return ok(addinfosession.render (Form.form(InfoSessionData.class).fill(data)));
            } else {
                int sessionId = context.getInfoSessionDAO().createInfoSession(
                        InfoSessionType.valueOf(data.type),
                        data.userId,
                        data.address.toAddress(),
                        data.time,
                        data.max_enrollees == null ? 0 : data.max_enrollees,
                        data.comments);

                // Schedule the reminder
                context.getJobDAO().createJob(
                        JobType.IS_REMINDER,
                        sessionId,
                        data.time.minusSeconds(60L * Integer.parseInt(context.getSettingDAO().getSettingForNow("infosession_reminder")))
                );

                return redirect(
                        routes.InfoSessions.showSessions() // return to infosession list
                );
            }
        }
    }

    /**
     * Method: GET
     * Returns if the user had been to an infosession
     *
     * @return
     */
    // used in injected context
    // TODO: only used in dashboard
    public static boolean didUserGoToInfoSession() {
        return DataAccess.getInjectedContext().getInfoSessionDAO().
                getInfoSessionWherePresent(CurrentUser.getId()) != 0;
    }

    /*
     * Method: GET
     * Returns the promise of list of the upcoming infosessions. When the user is enrolled already this also includes map data if enabled
     *
     * @return
     */
    /*
    @AllowRoles
    @InjectContext
    // TODO: inject context does not work here

    public static F.Promise<Result> showUpcomingSessionsOriginal() {
        final User user = DataProvider.getUserProvider().getUser();
        InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();
        final Tuple<InfoSession, EnrollementStatus> enrolled = dao.getLastInfoSession(user);

        final boolean didUserGoToInfoSession = didUserGoToInfoSession();
        boolean showMaps = enrolled != null && "true".equals (DataAccess.getInjectedContext().getSettingDAO().getSettingForNow("show_maps"));
        if (showMaps) {

            return Maps.getLatLongPromise(enrolled.getFirst().getAddress().getId()).map(
                    new F.Function<F.Tuple<Double, Double>, Result>() {
                        public Result apply(F.Tuple<Double, Double> coordinates) {
                            return ok(infosessions.render(enrolled.getFirst(),
                                    coordinates == null ? null : new Maps.MapDetails(coordinates._1, coordinates._2, 14, "Afspraak op " + enrolled.getFirst().getTime().toString("dd-MM-yyyy") + " om " + enrolled.getFirst().getTime().toString("HH:mm")),
                                    didUserGoToInfoSession));
                        }
                    }
            );
        } else {
            return F.Promise.promise(() -> ok(infosessions.render(enrolled == null ? null : enrolled.getFirst(), null, didUserGoToInfoSession)));
        }
    }
    */

    /**
     * Shows the upcoming session, together with the infosession which was chosen by the current user
     */

    @AllowRoles({})
    @InjectContext
    public static Result showUpcomingSessions() {
        // TODO: adjust so that it shows a map, like in showUpcomingSessionsOriginal
        InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();
        InfoSessionDAO.LastSessionResult lis = dao.getLastInfoSession(CurrentUser.getId());
        Maps.MapDetails mapDetails = new Maps.MapDetails();
        if (lis.session != null && lis.session.getAddress() != null) {
            mapDetails = new Maps.MapDetails(lis.session.getAddress().getLat(), lis.session.getAddress().getLng(), 14, "Infosessie");
        }
        if (lis.present) {
            return ok(infosessionsDone.render(
                lis.session,
                mapDetails
            ));
        } else {
            return ok(infosessions.render(
                dao.getUpcomingInfoSessions(),
                lis.session,
                mapDetails,
                lis.present)
            );
        }
    }

    /**
     * A list of all sessions. Divided into upcoming / past
     */
    @AllowRoles(UserRole.INFOSESSION_ADMIN)
    @InjectContext
    public static Result showSessions() {
        return ok(infosessionsAdmin.render());
    }

    /**
     * Page with infosessions
     */
    @AllowRoles(UserRole.INFOSESSION_ADMIN)
    @InjectContext
    public static Result showSessionsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {


        InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();
        if (searchString.endsWith("PENDING")) {
            return ok(infosessionsAdminPage.render(
                    dao.getFutureInfoSessions(page, pageSize), true
            ));
        } else {
            return ok(infosessionsAdminPage.render(
                    dao.getPastInfoSessions(page, pageSize), false
            ));
        }
    }

    @InjectContext
    public static Result showUpcomingSessionsRaw() {
        return ok(infosessionsraw.render(
                DataAccess.getInjectedContext().getInfoSessionDAO().getUpcomingInfoSessions()
        ));
    }

}
