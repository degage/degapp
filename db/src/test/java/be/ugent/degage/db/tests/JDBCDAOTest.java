package be.ugent.degage.db.tests;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.jdbc.JDBCDataAccess;
import be.ugent.degage.db.models.*;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

public class JDBCDAOTest {

    private DataAccessContext context;

    private AddressDAO addressDAO;
    private UserDAO userDAO;
    private CarDAO carDAO;
    private ReservationDAO reservationDAO;
    private CarRideDAO carRideDAO;
    private InfoSessionDAO infoSessionDAO;


    private List<Address> addresses = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Car> cars = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();
    private List<CarRide> carRides = new ArrayList<>();
    private List<InfoSession> infoSessions = new ArrayList<>();

    /**
     * Initializes the DAOs so they can be used in the test methods.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        DataAccessProvider provider = JDBCDataAccess.getTestDataAccessProvider();
        context = provider.getDataAccessContext();

        addressDAO = context.getAddressDAO();
        userDAO = context.getUserDAO();
        carDAO = context.getCarDAO();
        reservationDAO = context.getReservationDAO();
        carRideDAO = context.getCarRideDAO();
        infoSessionDAO = context.getInfoSessionDAO();
    }

    /**
     * Tests creating, getting, updating and deleting of addresses
     * @throws Exception
     */
    //@Test
    public void testAddressDAO() throws Exception {
        try {
            createAddresses();
            getAddressTest();
            updateAddressTest();
            deleteAddressesTest();
        } finally {
            context.rollback();
        }
    }

    /**
     * Tests creating, getting, updating and deleting of users
     * @throws Exception
     */
    //@Test
    public void testUserDAO() throws Exception {
        try {
            createAddresses();
            createUsers();
            getUserByIdTest(false);
            updateUserTest();
            deleteUserTest();
        } finally {
            context.rollback();
        }
    }

    /**
     * Tests creating, getting, updating and deleting of cars
     * @throws Exception
     */
    //@Test
    public void testCarDAO() throws Exception {
        try {
            createAddresses();
            createUsers();
            createCars();
            getCarTest();
            updateCarTest();
        } finally {
            context.rollback();
        }
    }

    /**
     * Tests creating, getting, updating and deleting of cars, without addresses
     * @throws Exception
     */
    //@Test
    public void testCarDAOWithoutAddresses() throws Exception {
        try {
            createUsers();
            createCarsWithoutAddresses();
            getCarTest();
            updateCarTest();
        } finally {
            context.rollback();
        }

    }

    /**
     * Tests creating, getting, updating and deleting of cars, without users
     * It should not be possible to create a car without a user
     * @throws Exception
     */
    //@Test
    public void testCarDAOWithoutUser() throws Exception {
        // Now let's try with User == null, but Cars.user_id cannot be null!
        try {
            createCarWithoutUser();
            Assert.fail("Cars.user_id cannot be null, createCarWithoutUser() should throw DataAccesException");
        } catch(DataAccessException e) {

        } finally {
            context.rollback();
        }
    }

    /**
     * Tests creating, getting, updating and deleting of reservations
     * @throws Exception
     */
    //@Test
    public void testReservationDAO() throws Exception {
        try {
            createAddresses();
            createUsers();
            createCars();
            createReservations();
            getReservationTest();
            updateReservationTest();
            deleteReservationsTest();
        } finally {
            context.rollback();
        }
    }

    /**
     * Tests creating, getting, updating and deleting of carrides
     * @throws Exception
     */
    //@Test
    public void testCarRideDAO() throws Exception {
        try {
            createAddresses();
            createUsers();
            createCars();
            createReservations();
            createCarRides();
            getCarRideTest();
            updateCarRideTest();
        } finally {
            context.rollback();
        }
    }

    /**
     * Tests creating, getting, updating and deleting of infosessions
     * @throws Exception
     */
    //@Test
    public void testInfoSessionDAO() throws Exception {
        try {
            createAddresses();
            createUsers();
            createInfoSessions();
            getInfoSessionTest();
            updateInfoSessionTest();
            deleteInfoSessionsTest();
        } finally {
            context.rollback();
        }
    }

