/* TestMessageDAO.java
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
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package database.mocking;

import java.util.ArrayList;
import java.util.List;

import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import org.joda.time.DateTime;

import play.libs.Time;
import be.ugent.degage.db.models.Message;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.MessageDAO;

public class TestMessageDAO implements MessageDAO{
	
	private List<Message> messages;
	private int idCounter;
	
	public TestMessageDAO(){
		messages = new ArrayList<>();
		idCounter=0;
	}

    @Override
    public int getAmountOfMessages(Filter filter) throws DataAccessException {
        return 0;
    }


    @Override
    public List<Message> getMessageList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        List<Message> list = new ArrayList<>();
        for(Message message : messages){
            if(message.getReceiver().getId()==1){ // TODO: get userId from filter
                list.add(message);
            }
        }
        return list;
    }

    @Override
	public int getNumberOfUnreadMessages(int userId) throws DataAccessException {
		int counter = 0;
		for(Message message : messages){
			if(!message.isRead()){
				counter++;
			}
		}
		return counter;
	}

    @Override
    public Message createMessage(User sender, User receiver, String subject, String body) throws DataAccessException {
            Message message = new Message(idCounter++,sender, receiver, false, subject, body, new DateTime());
            messages.add(message);
            return message;
    }

    @Override
	public void markMessageAsRead(int messageID) throws DataAccessException {
		for(Message message : messages){
			if(message.getId()==messageID){
				message.setRead(true);
				return;
			}
		}
	}

}
