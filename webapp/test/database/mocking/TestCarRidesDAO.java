package database.mocking;

import java.util.ArrayList;
import java.util.List;

import be.ugent.degage.db.models.CarRide;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.CarRideDAO;
import be.ugent.degage.db.DataAccessException;

public class TestCarRidesDAO implements CarRideDAO {
	
	private List<CarRide> rides;
	private int idCounter;
	
	public TestCarRidesDAO(){
		idCounter=0;
		rides = new ArrayList<>();
	}

    @Override
    public CarRide createCarRide(Reservation reservation, int startMileage, int endMileage, boolean damaged, int refueling) throws DataAccessException {
        CarRide ride = new CarRide(reservation);
        ride.setStartMileage(startMileage);
        ride.setEndMileage(endMileage);
        ride.setDamaged(damaged);
        ride.setRefueling(refueling);
        rides.add(ride);
        return ride;
    }

    @Override
	public CarRide getCarRide(int id) throws DataAccessException {
		for(CarRide ride : rides){
			if(ride.getReservation().getId()==id){
				return ride;
			}
		}
		return null;
	}

	@Override
	public void updateCarRide(CarRide carRide) throws DataAccessException {
		// ok
	}

}
