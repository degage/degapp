/* JDBCCheckDAO.java
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

package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.CheckDAO;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JDBC implementation of {@link CheckDAO}
 */
class JDBCCheckDAO extends AbstractDAO implements CheckDAO {

    public JDBCCheckDAO(JDBCDataAccessContext context) {
        super(context);
    }

    class KmDetails {
        public int startKm;
        public int endKm;
        public int reservationId;
        public LocalDateTime time;

        public KmDetails(int startKm, int endKm, int reservationId, LocalDateTime time) {
            this.startKm = startKm;
            this.endKm = endKm;
            this.time =  time;
            this.reservationId = reservationId;
        }

    }

    private List<KmDetails> getKmDetails(int billingId, int carId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT car_ride_start_km, car_ride_end_km, reservation_id, reservation_from " +
                        "FROM trips, billing " +
                "WHERE billing_id = ? AND reservation_car_id = ? AND reservation_status = 'FINISHED' " +
                    "AND reservation_from < billing_limit " +
                "ORDER BY car_ride_start_km, car_ride_end_km"
        )) {
            ps.setInt(1, billingId);
            ps.setInt(2, carId);
            return toList(ps, rs -> new KmDetails(
                    rs.getInt("car_ride_start_km" ),
                    rs.getInt("car_ride_end_km" ),
                    rs.getInt("reservation_id" ),
                    rs.getTimestamp("reservation_from").toLocalDateTime()
            ));
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch km details", ex);
        }
    }

    private TripAnomaly createAnomaly (KmDetails first, KmDetails second, int carId, AnomalyType type) {
        TripAnomaly anomaly = new TripAnomaly();
        anomaly.carId = carId;
        anomaly.type = type;

        anomaly.firstId = first.reservationId;
        anomaly.secondId = second.reservationId;
        anomaly.firstTime = first.time;
        anomaly.secondTime = second.time;

        anomaly.firstEndKm = first.endKm;
        anomaly.secondStartKm = second.startKm;
        return anomaly;

    }

    @Override
    public Iterable<TripAnomaly> getTripAnomalies(int billingId, int carId) throws DataAccessException {
        Collection<TripAnomaly> result = new ArrayList<>();
        List<KmDetails> details = getKmDetails(billingId, carId);
        if (details.size() > 0) {

            // overlaps and gaps
            for (int i = 1; i < details.size(); i++) {
                KmDetails prev = details.get (i-1);
                KmDetails current = details.get (i);
                if (current.startKm < prev.endKm) {
                    result.add(createAnomaly(prev, current, carId, AnomalyType.OVERLAP));
                } else if (current.startKm > prev.endKm) {
                    result.add(createAnomaly(prev, current, carId, AnomalyType.GAP));
                }
            }

            // zero length
            for (KmDetails current: details) {
                if (current.startKm == current.endKm) {
                    result.add(createAnomaly(current, current, carId, AnomalyType.ZERO_KM));
                }
            }
        }
        return result;
    }
}
