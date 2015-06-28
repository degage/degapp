/* JDBCBillingDAO.java
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
import be.ugent.degage.db.dao.BillingDAO;
import be.ugent.degage.db.models.*;
import com.google.common.primitives.Ints;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BillingDAO}
 */
public class JDBCBillingDAO extends AbstractDAO implements BillingDAO {

    private static final String BILLING_FIELDS =
            "billing_id, billing_description, billing_prefix, billing_limit, billing_start, billing_status, " +
                    "billing_simulation_date, billing_drivers_date, billing_owners_date ";


    public JDBCBillingDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static Billing populateBilling(ResultSet rs) throws SQLException {
        Date simulationDate = rs.getDate("billing_simulation_date");
        Date driversDate = rs.getDate("billing_drivers_date");
        Date ownersDate = rs.getDate("billing_owners_date");
        return new Billing(
                rs.getInt("billing_id"),
                rs.getString("billing_description"),
                rs.getString("billing_prefix"),
                rs.getDate("billing_start").toLocalDate(),
                rs.getDate("billing_limit").toLocalDate(),
                BillingStatus.valueOf(rs.getString("billing_status")),
                simulationDate == null ? null : simulationDate.toLocalDate(),
                driversDate == null ? null : driversDate.toLocalDate(),
                ownersDate == null ? null : ownersDate.toLocalDate()
        );
    }

    private static BillingInfo populateBillingInfo(ResultSet rs) throws SQLException {
        return new BillingInfo(
                rs.getInt("billing_id"),
                rs.getString("billing_description"),
                rs.getDate("billing_start").toLocalDate(),
                rs.getDate("billing_limit").toLocalDate(),
                BillingStatus.valueOf(rs.getString("billing_status")),
                rs.getInt("car_id"),
                rs.getString("car_name")
        );
    }

