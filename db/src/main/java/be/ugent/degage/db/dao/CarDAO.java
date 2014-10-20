/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

import java.util.List;

/**
 *
 * @author Laurent
 */
public interface CarDAO {

    public Car createCar(String name, String brand, String type, Address location, Integer seats, Integer doors, Integer year, boolean manual,
                         boolean gps, boolean hook, CarFuel fuel, Integer fuelEconomy, Integer estimatedValue, Integer ownerAnnualKm,
                         TechnicalCarDetails technicalCarDetails, CarInsurance insurance, User owner, String comments, boolean active, File photo) throws DataAccessException;
    public void updateCar(Car car) throws DataAccessException;
    public Car getCar(int id) throws DataAccessException;

    public Iterable<CarAvailabilityInterval> getAvailabilities(int carId) throws DataAccessException;
    public void addOrUpdateAvailabilities(Car car, Iterable<CarAvailabilityInterval> availabilities) throws DataAccessException;
    public void deleteAvailabilties(Iterable<CarAvailabilityInterval> availabilities) throws DataAccessException;

    public Iterable<User> getPrivileged(int carId) throws DataAccessException;
    public void addPrivileged(int carId, Iterable<User> users) throws DataAccessException;
    public void deletePrivileged(int carId, Iterable<User> users) throws DataAccessException;

    public int getAmountOfCars(Filter filter) throws DataAccessException;

    // TODO: List -> Iterable
    public List<Car> getCarList(int page, int pageSize) throws DataAccessException;
    public List<Car> getCarList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    // newer version - uses shorter SQL
    public Iterable<Car> listCars (FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public int countCars(Filter filter) throws DataAccessException;


    public List<Car> getCarsOfUser(int user_id) throws DataAccessException;
}
