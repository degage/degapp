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
        public int carId;
        public String carName;
        public LocalDateTime time;

        public KmDetails(int startKm, int endKm, int reservationId, LocalDateTime time, int carId, String carName) {
            this.startKm = startKm;
            this.endKm = endKm;
            this.time =  time;
            this.reservationId = reservationId;
            this.carId = carId;
            this.carName = carName;
        }

    }

    private List<KmDetails> getKmDetails(int billingId, int carId) throws DataAccessException {
        StringBuilder b = new StringBuilder();
        b.append ("SELECT car_ride_start_km, car_ride_end_km, reservation_id, reservation_from, car_name, car_id " +
                  "FROM billing, trips JOIN cars ON car_id = reservation_car_id " +
                  "WHERE billing_id = ? AND (reservation_status = 'FINISHED' || reservation_status='FROZEN') " +
                  "AND reservation_from < billing_limit ");
        if (carId > 0) {
            b.append ("AND reservation_car_id = ? ");
        }
        b.append ("ORDER BY car_name, car_ride_start_km, car_ride_end_km");
        try (PreparedStatement ps = prepareStatement(b.toString())) {
            ps.setInt(1, billingId);
            if (carId > 0) {
                ps.setInt(2, carId);
            }
            return toList(ps, rs -> new KmDetails(
                    rs.getInt("car_ride_start_km" ),
                    rs.getInt("car_ride_end_km" ),
                    rs.getInt("reservation_id" ),
                    rs.getTimestamp("reservation_from").toLocalDateTime(),
                    rs.getInt("car_id"),
                    rs.getString("car_name")
            ));
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch km details", ex);
        }
    }

    private TripAnomaly createAnomaly (KmDetails first, KmDetails second, AnomalyType type) {
        TripAnomaly anomaly = new TripAnomaly();
        anomaly.carId = first.carId;
        anomaly.carName = first.carName;
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
                if (current.carId == prev.carId) {
                    if (current.startKm < prev.endKm) {
                        result.add(createAnomaly(prev, current, AnomalyType.OVERLAP));
                    } else if (current.startKm > prev.endKm) {
                        result.add(createAnomaly(prev, current, AnomalyType.GAP));
                    }
                }
            }

            // zero length
            for (KmDetails current: details) {
                if (current.startKm == current.endKm) {
                    result.add(createAnomaly(current, current, AnomalyType.ZERO_KM));
                }
            }
        }
        return result;
    }

    @Override
    public Iterable<RefuelAnomaly> getRefuelAnomalies(int billingId, int carId) {
        StringBuilder b = new StringBuilder(
                "SELECT car_id, car_name, reservation_id, reservation_from, " +
                        "doubles.refuel_eurocents, refuel_id " +
                "FROM ( SELECT reservation_car_id, refuel_eurocents FROM billing, refuels " +
                        "JOIN reservations ON reservation_id = refuel_car_ride_id " +
                        "WHERE NOT refuel_archived " +
                        "AND reservation_from < billing_limit AND billing_id = ? " +
                        "AND (refuel_status='FROZEN' OR refuel_status='ACCEPTED') ");
        if (carId > 0) {
            b.append ("AND reservation_car_id = ? ");
        }
        b.append ("GROUP BY refuel_eurocents,reservation_car_id  HAVING COUNT(reservation_car_id) > 1 " +
                        ") AS doubles " +
                "JOIN refuels USING (refuel_eurocents) " +
                "JOIN reservations AS orig " +
                        "ON refuel_car_ride_id = reservation_id  AND orig.reservation_car_id = doubles.reservation_car_id " +
                "JOIN cars ON car_id = orig.reservation_car_id " +
                "ORDER BY car_name, doubles.refuel_eurocents, reservation_id"
        );

        try (PreparedStatement ps = prepareStatement( b.toString()  )) {
            ps.setInt(1, billingId);
            if (carId > 0) {
                ps.setInt(2, carId);
            }
            return toList( ps, rs -> {
                RefuelAnomaly ano = new RefuelAnomaly();
                ano.carId = rs.getInt("car_id");
                ano.carName = rs.getString("car_name");
                ano.reservationId = rs.getInt("reservation_id");
                ano.refuelId = rs.getInt("refuel_id");
                ano.eurocents = rs.getInt("refuel_eurocents");
                ano.from = rs.getTimestamp("reservation_from").toLocalDateTime();
                return ano;
            });
        } catch (SQLException e){
            throw new DataAccessException("Could not get refuel anomalies", e);
        }
    }
}
