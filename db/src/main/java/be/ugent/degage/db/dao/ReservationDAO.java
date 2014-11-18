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

import java.util.Collection;
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

    /**
     * Return the first reservation that follows the given reservation, unless it is more
     * than a day removed
     */
    public Reservation getNextReservation(int reservationId) throws DataAccessException;

    /**
     * Return the last reservation that preceeds the given reservation, unless it is more
     * than a day removed
     */
    public Reservation getPreviousReservation(int reservationId) throws DataAccessException;



    public void deleteReservation(Reservation reservation) throws DataAccessException;

    public int getAmountOfReservations(Filter filter) throws DataAccessException;

    /**
     * List of reservations (not cancelled not refused) where the given user is owner or driver
     */
    public Iterable<Reservation> getReservationListForUser(int userID) throws DataAccessException;


    public Iterable<Reservation> getReservationListPage(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public Iterable<Reservation> getReservationListForCar(int carID) throws DataAccessException;


    public int numberOfReservationsWithStatus(ReservationStatus status, int userId, boolean userIsLoaner);

    /**
     * Migrate reservations with status 'ACCEPTED' to status 'REQUEST_DETAILS' when the entire reservation is in the past
     */
    public void adjustReservationStatuses();

    /**
     * List of reservations for a certain car. Used in {@link #listCRInfo}
     */
    public static class CRInfo {
        public Car car;

        public Collection<Reservation> reservations;
    }

    /**
     * Return information on all reservations (exceot those cancelled) during a certain period of time, ordered by car.
     */
    public Iterable<CRInfo> listCRInfo (DateTime from, DateTime to);


}
