package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;
import java.sql.SQLException;

/**
 * Used for getting stats about payments, invoices, ...
 */
public interface PaymentStatisticsDAO {


    /**
     * Returns the percentage of invoices that is paid
     */
    public double getPercentagePaid(int billingId) throws DataAccessException;

    /**
     * Returns the percentage of invoices that is overdue
     */
    public double getPercentageOverdue(int billingId) throws DataAccessException;

    /**
     * Returns the percentage of invoices that is open
     */
    public double getPercentageOpen(int billingId) throws DataAccessException;

    /**
     * Returns the sum of all paid invoices
     */
    public double getAmountReceived(int billingId) throws DataAccessException;

    /**
     * Returns the sum of all unpaid invoices
     */
    public double getAmountToReceive(int billingId) throws DataAccessException;

    /**
     * Returns the sum of all paid invoices by degage
     */
    public double getAmountPaid(int billingId) throws DataAccessException;

    /**
     * Returns the sum of all unpaid invoices by degage
     */
    public double getAmountToPay(int billingId) throws DataAccessException;

    /**
     * Returns the average days to pay an invoice
     */
    public double getAverageTimeToPay(int billingId) throws DataAccessException;

    /**
     * Returns the total amount of invoices per user
     */
    public Iterable<PaymentUserStatistic> getStatsPerUser() throws DataAccessException;


    public Page<PaymentUserStatistic> listUserStats(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException;

    public PaymentUserStatistic getPaymentStatsByUser(int userId) throws DataAccessException;

}
