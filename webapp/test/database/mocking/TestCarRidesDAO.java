/* TestCarRidesDAO.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

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
