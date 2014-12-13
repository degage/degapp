/* TestReservationDAO.java
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
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package database.mocking;

import java.util.List;
import java.util.ArrayList;

import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationStatus;
import be.ugent.degage.db.models.User;

import org.joda.time.DateTime;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.ReservationDAO;
import utility.Cloner;

public class TestReservationDAO implements ReservationDAO{
	
	private List<Reservation> reservations;
	private int idCounter;
	
	public TestReservationDAO(){
		idCounter=0;
		reservations = new ArrayList<>();
	}

	@Override
	public Reservation createReservation(DateTime from, DateTime to, Car car, User user, String message) throws DataAccessException {
		Reservation reservation = new Reservation(idCounter++, car, user,from,to, message);
		reservations.add(reservation);
		return reservation;
	}

	@Override
	public void updateReservation(Reservation reservation) throws DataAccessException {
		for(int i = 0; i < reservations.size(); i++) {
            if(reservation.getId() == reservations.get(i).getId()) {
                reservations.set(i, reservation);
            }
        }
	}

	@Override
	public Reservation getReservation(int id) throws DataAccessException {
		for(Reservation reservation : reservations){
			if(reservation.getId()==id){
                Reservation r = new Reservation(reservation.getId(), reservation.getCar(), reservation.getUser(), reservation.getFrom(), reservation.getUntil(), reservation.getMessage());
                r.setStatus(reservation.getStatus());
                return r;
			}
		}
		return null;
	}

    @Override
    public Reservation getNextReservation(Reservation reservation) throws DataAccessException {
        return null;
    }

    @Override
    public Reservation getPreviousReservation(Reservation reservation) throws DataAccessException {
        return null;
    }

    @Override
	public void deleteReservation(Reservation reservation) throws DataAccessException {
		if(reservations.contains(reservation)){
			reservations.remove(reservation);
		}
	}
	@Override
	public List<Reservation> getReservationListForCar(int carID) throws DataAccessException {
		List<Reservation> list = new ArrayList<>();
		for(Reservation res : reservations){
			if(res.getCar().getId()==carID){
				list.add(res);
			}
		}
		return list;
	}

    @Override
    public int numberOfReservationsWithStatus(ReservationStatus status, int userId, boolean userIsOwner, boolean userIsLoaner) {
        return 0;
    }

    @Override
    public void updateTable() {

    }

    @Override
	public int getAmountOfReservations(Filter filter)
			throws DataAccessException {
        if(!filter.getValue(FilterField.RESERVATION_USER_OR_OWNER_ID).equals("")) {
            int id = Integer.parseInt(filter.getValue(FilterField.RESERVATION_USER_OR_OWNER_ID));
            List<Reservation> list = new ArrayList<>();
            for(Reservation res : reservations){
                if(res.getCar().getOwner().getId()==id || res.getUser().getId()== id){
                    list.add(res);
                }
            }
            return list.size();
        }
		return 0; // TODO: add filter methods
	}

	@Override
	public List<Reservation> getReservationListPage(FilterField orderBy,
			boolean asc, int page, int pageSize, Filter filter)
			throws DataAccessException {
        if(!filter.getValue(FilterField.RESERVATION_USER_OR_OWNER_ID).equals("")) {
            int id = Integer.parseInt(filter.getValue(FilterField.RESERVATION_USER_OR_OWNER_ID));
            List<Reservation> list = new ArrayList<>();
            for(Reservation res : reservations){
                if(res.getCar().getOwner().getId()==id || res.getUser().getId()== id){
                    list.add(res);
                }
            }
            return list.subList((page-1)*pageSize, page*pageSize > reservations.size() ? reservations.size() : page*pageSize);
        }

		return null; // TODO: add other filter methods
	}
	
}
