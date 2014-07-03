package database.mocking;

import java.util.ArrayList;
import java.util.List;

import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Notification;
import be.ugent.degage.db.models.User;

import org.joda.time.DateTime;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.NotificationDAO;

public class TestNotificationDAO implements NotificationDAO{

	private List<Notification> notifications;
	private int idCounter;
	
	public TestNotificationDAO(){
		notifications = new ArrayList<>();
		idCounter=0;
	}

    @Override
    public int getAmountOfNotifications(Filter filter) throws DataAccessException {
        return 0;
    }

    @Override
	public List<Notification> getNotificationListForUser(int userId) throws DataAccessException {
		List<Notification> list = new ArrayList<>();
		for(Notification notification : notifications){
			if(notification.getUser().getId()==userId){
				list.add(notification);
			}
		}
		return list;
	}

    @Override
    public List<Notification> getNotificationList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        List<Notification> list = new ArrayList<>();
        for(Notification notification : notifications){
            if(notification.getUser().getId()==1){ // TODO: getUserId from filter
                list.add(notification);
            }
        }
        return list.subList((page-1)*pageSize, page*pageSize > list.size() ? list.size() : page*pageSize );
    }

	@Override
	public int getNumberOfUnreadNotifications(int userId) throws DataAccessException {
		int counter = 0;
		for(Notification not : notifications){
			if(not.getUser().getId()==userId){
				counter++;
			}
		}
		return counter;
	}

    @Override
    public Notification createNotification(User user, String subject, String body) throws DataAccessException {
        Notification notification = new Notification(idCounter++, user, false, subject, body, new DateTime());
        notifications.add(notification);
        return notification;
    }

    @Override
    public void markNotificationAsRead(int notificationId) throws DataAccessException {
        //TODO
    }

}
