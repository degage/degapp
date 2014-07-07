package controllers;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.POST;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.running;
import static play.test.Helpers.status;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.UNAUTHORIZED;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import controllers.util.TestHelper;
import be.ugent.degage.db.models.Address;
import be.ugent.degage.db.models.EnrollementStatus;
import be.ugent.degage.db.models.InfoSession;
import be.ugent.degage.db.models.InfoSessionType;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserRole;

import db.DataAccess;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Cookie;
import play.mvc.Result;
import be.ugent.degage.db.DataAccessContext;
import providers.DataProvider;
import be.ugent.degage.db.dao.InfoSessionDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.dao.UserRoleDAO;

public class InfoSessionsControllerTest {

    // TODO: everything related to Approvals

    private UserDAO userDAO;
    private UserRoleDAO userRoleDAO;
    private List<User> users;
    private Map<User, String> pwMap;
    private User admin;
    private TestHelper helper;
    private Cookie cookie;

    @Before
    public void setUp() {
        helper = new TestHelper();
        helper.setTestProvider();
        DataAccessContext context = DataAccess.getContext();
        userDAO = context.getUserDAO();
        userRoleDAO = context.getUserRoleDAO();
        users = new ArrayList<>();
        pwMap = new HashMap<User, String>();
        admin = helper.createRegisteredUser("admin@test.com", "1234piano", "Pol", "Thijs", new UserRole[]{UserRole.INFOSESSION_ADMIN});
        Address address = new Address("Belgium", "9000", "Gent", "Sterre", "S2", "bib");
        admin.setAddressResidence(address);
        pwMap.put(admin, "1234piano");
        Scanner sc;
        try {
            sc = new Scanner(new File("test/database/random_users.txt"));
            sc.useDelimiter("\\t|\\r\\n");
            sc.nextLine(); //skip header first time
            int amount = 0;
            while (sc.hasNext() && amount < 10) {
                String email = sc.next();
                String pass = sc.next();
                String firstName = sc.next();
                String lastName = sc.next();
                sc.nextLine();

                User user = helper.createRegisteredUser(email, pass, firstName, lastName, new UserRole[]{UserRole.USER});
                pwMap.put(user, pass);
                users.add(user);
                amount++;
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("Kan test bestand niet vinden");
        }

    }

    // TODO: doesn't work yet, problem with time-input (invalid format?)
    @Test
    public void createInfoSessionSucces() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                cookie = helper.login(admin, pwMap.get(admin));
                // We voegen eerst een sessie rechtsreeks toe aan de database om het laatste id te achterhalen
                User user = helper.createRegisteredUser("test123@yahoo.com", "1234piano", "Bart", "Smidt", new UserRole[]{UserRole.USER});
                user.setAddressResidence(new Address("Belgium", "9000", "Gent", "Plateaustraat", "30", ""));
                userDAO.updateUser(user, true);

                // geplande sessies opvragen
                Result result1 = callAction(
                        controllers.routes.ref.InfoSessions.showUpcomingSessions(),
                        fakeRequest().withCookies(cookie)
                );
                assertEquals("Requesting upcoming sessions", OK, status(result1));

                // pagina om nieuwe sessie aan te maken
                Result result2 = callAction(
                        controllers.routes.ref.InfoSessions.newSession(),
                        fakeRequest().withCookies(cookie)
                );
                assertEquals("Requesting new session page", OK, status(result2));

                // infosessie aanmaken
                Map<String, String> data = new HashMap<>();
                data.put("address.street", "Straat");
                data.put("address.number", "1");
                data.put("address.city", "Gent");
                data.put("address.zipCode", "9000");
                data.put("max_enrollees", "50");
                //data.put("time", DateTime.now().plusDays(4).toString());
                data.put("time", "2015-01-01 00:05");
                data.put("type", InfoSessionType.NORMAL.getDescription());
                data.put("userId", "" + user.getId());

                Result result3 = callAction(
                        controllers.routes.ref.InfoSessions.createNewSession(),
                        fakeRequest(POST, "/infosession/new").withCookies(cookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Creating new session", OK, status(result3));
            }
        });
    }

    // TODO: doesn't work yet (same problem as above: time invalid)
    @Test
    public void editInfoSessionTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                cookie = helper.login(admin, pwMap.get(admin));
                User user = helper.createRegisteredUser("test123@yahoo.com", "1234piano", "Bart", "Smidt", new UserRole[]{UserRole.USER});
                user.setAddressResidence(new Address("Belgium", "9000", "Gent", "Plateaustraat", "30", ""));
                InfoSession session = helper.createInfoSession(InfoSessionType.NORMAL, user, user.getAddressResidence(), new DateTime(), 100);

                Map<String, String> data = new HashMap<>();
                data.put("address.street", "Straat");
                data.put("address.number", "1");
                data.put("address.city", "Gent");
                data.put("address.zipCode", "9000");
                data.put("max_enrollees", "10");
                data.put("time", session.getTime().plusDays(1).toString());
                data.put("type", InfoSessionType.NORMAL.toString());
                data.put("userId", "" + user.getId());

                // infosession editten
                Result result6 = callAction(
                        controllers.routes.ref.InfoSessions.editSessionPost(session.getId()),
                        fakeRequest(POST, "infosession/edit").withFormUrlEncodedBody(data).withCookies(cookie)
                );
                assertEquals("Editting infosession", OK, status(result6));
            }
        });
    }

    @Test
    public void enrollSessionTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                User host = helper.createRegisteredUser("test123@yahoo.be", "1234piano", "Alice", "Parker", new UserRole[]{UserRole.USER});
                host.setAddressResidence(helper.createAddress("Belgium", "9000", "Gent", "Plateaustraat", "30", ""));
                userDAO.updateUser(host, true);
                InfoSession session = helper.createInfoSession(InfoSessionType.NORMAL, host, host.getAddressResidence(), new DateTime(), 100);


                for (User user : users) {
                    cookie = helper.login(user, pwMap.get(user));

                    // bekijk geplande sessies
                    Result result = callAction(
                            controllers.routes.ref.InfoSessions.showUpcomingSessions(),
                            fakeRequest().withCookies(cookie)
                    );
                    assertEquals("Show upcomming sessions", OK, status(result));

                    // bekijk sessie details
                    Result result1 = callAction(
                            controllers.routes.ref.InfoSessions.detail(session.getId()),
                            fakeRequest().withCookies(cookie)
                    );
                    assertEquals("Show session details", OK, status(result1));

                    // inschrijven
                    Result result2 = callAction(
                            controllers.routes.ref.InfoSessions.enrollSession(session.getId()),
                            fakeRequest().withCookies(cookie)
                    );
                    assertEquals("Enroll user for session", 303, status(result2));

                    helper.logout();
                }

                cookie = helper.login(admin, pwMap.get(admin));
                for (User user : users) {
                    Result result = callAction(
                            controllers.routes.ref.InfoSessions.removeUserFromSession(session.getId(), user.getId()),
                            fakeRequest().withCookies(cookie)
                    );
                    assertEquals("Admin remove user", 303, status(result));
                }

                for (User user : users) {
                    cookie = helper.login(user, pwMap.get(user));

                    // opnieuw inschrijven
                    Result result2 = callAction(
                            controllers.routes.ref.InfoSessions.enrollSession(session.getId()),
                            fakeRequest().withCookies(cookie)
                    );
                    assertEquals("Enroll user for session", 303, status(result2));

                    helper.logout();
                }

                for (User user : users) {
                    cookie = helper.login(user, pwMap.get(user));

                    // uitschrijven
                    Result result2 = callAction(
                            controllers.routes.ref.InfoSessions.unenrollSession(),
                            fakeRequest(POST, "/login").withCookies(cookie)
                    );
                    assertEquals("Unenroll user for session", 303, status(result2));

                    helper.logout();
                }
            }
        });
    }

    /*
     * Alle users in deze test hebben geen rechten om aanpassingen aan infosessies te doen
     */
    @Test
    public void unAuthorizedUserTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                User host = helper.createRegisteredUser("test123@gmail.com", "1234piano", "Bart", "Peeters", new UserRole[]{UserRole.USER});
                host.setAddressResidence(new Address("Belgium", "9000", "Gent", "Plateaustraat", "30", ""));
                InfoSession session = helper.createInfoSession(InfoSessionType.NORMAL, host, host.getAddressResidence(), new DateTime(), 100);

                Iterator<User> it = users.listIterator(0);
                for (UserRole role : UserRole.values()) {
                    if (!role.equals(UserRole.INFOSESSION_ADMIN) && !role.equals(UserRole.SUPER_USER)) {
                        User user = it.next();
                        userRoleDAO.addUserRole(user.getId(), role);
                        userDAO.updateUser(user, true);

                        cookie = helper.login(user, pwMap.get(user));
                        // edit pagina opvragen, moet falen want niet authorized
                        Result result1 = callAction(
                                controllers.routes.ref.InfoSessions.editSession(session.getId()),
                                fakeRequest(POST, "/login").withCookies(cookie)
                        );
                        assertEquals("Unauthorized user accessing edit page", UNAUTHORIZED, status(result1));

                        Map<String, String> data = new HashMap<>();
                        data.put("address_zip", "9000");
                        data.put("address_city", "Gent");
                        data.put("address_street", "Stalhof");
                        data.put("address_number", "6");
                        // ook edit POST mag niet
                        Result result2 = callAction(
                                controllers.routes.ref.InfoSessions.editSessionPost(session.getId()),
                                fakeRequest(POST, "infosession/edit").withFormUrlEncodedBody(data).withCookies(cookie)
                        );
                        assertEquals("Unauthorized user editting session", UNAUTHORIZED, status(result2));


                        Map<String, String> data2 = new HashMap<>();
                        data2.put("time", "2008-03-09 16:05:07");
                        data2.put("addresstype", "host");
                        // nieuwe sessie aanmaken mag niet
                        Result result3 = callAction(
                                controllers.routes.ref.InfoSessions.createNewSession(),
                                fakeRequest(POST, "infosession/new").withFormUrlEncodedBody(data2).withCookies(cookie)
                        );
                        assertEquals("Unauthorized user creating new session", UNAUTHORIZED, status(result3));

                        // Andere users verwijderen mag ook niet
                        Result result4 = callAction(
                                controllers.routes.ref.InfoSessions.removeUserFromSession(session.getId(), users.get(1).getId()),
                                fakeRequest().withCookies(cookie)
                        );
                        assertEquals("Unauthorized user removing enrollees", UNAUTHORIZED, status(result4));

                        // Status van andere users mogen ook niet aangepast worden
                        Result result5 = callAction(
                                controllers.routes.ref.InfoSessions.setUserSessionStatus(session.getId(), users.get(2).getId(), EnrollementStatus.PRESENT.toString()),
                                fakeRequest(POST, "infosession/new").withFormUrlEncodedBody(data2).withCookies(cookie)
                        );
                        assertEquals("Unauthorized user changing user enrollement status", UNAUTHORIZED, status(result5));

                        userRoleDAO.removeUserRole(user.getId(), role);
                        helper.logout();

                    }
                }
            }
        });
    }
}