    /**
     * Tests creating, getting, updating and deleting of infosessions, with same enrollees
     * It should not be possible to enroll the same user twice in the same infosession
     * @throws Exception
     */
    //@Test
    public void testInfoSessionDAOWithSameEnrollees() throws Exception {
        try {
            createAddresses();
            createUsers();
            createInfoSessionWithSameEnrollees();
        }
        finally {
            context.rollback();
        }
    }

    /**
     * Creates 100 random addresses in the be.ugent.degage.database and in private List address
     */
    private void createAddresses() throws Exception {
        Scanner sc = new Scanner(JDBCDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_addresses.txt"));
        sc.useDelimiter("\\t");
        sc.nextLine(); //skip header first time
        while(sc.hasNext()) {
            // Todo: read country from scanner
            String country = "Belgium";
            String street = sc.next();
            String nr = sc.next();
            String zip = sc.next();
            String city = sc.nextLine();

            Address address = addressDAO.createAddress(country, zip, city, street, nr, "");

            addresses.add(address);
        }
        sc.close();
    }

    /**
    * First createAddresses() has to be called
    */
    private void getAddressTest() throws Exception {
        for(Address address : addresses) {
            Address returnAddress = addressDAO.getAddress(address.getId());

            Assert.assertEquals(address.getBus(),returnAddress.getBus());
            Assert.assertEquals(address.getZip(),returnAddress.getZip());
            Assert.assertEquals(address.getNumber(),returnAddress.getNumber());
            Assert.assertEquals(address.getStreet(),returnAddress.getStreet());
            Assert.assertEquals(address.getCity(),returnAddress.getCity());
        }
    }

    /**
    * First createAddresses() has to be called
    */
    private void updateAddressTest() throws Exception {
        for(Address address : addresses) {
            address.setStreet(address.getStreet() + " (test)");
            address.setZip(address.getZip() + "AB");
            address.setCity(address.getCity() + " AB");
            address.setNumber(address.getNumber() + " AB");
            addressDAO.updateAddress(address);
        }

        getAddressTest();
    }
    /**
     * First createAddresses() has to be called
     */
    private void deleteAddressesTest() throws Exception {
        Iterator<Address> iAddresses = addresses.iterator();
        while(iAddresses.hasNext()) {
            Address address = iAddresses.next();
            addressDAO.deleteAddress(address);
            try {
                Address returnAddress = addressDAO.getAddress(address.getId());
                if(returnAddress != null)
                    Assert.fail("Address not permanently deleted");
            } catch(DataAccessException e) {
                // This should happen.
            }
            iAddresses.remove();
        }
    }

    /**
     * Creates 100 random users in the be.ugent.degage.database and in private List users
     */
    private void createUsers() throws Exception {
        Scanner sc = new Scanner(JDBCDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_users.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine(); //skip header first time
        while(sc.hasNext()) {
            String email = sc.next();
            String pass = sc.next();
            String firstName = sc.next();
            String lastName = sc.next();

            // Skip other fields, is for update
            sc.nextLine();
            User user = userDAO.createUser(email,pass,firstName,lastName);

            users.add(user);
        }
        sc.close();
    }

    /**
     * First createUsers() has to be called
     */
    private void getUserByEmailTest() {
        for(User user : users) {
            User returnUser = userDAO.getUser(user.getEmail());

            Assert.assertEquals(returnUser, user);
        }
    }

    /**
     * First createUsers() has to be called
     */
    private void getUserByIdTest(boolean withRest) {
        for(User user : users) {
            User returnUser = userDAO.getUser(user.getId(), withRest);

            Assert.assertEquals(returnUser, user);
        }
    }

    /**
     * First createUsers() and createAddresses() has to be called
     */
    private void updateUserTest() throws Exception {
        Scanner sc = new Scanner(JDBCDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_users.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine(); //skip header first time
        for(User user : users) {
            // Skip email, pass, firstname, lastname
            sc.next();
            sc.next();
            sc.next();
            sc.next();
            user.setEmail(user.getEmail() + ".com");
            user.setFirstName(user.getFirstName() + "Test");
            user.setLastName(user.getLastName() + "Test");
            user.setPassword(user.getPassword() + "Test");
            String cellphone = sc.next();
            user.setCellphone(cellphone);
            String phone = sc.next();
            user.setPhone(phone);
            UserGender gender = UserGender.valueOf(sc.next());
            user.setGender(gender);
            int addressDomicileId = sc.nextInt();
            user.setAddressDomicile(addresses.get(addressDomicileId-1));
            int addressResidenceId = sc.nextInt();
            user.setAddressResidence(addresses.get(addressResidenceId - 1));
            UserStatus status = UserStatus.valueOf(sc.next());
            user.setStatus(status);
            String damageHistory = sc.next();
            user.setDamageHistory(damageHistory);
            boolean payedDesposit = sc.nextInt() == 1;
            user.setPayedDeposit(payedDesposit);
            boolean agreeTerms = sc.nextInt() == 1;
            user.setAgreeTerms(agreeTerms);
            int contractManagerId = sc.nextInt();
            //user.setContractManager(users.get(contractManagerId-1));
            userDAO.updateUser(user, true);
        }
        getUserByIdTest(true);
    }

    /**
     * First createUsers() has to be called
     */
    private void deleteUserTest() {
        for(User user : users) {
            userDAO.deleteUser(user);
            User returnUser = userDAO.getUser(user.getId(), true);
            Assert.assertEquals(returnUser.getStatus(), UserStatus.DROPPED);
        }
    }

    /**
     * Creates 100 random cars, without addresses, in the be.ugent.degage.database and in private List cars
     */
    private void createCarsWithoutAddresses() throws Exception {
        Scanner sc = new Scanner(JDBCDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_cars.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine(); // skip header first time
        int i = 1;
        while(sc.hasNext()) {
            String name = "naam" + i++; // TODO: also read from Scanner
            String brand = sc.next();
            String type = sc.next();
            int seats = sc.nextInt();
            int doors = sc.nextInt();
            int year = sc.nextInt();
            boolean gps = sc.nextBoolean();
            boolean hook = sc.nextBoolean();
            String fuel = sc.next();
            CarFuel carFuel = CarFuel.valueOf(fuel);
            int fuelEconomy = sc.nextInt();
            int estimatedValue = sc.nextInt();
            int ownerAnnualKm = sc.nextInt();
            int owner_id = sc.nextInt();

            // To keep it simple, we take a random user_id, therefore there have to be users in the be.ugent.degage.database/list
            User user = users.get(owner_id);

            // Null as address
            Address address = null;
            String comments = sc.next();

            Car car = carDAO.createCar(name, brand, type, address, seats, doors, year, gps, gps, hook, carFuel, fuelEconomy, estimatedValue, ownerAnnualKm, null, null, user, comments, hook, null);
            cars.add(car);
        }
        sc.close();
    }

    /**
     * Creates 1 random car in the be.ugent.degage.database and in private List cars
     * This should fail
     */
    private void createCarWithoutUser() throws Exception {
        Scanner sc = new Scanner(JDBCDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_cars.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine(); // skip header first time
        String name = "name"; // TODO: also read from Scanner
        String brand = sc.next();
        String type = sc.next();
        int seats = sc.nextInt();
        int doors = sc.nextInt();
        int year = sc.nextInt();
        boolean gps = sc.nextBoolean();
        boolean hook = sc.nextBoolean();
        String fuel = sc.next();
        CarFuel carFuel = CarFuel.valueOf(fuel);
        int fuelEconomy = sc.nextInt();
        int estimatedValue = sc.nextInt();
        int ownerAnnualKm = sc.nextInt();
        int owner_id = sc.nextInt();

        // Null as user
        User user = null;
        // Null as address (should not matter)
        Address address = null;
        String comments = sc.next();

        Car car = carDAO.createCar(name, brand, type, address, seats, doors, year, gps, gps, hook, carFuel, fuelEconomy, estimatedValue, ownerAnnualKm, null, null, user, comments, hook, null);
        cars.add(car);
        sc.close();
    }

    /**
     * Creates 100 random cars in the be.ugent.degage.database and in private List cars
     * First createUsers() and createAddresses() has to be called
     */
    private void createCars() throws Exception {
        Scanner sc = new Scanner(JDBCDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_cars.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine(); // skip header first time
        int i = 1;
        while(sc.hasNext()) {
            String name = "Naam" + i++; // TODO: also read from scanner
            String brand = sc.next();
            String type = sc.next();
            int seats = sc.nextInt();
            int doors = sc.nextInt();
            int year = sc.nextInt();
            boolean gps = sc.nextBoolean();
            boolean hook = sc.nextBoolean();
            String fuel = sc.next();
            CarFuel carFuel = CarFuel.valueOf(fuel);
            int fuelEconomy = sc.nextInt();
            int estimatedValue = sc.nextInt();
            int ownerAnnualKm = sc.nextInt();
            int owner_id = sc.nextInt();

            // To keep it simple, we take a random user_id, therefore there have to be users in the be.ugent.degage.database/list
            User user = users.get(owner_id);
            // To keep it simple, we give the Address the same id as the user
            Address address = addresses.get(owner_id);
            String comments = sc.next();

            Car car = carDAO.createCar(name, brand, type, address, seats, doors, year, gps, gps, hook, carFuel, fuelEconomy, estimatedValue, ownerAnnualKm, null, null, user, comments, hook, null);
            cars.add(car);
        }
        sc.close();
    }
    /**
    * First createCars() has to be called
    */
    private void getCarTest() {
        for(Car car : cars) {
            Car returnCar = carDAO.getCar(car.getId());
            Assert.assertEquals(car, returnCar);
        }
    }

    /**
    * First createCars() has to be called
    */
    private void updateCarTest() {
        for(Car car : cars) {
            // Other fields should be updated too
            car.setBrand(car.getBrand() + "test");
            car.setType(car.getType() + "test");

            carDAO.updateCar(car);
        }
        getCarTest();
    }

    /**
     * Creates 100 random reservations in the be.ugent.degage.database and in private List reservations
     * First createCars() has to be called
     */
    private void createReservations() throws Exception {
    	Scanner sc = new Scanner(JDBCDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_reservations.txt"));
        sc.useDelimiter("\\t|\\r\\n");
    	sc.nextLine();

    	while(sc.hasNext()){
            String fromString = sc.next();
            Date fromDate = new SimpleDateFormat("M/d/y H:m").parse(fromString);
            DateTime from = new DateTime(fromDate);

            String toString = sc.next();
            Date toDate = new SimpleDateFormat("M/d/y H:m").parse(toString);
            DateTime to = new DateTime(toDate);

    		int carid = sc.nextInt();
            Car car = cars.get(carid-1);

    		int userid = sc.nextInt();
            User user = users.get(userid-1);

    		Reservation reservation = reservationDAO.createReservation(from, to, car, user, "");

    		reservations.add(reservation);
    	}
    	sc.close();
    }

    /**
     * First createReservations() has to be called
     */
    private void getReservationTest() {
        for(Reservation reservation : reservations) {
            Reservation returnReservation = reservationDAO.getReservation(reservation.getId());

            Assert.assertEquals(reservation.getCar().getId(),returnReservation.getCar().getId());
            Assert.assertEquals(reservation.getUser().getId(),returnReservation.getUser().getId());
            Assert.assertEquals(reservation.getTo(),returnReservation.getTo());
            Assert.assertEquals(reservation.getFrom(),returnReservation.getFrom());
            Assert.assertEquals(reservation.getStatus(), returnReservation.getStatus());
        }
    }

    /**
     * First createReservations() has to be called
     */
    private void updateReservationTest() {
        for(Reservation reservation : reservations) {
            reservation.setCar(cars.get((reservation.getCar().getId() + 1) % 100));
            reservation.setUser(users.get((reservation.getUser().getId() + 1) % 100));
            reservation.setFrom(reservation.getFrom().plusHours(1));
            reservation.setTo(reservation.getTo().plusHours(1));

            reservationDAO.updateReservation(reservation);
        }
        getReservationTest();
    }

    /**
     * First createReservations() has to be called
     */
    private void deleteReservationsTest(){
    	Iterator<Reservation> i = reservations.iterator();
        while(i.hasNext()) {
            Reservation reservation = i.next();
            reservationDAO.deleteReservation(reservation);
            try {
                Reservation returnReservation = reservationDAO.getReservation(reservation.getId());
                if(returnReservation != null)
                    Assert.fail("Reservation not permanently deleted");
            } catch(DataAccessException e) {
                // This should happen.
            }
            i.remove();
        }
    }

    /**
     * Creates 100 random reservations in the be.ugent.degage.database and in private List reservations
     * First createReservations() has to be called
     */
    private void createCarRides() throws Exception {
        for(Reservation reservation : reservations) {
            CarRide carRide = carRideDAO.createCarRide(reservation, 0, 0, false, 0);

            carRides.add(carRide);
        }
    }

    /**
     * First createCarRides() has to be called
     */
    private void getCarRideTest() {
        for(CarRide carRide : carRides) {
            CarRide returncarRide = carRideDAO.getCarRide(carRide.getReservation().getId());

            // TODO: assertEquals of the actual reservations, and not only ID's?
            Assert.assertEquals(carRide.getReservation().getId(),returncarRide.getReservation().getId());
            Assert.assertEquals(carRide.isStatus(),returncarRide.isStatus());
            Assert.assertEquals(carRide.getStartMileage(),returncarRide.getStartMileage());
            Assert.assertEquals(carRide.getEndMileage(),returncarRide.getEndMileage());
        }
    }

    /**
     * First createCarRides() has to be called
     */
    private void updateCarRideTest() throws Exception {
        Scanner sc = new Scanner(JDBCDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_carrides.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine();

        for(CarRide carRide : carRides) {
            int statusInt = sc.nextInt();
            boolean status = statusInt == 1;
            carRide.setStatus(status);

            int start = sc.nextInt();
            carRide.setStartMileage(start);

            int end = sc.nextInt();
            carRide.setEndMileage(end);

            carRideDAO.updateCarRide(carRide);
        }
        getCarRideTest();
    }

    /**
     * Creates 100 random infosessions in the be.ugent.degage.database and in private List infosessions
     * First createUsers() and createAddresses() has to be called
     */
    private void createInfoSessions() throws Exception {
        Scanner sc = new Scanner(JDBCDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_infosessions.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine();

        while(sc.hasNext()){
            String timeString = sc.next();
            Date timeDate = new SimpleDateFormat("M/d/y H:m").parse(timeString);
            DateTime time = new DateTime(timeDate);

            int addressid = sc.nextInt();
            Address address = addresses.get(addressid-1);

            int hostid = sc.nextInt();
            User host = users.get(hostid-1);

            int u1id = sc.nextInt();
            User u1 = users.get(u1id-1);

            int u2id = sc.nextInt();
            User u2 = users.get(u2id - 1);

            int u3id = sc.nextInt();
            User u3 = users.get(u3id - 1);

            int u4id = sc.nextInt();
            User u4 = users.get(u4id - 1);

            int u5id = sc.nextInt();
            User u5 = users.get(u5id - 1);

            InfoSession infoSession = infoSessionDAO.createInfoSession(InfoSessionType.NORMAL, "", host, address, time, 0, "");
            infoSessionDAO.registerUser(infoSession, u1);
            infoSessionDAO.registerUser(infoSession, u2);
            infoSessionDAO.registerUser(infoSession, u3);
            infoSessionDAO.registerUser(infoSession, u4);
            infoSessionDAO.registerUser(infoSession, u5);

            infoSessions.add(infoSession);
        }
        sc.close();
    }

    /**
     * Creates a random infosession in the be.ugent.degage.database and in private List infosessions
     * First createUsers() and createAddresses() has to be called
     * This should fail
     */
    private void createInfoSessionWithSameEnrollees() throws Exception {
        Scanner sc = new Scanner(JDBCDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_infosessions.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine();

        // One InfoSession is enough, we don't need 100 because it should fail on the first one

        String timeString = sc.next();
        Date timeDate = new SimpleDateFormat("M/d/y H:m").parse(timeString);
        DateTime time = new DateTime(timeDate);

        int addressid = sc.nextInt();
        Address address = addresses.get(addressid-1);

        int hostid = sc.nextInt();
        User host = users.get(hostid-1);

        int u1id = sc.nextInt();
        User u1 = users.get(u1id-1);

        int u2id = sc.nextInt();
        User u2 = users.get(u2id - 1);

        int u3id = sc.nextInt();
        User u3 = users.get(u3id - 1);

        int u4id = sc.nextInt();
        User u4 = users.get(u4id - 1);

        int u5id = sc.nextInt();
        User u5 = users.get(u5id - 1);

        InfoSession infoSession = infoSessionDAO.createInfoSession(InfoSessionType.NORMAL, "", host, address, time, 0, "");
        infoSessionDAO.registerUser(infoSession, u1);
        infoSessionDAO.registerUser(infoSession, u2);
        infoSessionDAO.registerUser(infoSession, u3);
        infoSessionDAO.registerUser(infoSession, u4);
        infoSessionDAO.registerUser(infoSession, u5);
        try {
            infoSessionDAO.registerUser(infoSession, u5);
            Assert.fail("Should not be able to register same user twice on same InfoSession");
        } catch(DataAccessException e) {
            // This should happen
        }

        infoSessions.add(infoSession);

        sc.close();
    }


    /**
     * First createInfoSessions() has to be called
     */
    private void getInfoSessionTest() {
        for(InfoSession infoSession : infoSessions) {
            InfoSession returnInfoSession = infoSessionDAO.getInfoSession(infoSession.getId(), true);
            Assert.assertEquals(infoSession.getTime(), returnInfoSession.getTime());
            Assert.assertEquals(infoSession.getAddress().getId(), returnInfoSession.getAddress().getId());
            Assert.assertEquals(infoSession.getHost().getId(), returnInfoSession.getHost().getId());

            List<Enrollee> enrollees = infoSession.getEnrolled();
            List<Enrollee> returnEnrollees = returnInfoSession.getEnrolled();
            Assert.assertEquals(enrollees.size(), returnEnrollees.size());

            for(int i = 0; i < infoSession.getEnrolled().size(); i++) {
                Enrollee enrollee = enrollees.get(i);
                boolean sameEnrollee = false;
                for(int j = 0; j < infoSession.getEnrolled().size(); j++) {
                    Enrollee returnEnrollee = returnEnrollees.get(j);
                    sameEnrollee = sameEnrollee ||(enrollee.getUser().getId() == returnEnrollee.getUser().getId() && enrollee.getStatus() == returnEnrollee.getStatus());
                }
                Assert.assertTrue(sameEnrollee);
            }
        }
    }

    /**
     * First createInfoSessions() has to be called
     */
    private void updateInfoSessionTest() {
        for(InfoSession infoSession : infoSessions) {

            infoSession.setTime(infoSession.getTime().plusHours(1));
            infoSession.setAddress(addresses.get((infoSession.getAddress().getId() + 1) % 100));
            Enrollee delete = infoSession.getEnrolled().get(0);
            infoSession.deleteEnrollee(delete);

            infoSessionDAO.updateInfoSession(infoSession);
            infoSessionDAO.unregisterUser(infoSession, delete.getUser());
        }
        getInfoSessionTest();
    }

    /**
     * First createInfoSessions() has to be called
     */
    private void deleteInfoSessionsTest(){
        Iterator<InfoSession> i = infoSessions.iterator();
        while(i.hasNext()) {
            InfoSession infoSession = i.next();
            infoSessionDAO.deleteInfoSession(infoSession.getId());
            try {
                InfoSession returnInfoSession = infoSessionDAO.getInfoSession(infoSession.getId(), false);
                if(returnInfoSession != null)
                    Assert.fail("InfoSession not permanently deleted");

                // Now let's see if the enrollees are still in the be.ugent.degage.database...
                List<Enrollee> enrollees = infoSession.getEnrolled();
                try {
                    infoSessionDAO.unregisterUser(infoSession.getId(), enrollees.get(0).getUser().getId());
                    Assert.fail("InfoSession Enrollee not deleted");
                } catch(DataAccessException e) {
                    // This should happen
                }
            } catch(DataAccessException e) {
                // This should happen.
            }
            i.remove();
        }
    }
}
