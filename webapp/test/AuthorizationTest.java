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
