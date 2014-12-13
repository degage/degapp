/* AuthorizationTest.java
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

import controllers.routes;
import be.ugent.degage.db.models.Address;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import org.junit.*;

import play.cache.Cache;
import play.mvc.*;
import play.test.FakeRequest;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

/**
 * Created by Benjamin on 26/02/14.
 */
public class AuthorizationTest {

    private User dummy;

    /**
     * Set up method to initialize the dummy user.
     */
   /* @Before
    public void setup() {
        dummy = new User(1, "John.Doe@email.com", "John", "Doe", "password",
                new Address("0000", "NowhereCity", "LonelyStreet", "123456789", ""));
    } */

    /**
     * Testing whether a non-registered user, or non-logged-in user, can't enter the secured pages.
     * Expected the user to be redirected to the login page.
     */
    /*@Test
    public void noUserAuthorization() {
        Result result = callAction(routes.ref.InfoSessions.newSession());
        assertThat(status(result)).isEqualTo(Http.Status.SEE_OTHER);
    } */

    /**
     * Testing whether a logged-in non-authorized user can't enter the secured page.
     * Expected to display the unauthorized page.
     */
  /*  @Test
    public void userNotAutherized() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Cache.set("user:email:John.Doe@email.com", dummy);
                FakeRequest fakeRequest = new FakeRequest().withSession("email", "John.Doe@email.com");
                Result result = callAction(routes.ref.InfoSessions.newSession(), fakeRequest);
                assertThat(status(result)).isEqualTo(Http.Status.UNAUTHORIZED);
            }
        });
    } */

    /**
     * Testing whether a logged-in authorized user can enter the secured page.
     */
   /* @Test
    public void userAuthorized() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Cache.set("user:email:John.Doe@email.com", dummy);
                dummy.addRole(UserRole.ADMIN);
                FakeRequest fakeRequest = new FakeRequest().withSession("email", "John.Doe@email.com");
                Result result = callAction(routes.ref.InfoSessions.newSession(), fakeRequest);
                assertThat(status(result)).isEqualTo(Http.Status.OK);
                dummy.dropRole(UserRole.ADMIN);
            }
        });
    } */

}
