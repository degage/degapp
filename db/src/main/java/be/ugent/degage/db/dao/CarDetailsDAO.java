package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.CarDetails;

public interface CarDetailsDAO {
	public CarDetails createCarDetails(String licensePlate, int chasisNumber, String registration) throws DataAccessException;
	public void updateCarDetails(CarDetails carDetails) throws DataAccessException;
	public CarDetails getCarDetails(int id) throws DataAccessException;
	public void deleteCarDetails(CarDetails carDetails) throws DataAccessException;
}
