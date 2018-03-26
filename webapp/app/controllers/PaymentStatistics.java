package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.PaymentStatisticsDAO;
import be.ugent.degage.db.dao.BillingDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.DataAccessException;
import com.google.common.primitives.Ints;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.payments.*;
import controllers.util.Pagination;
import play.data.validation.Constraints;
import java.time.LocalDate;
import java.text.DateFormat;
import java.util.Date;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.*;
import play.data.Form;
import controllers.util.FileHelper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import db.CurrentUser;
import java.lang.Math;

public class PaymentStatistics extends Controller {


    private static double roundTwoDecimals(double num) {
        return (double) Math.round(num*100)/100; //trick to round to 2 decimals
    }


    @AllowRoles
    @InjectContext
    public static Result getStats() {
        DataAccessContext context = DataAccess.getInjectedContext();
        PaymentStatisticsDAO dao = context.getPaymentStatisticsDAO();
        Iterable<Billing> billings = DataAccess.getInjectedContext().getBillingDAO().listAllBillings();
        List<PaymentStatistic> res = new ArrayList<>();

        for (Billing billing : billings) {
            int id = billing.getId();
            res.add(new PaymentStatistic.Builder(id)
                    .billingDescription(billing.getDescription())
                    .percentagePaid(roundTwoDecimals(dao.getPercentagePaid(id)))
                    .percentageOpen(roundTwoDecimals(dao.getPercentageOpen(id)))
                    .percentageOverdue(roundTwoDecimals(dao.getPercentageOverdue(id)))
                    .amountToReceive(roundTwoDecimals(dao.getAmountToReceive(id)))
                    .amountReceived(roundTwoDecimals(dao.getAmountReceived(id)))
                    .amountToPay(roundTwoDecimals(dao.getAmountToPay(id)))
                    .amountPaid(roundTwoDecimals(dao.getAmountPaid(id)))
                    .averageTimeToPay(roundTwoDecimals(dao.getAverageTimeToPay(id)))
                    .build()
            );
        }

        return ok(paymentStatistics.render(res));
    }

    @AllowRoles
    @InjectContext
    public static Result getUserStats() {
        DataAccessContext context = DataAccess.getInjectedContext();
        PaymentStatisticsDAO dao = context.getPaymentStatisticsDAO();
        Iterable<PaymentUserStatistic> res = dao.getStatsPerUser();

        return ok(paymentUserStatistics.render(res));
    }


    /**
     * @param page         The page in the paymentlist
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string with form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of payments of the corresponding page
     */
    @AllowRoles
    @InjectContext
    public static Result showUserStatsPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {

        return ok(views.html.payments.paymentUserStatisticsPage.render(
                DataAccess.getInjectedContext().getPaymentStatisticsDAO().listUserStats(
                        FilterField.stringToField(orderBy, FilterField.NUMBER),
                        Pagination.parseBoolean(ascInt),
                        page, pageSize,
                        searchString
                )
        ));
    }

}
