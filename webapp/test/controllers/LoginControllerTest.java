package controllers;

import controllers.util.TestHelper;
import providers.DataProvider;
import be.ugent.degage.db.mocking.TestDataAccessProvider;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;
import org.junit.*;
import play.mvc.Result;
import play.mvc.Http.Cookie;

import java.util.HashMap;
import java.util.Map;

import static play.test.Helpers.*;
import static org.junit.Assert.*;

/**
 * Created by Cedric on 3/7/14.
 */
public class LoginControllerTest {

    private User user;
    private TestHelper helper;
    private Cookie loginCookie;

    @Before
    public void setUp(){
        helper = new TestHelper();
        helper.setTestProvider();
        user = helper.createRegisteredUser("test@test.com", "1234piano", "Pol", "Thijs",new UserRole[]{});
    }

    @Test
    public void testBadLogin(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                DataProvider.setDataAccessProvider(new TestDataAccessProvider()); // Required!!

                Map<String,String> data = new HashMap<>();
                data.put("email", "nonexistent@something.com");
                data.put("password", "thiswillnotwork");
                Result result = callAction(
                        controllers.routes.ref.Login.authenticate("/"),
                        fakeRequest().withFormUrlEncodedBody(data)
                );
                assertEquals(BAD_REQUEST, status(result));
            }
        });
    }

    // TODO: Can we really have a dependency on our models in controller testing??
    @Test
    public void testGoodLogin(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Test if we can access dashboard now
                Result result2 = callAction(
                        controllers.routes.ref.Dashboard.index(),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Requesting dashboard when logged in", OK, status(result2));

                helper.logout();

                // Test if we cannot access dashboard now
                Result result4 = callAction(
                        controllers.routes.ref.Dashboard.index(),
                        fakeRequest().withCookies()
                );
                assertNotEquals("Requesting dashboard when nog loggedin", OK, status(result4));
            }
        });
    }
}
