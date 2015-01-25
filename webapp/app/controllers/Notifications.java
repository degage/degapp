/* Notifications.java
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
import be.ugent.degage.db.dao.NotificationDAO;
import be.ugent.degage.db.models.Notification;
import be.ugent.degage.db.models.User;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.notifiers.notifications;
import views.html.notifiers.notificationspage;

import java.util.List;

/**
 * Created by stefaan on 22/03/14.
 */
public class Notifications extends Controller {

    /**
     * Method: GET
     *
     * @return index page containing all the received notifications of a specific user
     */
    @AllowRoles({})
    @InjectContext
    public static Result showNotifications() {
        return ok(notifications.render());
    }

    @AllowRoles({})
    @InjectContext
    public static Result showNotificationsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        User user = DataProvider.getUserProvider().getUser();
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        filter.putValue(FilterField.USER_ID, user.getId() + "");
        return ok(notificationList(page, pageSize, field, asc, filter));


    }

    // used with injected context
    private static Html notificationList(int page, int pageSize, FilterField orderBy, boolean asc, Filter filter) {
        NotificationDAO dao = DataAccess.getInjectedContext().getNotificationDAO();

        List<Notification> list = dao.getNotificationList(orderBy, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfNotifications(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return notificationspage.render(list, page, amountOfResults, amountOfPages);
    }

    /**
     * Method: GET
     *
     * @param notificationId Id of the message that has to be marked as read
     * @return message index page
     */
    @AllowRoles({})
    @InjectContext
    public static Result markNotificationAsRead(int notificationId) {
        User user = DataProvider.getUserProvider().getUser();
        NotificationDAO dao = DataAccess.getInjectedContext().getNotificationDAO();
        dao.markNotificationAsRead(notificationId);
        DataProvider.getCommunicationProvider().invalidateNotifications(user.getId());
        return redirect(routes.Notifications.showNotifications());
    }

    /**
     * Method: GET
     *
     * @return notification index page
     */
    @AllowRoles({})
    @InjectContext
    public static Result markAllNotificationsAsRead() {
        User user = DataProvider.getUserProvider().getUser();
        NotificationDAO dao = DataAccess.getInjectedContext().getNotificationDAO();
        dao.markAllNotificationsAsRead(user.getId());
        DataProvider.getCommunicationProvider().invalidateNotifications(user.getId());
        return redirect(routes.Notifications.showNotifications());
    }

}
