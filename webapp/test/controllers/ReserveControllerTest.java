/* ReserveControllerTest.java
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

import controllers.util.TestHelper;
import be.ugent.degage.db.DataAccessContext;
import providers.DataProvider;
import be.ugent.degage.db.models.*;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Cookie;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.*;
import static play.test.Helpers.status;

/**
 * Created by HannesM on 4/04/14.
 */
public class ReserveControllerTest {

    private User user;
    private TestHelper helper;
    private Cookie loginCookie;

    @Before
    public void setUp(){
        helper = new TestHelper();
        helper.setTestProvider();
        user = helper.createRegisteredUser("test@test.com", "1234piano", "Pol", "Thijs",new UserRole[]{UserRole.CAR_USER});
    }

    /**
     * Tests method Reserve.reserve()
     * Once with an existing car, and once with a non-existing car
     */
    @Test
    public void testReserve(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Test if we can see a reservation-screen of a car
                // First create a car
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                Result result2 = callAction(
                        controllers.routes.ref.Reserve.reserve(car.getId(), "1993-09-26 03:00", "1993-09-26 04:00"),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Requesting reserve", OK, status(result2));

                // Test if we can see a reservation-screen of a car that doesn't exist
                Result result3 = callAction(
                        controllers.routes.ref.Reserve.reserve(car.getId()-1, "1993-09-26 03:00", "1993-09-26 04:00"),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Requesting reserve for unexisting car", BAD_REQUEST, status(result3));
                helper.logout();
            }
        });
    }

     /**
     * Tests method Reserve.confirmReservation()
     * With existing car, overlapping dates, from > until, date that doesnt exist, date in the past, nonexisting car
     */
    @Test
    public void testConfirmReservation() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Test if we can make a reservation of a car
                // First create a car
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                Map<String,String> reserveData = new HashMap<>();
                reserveData.put("from", "2015-01-01 00:00");
                reserveData.put("until", "2015-01-02 00:00");
                reserveData.put("message", "Dankuwel");
                Result result2 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation", 303, status(result2));

                // Try to create a reservation that overlaps with the reservation just created
                reserveData.put("from", "2015-01-01 00:05");
                reserveData.put("until", "2015-01-02 00:05");
                Result result3 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation with overlapping dates from other reservation", BAD_REQUEST, status(result3));

                // Test if we can make a reservation where from > until
                reserveData.put("from", "2016-01-02 00:05");
                reserveData.put("until", "2016-01-01 00:05");
                Result result4 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation where from > until", BAD_REQUEST, status(result4));

                // Test if we can make a reservation on a date that doesn't exist (feb 30th)
                reserveData.put("from", "2014-02-30 00:00");
                reserveData.put("until", "2014-03-30 00:00");
                Result result5 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation on nonexisting date", BAD_REQUEST, status(result5));

                // Test if we can make a reservation on a date that is in the past
                reserveData.put("from", "1999-02-01 00:00");
                reserveData.put("until", "1999-02-02 00:00");
                Result result6 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation on old date", 303, status(result6));

                // Test if we can make a reservation of a car that doesn't exist
                reserveData.put("from", "2015-01-01 00:00");
                reserveData.put("until", "2015-01-02 00:00");
                Result result7 = callAction(
                        controllers.routes.ref.Reserve.confirmReservation(car.getId() - 1),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Creating reservation for nonexisting car", BAD_REQUEST, status(result7));

                helper.logout();
            }
        });
    }


    /**
     * Tests if we can only use the Reserve-methods as CAR_USER (or SUPER_USER)
     * Also tests index() and showCarsPage() (no need to test them more than default, because it is dependent on mocks)
     */
    @Test
    public void authorizeTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                // Create new user without roles
                User user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Pol2", "Thijs");
                loginCookie = helper.login(user2,"1234piano");

                UserRole[] userRoles = {UserRole.CAR_USER, UserRole.INFOSESSION_ADMIN, UserRole.MAIL_ADMIN, UserRole.PROFILE_ADMIN, UserRole.RESERVATION_ADMIN, UserRole.SUPER_USER, UserRole.CAR_OWNER};

                for(int i = 0; i < userRoles.length; i++) {
                    if(i != 0) {
                        helper.removeUserRole(user2, userRoles[i - 1]);
                    }
                    helper.addUserRole(user2, userRoles[i]);

                    // Now let's try to see the pages

                    // index()
                    Result result = callAction(
                            controllers.routes.ref.Reserve.index(),
                            fakeRequest().withCookies(loginCookie)
                    );
                    if(userRoles[i] == UserRole.CAR_USER || userRoles[i] == UserRole.SUPER_USER)
                        assertEquals("Requesting reserve as " + userRoles[i].toString(), OK, status(result));
                    else
                        assertEquals("Requesting reserve as " + userRoles[i].toString(), UNAUTHORIZED, status(result));

                    // showCarsPage()
                    Result resultShowCarsPage = callAction(
                            controllers.routes.ref.Reserve.showCarsPage(1, 1, "", ""),
                            fakeRequest().withCookies(loginCookie)
                    );
                    if(userRoles[i] == UserRole.CAR_USER || userRoles[i] == UserRole.SUPER_USER)
                        assertEquals("Requesting reserve as " + userRoles[i].toString(), OK, status(resultShowCarsPage));
                    else
                        assertEquals("Requesting reserve as " + userRoles[i].toString(), UNAUTHORIZED, status(resultShowCarsPage));

                    // getCarModal()
                    Car car = helper.createCar("MijnenAuto" + i, "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");

                    // reserve()
                    Result result1 = callAction(
                            controllers.routes.ref.Reserve.reserve(car.getId(), "1993-09-26 03:00", "1993-09-26 04:00"),
                            fakeRequest().withCookies(loginCookie)
                    );
                    if(userRoles[i] == UserRole.CAR_USER || userRoles[i] == UserRole.SUPER_USER)
                        assertEquals("Requesting reserve as " + userRoles[i].toString(), OK, status(result1));
                    else
                        assertEquals("Requesting reserve as " + userRoles[i].toString(), UNAUTHORIZED, status(result1));

                    // confirmreservation()
                    Map<String,String> reserveData = new HashMap<>();
                    reserveData.put("from", "2015-01-01 00:00");
                    reserveData.put("until", "2015-01-02 00:00");
                    Result result2 = callAction(
                            controllers.routes.ref.Reserve.confirmReservation(car.getId()),
                            fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                    );
                    if(userRoles[i] == UserRole.CAR_USER || userRoles[i] == UserRole.SUPER_USER)
                        assertEquals("Creating reservation as " + userRoles[i].toString(), 303, status(result2));
                    else
                        assertEquals("Creating reservation as " + userRoles[i].toString(), UNAUTHORIZED, status(result2));

                    helper.logout();
                }
            }
        });
    }
}
