/* Receipts.java
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

import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.receipts.receipts;
import views.html.receipts.receiptspage;

import java.time.LocalDate;
import java.util.List;

public class Receipts extends Controller {

    private static final int PAGE_SIZE = 10;

    /**
     * @return The users index-page with all users
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result index() {
        return ok(receipts.render());
    }

    /**
     * @param page    The page in the userlists
     * @param ascInt  An integer representing ascending (1) or descending (0)
     * @param orderBy A field representing the field to order on
     * @return A partial page with a table of users of the corresponding page
     */
    // @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result showReceiptsPage(int page, int ascInt, String orderBy, String date) {
        // TODO: orderBy not as String-argument?
        FilterField receiptsField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(date);
        User currentUser = DataProvider.getUserProvider().getUser();
        ReceiptDAO dao = DataAccess.getInjectedContext().getReceiptDAO();

        if (receiptsField == null) {
            receiptsField = FilterField.RECEIPT_DATE;
        }
        String filterString = filter.getValue(FilterField.RECEIPT_DATE);
        LocalDate localDate = Utils.toLocalDate(filterString);

        List<Receipt> listOfReceipts = dao.getReceiptsList(receiptsField, asc, page, PAGE_SIZE, localDate, currentUser);

        int amountOfResults = dao.getAmountOfReceipts(localDate, currentUser);
        //int amountOfResults = listOfReceipts.size();
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) PAGE_SIZE);

        //if(){rendernew()}
        return ok(receiptspage.render(listOfReceipts, page, amountOfResults, amountOfPages));
    }


}
