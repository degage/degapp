
package controllers;

import be.ugent.degage.db.models.*;
import controllers.util.PickerLine;
import controllers.util.InvoicePickerLine;
import db.DataAccess;
import db.InjectContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.Collection;

public class InvoicePicker extends Controller {

    private static final int MAX_VISIBLE_RESULTS = 10;

    @AllowRoles
    @InjectContext
    public static Result getList(String search) {
        if (search.isEmpty()) {
            return ok(); // normally does not occur
        } else {
            Collection<InvoicePickerLine> lines = new ArrayList<>();
            for (Invoice invoice : DataAccess.getInjectedContext().getInvoiceDAO().listInvoiceByNumber(search, MAX_VISIBLE_RESULTS)) {
                UserHeader user = DataAccess.getInjectedContext().getUserDAO().getUserByInvoice(invoice);
                lines.add (new InvoicePickerLine(invoice.getNumber(),search, invoice.getId(), invoice.getAmount(), user));
            }
            return ok(views.html.picker.invoicepickerlines.render(lines));
        }
    }
}
