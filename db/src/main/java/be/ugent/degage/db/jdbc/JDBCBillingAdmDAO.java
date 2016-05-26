/* JDBCBillingAdmDAO.java
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
import be.ugent.degage.db.dao.BillingAdmDAO;
import be.ugent.degage.db.models.BillingDetailsUser;
import be.ugent.degage.db.models.BillingDetailsUserKm;
import be.ugent.degage.db.models.KmPrice;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BillingAdmDAO}
 */
class JDBCBillingAdmDAO extends AbstractDAO implements BillingAdmDAO {

    JDBCBillingAdmDAO(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public void archive(int billingId) {
        try (CallableStatement cs = prepareCall("{call billing_archive(?)}")) {
            cs.setInt(1, billingId);
            cs.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not archive billing", e);
        }
    }

    @Override
    public void computeSimulation(int billingId) {
        try (CallableStatement cs = prepareCall("{call billing_simulation(?)}")) {
            cs.setInt(1, billingId);
            cs.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not start simulation", e);
        }
    }

    @Override
    public void computeUserInvoices(int billingId) {
        try (CallableStatement cs = prepareCall("{call billing_user_finalize(?)}")) {
            cs.setInt(1, billingId);
            cs.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not finalize user invoices", e);
        }
    }

    @Override
    public void computeCarInvoices(int billingId) {
        try (CallableStatement cs = prepareCall("{call billing_car_finalize(?)}")) {
            cs.setInt(1, billingId);
            cs.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not finalize car invoices", e);
        }
    }

    @Override
    public void createBilling(String description, String prefix, LocalDate start, LocalDate limit) {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO billing(billing_description,billing_prefix,billing_start,billing_limit) VALUES (?,?,?,?)"
        )) {
            ps.setString(1, description);
            ps.setString(2, prefix);
            ps.setDate(3, Date.valueOf(start));
            ps.setDate(4, Date.valueOf(limit));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not create billing", e);
        }
    }

    @Override
    public List<CarInfo> listCarBillingInfo(int billingId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT car_id, car_name, included, " +
                        "(car_deprec IS NULL OR car_deprec = 0 OR car_deprec_limit IS NULL OR car_deprec_limit = 0 " +
                        " OR car_deprec_last IS NULL OR car_deprec = 0) AS incomplete, d " +
                        "FROM cars_billed " +
                        "LEFT JOIN ( SELECT reservation_car_id AS id, 1 as d FROM trips,billing " +
                        "WHERE reservation_status > 5 AND reservation_from < billing_limit AND billing_id = ? " + //[ENUM INDEX]
                "UNION " +
                "SELECT reservation_car_id AS id, 1 as d FROM refuels " +
                        "JOIN reservations ON reservation_id = refuel_car_ride_id " +
                        "JOIN billing " +
                        " WHERE refuel_status != 'REFUSED' AND NOT refuel_archived AND reservation_from < billing_limit AND billing_id = ? " +
                        ") AS tmp ON car_id=id " +
                        "JOIN cars USING(car_id) WHERE billing_id = ? ORDER BY car_name"
        )) {
            ps.setInt(1, billingId);
            ps.setInt(2, billingId);
            ps.setInt(3, billingId);
            //System.err.println(ps);
            return toList(ps, rs -> {
                CarInfo info = new CarInfo();
                info.carId = rs.getInt("car_id");
                info.carName = rs.getString("car_name");
                info.incomplete = rs.getBoolean("incomplete");
                info.included = rs.getBoolean("included");
                info.nodata = rs.getObject("d") == null;
                return info;
            });
        } catch (SQLException e) {
            throw new DataAccessException("Could not create billing", e);
        }
    }

    @Override
    public void updateCarsBilled(int billingId, Iterable<Integer> carsToInclude) {
        // clear all
        try (PreparedStatement ps = prepareStatement(
                "UPDATE cars_billed SET included=FALSE WHERE billing_id = ?"
        )) {
            ps.setInt(1, billingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not reset cars_billed", e);
        }

        // include indicated cars
        try (PreparedStatement ps = prepareStatement(
                "UPDATE cars_billed SET included=TRUE WHERE billing_id = ? AND car_id = ?"
        )) {
            // batch update
            for (Integer carId : carsToInclude) {
                ps.setInt(1, billingId);
                ps.setInt(2, carId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Could not set cars_billed", e);
        }
    }

    @Override
    public void updateToPreparing(int billingId) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE billing SET billing_status='PREPARING' " +
                        "WHERE billing_status='CREATED' AND billing_id = ?"
        )) {
            ps.setInt(1, billingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not change billing status", e);
        }
    }

    @Override
    public void updatePricing(int billingId, Iterable<KmPrice> pricing) {
        // first delete original pricing
        try (PreparedStatement ps = prepareStatement(
                "DELETE FROM km_price WHERE km_price_billing_id = ?"
        )) {
            ps.setInt(1, billingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not remove original pricing", e);
        }
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO km_price(km_price_billing_id,km_price_from,km_price_eurocents,km_price_factor) " +
                        "VALUES (?,?,?,?)"
        )) {
            for (KmPrice price : pricing) {
                ps.setInt(1, billingId);
                ps.setInt(2, price.getFromKm());
                ps.setInt(3, price.getEurocents());
                ps.setInt(4, price.getFactor());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update pricing", e);
        }
    }

    @Override
    public Iterable<CarBillingInfo> listCarBillingOverview(int billingId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT car_id, name, fuel, deprec, costs, total, sc FROM b_car_overview WHERE billing_id = ? ORDER BY car_id"
        )) {
            ps.setInt(1, billingId);
            return toList(ps, rs -> {
                CarBillingInfo cbi = new CarBillingInfo();
                cbi.carId = rs.getInt("car_id");
                cbi.carName = rs.getString("name");
                cbi.fuel = rs.getInt("fuel");
                cbi.deprec = rs.getInt("deprec");
                cbi.costs = rs.getInt("costs");
                cbi.total = rs.getInt("total");
                cbi.structuredComment = rs.getString("sc");
                return cbi;
            });
        } catch (SQLException e) {
            throw new DataAccessException("Could not list billing", e);
        }
    }

    @Override
    public List<UserBillingInfo> listUserBillingOverview(int billingId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT user_id, name, km, fuel, total, sc, seq_nr FROM b_user_overview WHERE billing_id = ? ORDER BY user_id"
        )) {
            ps.setInt(1, billingId);
            return toList(ps, rs -> {
                UserBillingInfo ubi = new UserBillingInfo();
                ubi.userId = rs.getInt("user_id");
                ubi.userName = rs.getString("name");
                ubi.km = rs.getInt("km");
                ubi.fuel = rs.getInt("fuel");
                ubi.total = rs.getInt("total");
                ubi.structuredComment = rs.getString("sc");
                ubi.seqNr = rs.getInt("seq_nr");
                return ubi;
            });
        } catch (SQLException e) {
            throw new DataAccessException("Could not list billing", e);
        }
    }

    private static void setKilometers(BillingDetailsUserKm bdu, List<Integer> list) {
        if (bdu == null) {
            return;
        }
        int size = list.size();
        int[] tab = new int[size];
        bdu.setTotalKilometers(list.get(0));
        for (int i = 1; i < size; i++) {
            tab[i - 1] = list.get(i - 1) - list.get(i);
        }
        tab[size - 1] = list.get(size - 1);
        bdu.setKilometersInRange(tab);
    }

    // TODO: combine getUserKmDetails and listUserBillingOverview into a single call
    // so that no merging needs to be done later. Might be difficult, because some
    // users have no kilometers and only fuel

    /**
     * Retreive all user information for the given billing
     */
    public List<BillingDetailsUserKm> getUserKmDetails(int billingId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT bt_user_id, sum_of_excess_kms " +
                        "FROM b_user_km WHERE bt_billing_id = ? ORDER BY bt_user_id ASC, km_price_from ASC"
        )) {
            ps.setInt(1, billingId);
            List<BillingDetailsUserKm> result = new ArrayList<>();
            int userId = 0;
            BillingDetailsUserKm bduk = null;
            List<Integer> kmList = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int newUserId = rs.getInt("bt_user_id");
                    // start a new user?
                    if (bduk == null || newUserId != userId) {
                        // finalize old record
                        setKilometers(bduk, kmList);
                        // start a new record
                        userId = newUserId;
                        bduk = new BillingDetailsUserKm(userId);
                        result.add(bduk);

                        kmList.clear();
                    }
                    kmList.add(rs.getInt("sum_of_excess_kms"));
                }
                setKilometers(bduk, kmList);
                return result;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get user details", ex);
        }
    }


}
