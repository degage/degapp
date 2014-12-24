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
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import controllers.util.Addresses;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.infosession.*;

import java.time.Instant;
import java.util.*;

/**
 * Created by Cedric on 2/21/14.
 */
public class InfoSessions extends Controller {

    public static class InfoSessionCreationModel {

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

            userId = i.getHost().getId();
            userIdAsString = i.getHost().getFullName();

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
    @AllowRoles(value = {UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result newSession() {
        User user = DataProvider.getUserProvider().getUser();

        InfoSessionCreationModel model = new InfoSessionCreationModel();
        model.userId = user.getId();
        model.userIdAsString = user.getFullName();
        model.address.populate(user.getAddressDomicile());
        model.type = "NORMAL";
        return ok(addinfosession.render(
                Form.form(InfoSessionCreationModel.class).fill(model))
        );
    }

    /**
     * Method: GET
     *
     * @param sessionId SessionId to edit
     * @return An infosession form for given id
     */
    @AllowRoles(value = {UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result editSession(int sessionId) {
        InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();
        InfoSession is = dao.getInfoSession(sessionId);
        if (is == null) {
            flash("danger", "Infosessie met ID=" + sessionId + " bestaat niet.");
            return redirect(routes.InfoSessions.showUpcomingSessions());
        } else {
            InfoSessionCreationModel model = new InfoSessionCreationModel();
            model.populate(is);

            Form<InfoSessionCreationModel> editForm = Form.form(InfoSessionCreationModel.class).fill(model);
            return ok(editinfosession.render(editForm, sessionId));
        }
    }

    /**
     * Method: GET
     *
     * @param sessionId SessionID to remove
     * @return A result redirect whether delete was successfull or not.
     */
    @AllowRoles(value = {UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result removeSession(int sessionId) {
        DataAccessContext context = DataAccess.getInjectedContext();

        context.getInfoSessionDAO().deleteInfoSession(sessionId);

        // Delete the reminder
        context.getJobDAO().deleteJob(JobType.IS_REMINDER, sessionId);

        flash("success", "De infosessie werd succesvol verwijderd.");
        return redirect(routes.InfoSessions.showUpcomingSessions());
    }

    /**
     * Method: POST
     * Edits the session for given ID, based on submitted form data
     *
     * @param sessionId SessionID to edit
     * @return Redirect to edited session, or the form if errors occurred
     */
    @AllowRoles(value = {UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result editSessionPost(int sessionId) {
        Form<InfoSessionCreationModel> editForm = Form.form(InfoSessionCreationModel.class).bindFromRequest();
        if (editForm.hasErrors()) {
            return badRequest(editinfosession.render(editForm, sessionId));
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            UserDAO udao = context.getUserDAO();
            InfoSessionCreationModel model = editForm.get();
            if ("copyAddress".equals(model.submit)) {
                User host = udao.getUser(model.userId);
                model.address.populate(host.getAddressResidence());
                return ok(addinfosession.render (Form.form(InfoSessionCreationModel.class).fill(model)));
            } else {
                InfoSessionDAO dao = context.getInfoSessionDAO();
                InfoSession session = dao.getInfoSession(sessionId);
                UserHeader host = udao.getUserHeader(model.userId);
                session.setHost(host);

                // update address
                session.setAddress(model.address.toAddress());

                // update time
                Instant time = model.time;
                if (!session.getTime().equals(time)) {
                    session.setTime(time);

                    // Schedule the reminder
                    JobDAO jdao = context.getJobDAO();
                    jdao.deleteJob(JobType.IS_REMINDER, session.getId()); // remove old reminder
                    jdao.createJob(
                            JobType.IS_REMINDER,
                            session.getId(),
                            session.getTime().minusSeconds(60 * Integer.parseInt(context.getSettingDAO().getSettingForNow("infosession_reminder")))
                    );
                }

                // check if amountOfAttendees < new max
                int amountOfAttendees = dao.getAmountOfAttendees(session.getId());
                if (model.max_enrollees != 0 && model.max_enrollees < amountOfAttendees) {
                    flash("danger", "Er zijn al meer inschrijvingen dan het nieuwe toegelaten aantal. Aantal huidige inschrijvingen: " + amountOfAttendees + ".");
                    return badRequest(editinfosession.render(editForm, sessionId));
                } else {
                    session.setMaxEnrollees(model.max_enrollees);
                }

                // type
                session.setType(InfoSessionType.valueOf(model.type));

                // comments
                session.setComments(model.comments);

                dao.updateInfoSession(session);
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
    @AllowRoles
    @InjectContext
    public static Result unenrollSession() {
        InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();

        InfoSession alreadyAttending = dao.getAttendingInfoSession(CurrentUser.getId());
        if (alreadyAttending == null) {
            flash("danger", "Je bent niet ingeschreven voor een toekomstige infosessie.");
            return redirect(routes.InfoSessions.showUpcomingSessions());
        } else {
            dao.unregisterUser(alreadyAttending.getId(), CurrentUser.getId());

            flash("success", "Je bent succesvol uitgeschreven uit deze infosessie.");
            return redirect(routes.InfoSessions.showUpcomingSessions());
        }
    }

    // TODO: reintegrate the Map (using a promise instead of a result?)
    @AllowRoles
    @InjectContext
    public static Result detail(int sessionId) {
        InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();
        return ok(detail.render(
                dao.getInfoSession(sessionId),
                Form.form(UserpickerData.class),
                dao.getAttendingInfoSession(CurrentUser.getId()),
                dao.getEnrollees(sessionId), null));
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
    @AllowRoles(value = {UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result removeUserFromSession(int sessionId, int userId) {

        DataAccess.getInjectedContext().getInfoSessionDAO().unregisterUser(sessionId, userId);

        flash("success", "De gebruiker werd succesvol uitgeschreven uit de infosessie.");
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
    @AllowRoles({UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result setUserSessionStatus(int sessionId, int userId, String status) {
        DataAccess.getInjectedContext().getInfoSessionDAO().setUserEnrollmentStatus(
                sessionId,
                userId,
                Enum.valueOf(EnrollementStatus.class, status)
        );
        flash("success", "De gebruikersstatus werd met succes aangepast.");
        return redirect(routes.InfoSessions.detail(sessionId));
    }

    // TODO: make this generally available for extension
    public static class UserpickerData {
        public Integer userId;

        @Constraints.Required
        public String userIdAsString;

        public List<ValidationError> validate() {
            if (userId == null || userId <= 0) {
                // needed for those cases where a string is input which does not correspond with a real person
                return Arrays.asList (new ValidationError("userId", "Gelieve een bestaande persoon te selecteren"));
            } else {
                return null;
            }
        }

        public void populate (UserHeader user) {
            if (user != null) {
                userId = user.getId();
                userIdAsString = user.getFullName();
            }
        }

    }

    /**
     * Method: POST
     * Adds a user to the given infosession
     *
     * @param sessionId
     * @return
     */
    @AllowRoles({UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result addUserToSession(int sessionId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        InfoSessionDAO idao = context.getInfoSessionDAO();
        Form<UserpickerData> form = Form.form(UserpickerData.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(detail.render(
                    idao.getInfoSession(sessionId),
                    form,
                    idao.getAttendingInfoSession(CurrentUser.getId()),
                    idao.getEnrollees(sessionId), null));
        } else {
            // TODO: loop in database
            int userId = form.get().userId;
            for (Enrollee others : idao.getEnrollees(sessionId)) {
                if (others.getUser().getId() == userId) {
                    flash("danger", "De gebruiker is reeds ingeschreven voor deze sessie.");
                    return redirect(routes.InfoSessions.detail(sessionId));
                }
            }

            // Now we enroll
            idao.registerUser(sessionId, userId); // TODO: do not allow registration if already registered

            flash("success", "De gebruiker werd succesvol toegevoegd aan deze infosessie.");
            return redirect(routes.InfoSessions.detail(sessionId));
        }
    }

    /**
     * Method: GET
     *
     * @param sessionId The sessionId to enroll to
     * @return A redirect to the detail page to which the user has subscribed
     */
    @AllowRoles
    @InjectContext
    public static Result enrollSession(int sessionId) {
        if (CurrentUser.hasFullStatus()) {
            flash("warning", "Je bent al goedgekeurd door onze administrator. Inschrijven is wel nog steeds mogelijk.");
        }
        DataAccessContext context = DataAccess.getInjectedContext();
        InfoSessionDAO dao = context.getInfoSessionDAO();

        InfoSession alreadyAttending = dao.getAttendingInfoSession(CurrentUser.getId());
        InfoSession session = dao.getInfoSession(sessionId); //TODO: just add going subclause (like in getAttending requirement)
        int numberOfAttendees = dao.getAmountOfAttendees(sessionId);
        if (session == null) {
            flash("danger", "Sessie met ID = " + sessionId + " bestaat niet.");
            return redirect(routes.InfoSessions.showUpcomingSessions());
        } else {
            if (session.getMaxEnrollees() != 0 && numberOfAttendees >= session.getMaxEnrollees()) {
                flash("danger", "Deze infosessie zit reeds vol.");
                return redirect(routes.InfoSessions.showUpcomingSessions());
            } else {
                if (alreadyAttending != null && alreadyAttending.getTime().isAfter(Instant.now())) {
                    dao.unregisterUser(alreadyAttending.getId(), CurrentUser.getId());
                }
                dao.registerUser(sessionId, CurrentUser.getId()); // TODO: disallow registration when full

                flash("success",
                        (alreadyAttending == null ?
                        "Je bent met succes ingeschreven voor de infosessie op " :
                        "Je bent van infosessie veranderd naar ")
                                + Utils.toLocalizedString(session.getTime())+ ".");

                // TODO: avoid this?
                UserHeader user = context.getUserDAO().getUserHeader(CurrentUser.getId());
                Notifier.sendInfoSessionEnrolledMail(context, user, session);
                return redirect(routes.InfoSessions.detail(sessionId));
            }
        }
    }


    /**
     * Method: POST
     * Creates a new infosession based on submitted form data
     *
     * @return A redirect to the newly created infosession, or the infosession edit page if the form contains errors.
     */
    @AllowRoles(value = {UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result createNewSession() {
        Form<InfoSessionCreationModel> createForm = Form.form(InfoSessionCreationModel.class).bindFromRequest();
        if (createForm.hasErrors()) {
            return badRequest(addinfosession.render(createForm));
        } else {
            InfoSessionCreationModel model = createForm.get();
            DataAccessContext context = DataAccess.getInjectedContext();
            UserDAO udao = context.getUserDAO();
            if ("copyAddress".equals(model.submit)) {
                User host = udao.getUser(model.userId);
                model.address.populate(host.getAddressResidence());
                return ok(addinfosession.render (Form.form(InfoSessionCreationModel.class).fill(model)));
            } else {
                InfoSessionDAO dao = context.getInfoSessionDAO();


                InfoSessionType type = InfoSessionType.valueOf(model.type);

                UserHeader host = udao.getUserHeader(model.userId);
                InfoSession session = dao.createInfoSession(type, host, model.address.toAddress(), model.time,
                        model.max_enrollees == null ? 0 : model.max_enrollees,
                        model.comments);

                // Schedule the reminder
                context.getJobDAO().createJob(
                        JobType.IS_REMINDER,
                        session.getId(),
                        session.getTime().minusSeconds(60L * Integer.parseInt(context.getSettingDAO().getSettingForNow("infosession_reminder")))
                );


                return redirect(
                        routes.InfoSessions.showUpcomingSessions() // return to infosession list
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
                getInfoSessionWherePresent(CurrentUser.getId()) != null;
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

    @AllowRoles
    @InjectContext
    public static Result showUpcomingSessions() {
        // TODO: adjust so that it shows a map, like in showUpcomingSessionsOriginal
        InfoSessionDAO dao = DataAccess.getInjectedContext().getInfoSessionDAO();
        InfoSessionDAO.LastSessionResult lis = dao.getLastInfoSession(CurrentUser.getId());

        return ok(infosessions.render(
                dao.getInfoSessions(true),
                lis.session,
                null,
                lis.present)
        );

    }

    /**
     * Method: GET*
     *
     * @return A table containing the upcoming sessions
     */
    @AllowRoles({UserRole.INFOSESSION_ADMIN})
    @InjectContext
    public static Result showSessions() {
        return ok(infosessionsAdmin.render(
                DataAccess.getInjectedContext().getInfoSessionDAO().getInfoSessions(false)
        ));
    }
}
