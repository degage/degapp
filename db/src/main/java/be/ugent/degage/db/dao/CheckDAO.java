/* CheckDAO.java
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

package be.ugent.degage.db.dao;

import java.time.LocalDateTime;

/**
 * DAO which checks for inconsistencies and anomalies in the database.
 */
public interface CheckDAO {

    public enum AnomalyType {
        OVERLAP, GAP, ZERO_KM
    }

    /**
     * Return a list of anomalies concerning finished trips
     *
     * @param carId     The car for which the anomalies should be checked, if zero then
     *                  check all cars.
     * @param billingId Only check trips that start before the limit date in this billing
     */
    public Iterable<TripAnomaly> getTripAnomalies(int billingId, int carId);


    public class TripAnomaly {
        public AnomalyType type;
        public int carId;  // id of the car
        public String carName;

        public int firstId;
        public int secondId; // ids of the reservations
        public LocalDateTime firstTime;
        public LocalDateTime secondTime; // start time of the reservations
        public int firstEndKm;
        public int secondStartKm;

    }

    /**
     * Return a list of anomalies concerning  refuels
     *
     * @param carId     The car for which the anomalies should be checked, if zero then
     *                  check all cars.
     * @param billingId Only check trips that start before the limit date in this billing
     */
    public Iterable<RefuelAnomaly> getRefuelAnomalies(int billingId, int carId);

    public class RefuelAnomaly {
        public int carId;  // id of the car
        public String carName;
        public int reservationId;
        public int refuelId;
        public LocalDateTime from;
        public int eurocents;
    }


}
