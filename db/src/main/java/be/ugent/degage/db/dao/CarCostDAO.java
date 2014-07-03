package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/04/14.
 */
public interface CarCostDAO {

    public CarCost createCarCost(Car car, BigDecimal amount, BigDecimal mileage, String description, DateTime time, int fileId) throws DataAccessException;
    public int getAmountOfCarCosts(Filter filter) throws DataAccessException;
    public List<CarCost> getCarCostList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public void updateCarCost(CarCost carCost) throws DataAccessException;
    public CarCost getCarCost(int id) throws DataAccessException;public void endPeriod() throws DataAccessException;
    public List<CarCost> getBillCarCosts(Date date, int car) throws DataAccessException;

}
