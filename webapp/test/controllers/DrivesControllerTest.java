/* DrivesControllerTest.java
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
import be.ugent.degage.db.models.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.test.Helpers.status;

/**
 * Created by HannesM on 5/04/14.
 */
public class DrivesControllerTest {
    private User user;
    private TestHelper helper;
    private Http.Cookie loginCookie;

    // TODO: reservationsWithStatus, provideDriveInfo, approveDriveInfo

    @Before
    public void setUp(){
        helper = new TestHelper();
        helper.setTestProvider();
        user = helper.createRegisteredUser("test@test.com", "1234piano", "Pol", "Thijs",new UserRole[]{UserRole.CAR_USER, UserRole.CAR_OWNER});
    }

    /**
     * Tests Drives.index()
     */
    @Test
    public void testIndex() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car
                // First create a reservation to see
                Address address = helper.createAddress("Belgium", "9000", "Gent", "Straat", "1", "");
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.index(),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Index of drives", OK, status(result2));

                helper.logout();
            }
        });
    }

    /**
     * Tests Drives.showDrivesPage()
     */
    @Test
    public void testShowDrivesPage() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car
                // First create a reservation, so there's something to show
                Address address = helper.createAddress("Belgium", "9000", "Gent", "Straat", "1", "");
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.showDrivesPage(1, 1, "", ""),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("ShowDrivesPage first page", OK, status(result2));

                helper.logout();
            }
        });
    }

    /**
     * Tests Drives.drivesAdmin() as authorized and as unauthorized
     */
    @Test
    public void testDrivesAdmin() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car
                // First create a reservation to see
                Address address = helper.createAddress("Belgium", "9000", "Gent", "Straat", "1", "");
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.drivesAdmin(),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Index of drives adminpage without adminrights", UNAUTHORIZED, status(result2));

                helper.addUserRole(user, UserRole.RESERVATION_ADMIN);
                loginCookie = helper.login(user,"1234piano");

                Result result3 = callAction(
                        controllers.routes.ref.Drives.drivesAdmin(),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Index of drives adminpage with adminrights", OK, status(result3));

                helper.removeUserRole(user, UserRole.RESERVATION_ADMIN);
                helper.logout();
            }
        });
    }

    /**
     * Tests method Drives.details()
     * Once with an existing reservation, of own car that I reserved/didn't reserve, of other car that I reserved/didn't reserve, and once with a non-existing reservation
     */
    @Test
    public void testDetails(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car
                // First create a reservation
                Address address = helper.createAddress("Belgium", "9000", "Gent", "Straat", "1", "");
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.details(reservation.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Details of reservation of own car", OK, status(result2));

                // Reservation of other car that I reserved
                User user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Niet", "Ik");
                Car car2 = helper.createCar("NietMijnenAuto", "Opel", "Corsa", null, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user2, "");
                Reservation reservation2 = helper.createReservation(from, to, car2, user);

                Result result4 = callAction(
                        controllers.routes.ref.Drives.details(reservation2.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Details of reservation of other car", OK, status(result4));

                // Reservation of other car that I didn't reserve
                Reservation reservation3 = helper.createReservation(from, to, car2, user2);

                Result result5 = callAction(
                        controllers.routes.ref.Drives.details(reservation3.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Details of reservation of other car that I didn't reserve", BAD_REQUEST, status(result5));

                // Reservation of my car that other user reserved
                Reservation reservation4 = helper.createReservation(from, to, car, user2);

                Result result6 = callAction(
                        controllers.routes.ref.Drives.details(reservation4.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Details of reservation of my car, reserved by other user", OK, status(result6));

                // Reservation that doesn't exist
                Result result3 = callAction(
                        controllers.routes.ref.Drives.details(reservation.getId() - 1),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Details of unexisting reservation", BAD_REQUEST, status(result3));

                helper.logout();
            }
        });
    }

    /**
     * Tests Drives.adjustDetails()
     */
    @Test
    public void testAdjustDetails() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car, shorter until
                // First create a reservation
                Address address = helper.createAddress("Belgium", "9000", "Gent", "Straat", "1", "");
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Map<String,String> reserveData = new HashMap<>();
                reserveData.put("from", "2015-01-01 00:00");
                reserveData.put("until", "2015-01-01 23:59");
                Result result2 = callAction(
                        controllers.routes.ref.Drives.adjustDetails(reservation.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details to shorter until of reservation of own car", OK, status(result2));

                // shorter from and until
                reserveData.put("from", "2015-01-01 00:01");
                reserveData.put("until", "2015-01-01 23:59");
                Result result3 = callAction(
                        controllers.routes.ref.Drives.adjustDetails(reservation.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details of reservation to shorter from and until of own car", OK, status(result3));

                //longer until
                reserveData.put("from", "2015-01-01 00:00");
                reserveData.put("until", "2015-02-01 23:59");
                Result result4 = callAction(
                        controllers.routes.ref.Drives.adjustDetails(reservation.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details to longer until of reservation of own car", BAD_REQUEST, status(result4));

                // longer from
                reserveData.put("from", "2014-12-01 00:00");
                reserveData.put("until", "2015-02-01 23:59");
                Result result5 = callAction(
                        controllers.routes.ref.Drives.adjustDetails(reservation.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details to longer from of reservation of own car", BAD_REQUEST, status(result5));

                // reservation already refused
                reservation.setStatus(ReservationStatus.REFUSED);
                reserveData.put("from", "2014-12-01 00:00");
                reserveData.put("until", "2015-01-01 23:59");
                Result resul = callAction(
                        controllers.routes.ref.Drives.adjustDetails(reservation.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details to shorter from of reservation of own car, that is already refused", BAD_REQUEST, status(resul));

                // reservation already cancelled
                reservation.setStatus(ReservationStatus.CANCELLED);
                reserveData.put("from", "2014-12-01 00:00");
                reserveData.put("until", "2015-01-01 23:59");
                Result resul2 = callAction(
                        controllers.routes.ref.Drives.adjustDetails(reservation.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details to shorter from of reservation of own car, that is already cancelled", BAD_REQUEST, status(resul2));

                // reservation already refused
                reservation.setStatus(ReservationStatus.FINISHED);
                reserveData.put("from", "2014-12-01 00:00");
                reserveData.put("until", "2015-01-01 23:59");
                Result resul3 = callAction(
                        controllers.routes.ref.Drives.adjustDetails(reservation.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details to shorter from of reservation of own car, that is already finished", BAD_REQUEST, status(resul3));

                // Reservation of other car that I reserved
                User user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Niet", "Ik");
                Car car2 = helper.createCar("NietMijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user2, "");
                Reservation reservation2 = helper.createReservation(from, to, car2, user);
                reserveData.put("from", "2015-01-01 00:00");
                reserveData.put("until", "2015-01-01 23:59");
                Result result6 = callAction(
                        controllers.routes.ref.Drives.adjustDetails(reservation2.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details to shorter until of reservation of other car that I reserved", OK, status(result6));

                // Reservation of other car that I didn't reserve
                Reservation reservation3 = helper.createReservation(from, to, car2, user2);
                reserveData.put("from", "2015-01-01 00:00");
                reserveData.put("until", "2015-01-01 23:58");
                Result result7 = callAction(
                        controllers.routes.ref.Drives.adjustDetails(reservation3.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details to shorter until of reservation of other car that I didn't reserve", BAD_REQUEST, status(result7));

                // Reservation of my car that other user reserved
                Reservation reservation4 = helper.createReservation(from, to, car, user2);
                reserveData.put("from", "2015-01-01 00:00");
                reserveData.put("until", "2015-01-01 23:59");
                Result result8 = callAction(
                        controllers.routes.ref.Drives.adjustDetails(reservation4.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details to shorter until of reservation of own car that other user reserved", BAD_REQUEST, status(result8));

                // Reservation that doesn't exist
                Result result9 = callAction(
                        controllers.routes.ref.Drives.adjustDetails(-1),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(reserveData)
                );
                assertEquals("Adjust details to shorter until of reservation that doesn't exist", BAD_REQUEST, status(result9));


                helper.logout();
            }
        });
    }

    // TODO: doesn't work anymore...
    /**
     * Tests method Drives.approveReservation()
     * Once with an existing reservation of own car, of other car, of other car by other user, already cancelled, accepted, refused
     * Once with a non-existing reservation
     */
    @Test
    public void testApproveReservation(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                Map<String,String> data = new HashMap<>();
                data.put("status", ReservationStatus.ACCEPTED.toString());
                data.put("remark", "Hahaha!");

                // Reservation of own car
                // First create a reservation
                Address address = helper.createAddress("Belgium", "9000", "Gent", "Straat", "1", "");
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation.getId()),
                        fakeRequest().withFormUrlEncodedBody(data).withCookies(loginCookie)
                );
                assertEquals("Approving reservation of own car", OK, status(result2));

                // Reservation of other car that I reserved
                User user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Niet", "Ik");
                Car car2 = helper.createCar("NietMijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user2, "");
                Reservation reservation2 = helper.createReservation(from, to, car2, user);

                Result result4 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation2.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of other car", BAD_REQUEST, status(result4));

                // Reservation of other car that I didn't reserve
                Reservation reservation3 = helper.createReservation(from, to, car2, user2);

                Result result5 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation3.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of other car that I didn't reserve", BAD_REQUEST, status(result5));

                // Reservation of my car that other user reserved
                Reservation reservation4 = helper.createReservation(from, to, car, user2);

                Result result6 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation4.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user", OK, status(result6));

                // Reservation of my car that other user reserved AND CANCELLED
                Reservation reservation5 = helper.createReservation(from, to, car, user2);
                reservation5.setStatus(ReservationStatus.CANCELLED);
                helper.updateReservation(reservation5);

                Result result7 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation5.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already cancelled", BAD_REQUEST, status(result7));

                // Reservation of my car that other user reserved AND ALREADY REFUSED
                Reservation reservation6 = helper.createReservation(from, to, car, user2);
                reservation6.setStatus(ReservationStatus.REFUSED);
                helper.updateReservation(reservation6);

                Result result8 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation6.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already refused", BAD_REQUEST, status(result8));

                // Reservation of my car that other user reserved AND ALREADY ACCEPTED
                Reservation reservation7 = helper.createReservation(from, to, car, user2);
                reservation7.setStatus(ReservationStatus.ACCEPTED);
                helper.updateReservation(reservation7);

                Result result9 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation7.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already accepted", BAD_REQUEST, status(result9));


                // Reservation that doesn't exist
                Result result3 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation.getId() - 1),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving unexisting reservation", BAD_REQUEST, status(result3));

                helper.logout();
            }
        });
    }

    // TODO: doesn't work anymore...
    /**
     * Tests method Drives.refuseReservation()
     * Once with an existing reservation of own car, of other car, of other car by other user, once with empty reason, already cancelled, accepted, refused
     * Once with a non-existing reservation
     */
    @Test
    public void testRefuseReservation(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // First create a reservation
                Address address = helper.createAddress("Belgium", "9000", "Gent", "Straat", "1", "");
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Map<String,String> data = new HashMap<>();
                data.put("status", ReservationStatus.REFUSED.toString());
                data.put("remarks", "Test reden");
                Result result1 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Refusing reservation of own car", OK, status(result1));

                // With empty reason
                data.put("remarks", "");
                Result result2 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Refusing reservation without remarks", BAD_REQUEST, status(result2));

                data.put("remarks", "Test reden");
                // Reservation of other car that I reserved
                User user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Niet", "Ik");
                Car car2 = helper.createCar("NietMijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user2, "");
                Reservation reservation2 = helper.createReservation(from, to, car2, user);

                Result result4 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation2.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Refusing reservation of other car", BAD_REQUEST, status(result4));

                // Reservation of other car that I didn't reserve
                Reservation reservation3 = helper.createReservation(from, to, car2, user2);

                Result result5 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation3.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Refusing reservation of other car that I didn't reserve", BAD_REQUEST, status(result5));

                // Reservation of my car that other user reserved
                Reservation reservation4 = helper.createReservation(from, to, car, user2);

                Result result6 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation4.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Refusing reservation of my car, reserved by other user", OK, status(result6));

                // Reservation of my car that other user reserved AND CANCELLED
                Reservation reservation5 = helper.createReservation(from, to, car, user2);
                reservation5.setStatus(ReservationStatus.CANCELLED);
                helper.updateReservation(reservation5);

                Result result7 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation5.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already cancelled", BAD_REQUEST, status(result7));

                // Reservation of my car that other user reserved AND ALREADY REFUSED
                Reservation reservation6 = helper.createReservation(from, to, car, user2);
                reservation6.setStatus(ReservationStatus.REFUSED);
                helper.updateReservation(reservation6);

                Result result8 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation6.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already refused", BAD_REQUEST, status(result8));

                // Reservation of my car that other user reserved AND ALREADY ACCEPTED
                Reservation reservation7 = helper.createReservation(from, to, car, user2);
                reservation7.setStatus(ReservationStatus.ACCEPTED);
                helper.updateReservation(reservation7);

                Result result9 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation7.getId()),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Approving reservation of my car, reserved by other user but already accepted", BAD_REQUEST, status(result9));

                // Reservation that doesn't exist
                Result result3 = callAction(
                        controllers.routes.ref.Drives.setReservationStatus(reservation.getId() - 1),
                        fakeRequest().withCookies(loginCookie).withFormUrlEncodedBody(data)
                );
                assertEquals("Refusing unexisting reservation", BAD_REQUEST, status(result3));

                helper.logout();
            }
        });
    }

    /**
     * Tests method Drives.refuseReservation()
     * Once with an existing reservation of own car, of other car, of other car by other user,, already cancelled, accepted, refused
     * Once with a non-existing reservation
     */
    @Test
    public void testCancelReservation(){
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                helper.setTestProvider();
                loginCookie = helper.login(user,"1234piano");

                // Reservation of own car
                // First create a reservation
                Address address = helper.createAddress("Belgium", "9000", "Gent", "Straat", "1", "");
                Car car = helper.createCar("MijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user, "");
                DateTime from = new DateTime(2015, 1, 1, 0, 0);
                DateTime to = new DateTime(2015, 1, 2, 0, 0);
                Reservation reservation = helper.createReservation(from, to, car, user);

                Result result2 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of own car", OK, status(result2));

                // Reservation of other car that I reserved
                User user2 = helper.createRegisteredUser("test2@test.com", "1234piano", "Niet", "Ik");
                Car car2 = helper.createCar("NietMijnenAuto", "Opel", "Corsa", address, 5, 3, 2005, false, false, CarFuel.GAS, 1, 1, 1, user2, "");
                Reservation reservation2 = helper.createReservation(from, to, car2, user);

                Result result4 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation2.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of other car", OK, status(result4));

                // Reservation of other car that I didn't reserve
                Reservation reservation3 = helper.createReservation(from, to, car2, user2);

                Result result5 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation3.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of other car that I didn't reserve", BAD_REQUEST, status(result5));

                // Reservation of my car that other user reserved
                Reservation reservation4 = helper.createReservation(from, to, car, user2);

                Result result6 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation4.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of my car, reserved by other user", BAD_REQUEST, status(result6));

                // Reservation of other car that I reserved AND CANCELLED
                Reservation reservation5 = helper.createReservation(from, to, car2, user);
                reservation5.setStatus(ReservationStatus.CANCELLED);
                helper.updateReservation(reservation5);

                Result result7 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation5.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of my car, reserved by other user but already cancelled", BAD_REQUEST, status(result7));

                // Reservation of other car that I reserved AND ALREADY REFUSED
                Reservation reservation6 = helper.createReservation(from, to, car2, user);
                reservation6.setStatus(ReservationStatus.REFUSED);
                helper.updateReservation(reservation6);

                Result result8 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation6.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of my car, reserved by other user but already refused", BAD_REQUEST, status(result8));

                // Reservation of other car that I reserved AND ALREADY ACCEPTED
                Reservation reservation7 = helper.createReservation(from, to, car2, user);
                reservation7.setStatus(ReservationStatus.ACCEPTED);
                helper.updateReservation(reservation7);

                Result result9 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation7.getId()),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling reservation of my car, reserved by other user but already accepted", OK, status(result9));

                // Reservation that doesn't exist
                Result result3 = callAction(
                        controllers.routes.ref.Drives.cancelReservation(reservation.getId() - 1),
                        fakeRequest().withCookies(loginCookie)
                );
                assertEquals("Cancelling unexisting reservation", BAD_REQUEST, status(result3));

                helper.logout();
            }
        });
    }
}
