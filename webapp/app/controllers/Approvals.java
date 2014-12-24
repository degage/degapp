/* Approvals.java
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
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.approvals.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 */
public class Approvals extends Controller {


    private static Iterable<String> checkApprovalConditions(int userId, DataAccessContext context) {
        UserDAO udao = context.getUserDAO();
        FileDAO fdao = context.getFileDAO();
        // TODO: minimize data transfer by using a query with true/false results
        User user = udao.getUser(userId); // gets the full user instead of small cached one
        Iterable<File> identityFiles = fdao.getIdFiles(userId);
        Iterable<File> licenseFiles = fdao.getLicenseFiles(userId);

        Collection<String> errors = new ArrayList<>();
        if (user.getAddressDomicile() == null)
            errors.add("Domicilieadres ontbreekt.");
        if (user.getAddressResidence() == null)
            errors.add("Verblijfsadres ontbreekt.");
        if (user.getIdentityCard() == null)
            errors.add("Identiteitskaart ontbreekt.");
        if (user.getIdentityCard() != null && (!identityFiles.iterator().hasNext()))
            errors.add("Bewijsgegevens identiteitskaart ontbreken");
        if (user.getLicense() == null)
            errors.add("Rijbewijs ontbreekt.");
        if (!user.isPayedDeposit())
            errors.add("Waarborg nog niet betaald.");
        if (user.getLicense() != null && (!licenseFiles.iterator().hasNext()))
            if (user.getCellphone() == null && user.getPhone() == null)
                errors.add("Telefoon/GSM ontbreekt.");
        return errors;
    }

    private static String getTermsAndConditions(DataAccessContext context) {
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate t = dao.getTemplate(MailType.TERMS);
        return t.getBody();
    }

    /**
     * Method: GET
     * A page to request full user approval
     *
     * @return The page to request approval
     */
    @AllowRoles
    @InjectContext
    public static Result requestApproval() {
        if (CurrentUser.hasFullStatus()) {
            flash("warning", "Je bent reeds een volwaardig lid.");
            return redirect(routes.Application.index());
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            if (context.getApprovalDAO().hasApprovalPending(CurrentUser.getId())) {
                flash("warning", "We nemen op dit moment je aanvraag om lid te worden in beschouwing.");
                return redirect(routes.Application.index());
            } else if (context.getInfoSessionDAO().getInfoSessionWherePresent(CurrentUser.getId()) == null) {
                flash("danger", "Je bent nog niet naar een infosessie geweest en kan dus nog geen lid worden.");
                return redirect(routes.InfoSessions.showUpcomingSessions());
            } else {
                return ok(approvalrequest.render(
                                checkApprovalConditions(CurrentUser.getId(), context),
                                Form.form(RequestApprovalData.class),
                                getTermsAndConditions(context))
                );
            }
        }
    }

    @AllowRoles
    @InjectContext
    public static Result requestApprovalPost() {
        if (CurrentUser.hasRole(UserRole.CAR_OWNER) && CurrentUser.hasRole(UserRole.CAR_USER)) {
            flash("warning", "Je bent reeds een volwaardige gebruiker.");
            return redirect(routes.Application.index());
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            Form<RequestApprovalData> form = Form.form(RequestApprovalData.class).bindFromRequest();
            if (form.hasErrors()) {
                if (context.getApprovalDAO().hasApprovalPending(CurrentUser.getId())) {
                    flash("warning", "Er is reeds een toelatingsprocedure voor deze gebruiker in aanvraag.");
                    return redirect(routes.Application.index());
                } else if (context.getInfoSessionDAO().getInfoSessionWherePresent(CurrentUser.getId()) == null) {
                    flash("danger", "Je bent nog niet aanwezig geweest op een infosessie.");
                    return redirect(routes.InfoSessions.showUpcomingSessions());
                } else {
                    return badRequest(approvalrequest.render(
                                    checkApprovalConditions(CurrentUser.getId(), context),
                                    form,
                                    getTermsAndConditions(context)
                            )
                    );
                }
            } else {
                Integer isp = context.getInfoSessionDAO().getInfoSessionWherePresent(CurrentUser.getId());
                if (isp == null) {
                    flash("danger", "Je bent nog niet aanwezig geweest op een infosessie.");
                    return redirect(routes.InfoSessions.showUpcomingSessions());
                } else {
                    // TODO: user is retrieved as header AND in full
                    context.getApprovalDAO().createApproval(CurrentUser.getId(), isp, form.get().message);
                    UserDAO udao = context.getUserDAO();
                    udao.getUserHeader(CurrentUser.getId()).setStatus(UserStatus.FULL_VALIDATING); //set to validation
                    udao.updateUser(udao.getUser(CurrentUser.getId())); //full update   // TODO: partial update?
                    return redirect(routes.Application.index());
                }
            }
        }
    }

