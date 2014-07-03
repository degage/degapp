package controllers;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.POST;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.running;
import static play.test.Helpers.status;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import controllers.util.TestHelper;
import be.ugent.degage.db.models.Address;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.CarFuel;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserGender;
import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.models.UserStatus;

import org.junit.Before;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;

import play.mvc.Http.Cookie;
import play.mvc.Result;
import be.ugent.degage.db.AddressDAO;
import be.ugent.degage.db.CarDAO;
import be.ugent.degage.db.DataAccessContext;
import providers.DataProvider;
import be.ugent.degage.db.UserDAO;
import be.ugent.degage.db.UserRoleDAO;
import be.ugent.degage.db.mocking.TestDataAccessProvider;

public class CarsControllerTest {

    // TODO: CarCosts, CarPriviliges, CarAvailabilities

	private List<Car> cars, users;
	private CarDAO carDAO;
	private User sharer;
	private UserDAO userDAO;
	private UserRoleDAO userRoleDAO;
	private AddressDAO addressDAO;
	private Map<User,String> pwMap;
	private TestHelper helper;
	private Cookie cookie;

	@Before
	public void setUp(){
		helper = new TestHelper();
		cars = new ArrayList<>();
		users = new ArrayList<>();
		helper.setTestProvider();
		DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext();
		carDAO = context.getCarDAO();
		userDAO = context.getUserDAO();
		userRoleDAO = context.getUserRoleDAO();
		addressDAO = context.getAddressDAO();
		pwMap = new HashMap<User, String>();
		sharer = helper.createRegisteredUser("loaner@degage.be", "1234piano", "Jan", "Peeters", new UserRole[]{UserRole.CAR_OWNER});
		pwMap.put(sharer, "1234piano");
		Scanner sc, addrSc, userSc;
		try {
			sc = new Scanner(new File("test/database/random_cars.txt"));
			addrSc = new Scanner(new File("test/database/random_addresses.txt"));
			userSc = new Scanner(new File("test/database/random_users.txt"));
			sc.useDelimiter("\\t|\\r\\n");
			userSc.useDelimiter("\\t|\\r\\n");
			addrSc.useDelimiter("\\t|\\r\\n");
	        sc.nextLine(); //skip header first time  
	        addrSc.nextLine();
	        userSc.nextLine();
	        int i=0;
	        while(sc.hasNext() && addrSc.hasNext() && userSc.hasNext() && i < 10) {
	        	String name = "name" + i++;// TODO: also read from Scanner
	            String brand = sc.next();
	            String type = sc.next();
	            int seats = sc.nextInt();
	            int doors =sc.nextInt();
	            int year = sc.nextInt();
	            boolean gps = sc.nextBoolean();
	            boolean hook = sc.nextBoolean();
	            CarFuel fuel = CarFuel.valueOf(sc.next());
	            int economy = sc.nextInt();
	            int estimatedValue = sc.nextInt();
	            int annualKm = sc.nextInt();
	            sc.nextInt(); // Owner id not used
	            String comments = sc.next();
	            
	            
	            String street = addrSc.next();
	            String number = addrSc.next();
	            String zip = addrSc.next();
	            String city = addrSc.next();
	            
	            Address address = addressDAO.createAddress("Belgium", zip, city, street, number, "");
	            
	            String email = userSc.next();
	            String password = userSc.next();
	            String firstname = userSc.next();
	            String lastname = userSc.next();
	            userSc.nextLine();
	            
	            User user = helper.createRegisteredUser(email, password, firstname, lastname, new UserRole[]{UserRole.CAR_OWNER});
	            Car car = carDAO.createCar(name, brand, type, address, seats, doors, year, false, gps, hook, fuel, economy, estimatedValue, annualKm, null, null, user, comments, true);
	            pwMap.put(user, password);		 
	            cars.add(car);
	        }
	        sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("Kan test bestand niet vinden");
		} 
	}
	
