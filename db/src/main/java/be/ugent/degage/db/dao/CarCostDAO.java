package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.CarCost;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/04/14.
 */
public interface CarCostDAO {

    public CarCost createCarCost(Car car, BigDecimal amount, BigDecimal mileage, String description, LocalDate date, int fileId) throws DataAccessException;
    public int getAmountOfCarCosts(Filter filter) throws DataAccessException;
    public List<CarCost> getCarCostList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public void updateCarCost(CarCost carCost) throws DataAccessException;
    public CarCost getCarCost(int id) throws DataAccessException;public void endPeriod() throws DataAccessException;
    public List<CarCost> getBillCarCosts(LocalDate date, int car) throws DataAccessException;

}
