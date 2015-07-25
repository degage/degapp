package controllers;

import be.ugent.degage.db.dao.CheckDAO;
import be.ugent.degage.db.models.Billing;
import be.ugent.degage.db.models.BillingStatus;
import be.ugent.degage.db.models.UserRole;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.billingadm.anomalies;
import views.html.billingadm.listAll;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

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

    private static boolean allArchived(Iterable<Billing> list) {
        for (Billing billing : list) {
            if (billing.getStatus() != BillingStatus.ARCHIVED) {
                return false;
            }
        }
        return true;
    }

    public static class Data {
        @Constraints.Required
        public String description;

        @Constraints.Required
        public String prefix;

        @Constraints.Required
        public LocalDate from;

        @Constraints.Required
        public LocalDate until;

        public List<ValidationError> validate() {
            // TODO: avoid copy from e.g. ReservationData
            if (from.isBefore(until)) {
                return null;
            } else {
                return Collections.singletonList(
                        new ValidationError("until", "Het einde van de periode moet na het begin van de periode liggen")
                );
            }
        }
    }

    /**
     * Produce a list of all billings for administration purposes.
     */
    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result listAll() {
        Iterable<Billing> list = DataAccess.getInjectedContext().getBillingDAO().listAllBillings();
        Form<Data> form = null;
        if (allArchived(list)) {
            form = Form.form(Data.class);
        }
        return ok(listAll.render(list, form));
    }

    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result archive(int billingId) {
        DataAccess.getInjectedContext().getBillingAdmDAO().archive(billingId);
        return redirect(routes.BillingsAdmin.listAll());
    }

    @InjectContext
    @AllowRoles(UserRole.SUPER_USER)
    public static Result doCreate() {
        Form<Data> form = Form.form(Data.class).bindFromRequest();
        if (form.hasErrors()) {
            Iterable<Billing> list = DataAccess.getInjectedContext().getBillingDAO().listAllBillings();
            return badRequest(listAll.render(list, form));
        } else {
            Data data = form.get();
            DataAccess.getInjectedContext().getBillingAdmDAO().createBilling(
                    data.description,
                    data.prefix,
                    data.from,
                    data.until.plusDays(1)
            );
            return redirect(routes.BillingsAdmin.listAll());
        }
    }
}
