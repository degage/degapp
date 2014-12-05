package providers;

import be.ugent.degage.db.*;
import be.ugent.degage.db.jdbc.JDBCFilter;
import be.ugent.degage.db.models.Message;
import be.ugent.degage.db.models.Notification;
import play.cache.Cache;

import java.util.List;

/**
 * Created by stefaan on 20/03/14.
 */
public class CommunicationProvider {

    public static final int AMOUNT_OF_VISIBLE_NOTIFICATIONS = 3;
    public static final int AMOUNT_OF_VISIBLE_MESSAGES = 3;

    private static final String NOTIFICATIONS_BY_ID = "notification:id:%d";
    private static final String NOTIFICATION_NUMBER_BY_ID = "notification_number:id:%d";
    private static final String MESSAGES_BY_ID = "message:id:%d";
    private static final String MESSAGE_NUMBER_BY_ID = "notification_number:id:%d";
    private DataAccessProvider provider;

    public CommunicationProvider(DataAccessProvider provider) {
        this.provider = provider;
    }

    public List<Notification> getNotifications(int userId) {
        String key = String.format(NOTIFICATIONS_BY_ID, userId);
        Object obj = Cache.get(key);
        if (obj == null || !(obj instanceof List)) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                Filter filter = new JDBCFilter();

                filter.putValue(FilterField.USER_ID, userId + "");
                List<Notification> notifications = context.getNotificationDAO().getNotificationList(null, false, 1, AMOUNT_OF_VISIBLE_NOTIFICATIONS, filter);
                Cache.set(key, notifications);
                return notifications;
            }
        } else {
            return (List<Notification>) obj; //Type erasure problem from Java, works at runtime
        }
    }

    public int getNumberOfUnreadNotifications(int userId) {
        String key = String.format(NOTIFICATION_NUMBER_BY_ID, userId);
        Object obj = Cache.get(key);
        if (obj == null || !(obj instanceof List)) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                int unread_number = context.getNotificationDAO().getNumberOfUnreadNotifications(userId);
                Cache.set(key, unread_number);
                return unread_number;
            }
        } else {
            return (Integer) obj; //Type erasure problem from Java, works at runtime
        }
    }

    public void invalidateNotifications(int userId) {
        Cache.remove(String.format(NOTIFICATIONS_BY_ID, userId));
    }

    public void invalidateNotificationNumber(int userId) {
        Cache.remove(String.format(NOTIFICATION_NUMBER_BY_ID, userId));
    }

    public Iterable<Message> getMessages(int userId) {
        String key = String.format(MESSAGES_BY_ID, userId);
        Object obj = Cache.get(key);
        if (obj == null || !(obj instanceof Iterable)) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                Iterable<Message> messages = context.getMessageDAO().listUnreadMessagesTo(userId, AMOUNT_OF_VISIBLE_MESSAGES);
                Cache.set(key, messages);
                return messages;
            }
        } else {
            return (Iterable<Message>) obj; //Type erasure problem from Java, works at runtime
        }
    }

    public void invalidateMessages(int userId) {
        Cache.remove(String.format(MESSAGES_BY_ID, userId));
    }

    public int getNumberOfUnreadMessages(int userId) {
        String key = String.format(MESSAGE_NUMBER_BY_ID, userId);
        Object obj = Cache.get(key);
        if (obj == null || !(obj instanceof List)) {
            try (DataAccessContext context = provider.getDataAccessContext()) {
                int unread_number = context.getMessageDAO().countUnreadMessagesTo(userId);
                Cache.set(key, unread_number);
                return unread_number;
            }
        } else {
            return (Integer) obj; //Type erasure problem from Java, works at runtime
        }
    }

    public void invalidateMessageNumber(int userId) {
        Cache.remove(String.format(MESSAGE_NUMBER_BY_ID, userId));
    }
}
