/* EmailTemplates.java
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

package controllers;

import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.TemplateDAO;
import be.ugent.degage.db.models.EmailTemplate;
import be.ugent.degage.db.models.UserRole;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import views.html.emailtemplates.edit;
import views.html.emailtemplates.emailtemplates;
import views.html.emailtemplates.emailtemplatespage;

import java.util.List;
import java.util.Map;


/**
 * Controller responsible for showing and editing message templates
 */
public class EmailTemplates extends Controller {

    /**
     * Method: GET
     *
     * @return all the templates that are available in the system
     */
    @AllowRoles({UserRole.MAIL_ADMIN})
    @InjectContext
    public static Result showExistingTemplates() {
        return ok(emailtemplates.render());
    }

    @AllowRoles({UserRole.MAIL_ADMIN})
    @InjectContext
    public static Result showExistingTemplatesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        // TODO: orderBy not as String-argument?
        FilterField carField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);
        return ok(templateList(page, pageSize, carField, asc, filter));
    }

    // used with injected context
    private static Html templateList(int page, int pageSize, FilterField orderBy, boolean asc, Filter filter) {
        TemplateDAO dao = DataAccess.getInjectedContext().getTemplateDAO();

        if (orderBy == null) {
            orderBy = FilterField.TEMPLATE_NAME;
        }
        List<EmailTemplate> listOfTemplates = dao.getTemplateList(orderBy, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfTemplates(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return emailtemplatespage.render(listOfTemplates, page, amountOfResults, amountOfPages);
    }

    /**
     * Method: GET
     *
     * @param templateId the id of the template of which the details are requested
     * @return the detail page of specific template
     */
    @AllowRoles({UserRole.MAIL_ADMIN})
    @InjectContext
    public static Result showTemplate(int templateId) {
        TemplateDAO dao = DataAccess.getInjectedContext().getTemplateDAO();
        EmailTemplate template = dao.getTemplate(templateId);
        if (template == null) {
            return badRequest("Template bestaat niet.");
        } else {
            return ok(edit.render(template));
        }

    }

    /**
     * Method: POST
     * Called when a template is edited
     *
     * @return templates index page
     */
    @AllowRoles({UserRole.MAIL_ADMIN})
    @InjectContext
    public static Result editTemplate() {
        TemplateDAO dao = DataAccess.getInjectedContext().getTemplateDAO();
        final Map<String, String[]> values = request().body().asFormUrlEncoded();
        String templateBody = values.get("template_body")[0];
        String templateSubject = values.get("template_subject")[0];
        int templateId = Integer.parseInt(values.get("template_id")[0]);
        boolean templateSendMail = Boolean.parseBoolean(values.get("template_send_mail")[0]);
        dao.updateTemplate(templateId, templateBody, templateSubject, templateSendMail);
        return ok(routes.EmailTemplates.showExistingTemplates().toString());
    }
}
