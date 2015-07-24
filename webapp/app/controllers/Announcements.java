/* Announcements.java
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

import be.ugent.degage.db.dao.AnnouncementDAO;
import be.ugent.degage.db.models.Announcement;
import be.ugent.degage.db.models.UserRole;
import controllers.util.MarkdownEngine;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.announcements.edit;
import views.html.announcements.list;

/**
 * Management of announcements
 */
public class Announcements extends Controller {

    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result index() {
        return ok(list.render(
                DataAccess.getInjectedContext().getAnnouncementDAO().listAnnouncements()
        ));
    }

    public static class Data {
        public String markdown;

        public String action;
    }

    /**
     * Show the page for editing an announcement
     */
    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result edit(String key) {
        Announcement announcement = DataAccess.getInjectedContext().getAnnouncementDAO().getAnnouncementFull(key);
        Data data = new Data();
        data.markdown = announcement.getMarkdown();
        return ok(edit.render(
                Form.form(Data.class).fill(data),
                key,
                announcement.getDescription(),
                announcement.getHtml(),
                announcement.getMarkdown()
        ));
    }

    /**
     * Process the page for editing an announcement
     */
    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result doEdit(String key) {
        // TODO: also allow save instead of only preview
        Data data = Form.form(Data.class).bindFromRequest().get();
        AnnouncementDAO dao = DataAccess.getInjectedContext().getAnnouncementDAO();
        if ("default".equals(data.action)) {
            // save
            dao.updateAnnouncement(key, MarkdownEngine.toHtml(data.markdown), data.markdown);
            return redirect(routes.Announcements.index());
        } else {
            // preview
            return ok(edit.render(
                    Form.form(Data.class).fill(data),
                    key,
                    dao.getAnnouncementDescription(key),
                    MarkdownEngine.toHtml(data.markdown),
                    data.markdown
            ));
        }
    }
}
