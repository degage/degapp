package schedulers.utils;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.models.Billing;
import be.ugent.degage.db.models.CarHeaderShort;
import be.ugent.degage.db.models.Invoice;
import controllers.Billings;
import controllers.Billings.CarInvoiceDetails;
import it.innove.play.pdf.PdfGenerator;
import views.html.billing.carInvoice;

import play.twirl.api.Html;

/**
 * TODO: TEST THIS CLASS 
 */
public class CarInvoiceGenerator extends ReportGenerator{ 
    

    @Override
    public Html getHtmlTemplate(DataAccessContext context, int invoiceId) throws IllegalArgumentException{
        InvoiceDAO dao = context.getInvoiceDAO() ; 
        Invoice invoice = dao.getInvoice(invoiceId); 
        if(invoice ==  null ){
            throw new IllegalArgumentException("Invalid invoice id  for car invoice pdf generation. " +  invoiceId);
        }

        int billingId = invoice.getBillingId();
        int carId = invoice.getCarId(); 

        CarHeaderShort  car = context.getCarDAO().getCarHeaderShort(carId);
        Billing billing = context.getBillingDAO().getBilling(billingId); 
        CarInvoiceDetails carInvoiceDetails = Billings.getCarInvoiceDetails(car, billing);
        return carInvoice.render(
            carInvoiceDetails.getBilling(),
            carInvoiceDetails.getBillNr(),
            carInvoiceDetails.getCar(),
            carInvoiceDetails.getUser(),
            carInvoiceDetails.getTables(),
            carInvoiceDetails.getTableTotal(),
            carInvoiceDetails.getBCar(),
            carInvoiceDetails.getRemainingValue(),
            carInvoiceDetails.getStruct()
        );
    } 

}