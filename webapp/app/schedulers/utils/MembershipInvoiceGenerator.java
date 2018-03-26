package schedulers.utils;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.models.InvoiceAndUser;
import be.ugent.degage.db.models.InvoiceType;
import views.html.billing.*;
import play.twirl.api.Html;

import it.innove.play.pdf.PdfGenerator;


/**
 * TODO: TEST THIS CLASS 
 */
public class MembershipInvoiceGenerator  extends ReportGenerator{


    @Override
    public Html getHtmlTemplate (DataAccessContext context, int invoiceId)throws IllegalArgumentException{
        InvoiceDAO invoiceDAO = context.getInvoiceDAO(); 
        InvoiceAndUser invoiceAndUser =  invoiceDAO.getInvoiceAndUser(invoiceId);

        if(invoiceAndUser == null){
            throw new IllegalArgumentException("Invoice cannot be found for membership invoice generation. " + invoiceId);
        };
        return carMembershipInvoice.render(invoiceAndUser);
    }

}