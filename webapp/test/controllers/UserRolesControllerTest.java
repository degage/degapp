/* UserRolesControllerTest.java
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

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.POST;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.running;
import static play.test.Helpers.status;

import java.util.HashMap;
import java.util.Map;

import controllers.util.TestHelper;
import be.ugent.degage.db.models.Address;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;

import db.DataAccess;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Cookie;
import play.mvc.Result;
import be.ugent.degage.db.DataAccessContext;
import providers.DataProvider;
import be.ugent.degage.db.dao.UserRoleDAO;

public class UserRolesControllerTest {

	private User admin;
	private UserRoleDAO userRoleDAO;
	private Cookie loginCookie;
	private TestHelper helper;
	private Cookie cookie;
	
	
	@Before
	public void setUp(){
		helper = new TestHelper();
		helper.setTestProvider();
		DataAccessContext context = DataAccess.getContext();
		userRoleDAO = context.getUserRoleDAO();
		admin = helper.createRegisteredUser("admin@test.com", "1234piano", "Pol", "Thijs",  new UserRole[]{UserRole.SUPER_USER});
		Address address = new Address("Belgium","9000", "Gent", "Sterre", "S2", "bib");
		admin.setAddressResidence(address);	
	}
	
	@Test
	public void unauthorizedUserTest(){
		running(fakeApplication(),new Runnable() {
			@Override
			public void run() {
				helper.setTestProvider();
				
				// We geven een user alle rechten behalve SUPERUSER
				User user = helper.createRegisteredUser("user@noadmin.com", "1234piano", "Trudy", "Smith",  new UserRole[]{UserRole.USER});
				for(UserRole role : UserRole.values()){
					userRoleDAO.addUserRole(user.getId(), role);
				}
				userRoleDAO.removeUserRole(user.getId(), UserRole.SUPER_USER);
				cookie = helper.login(user, "1234piano");
				
				// niet-SUPERUSER kan niet op de overzichtspagina
				Result result = callAction(
		        		controllers.routes.ref.UserRoles.index(),
		        		fakeRequest().withCookies(cookie)
		        );
		        assertEquals("Requesting user roles overview", UNAUTHORIZED, status(result));
				
		        // Kan ook niet op de edit pagina voor userroles van een gebruiker (of zichzelf in dit geval)
		        Result result1 = callAction(
		        		controllers.routes.ref.UserRoles.edit(user.getId()),
		        		fakeRequest().withCookies(cookie)
		        );
		        assertEquals("Requesting user roles edit page", UNAUTHORIZED, status(result1));
		        
		        Map<String,String> data = new HashMap<>();
		    	data.put("role", UserRole.SUPER_USER.toString());        
		        // Kan ook niet editten (bv zichzelf SUPERUSER maken)
		        Result result2 = callAction(
		        		controllers.routes.ref.UserRoles.editPost(user.getId()),
		        		fakeRequest().withFormUrlEncodedBody(data).withCookies(cookie)
		        );
		        assertEquals("Requesting user roles overview", UNAUTHORIZED, status(result2));
		        
		        data.clear();
		        // Of al zijn rollen af nemen
		        Result result3 = callAction(
		        		controllers.routes.ref.UserRoles.editPost(user.getId()),
		        		fakeRequest().withFormUrlEncodedBody(data).withCookies(cookie)
		        );
		        assertEquals("Requesting user roles overview", UNAUTHORIZED, status(result3));
		        
		        helper.logout();
			}
		});
	}
	
	@Test
	public void adminTest(){
		running(fakeApplication(),new Runnable() {
			@Override
			public void run() {
				helper.setTestProvider();
				cookie = helper.login(admin,"1234piano");
				User user = helper.createRegisteredUser("user@test.com", "1234piano", "Jan", "Peeters", new UserRole[]{UserRole.USER});
				
				// de index pagina van de user roles opvragen, admin moet dit kunnen
				Result result = callAction(
		        		controllers.routes.ref.UserRoles.index(),
		        		fakeRequest().withCookies(cookie)
		        );
		        assertEquals("Requesting user roles overview", OK, status(result));
		        
		        // De edit pagina opvragen
		        Result result1 = callAction(
		        		controllers.routes.ref.UserRoles.edit(user.getId()),
		        		fakeRequest().withCookies(cookie)
		        );
		        assertEquals("Requesting user roles edit page from a user", OK, status(result1));
		        
		        // Een user eens alle mogelijke roles geven		        
		        for(UserRole role : UserRole.values()){
		        	Map<String,String> data = new HashMap<>();
		        	data.put("role", role.toString());
		        	Result result2 = callAction(
		            		controllers.routes.ref.UserRoles.editPost(user.getId()),
		            		fakeRequest().withFormUrlEncodedBody(data).withCookies(cookie)
		            );
		            assertEquals("Editting user role", OK, status(result2));
		        }
		        // user terugzetten zoals het was
		        Map<String,String> dataRole = new HashMap<>();
		    	dataRole.put("role", UserRole.USER.toString());
		    	Result result5 = callAction(
		        		controllers.routes.ref.UserRoles.editPost(user.getId()),
		        		fakeRequest().withFormUrlEncodedBody(dataRole).withCookies(cookie)
		        );
		        assertEquals("Resetting UserRoles", OK, status(result5));
		        
		        // Uw eigen super user rechten afnemen mag niet
		        Map<String,String> data = new HashMap<>();
		    	data.put("role", UserRole.USER.toString()); // er zit geen SUPERUSER in de nieuwe data set
		    	Result result2 = callAction(
		        		controllers.routes.ref.UserRoles.editPost(admin.getId()),
		        		fakeRequest().withFormUrlEncodedBody(data).withCookies(cookie)
		        );
		    	assertEquals("Deleting own superuser role not allowed", BAD_REQUEST, status(result2));
		        
		        // Wanneer de user niet bestaat moet dat een foutboodschap geven
		        Result result3 = callAction(
		        		controllers.routes.ref.UserRoles.edit(9000),
		        		fakeRequest().withCookies(cookie)
		        );
		        Result result4 = callAction(
		        		controllers.routes.ref.UserRoles.editPost(9000),
		        		fakeRequest(POST, "/userroles/edit").withFormUrlEncodedBody(data).withCookies(cookie)
		        );
		        assertEquals("User does not exist", BAD_REQUEST, status(result3));
		        assertEquals("User does not exist", BAD_REQUEST, status(result4));
			}
		});
		
	}
}