    @Override
    public Iterable<Billing> listAllBillings() {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + BILLING_FIELDS + "FROM billing ORDER BY billing_limit DESC"
        )) {
            return toList(ps, JDBCBillingDAO::populateBilling);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all billings", ex);
        }
    }

    private static final String BILLING_INFO_FIELDS =
            "billing_id, billing_description, billing_limit, billing_start, billing_status ";

    @Override
    public Iterable<BillingInfo> listBillingsForUser(int userId) {
        try (PreparedStatement ps = prepareStatement(
                "(SELECT " + BILLING_INFO_FIELDS + ", 0 AS car_id, NULL AS car_name " +
                        "FROM billing JOIN b_user ON bu_billing_id = billing_id " +
                        "WHERE bu_user_id = ?)" +
                        "UNION " +
                        "(SELECT " + BILLING_INFO_FIELDS + ", cars.car_id, car_name " +
                        "FROM billing JOIN cars_billed USING (billing_id) " +
                        "JOIN cars ON cars.car_id = cars_billed.car_id " +
                        "WHERE billing_status = 'ALL_DONE' AND car_owner_user_id = ?) " +
                        "ORDER BY billing_limit DESC, car_id ASC"
        )) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            return toList(ps, JDBCBillingDAO::populateBillingInfo);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list billings for user", ex);
        }
    }

    @Override
    public Billing getBilling(int id) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + BILLING_FIELDS + "FROM billing WHERE billing_id = ?"
        )) {
            ps.setInt(1, id);
            return toSingleObject(ps, JDBCBillingDAO::populateBilling);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get billings", ex);
        }
    }

    public Iterable<KmPrice> listKmPrices(int billingId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT km_price_from, km_price_eurocents FROM km_price WHERE km_price_billing_id = ? " +
                        "ORDER BY km_price_from"
        )) {
            ps.setInt(1, billingId);
            return toList(ps, rs ->
                            new KmPrice(rs.getInt("km_price_from"), rs.getInt("km_price_eurocents"))
            );
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get km prices", ex);
        }
    }

    @Override
    public KmPriceDetails getKmPriceDetails(int billingId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT km_price_from, km_price_eurocents FROM km_price WHERE km_price_billing_id = ? " +
                        "ORDER BY km_price_from"
        )) {
            ps.setInt(1, billingId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> froms = new ArrayList<>();
                List<Integer> prices = new ArrayList<>();
                while (rs.next()) {
                    prices.add(rs.getInt("km_price_eurocents"));
                    froms.add(rs.getInt("km_price_from"));
                }
                return new KmPriceDetails(
                        Ints.toArray(froms),
                        Ints.toArray(prices)
                );
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get km prices", ex);
        }
    }

    @Override
    public BillingDetailsUser getUserDetails(int billingId, int userId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT bu_seq_nr FROM b_user WHERE bu_billing_id = ? AND bu_user_id = ?"
        )) {
            ps.setInt(1, billingId);
            ps.setInt(2, userId);
            return toSingleObject(ps, rs -> new BillingDetailsUser(
                    userId, rs.getInt("bu_seq_nr")
            )); // TODO
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get user details", ex);
        }
    }

    @Override
    public Iterable<BillingDetailsTrip> listTripDetails(int billingId, int userId, boolean privileged) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT bt_reservation_id, bt_car_name, bt_km, bt_km_cost, bt_datetime FROM b_trip " +
                        "WHERE bt_billing_id = ? AND bt_user_id = ? AND bt_privileged = ? AND bt_km > 0 " +
                        "ORDER BY bt_datetime")) {
            ps.setInt(1, billingId);
            ps.setInt(2, userId);
            ps.setBoolean(3, privileged);
            return toList(ps, rs -> new BillingDetailsTrip(
                    rs.getInt("bt_reservation_id"),
                    rs.getString("bt_car_name"),
                    rs.getInt("bt_km"),
                    rs.getInt("bt_km_cost"),
                    rs.getTimestamp("bt_datetime").toLocalDateTime()
            ));
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get trip details", ex);
        }
    }

    @Override
    public Iterable<BillingDetailsFuel> listFuelDetails(int billingId, int userId, boolean privileged) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT bf_reservation_id, bf_refuel_id, bf_car_name, bf_fuel_cost, bf_datetime FROM b_fuel " +
                        "WHERE bf_billing_id = ? AND bf_user_id = ? AND bf_privileged = ? " +
                        "ORDER BY bf_datetime"
        )) {
            ps.setInt(1, billingId);
            ps.setInt(2, userId);
            ps.setBoolean(3, privileged);
            return toList(ps, rs -> new BillingDetailsFuel(
                    rs.getInt("bf_reservation_id"),
                    rs.getInt("bf_refuel_id"),
                    rs.getString("bf_car_name"),
                    rs.getInt("bf_fuel_cost"),
                    rs.getTimestamp("bf_datetime").toLocalDateTime()

            ));
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get fuel details", ex);
        }
    }

    @Override
    public Iterable<BillingDetailsOwner> listOwnerDetails(int billingId, int carId) {
        // TODO: combine these into fewer SQL statements? Or at least maek a view?

        // get names and ids of owners and cars
        Iterable<UserHeader> users;
        try (PreparedStatement ps = prepareStatement(
                "SELECT u, user_firstname, user_lastname " +
                        "FROM users JOIN (" +
                        "SELECT car_privilege_user_id as u from carprivileges WHERE car_privilege_car_id=? " +
                        "UNION " +
                        "SELECT car_owner_user_id as u From cars WHERE car_id=?) AS t " +
                        "ON user_id=u"
        )) {
            ps.setInt(1, carId);
            ps.setInt(2, carId);
            users = toList(ps, rs -> new UserHeader(
                    rs.getInt("u"),
                    null,
                    rs.getString("user_firstname"),
                    rs.getString("user_lastname"),
                    null, null, null, null
            ));
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get fuel details", ex);
        }

        List<BillingDetailsOwner> result = new ArrayList<>();
        for (UserHeader user : users) {
            result.add(new BillingDetailsOwner(
                    user.getFullName(),
                    listTripDetails(billingId, user.getId(), true),
                    listFuelDetails(billingId, user.getId(), true)
            ));
        }
        return result;
    }

    @Override
    public BillingDetailsCar getCarDetails(int billingId, int carId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT bc_first_km, bc_last_km,  bc_owner_km, bc_deprec_km, bc_fuel_total, " +
                        "bc_fuel_owner, bc_fuel_due, bc_deprec_recup, car_deprec, bc_seq_nr," +
                        "bc_costs, bc_costs_recup " +
                    "FROM b_cars JOIN cars ON bc_car_id = car_id " +
                    "WHERE bc_billing_id = ? AND bc_car_id = ?"
        )) {
            ps.setInt(1, billingId);
            ps.setInt(2, carId);
            return toSingleObject(ps, rs ->
                            new BillingDetailsCar(
                                    rs.getInt("bc_first_km"), rs.getInt("bc_last_km"),
                                    rs.getInt("bc_owner_km"), rs.getInt("bc_deprec_km"),
                                    rs.getInt("bc_fuel_total"), rs.getInt("bc_fuel_owner"),
                                    rs.getInt("car_deprec"), rs.getInt("bc_deprec_recup"),
                                    rs.getInt("bc_fuel_due"), rs.getInt("bc_seq_nr"),
                                    rs.getInt("bc_costs"), rs.getInt("bc_costs_recup")
                            )
            );
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retreive car billing details", ex);
        }

    }
}
