/* Contracts.java
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

import be.ugent.degage.db.dao.MembershipDAO;
import be.ugent.degage.db.models.Membership;
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.models.UserRole;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.contracts.contracts;
import views.html.contracts.contractsPage;
import views.html.contracts.edit;

import java.time.LocalDate;

/**
 * Controller that handles contract related actions.
 */
public class Contracts extends Controller {

    public static class Data {
        public boolean signed;
        public String date;

        public Data populate(LocalDate contractDate) {
            if (contractDate == null) {
                this.date = "";
                this.signed = false;
            } else {
                this.date = Utils.toDateString(contractDate);
                this.signed = true;
            }
            return this;
        }
    }

    // must be used with injected context - used in contracts menu
    public static int getNrOfUnsignedContracts() {
        return DataAccess.getInjectedContext().getMembershipDAO().getNrOfUnsignedContracts(CurrentUser.getId());
    }

    @AllowRoles({UserRole.CONTRACT_ADMIN, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result contract(int userId) {
        Membership membership = DataAccess.getInjectedContext().getMembershipDAO().getMembership(userId);
        if (CurrentUser.hasRole(UserRole.PROFILE_ADMIN) || CurrentUser.is(membership.getContractAdmin())) {
            return ok(edit.render(
                    Form.form(Data.class).fill(new Data().populate(membership.getContractDate())),
                    userId,
                    membership.getFullName()
                    )
            );
        } else {
            return badRequest();
        }
    }

    @AllowRoles({UserRole.CONTRACT_ADMIN, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result contractPost(int userId) {
        Form<Data> form = Form.form(Data.class).bindFromRequest();
        MembershipDAO dao = DataAccess.getInjectedContext().getMembershipDAO();
        Membership membership = dao.getMembership(userId);
        if (form.hasErrors()) {
            return badRequest(edit.render(form, membership.getId(), membership.getFullName()));
        } else if (CurrentUser.hasRole(UserRole.PROFILE_ADMIN) || CurrentUser.is(membership.getContractAdmin())) {
            Data data = form.get();
            dao.updateUserContract(userId, data.signed ? Utils.toLocalDate(data.date) : null);
            return redirect(routes.Contracts.showContracts(0));
        } else {
            return badRequest();
        }
    }

    @AllowRoles({UserRole.CONTRACT_ADMIN})
    @InjectContext
    public static Result showContracts(int tab) {
        return ok(contracts.render(tab));
    }

    @AllowRoles({UserRole.CONTRACT_ADMIN})
    @InjectContext
    public static Result showContractsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        int type = 2;
        if (searchString.endsWith("PENDING")) {
            type = 0;
        } else if (searchString.endsWith("ACCEPTED")) {
            type = 1;
        }
        return ok(contractsPage.render(DataAccess.getInjectedContext().getMembershipDAO().getContractees(
                CurrentUser.getId(),
                type,
                page, pageSize), type));
    }


}
