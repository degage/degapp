package controllers;

import be.ugent.degage.db.dao.CheckDAO;
import be.ugent.degage.db.models.Billing;
import be.ugent.degage.db.models.BillingStatus;
import be.ugent.degage.db.models.UserRole;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.billingadm.anomalies;
import views.html.billingadm.listAll;

/**
 * Actions related to billing (management).
 */
public class BillingsAdmin extends Controller {

    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result showAnomalies(int billingId, int carId) {
        Iterable<CheckDAO.TripAnomaly> list = DataAccess.getInjectedContext().getCheckDAO().getTripAnomalies(billingId, carId);
        return ok(anomalies.render(list));
    }

    private static boolean allArchived (Iterable<Billing> list) {
        for (Billing billing : list) {
            if (billing.getStatus() != BillingStatus.ARCHIVED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Produce a list of all billings for administration purposes.
     */
    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result listAll() {
        Iterable<Billing> list = DataAccess.getInjectedContext().getBillingDAO().listAllBillings();
        return ok(listAll.render(list, allArchived(list)));
    }

    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result archive(int billingId) {
        DataAccess.getInjectedContext().getBillingAdmDAO().archive(billingId);
        return redirect(routes.BillingsAdmin.listAll());
    }
}
