/* TestNotificationDAO.java
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
