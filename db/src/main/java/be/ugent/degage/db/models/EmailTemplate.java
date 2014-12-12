/* EmailTemplate.java
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

package be.ugent.degage.db.models;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 01/03/14.
 */
public class EmailTemplate {

    private int id;
    private String title;
    private String subject;
    private String body;
    private List<String> tags;
    private boolean sendMail;
    private boolean sendMailChangeable;

    public EmailTemplate(int id, String title, String body, List<String> tags, String subject, boolean sendMail, boolean sendMailChangeable){
        this.id = id;
        this.title = title;
        this.body = body;
        this.tags = tags;
        this.subject = subject;
        this.sendMail = sendMail;
        this.sendMailChangeable = sendMailChangeable;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public boolean getSendMail() {
        return sendMail;
    }

    public boolean getSendMailChangeable() {
        return sendMailChangeable;
    }

    public String getBody() {
        return body;
    }

    public List<String> getUsableTags(){
        return tags;
    }

}
