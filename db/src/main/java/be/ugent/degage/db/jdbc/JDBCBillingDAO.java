package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.BillingDAO;
import be.ugent.degage.db.models.*;
import com.google.common.primitives.Ints;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BillingDAO}
 */
public class JDBCBillingDAO extends AbstractDAO implements BillingDAO {

    public JDBCBillingDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static Billing populateBilling(ResultSet rs) throws SQLException {
        return new Billing(
                rs.getInt("billing_id"),
                rs.getString("billing_description"),
                rs.getString("billing_prefix"),
                rs.getDate("billing_start").toLocalDate(),
                rs.getDate("billing_limit").toLocalDate(),
                BillingStatus.valueOf(rs.getString("billing_status"))
        );
    }

    @Override
    public Iterable<Billing> listAllBillings() {
        try (PreparedStatement ps = prepareStatement(
                "SELECT billing_id, billing_description, billing_prefix, billing_limit, billing_start, billing_status " +
                        "FROM billing ORDER BY billing_limit DESC"
        )) {
            return toList(ps, JDBCBillingDAO::populateBilling);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all billings", ex);
        }
    }

    @Override
    public Iterable<Billing> listBillingsForUser(int userId) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT billing_id, billing_description, billing_prefix, billing_limit, billing_start, billing_status " +
                        "FROM billing JOIN b_user ON bu_billing_id = billing_id " +
                        "WHERE bu_user_id = ? ORDER BY billing_limit DESC"
        )) {
            ps.setInt(1, userId);
            return toList(ps, JDBCBillingDAO::populateBilling);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list billings for user", ex);
        }
    }

    @Override
    public Billing getBilling(int id) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT billing_id, billing_description, billing_prefix, billing_limit, billing_start, billing_status " +
                        "FROM billing WHERE billing_id = ?"
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
            ps.setInt (1, billingId);
            ps.setInt (2, userId);
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
                        "WHERE bt_billing_id = ? AND bt_user_id = ? AND bt_privileged = ? " +
                        "ORDER BY bt_datetime")) {
            ps.setInt (1, billingId);
            ps.setInt (2, userId);
            ps.setBoolean(3, privileged);
            return toList (ps, rs -> new BillingDetailsTrip(
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
            ps.setInt (1, billingId);
            ps.setInt (2, userId);
            ps.setBoolean(3, privileged);
            return toList (ps, rs -> new BillingDetailsFuel(
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
}
