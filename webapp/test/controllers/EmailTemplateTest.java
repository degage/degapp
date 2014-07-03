package controllers;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.test.Helpers.POST;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.running;
import static play.test.Helpers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controllers.util.TestHelper;
import be.ugent.degage.db.models.EmailTemplate;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Cookie;
import play.mvc.Result;
import be.ugent.degage.db.DataAccessContext;
import providers.DataProvider;
import be.ugent.degage.db.TemplateDAO;

public class EmailTemplateTest {
	
	private TemplateDAO emailDAO;
	private User admin;
	private List<EmailTemplate> templates;
	private List<User> nonAdmins;
	private TestHelper helper;
	private Cookie cookie;
	
	
	@Before
	public void setUp(){
		helper = new TestHelper();
		helper.setTestProvider();
		DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext();
		emailDAO = context.getTemplateDAO();
		templates = emailDAO.getAllTemplates();
		nonAdmins = new ArrayList<>();
		admin = helper.createRegisteredUser("admin@test.com", "1234piano", "Pol", "Thijs",new UserRole[]{UserRole.MAIL_ADMIN});
		int i=0;
		for(UserRole role : UserRole.values()){
			i++;
			if(role!=UserRole.MAIL_ADMIN && role!=UserRole.SUPER_USER){
				nonAdmins.add(helper.createRegisteredUser("gebruiker"+i+"@nonadmin.com", "1234piano", "Jan", "Peeters", new UserRole[]{role}));
			}
		}
	}
	
	@Test
	public void editTemplateSucces(){
		running(fakeApplication(),new Runnable() {
			
			@Override
			public void run() {
				helper.setTestProvider();
				cookie = helper.login(admin,"1234piano");
				for(EmailTemplate template : templates){
					// Individueel template laten zien
					Result result = callAction(
			                controllers.routes.ref.EmailTemplates.showTemplate(template.getId()),
			                fakeRequest().withCookies(cookie)
			        );
					assertEquals("Show individual template", OK, status(result));
					
					Map<String,String> data = new HashMap<>();
					data.put("template_id", ""+template.getId());
					data.put("template_body", "Dit is de nieuwe body voor de template met id: " + template.getId() + ".");
					data.put("template_subject", "Geen onderwerp");
					
					// template aanpassen
					Result result1 = callAction(
			                controllers.routes.ref.EmailTemplates.editTemplate(),
			                fakeRequest(POST,"/emailtemplate/edit")
			        );
					assertEquals("Editing template", 303, status(result1));
				}
				helper.logout();
			}
		});
		
		
	}
	
	@Test
	public void showTemplateSucces(){
		running(fakeApplication(),new Runnable() {
			
			@Override
			public void run() {
				helper.setTestProvider();
				cookie = helper.login(admin,"1234piano");
				// Alle templates laten zien
				Result result = callAction(
		                controllers.routes.ref.EmailTemplates.showExistingTemplates(),
		                fakeRequest().withCookies(cookie)
		        );
		        assertEquals("Show email templates", OK, status(result));
		        helper.logout();
			}	
		});
		
	}
	
	@Test
	public void showTemplateFail(){
		running(fakeApplication(),new Runnable() {
			
			@Override
			public void run() {
				helper.setTestProvider();
				for(User user : nonAdmins){
					cookie = helper.login(user,"1234piano");
					// Alle templates laten zien, moet falen want we hebben niet de juiste rechten
					Result result = callAction(
			                controllers.routes.ref.EmailTemplates.showExistingTemplates(),
			                fakeRequest().withCookies(cookie)
			        );
			        assertEquals("Show email templates", UNAUTHORIZED, status(result));
			        helper.logout();
				}
			}
		});
		
	}
	
	@Test
	public void editTemplateFail(){
		running(fakeApplication(),new Runnable() {
			
			@Override
			public void run() {
				helper.setTestProvider();
				for(User user : nonAdmins){
					cookie = helper.login(user,"1234piano");
					// Template proberen editen, moet falen want we hebben niet de juiste rechten
					Result result = callAction(
			                controllers.routes.ref.EmailTemplates.editTemplate(),
			                fakeRequest().withCookies(cookie)
			        );
			        assertEquals("Show email templates", UNAUTHORIZED, status(result));
			        helper.logout();
				}
			}
		});
		
	}
}
