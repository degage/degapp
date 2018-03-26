package controllers.api;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.Payment;
import be.ugent.degage.db.models.PaymentAndUser;
import be.ugent.degage.db.models.Billing;
import be.ugent.degage.db.models.PaymentUserStatistic;
import be.ugent.degage.db.models.PaymentStatistic;
import be.ugent.degage.db.models.Page;
import be.ugent.degage.db.models.UserRole;
import be.ugent.degage.db.dao.PaymentStatisticsDAO;
import com.google.common.base.Strings;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.cars.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import controllers.AllowRoles;
import controllers.util.Pagination;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import play.libs.Json;

public class ApiPaymentStatistics extends Controller {

  private static double roundTwoDecimals(double num) {
      return (double) Math.round(num*100)/100; //trick to round to 2 decimals
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getUserStats(int page, int pageSize, int ascInt, String orderBy, String searchString) {
    Page<PaymentUserStatistic> stats = DataAccess.getInjectedContext().getPaymentStatisticsDAO().listUserStats(
            FilterField.stringToField(orderBy, FilterField.NUMBER),
            Pagination.parseBoolean(ascInt),
            page, pageSize,
            searchString
    );
    return ok(GsonHelper.getGson().toJson(stats));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getPaymentStatsByUser(int userId) {
    return ok(GsonHelper.getGson().toJson(DataAccess.getInjectedContext().getPaymentStatisticsDAO().getPaymentStatsByUser(userId)));
  }

  @InjectContext
  @AllowRoles({UserRole.INVOICE_ADMIN})
  public static Result getQuarterStats() {
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

      return ok(GsonHelper.getGson().toJson(res));
  }
}
