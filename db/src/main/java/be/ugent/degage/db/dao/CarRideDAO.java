package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.CarRide;
import be.ugent.degage.db.models.Reservation;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by HannesM on 10/03/14.
 */
public interface CarRideDAO {
    public CarRide createCarRide(Reservation reservation, int startMileage, int endMileage, boolean damaged, int refueling) throws DataAccessException;
    public CarRide getCarRide(int id) throws DataAccessException;
    public void updateCarRide(CarRide carRide) throws DataAccessException;
    public void endPeriod() throws DataAccessException;
    public List<CarRide> getBillRidesForLoaner(LocalDate date, int user) throws DataAccessException;
    public List<CarRide> getBillRidesForCar(LocalDate date, int car) throws DataAccessException;
}
