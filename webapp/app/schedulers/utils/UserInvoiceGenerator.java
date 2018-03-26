package schedulers.utils;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.BillingDAO;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.models.Billing;
import be.ugent.degage.db.models.BillingDetailsUser;
import be.ugent.degage.db.models.Invoice;
import be.ugent.degage.db.models.InvoiceAndUser;
import be.ugent.degage.db.models.KmPriceDetails;
import controllers.Billings;
import controllers.Billings.InvoiceLine;
import it.innove.play.pdf.PdfGenerator;
import views.html.billing.userInvoiceFinal;

import play.twirl.api.Html;

/**
 * TODO: TEST THIS CLASS 
 */
public class UserInvoiceGenerator extends ReportGenerator {



    /**
     * TODO: CHECK BILLING STATUS AND CREATE TEMPLATE DEPENDING ON THE STATUS. 
     * (extend this class)
     */
    @Override
    public Html getHtmlTemplate (DataAccessContext context, int invoiceId)throws IllegalArgumentException {
        InvoiceDAO invoiceDAO = context.getInvoiceDAO();
        BillingDAO billingDAO = context.getBillingDAO();
        InvoiceAndUser invoiceAndUser = invoiceDAO.getInvoiceAndUser(invoiceId);
        Billing billing =  billingDAO.getBilling(invoiceAndUser.getInvoice().getBillingId());
        int billingId = billing.getId();
        int userId = invoiceAndUser.getUser().getId();
        BillingDetailsUser bUser = billingDAO.getUserDetails(billingId, userId);

        if(bUser == null){
            throw new IllegalArgumentException("Invalid invoice id for user detail report generation."); 
        }

        KmPriceDetails priceDetails = billingDAO.getKmPriceDetails(billingId);
        Iterable<InvoiceLine> invoiceLines = 
                Billings.getInvoiceLines(
                    billingDAO.listTripDetails(billingId, userId, false),
                    billingDAO.listFuelDetails(billingId, userId, false),
                    priceDetails
                );
        
        String billNr = Billings.getUserBillNr(billing.getPrefix(), bUser.getIndex());
        
        return userInvoiceFinal.render(
                        billing,
                        billNr,
                        context.getUserDAO().getUser(userId),
                        new Billings.KmPriceInfo(priceDetails),
                        invoiceLines,
                        Billings.total(invoiceLines, priceDetails.getFroms().length),
                        Billings.structuredComment(billingId, 0, bUser));
    
    }


    

}