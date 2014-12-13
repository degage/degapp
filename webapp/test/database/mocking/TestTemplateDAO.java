/* TestTemplateDAO.java
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

import be.ugent.degage.db.models.EmailTemplate;
import be.ugent.degage.db.models.MailType;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.TemplateDAO;

public class TestTemplateDAO implements TemplateDAO{
	
	private List<EmailTemplate> list;
	
	public TestTemplateDAO(){
		list = new ArrayList<>();
        //VERIFICATION(1), WELCOME(2), INFOSESSION_ENROLLED(3), RESERVATION_APPROVE_REQUEST(4), RESERVATION_APPROVED_BY_OWNER(5), RESERVATION_REFUSED_BY_OWNER(6), PASSWORD_RESET(7);


        list.add(new EmailTemplate(1, "VERIFICATION", "", new ArrayList<String>(), "VERIFICATION", false, false));
        list.add(new EmailTemplate(2, "WELCOME", "", new ArrayList<String>(), "WELCOME", false, false));
        list.add(new EmailTemplate(3, "INFOSESSION_ENROLLED", "", new ArrayList<String>(), "INFOSESSION_ENROLLED", false, false));
        list.add(new EmailTemplate(4, "RESERVATION_APPROVE_REQUEST", "", new ArrayList<String>(), "RESERVATION_APPROVE_REQUEST", false, false));
        list.add(new EmailTemplate(5, "RESERVATION_APPROVED_BY_OWNER", "", new ArrayList<String>(), "RESERVATION_APPROVED_BY_OWNER", false, false));
        list.add(new EmailTemplate(6, "RESERVATION_REFUSED_BY_OWNER", "", new ArrayList<String>(), "RESERVATION_REFUSED_BY_OWNER", false, false));
        list.add(new EmailTemplate(7, "PASSWORD_RESET", "", new ArrayList<String>(), "PASSWORD_RESET", false, false));
	}

	@Override
	public EmailTemplate getTemplate(int templateID) throws DataAccessException {
		for(EmailTemplate template : list){
			if(template.getId() == templateID){
				return new EmailTemplate(templateID, template.getTitle(), template.getBody(), template.getUsableTags(), template.getSubject(), template.getSendMail()
						, template.getSendMailChangeable());
			}
		}
        // For now, let's give a default one when it's not find (so we don't get errors in the controllers)
		return list.get(0);
	}

	@Override
	public EmailTemplate getTemplate(MailType type) throws DataAccessException {
		for(EmailTemplate template : list){
			if(template.getId() == type.getKey()){
				return new EmailTemplate(template.getId(), template.getTitle(), template.getBody(), template.getUsableTags(), template.getSubject(), template.getSendMail()
						, template.getSendMailChangeable());
			}
		}
        // For now, let's give a default one when it's not find (so we don't get errors in the controllers)
        return list.get(0);
	}

	@Override
	public List<EmailTemplate> getAllTemplates() throws DataAccessException {
		return list;
	}

	@Override
	public void updateTemplate(int templateID, String templateBody,
			String templateSubject, boolean templateSendMail)
			throws DataAccessException {
		for(EmailTemplate template : list){
			if(template.getId() == templateID){
				list.add(new EmailTemplate(templateID, template.getTitle(), templateBody, template.getUsableTags(), templateSubject, templateSendMail, template.getSendMailChangeable()));
				list.remove(template);
			}
		}
		
	}

		
}