	@Test
	public void formTest(){
		running(fakeApplication(),new Runnable() {
			
			@Override
			public void run() {
				helper.setTestProvider();
				User user = cars.get(0).getOwner();
				cookie = helper.login(user, pwMap.get(user));
				
				Map<String,String> data = new HashMap<>();
			    data.put("name","TestCar");
			    data.put("brand", "Audi");
			    data.put("type", "groot");
			    data.put("seats", "5");
			    data.put("year", "2012");
			    data.put("comments", "Dit is een test");
			    
			    data.put("doors", "1");
			    Result result1 = callAction(
		                controllers.routes.ref.Cars.addNewCar(),
		                fakeRequest(POST, "/cars/new").withFormUrlEncodedBody(data).withCookies(cookie)
			        );
			    assertEquals("No cars with less then 2 doors", BAD_REQUEST, status(result1));
			    
			    data.put("doors", "5");
			    data.put("seats", "1");
			    
			    Result result2 = callAction(
		                controllers.routes.ref.Cars.addNewCar(),
		                fakeRequest(POST, "/cars/new").withFormUrlEncodedBody(data).withCookies(cookie)
			        );
			    assertEquals("No cars with less then 2 seats", BAD_REQUEST, status(result2));
			    
			    data.put("seats", "5");
			    data.put("brand", "");
			    
			    Result result3 = callAction(
		                controllers.routes.ref.Cars.addNewCar(),
		                fakeRequest(POST, "/cars/new").withFormUrlEncodedBody(data).withCookies(cookie)
			        );
			    assertEquals("No car brand", BAD_REQUEST, status(result3));
			    
			    data.put("brand", "Audi");
			    data.put("name", "");
			    
			    Result result4 = callAction(
		                controllers.routes.ref.Cars.addNewCar(),
		                fakeRequest(POST, "/cars/new").withFormUrlEncodedBody(data).withCookies(cookie)
			        );
			    assertEquals("No car name", BAD_REQUEST, status(result4));
			}
		});
	}
	
