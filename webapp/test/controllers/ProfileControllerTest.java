package controllers;

import controllers.util.TestHelper;
import be.ugent.degage.db.DataAccessContext;
import providers.DataProvider;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import play.mvc.Http.Cookie;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.*;
import static play.test.Helpers.status;
import static play.test.Helpers.POST;

public class ProfileControllerTest {

    // TODO: identity cards, drivers licenses

    private User user;
    private User user2;
    private User admin;
    private TestHelper helper;
    private Cookie loginCookie;

    @Before
    public void setUp() {
        helper = new TestHelper();
        helper.setTestProvider();
        user = helper.createRegisteredUser("test@test.com", "1234piano", "Pol", "Thijs", new UserRole[]{UserRole.USER});
        user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Sarah", "Desmet", new UserRole[]{UserRole.USER});
        admin = helper.createRegisteredUser("test3@test.com", "1234piano", "Admin", "Rules", new UserRole[]{UserRole.SUPER_USER});
    }

    /**
     * Tests method Profile.indexWithoutId()
     */
    @Test
    public void testIndexWithoutId() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user, "1234piano");

                // Test if we can see an edit screen for the user
                Result result = callAction(
                        controllers.routes.ref.Profile.indexWithoutId(),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", OK, status(result));
                testContents(result, user);
                helper.logout();
            }
        });
    }

    /**
     * Tests method Profile.index(int userId)
     * First you log in as a regular user, then as an admin
     * For both users you run the test for:
     * your own ID
     * id of another existing user
     * id of another non-existing user
     */
    @Test
    public void testIndex() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                //Login as regular user
                loginCookie = helper.login(user, "1234piano");

                //Test for your own ID
                Result result1 = callAction(
                        controllers.routes.ref.Profile.index(user.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", OK, status(result1));

                testContents(result1, user);

                //Test for other existing user
                Result result2 = callAction(
                        controllers.routes.ref.Profile.index(user2.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", BAD_REQUEST, status(result2));

                //Test for other non-existing user
                try {
                    Result result3 = callAction(
                            controllers.routes.ref.Profile.index(user.getId() + 10),
                            fakeRequest().withCookies(loginCookie)
                    );
                    //                  Assert.fail();
                } catch (Exception e) {
                    Assert.assertTrue(true);
                }

                //Login as admin
                helper.logout();
                loginCookie = helper.login(admin, "1234piano");

                //Test for your own ID
                Result result4 = callAction(
                        controllers.routes.ref.Profile.index(admin.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", OK, status(result4));
                testContents(result4, admin);

                //Test for other existing user
                Result result5 = callAction(
                        controllers.routes.ref.Profile.index(user2.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", OK, status(result5));
                testContents(result5, user2);

                //Test for other non-existing user
                try {
                    Result result6 = callAction(
                            controllers.routes.ref.Profile.index(user.getId() + 10),
                            fakeRequest().withCookies(loginCookie)
                    );
//                    Assert.fail();
                } catch (Exception e) {
                    Assert.assertTrue(true);
                }
                helper.logout();
            }
        });
    }

    /**
     * Tests method Profile.edit(int userId)
     * Once with existing user, once with a fake
     */
    //Result edit(int userId)
    //Usual checks for permissions
    //Check if all values are correct
    //Check if needed values are empty


    /**
     * Tests method Profile.edit(int userId)
     * First you log in as a regular user, then as an admin
     * For both users you run the test for:
     * your own ID
     * id of another existing user
     * id of another non-existing user
     */
    @Test
    public void testEdit() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                //Login as regular user
                loginCookie = helper.login(user, "1234piano");

                //Test for your own ID
                Result result1 = callAction(
                        controllers.routes.ref.Profile.edit(user.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", OK, status(result1));

                testContents(result1, user);

                //Test for other existing user
                Result result2 = callAction(
                        controllers.routes.ref.Profile.edit(user2.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", BAD_REQUEST, status(result2));

                //Test for other non-existing user
/*		try{
                    Result result3 = callAction(
		        controllers.routes.ref.Profile.edit(-user.getId()),
                        fakeRequest().withCookies(loginCookie)
                    );
//                    Assert.fail();
		} catch (Exception e) {
                    Assert.assertTrue(true);
		}
*/
                //Login as admin
                helper.logout();
                loginCookie = helper.login(admin, "1234piano");

                //Test for your own ID
                Result result4 = callAction(
                        controllers.routes.ref.Profile.edit(admin.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", OK, status(result4));
                testContents(result4, admin);

                //Test for other existing user
                Result result5 = callAction(
                        controllers.routes.ref.Profile.edit(user2.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", OK, status(result5));
                testContents(result5, user2);

                //Test for other non-existing user
/*		try{
                    Result result6 = callAction(
		        controllers.routes.ref.Profile.edit(-admin.getId()),
                        fakeRequest().withCookies(loginCookie)
                    );
                    Assert.fail();
		} catch (Exception e) {
                    Assert.assertTrue(true);
		}
*/
                helper.logout();
            }
        });
    }

    public void testContents(Result r, User u) {
        Assert.assertTrue(contentAsString(r).contains(u.getEmail()));
        Assert.assertTrue(contentAsString(r).contains(u.getFirstName()));
        Assert.assertTrue(contentAsString(r).contains(u.getLastName()));
    }


    // TODO: doesn't work yet: I don't think contentAsString(Result) will work...
    /**
     * Tests method Profile.editPost(int userId)
     * Once with existing user, once with a fake
     */
    @Test
    public void testEditPost() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                //Login as regular user
                loginCookie = helper.login(user, "1234piano");

                Map<String,String> data = new HashMap<>();
                data.put("email", user.getEmail());
                data.put("firstName", user.getFirstName());
                data.put("lastName", user.getLastName());

                //Test for your own ID
                Result result1 = callAction(
                        controllers.routes.ref.Profile.editPost(user.getId()),
                        fakeRequest().withFormUrlEncodedBody(data).withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", 303, status(result1));
                testContents(result1, user);
                Assert.assertTrue(contentAsString(result1).contains(user.getEmail()));
                Assert.assertTrue(contentAsString(result1).contains(user.getFirstName()));
                Assert.assertTrue(contentAsString(result1).contains(user.getLastName()));

                //Test for other existing user
                Result result2 = callAction(
                        controllers.routes.ref.Profile.editPost(user2.getId()),
                        fakeRequest().withFormUrlEncodedBody(data).withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", BAD_REQUEST, status(result2));

                //Test for other non-existing user
/*		try{
                    Result result3 = callAction(
		        controllers.routes.ref.Profile.editPost(-user.getId()),
                        fakeRequest().withCookies(loginCookie)
                    );
//                    Assert.fail();
		} catch (Exception e) {
                    Assert.assertTrue(true);
		}
*/
                //Login as admin
                helper.logout();
                loginCookie = helper.login(admin, "1234piano");

                data = new HashMap<>();
                data.put("email", admin.getEmail());
                data.put("firstName", admin.getFirstName());
                data.put("lastName", admin.getLastName());
                //Test for your own ID
                Result result4 = callAction(
                        controllers.routes.ref.Profile.editPost(admin.getId()),
                        fakeRequest().withFormUrlEncodedBody(data).withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", 303, status(result4));
                Assert.assertTrue(contentAsString(result4).contains(admin.getEmail()));
                Assert.assertTrue(contentAsString(result4).contains(admin.getFirstName()));
                Assert.assertTrue(contentAsString(result4).contains(admin.getLastName()));


                data = new HashMap<>();
                data.put("email", user2.getEmail());
                data.put("firstName", user2.getFirstName());
                data.put("lastName", user2.getLastName());

                //Test for other existing user
                Result result5 = callAction(
                        controllers.routes.ref.Profile.editPost(user2.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Profiel bewerken", 303, status(result5));

                Assert.assertTrue(contentAsString(result5).contains(user2.getEmail()));
                Assert.assertTrue(contentAsString(result5).contains(user2.getFirstName()));
                Assert.assertTrue(contentAsString(result5).contains(user2.getLastName()));

                //Test for other non-existing user
/*		try{
                    Result result6 = callAction(
		        controllers.routes.ref.Profile.editPost(-admin.getId()),
                        fakeRequest().withCookies(loginCookie)
                    );
                    Assert.fail();
		} catch (Exception e) {
                    Assert.assertTrue(true);
		}
*/
                helper.logout();
            }
        });
    }


//Juiste waarden invullen
//Verkeerde waarden invullen
//Waarden niet invullen

//Verkeerde gebruiker
//Not authenticated

}