    @AllowRoles({UserRole.INFOSESSION_ADMIN, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result pendingApprovalList() {
        return ok(approvals.render());
    }

    @AllowRoles({UserRole.INFOSESSION_ADMIN, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result pendingApprovalListPaged(int page) {
        DataAccessContext context = DataAccess.getInjectedContext();
        int pageSize = Integer.parseInt(context.getSettingDAO().getSettingForNow("infosessions_page_size")); // should be application constant?
        ApprovalDAO dao = context.getApprovalDAO();
        Iterable<Approval> approvalsList = dao.getApprovals(page, pageSize);
        int amountOfResults = dao.getApprovalCount();
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return ok(approvalpage.render(approvalsList, page, amountOfResults, amountOfPages));
    }

    // used in injected context
    private static Result approvalForm(Approval ap, DataAccessContext context, Form<ApprovalAdminData> form, boolean bad) {
        EnrollementStatus status = EnrollementStatus.ABSENT;
        if (ap.getSession() != null) {
            InfoSessionDAO idao = context.getInfoSessionDAO();
            status = idao.getUserEnrollmentStatus(ap.getSession().getId(), ap.getUser().getId());
        }

        if (!bad) {
            return ok(approvaladmin.render(ap, status, checkApprovalConditions(ap.getUser().getId(), context), form));
        } else {
            return badRequest(approvaladmin.render(ap, status, checkApprovalConditions(ap.getUser().getId(), context), form));
        }
    }

    @AllowRoles({UserRole.INFOSESSION_ADMIN, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result approvalDetails(int approvalId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ApprovalDAO dao = context.getApprovalDAO();
        Approval ap = dao.getApproval(approvalId);
        if (ap.getAdmin() == null) {
            flash("danger", "Gelieve eerst een contractverantwoordelijke op te geven.");
            return redirect(routes.Approvals.approvalAdmin(approvalId));
        } else {
            ApprovalAdminData model = new ApprovalAdminData();
            model.message = ap.getAdminMessage();
            model.status = (ap.getStatus() == Approval.ApprovalStatus.ACCEPTED || ap.getStatus() == Approval.ApprovalStatus.PENDING
                    ? ApprovalAdminData.Action.ACCEPT : ApprovalAdminData.Action.DENY).name();
            Set<UserRole> userRoles = DataAccess.getInjectedContext().getUserRoleDAO().getUserRoles(ap.getUser().getId());

            model.sharer = userRoles.contains(UserRole.CAR_OWNER);
            model.user = userRoles.contains(UserRole.CAR_USER);

            return approvalForm(ap, context, Form.form(ApprovalAdminData.class).fill(model), false);
        }

    }

    @AllowRoles({UserRole.INFOSESSION_ADMIN, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result approvalAdmin(int approvalId) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ApprovalDAO dao = context.getApprovalDAO();
        Approval ap = dao.getApproval(approvalId);

        EnrollementStatus status = EnrollementStatus.ABSENT;
        if (ap.getSession() != null) {
            InfoSessionDAO idao = context.getInfoSessionDAO();
            status = idao.getUserEnrollmentStatus(ap.getSession().getId(), ap.getUser().getId());
        }
        InfoSessions.UserpickerData data = new InfoSessions.UserpickerData();
        data.populate (ap.getAdmin());
        return ok(setcontractadmin.render(ap, status, Form.form(InfoSessions.UserpickerData.class).fill(data)));
    }

    @AllowRoles({UserRole.INFOSESSION_ADMIN, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result approvalAdminPost(int id) {
        DataAccessContext context = DataAccess.getInjectedContext();
        ApprovalDAO adao = context.getApprovalDAO();
        Approval app = adao.getApproval(id);
        Form<InfoSessions.UserpickerData> form = Form.form(InfoSessions.UserpickerData.class).bindFromRequest();
        if (form.hasErrors()) {
                // TODO: code in common with approvalAdmin
            EnrollementStatus status = EnrollementStatus.ABSENT;
            if (app.getSession() != null) {
                InfoSessionDAO idao = context.getInfoSessionDAO();
                status = idao.getUserEnrollmentStatus(app.getSession().getId(), app.getUser().getId());
            }
            return ok(setcontractadmin.render(app, status, form));
        } else {
            int userId = form.get().userId;

            UserDAO udao = context.getUserDAO();
            UserHeader contractManager = udao.getUserHeader(userId);

            Set<UserRole> userRoles = context.getUserRoleDAO().getUserRoles(userId);
            if (userRoles.contains(UserRole.INFOSESSION_ADMIN) || userRoles.contains(UserRole.SUPER_USER)) {
                // TODO: introduce hasRole method in DAO
                app.setAdmin(contractManager);
                adao.setApprovalAdmin(id, userId);

                Notifier.sendContractManagerAssignedMail(app.getUser(), app);
                flash("success", "De aanvraag werd successvol toegewezen aan " + contractManager);
                return redirect(routes.Approvals.pendingApprovalList());
            } else {
                flash("danger", contractManager + " heeft geen infosessie beheerdersrechten.");
                return redirect(routes.Approvals.approvalAdmin(id));
            }
        }
    }

    /**
     * Method: POST
     *
     * @param approvalId
     * @return
     */
    @AllowRoles({UserRole.INFOSESSION_ADMIN, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result approvalAdminAction(int approvalId) {
        Form<ApprovalAdminData> form = Form.form(ApprovalAdminData.class).bindFromRequest();
        DataAccessContext context = DataAccess.getInjectedContext();
        ApprovalDAO dao = context.getApprovalDAO();
        Approval ap = dao.getApproval(approvalId);

        if (form.hasErrors()) {
            return approvalForm(ap, context, form, true);
        }

        ApprovalAdminData m = form.get();
        ApprovalAdminData.Action action = m.getAction();


        UserDAO udao = context.getUserDAO();
        ap.setAdmin(udao.getUserHeader(CurrentUser.getId()));
        ap.setAdminMessage(m.message);

        if (action == ApprovalAdminData.Action.ACCEPT) {

            ap.setStatus(Approval.ApprovalStatus.ACCEPTED);
            dao.updateApproval(ap);

            // Set contact admin
            User user = udao.getUser(ap.getUser().getId());
            user.setStatus(UserStatus.FULL);
            user.setAgreeTerms(true); //TODO: check if we can accept this earlier, as it is accepted once approval is submitted
            udao.updateUser(user); //full update

            // Add the new user roles
            UserRoleDAO roleDao = context.getUserRoleDAO();
            if (m.sharer)
                roleDao.addUserRole(ap.getUser().getId(), UserRole.CAR_OWNER);
            if (m.user)
                roleDao.addUserRole(ap.getUser().getId(), UserRole.CAR_USER);
            Notifier.sendMembershipStatusChanged(ap.getUser(), true, m.message);
            flash("success", "De gebruikersrechten werden succesvol aangepast.");

            return redirect(routes.Approvals.pendingApprovalList());
        } else if (action == ApprovalAdminData.Action.DENY) {
            //TODO Warning, if status was not pending, possibly have to remove user roles
            ap.setStatus(Approval.ApprovalStatus.DENIED);
            dao.updateApproval(ap);
            Notifier.sendMembershipStatusChanged(ap.getUser(), false, m.message);
            flash("success", "De aanvraag werd met succes afgekeurd.");

            return redirect(routes.Approvals.pendingApprovalList());
        } else {
            return badRequest("Unspecified.");
        }


    }

    public static class RequestApprovalData {
        public String message;
        public boolean acceptsTerms;

        public String validate() {
            if (!acceptsTerms)
                return "Gelieve de algemene voorwaarden te accepteren";
            else
                return null;
        }
    }

    public static class ApprovalAdminData {
        public String message;
        public String status;
        public boolean sharer;
        public boolean user;

        public enum Action {
            ACCEPT("Aanvaarden"),
            DENY("Verwerpen");

            private String description;

            private Action(String description) {
                this.description = description;
            }

            @Override
            public String toString() {
                return description;
            }
        }

        public Action getAction() {
            return Enum.valueOf(Action.class, status);
        }

        public String validate() {
            if (getAction() == Action.ACCEPT && !sharer && !user) { //if the user is accepted, but no extra rules specified
                return "Gelieve aan te geven welke rechten deze gebruiker toegewezen krijgt.";
            } else return null;
        }
    }
}
