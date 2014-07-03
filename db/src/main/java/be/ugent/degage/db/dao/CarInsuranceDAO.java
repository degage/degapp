package be.ugent.degage.db.dao;

import java.util.Date;
import java.util.List;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.CarInsurance;

public interface CarInsuranceDAO {
	public CarInsurance createCarInsurance(Date expiration, int bonus_malus, int polisNr, Car car) throws DataAccessException;
	public void updateCarInsurance(CarInsurance insurance) throws DataAccessException;
	public void deleteCarInsurance(CarInsurance insurance) throws DataAccessException;
	public List<CarInsurance> getAllCarInsurances(Car car) throws DataAccessException;
	public void deleteAllCarInsurances(Car car) throws DataAccessException;
}
