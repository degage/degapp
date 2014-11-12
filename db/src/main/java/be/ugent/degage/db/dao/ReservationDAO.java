/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationStatus;
import be.ugent.degage.db.models.User;
import org.joda.time.DateTime;

import java.util.List;

/**
 */
public interface ReservationDAO {

    /**
     * Add a new reservation to the database. If the user is privileged for the car then the reservation is accepted automatically.
     */
    public Reservation createReservation(DateTime from, DateTime to, int carId, int userId, String message) throws DataAccessException;


    public void updateReservation(Reservation reservation) throws DataAccessException;
    public Reservation getReservation(int id) throws DataAccessException;
    public Reservation getNextReservation(Reservation reservation) throws DataAccessException;
    public Reservation getPreviousReservation(Reservation reservation) throws DataAccessException;
    public void deleteReservation(Reservation reservation) throws DataAccessException;

    public int getAmountOfReservations(Filter filter) throws DataAccessException;
    public List<Reservation> getReservationListForUser(int userID) throws DataAccessException;
    public List<Reservation> getReservationListPage(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public List<Reservation> getReservationListForCar(int carID) throws DataAccessException;
    public int numberOfReservationsWithStatus(ReservationStatus status, int userId, boolean userIsOwner, boolean userIsLoaner);

    public void updateTable();
}
