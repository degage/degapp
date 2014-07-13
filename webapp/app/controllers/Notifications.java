package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.NotificationDAO;
import be.ugent.degage.db.models.Notification;
import be.ugent.degage.db.models.User;
import controllers.Security.RoleSecured;
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
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result showNotifications() {
        return ok(notifications.render());
    }

    @RoleSecured.RoleAuthenticated()
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
    @RoleSecured.RoleAuthenticated()
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
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result markAllNotificationsAsRead() {
        User user = DataProvider.getUserProvider().getUser();
        NotificationDAO dao = DataAccess.getInjectedContext().getNotificationDAO();
        dao.markAllNotificationsAsRead(user.getId());
        DataProvider.getCommunicationProvider().invalidateNotifications(user.getId());
        return redirect(routes.Notifications.showNotifications());
    }

}