/* Announcement.java
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

/**
 * Represent a (static) message that is displayed on a certain web page, e.g., on the login page,
 * or on the main dashboard page.
 */
public class Announcement {

    private String key;

    private String description;

    private String html;

    private String markdown;

    public Announcement(String key, String description, String html, String markdown) {
        this.key = key;
        this.description = description;
        this.html = html;
        this.markdown = markdown;
    }

    /**
     * Key by which this message is known.
     */
    public String getKey() {
        return key;
    }

    /**
     * Short description of where this message is used
     */
    public String getDescription() {
        return description;
    }

    /**
     * HTML version of this message. (Read only)
     */
    public String getHtml() {
        return html;
    }

    /**
     * Markdown version of this message. Can be edited by the user.
     */
    public String getMarkdown() {
        return markdown;
    }
}
