/* TestHelper.java
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

package controllers.util;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.POST;
import static play.test.Helpers.callAction;
import static play.test.Helpers.cookie;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.ugent.degage.db.*;
import be.ugent.degage.db.dao.AddressDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.dao.UserRoleDAO;
import be.ugent.degage.db.jdbc.JDBCDataAccess;
import be.ugent.degage.db.models.*;
import db.DataAccess;
import org.mindrot.jbcrypt.BCrypt;

import play.mvc.Http.Cookie;
import play.mvc.Result;
import providers.DataProvider;

public class TestHelper {
	
	private UserDAO userDAO;
	private UserRoleDAO userRoleDAO;
	private AddressDAO addressDAO;
	private DataAccessProvider provider;
	
	public TestHelper(){
		provider = JDBCDataAccess.getTestDataAccessProvider();
		DataAccessContext context = provider.getDataAccessContext();
		userDAO = context.getUserDAO();
		userRoleDAO = context.getUserRoleDAO();
		addressDAO = context.getAddressDAO();
	}
	
	public void setTestProvider(){
		if(provider==null){
			provider = new TestDataAccessProvider();
		}
		DataProvider.setDataAccessProvider(provider);
	}
	
	private static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public User createRegisteredUser(String email, String password, String firstName, String lastName) {
        return createRegisteredUser(email, password, firstName, lastName, new UserRole[] {});
    }
	public User createRegisteredUser(String email, String password, String firstName, String lastName, UserRole[] roles){
        User user = userDAO.createUser(email, hashPassword(password), firstName, lastName, UserStatus.FULL)
        for(UserRole role : roles){
        	userRoleDAO.addUserRole(user.getId(), role);
        }        
        userDAO.updateUser(user, true);
        return userDAO.getUser(user.getId(), false);
    }

    public  void addUserRole(User user, UserRole role) {
        UserRoleDAO dao = DataAccess.getContext().getUserRoleDAO();
        dao.addUserRole(user.getId(), role);
        DataProvider.getUserRoleProvider().invalidateRoles(user);
    }

    public  void removeUserRole(User user, UserRole role) {
        UserRoleDAO dao = DataAccess.getContext().getUserRoleDAO();
        dao.removeUserRole(user.getId(), role);
        DataProvider.getUserRoleProvider().invalidateRoles(user);
    }

    public Car createCar(String name, String brand, String type,
                                Address location, int seats, int doors, int year, boolean gps,
                                boolean hook, CarFuel fuel, int fuelEconomy, int estimatedValue,
                                int ownerAnnualKm, User owner, String comments) {
        CarDAO dao = DataAccess.getContext().getCarDAO();
        Car car = dao.createCar(name, brand, type, location, seats, doors, year, false, gps, hook, fuel, fuelEconomy, estimatedValue, ownerAnnualKm, null, null, owner, comments, true);
        return car;
    }

    public Reservation createReservation(DateTime from, DateTime to, Car car, User user) {
        ReservationDAO dao = DataAccess.getContext().getReservationDAO();
        Reservation reservation = dao.createReservation(from, to, car, user, "");
        return reservation;
    }

    public void updateReservation(Reservation r) {
        ReservationDAO dao = DataAccess.getContext().getReservationDAO();
        dao.updateReservation(r);
    }
	
	public InfoSession createInfoSession(InfoSessionType type, User host, Address address, DateTime time, int max){
		InfoSessionDAO dao = DataAccess.getContext().getInfoSessionDAO();
		InfoSession session = dao.createInfoSession(type, "", host, address, time, max, "");
		return session;
	}
	
	public Address createAddress(String country, String zip, String city, String street, String number, String bus){
		return addressDAO.createAddress(country, zip, city, street, number, bus);
	}
	
	public Cookie login(User user, String password){
		Map<String,String> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("password", password);

        // inloggen
        Result result = callAction(
                controllers.routes.ref.Login.authenticate("/"),
                fakeRequest(POST, "/login").withFormUrlEncodedBody(data)
        );
        assertEquals("Valid login", 303, status(result));
        
        return cookie("PLAY_SESSION", result);
	}
	
	public void logout(){
		// uitloggen
		Result result3 = callAction(
                controllers.routes.ref.Login.logout(),
                fakeRequest()
        );
        assertEquals("Valid logout", 303, status(result3));
	}

}