	@Test
	public void unauthorizedUserTest(){
		running(fakeApplication(),new Runnable() {
			
			@Override
			public void run() {
				helper.setTestProvider();
				User user = helper.createRegisteredUser("test123@gmail.com", "1234piano", "Bart", "Peeters", new UserRole[]{UserRole. USER});
				user.setAddressResidence(new Address("Belgium","9000", "Gent", "Plateaustraat", "30", ""));
				cookie = helper.login(user, "1234piano");
				for(UserRole role : UserRole.values()){
					userRoleDAO.addUserRole(user.getId(), role);
					if(!role.equals(UserRole.RESERVATION_ADMIN) && !role.equals(UserRole.SUPER_USER)){
					    
					    if(!role.equals(UserRole.CAR_OWNER)){
							Result result1 = callAction(
					                controllers.routes.ref.Cars.newCar(),
					                fakeRequest().withCookies(cookie)
						        );
						    assertEquals("No rights to acces new car page", UNAUTHORIZED, status(result1));
						    
						    Result result2 = callAction(
					                controllers.routes.ref.Cars.addNewCar(),
					                fakeRequest().withCookies(cookie)
						        );
						    assertEquals("No rights to add new car", UNAUTHORIZED, status(result2));
						    
						    Result result3 = callAction(
					                controllers.routes.ref.Cars.editCar(cars.get(0).getId()),
					                fakeRequest().withCookies(cookie)
						        );
						    assertEquals("No rights to acces edit car page", UNAUTHORIZED, status(result3));
						    
						    Result result4 = callAction(
					                controllers.routes.ref.Cars.editCarPost(cars.get(0).getId()),
					                fakeRequest().withCookies(cookie)
						        );
						    assertEquals("No rights to edit car", UNAUTHORIZED, status(result4));
					        
						    if(!role.equals(UserRole.CAR_USER)){
						    	Result result5 = callAction(
						                controllers.routes.ref.Cars.showCars(),
						                fakeRequest().withCookies(cookie)
							        );
							    assertEquals("No rights to view cars", UNAUTHORIZED, status(result5));
							    
							    Result result6 = callAction(
						                controllers.routes.ref.Cars.showCarsPage(0, 0, "DESC","test"),
						                fakeRequest().withCookies(cookie)
							        );
							    assertEquals("No rights to filter cars", UNAUTHORIZED, status(result6));
							    
							    Result result7 = callAction(
						                controllers.routes.ref.Cars.detail(cars.get(0).getId()),
						                fakeRequest().withCookies(cookie)
							        );
							    assertEquals("No rights to view car details", UNAUTHORIZED, status(result7));
						    }
					    }
					
					}
					userRoleDAO.removeUserRole(user.getId(), role);
				}		
				helper.logout();
			}
		});
	}
	
	
	@Test
	public void addCarSucces(){
		running(fakeApplication(),new Runnable() {
			
			@Override
			public void run() {
				helper.setTestProvider();
				User user = cars.get(0).getOwner();
				cookie = helper.login(user, pwMap.get(user));
				
				// TOEVOEGEN //
				
				// Pagina voor nieuwe auto aan te maken
				Result result = callAction(
		                controllers.routes.ref.Cars.newCar(),
		                fakeRequest().withCookies(cookie)
			        );
			    assertEquals("New car page", OK, status(result));
			    
			    // Nieuwe auto toevoegen
			    Map<String,String> data = new HashMap<>();
			    data.put("name","TestCar");
                data.put("userId", "" + user.getId());
			    data.put("brand", "Audi");
			    data.put("type", "groot");
			    data.put("seats", "5");
			    data.put("doors", "5");
			    data.put("year", "2012");
			    data.put("comments", "Dit is een test");
                data.put("address.street", "Straat");
                data.put("address.number", "1");
                data.put("address.city", "Gent");
                data.put("address.zipCode", "9000");
			    Result result1 = callAction(
		                controllers.routes.ref.Cars.addNewCar(),
		                fakeRequest(POST, "/cars/new").withFormUrlEncodedBody(data).withCookies(cookie)
			        );
			    assertEquals("Add new car", 303, status(result1));
			    
			    helper.logout();
			}
		});
		
		
	}
	
	
	@Test
	public void editCar(){
		running(fakeApplication(),new Runnable() {
			
			@Override
			public void run() {
				helper.setTestProvider();
				User user = cars.get(0).getOwner();
				cookie = helper.login(user, pwMap.get(user));
				
			    // Editeer pagina opvragen
			    Result result2 = callAction(
		                controllers.routes.ref.Cars.editCar(cars.get(0).getId()), 
		                fakeRequest().withCookies(cookie)
			        );
			    
			    Map<String,String> data = new HashMap<>();
                data.put("name","TestCar");
                data.put("userId", "" + user.getId());
                data.put("brand", "Audi");
                data.put("type", "groot");
                data.put("seats", "5");
                data.put("doors", "4"); // 4 deuren ipv 5
                data.put("year", "2012");
                data.put("comments", "Dit is een test");
                data.put("address.street", "Straat");
                data.put("address.number", "1");
                data.put("address.city", "Gent");
                data.put("address.zipCode", "9000");
			    Result result2post = callAction(
		                controllers.routes.ref.Cars.editCarPost(cars.get(0).getId()), 
		                fakeRequest().withFormUrlEncodedBody(data).withCookies(cookie)
			        );
			    assertEquals("Edit car page", OK, status(result2));
			    assertEquals("Edit car page", 303, status(result2post));
			    
			    // Editeer wagen die niet bestaat
			    Result result3 = callAction(
		                controllers.routes.ref.Cars.editCar(9000),
		                fakeRequest().withCookies(cookie)
			        );
			    Result result4 = callAction(
		                controllers.routes.ref.Cars.editCarPost(9000),
		                fakeRequest().withFormUrlEncodedBody(data).withCookies(cookie)
			        );
			    assertEquals("Edit a not existing car", BAD_REQUEST, status(result3));
			    assertEquals("Edit a not existing car", BAD_REQUEST, status(result4));
			 			    
			    // Editeer wagen die niet van de aangemelde user is
			    Result result5 = callAction(
		                controllers.routes.ref.Cars.editCar(cars.get(1).getId()),
		                fakeRequest().withCookies(cookie)
			        );
			    Result result6 = callAction(
		                controllers.routes.ref.Cars.editCarPost(cars.get(1).getId()),
		                fakeRequest().withFormUrlEncodedBody(data).withCookies(cookie)
			        );
			    assertEquals("Edit a foreign car", BAD_REQUEST, status(result5));
			    assertEquals("Edit a foreign car", BAD_REQUEST, status(result6));

			    helper.logout();    	
			}
		});
		
		
	}

    // TODO: Does not work yet
	@Test
	public void carDetails(){
		running(fakeApplication(),new Runnable() {
			
			@Override
			public void run() {
				helper.setTestProvider();
				User user = cars.get(0).getOwner();
				cookie = helper.login(user, pwMap.get(user));

			    // details opvragen van auto (zowel eigen auto als auto van iemand anders)
			    Result result7 = callAction(
		                controllers.routes.ref.Cars.detail(cars.get(0).getId()),
		                fakeRequest().withCookies(cookie)
			        );
			    Result result8 = callAction(
		                controllers.routes.ref.Cars.detail(0),
		                fakeRequest().withCookies(cookie)
			        );
			    assertEquals("Car details", OK, status(result7));
			    assertEquals("Car details", OK, status(result8));
			    
			    // details opvragen van een auto die niet bestaat
			    Result result9 = callAction(
		                controllers.routes.ref.Cars.detail(9000),
		                fakeRequest().withCookies(cookie)
			        );
			    assertEquals("Car details of not existing car", BAD_REQUEST, status(result9));
			  
			    helper.logout();    	
			}
		});
		
		
	}
}
