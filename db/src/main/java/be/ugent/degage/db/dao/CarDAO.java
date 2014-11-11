/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

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

    /**
     * Return a list of all cars, with full information (in order to export it to a spread sheet)
     */
    public Iterable<Car> listAllCars () throws DataAccessException;


    /**
     * Returns a filtered list of cars. Only <i>active</i> cars are shown.
     */
    public Iterable<Car> getCarList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    /**
     * Number of cars that can be returned by equivalent call to {@link #getCarList}
     */
    public int getAmountOfCars(Filter filter) throws DataAccessException;

    // newer version - uses shorter SQL
    public Iterable<Car> listCars (FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public int countCars(Filter filter) throws DataAccessException;

    public Iterable<Car> listCarsOfUser (int userId)  throws DataAccessException;

    public boolean isCarOfUser (int carId, int userId) throws DataAccessException;

}
