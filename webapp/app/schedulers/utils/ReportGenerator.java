package schedulers.utils;


import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.models.InvoiceType;
import it.innove.play.pdf.PdfGenerator;
import play.twirl.api.Html;
import play.Play;

/**
 * Report generation based on invoice type.
 * 
 ***************    IMPORTANT   *****************
 * GENERATOR DOES EXACTLY THE SAME THING AS SOME OF THE FUNCTIONS IN BILLINGS
 * BUT BILLINGS GENERATES THE PDF AND WRAPS IT IN A RESULT IN 
 * ONE METHOD MAKING IT VERY INFLEXIBLE!!! 
 * 
 * USE GENERATORS IN THE FUTURE FOR HTML TEMPLATE GENERATION OF REPORTS WHICH COULD 
 * LATER BE CONVERTED TO PDF
 * 
 * YOU CAN ALSO DIRECTLY GET PDF CONVERTED HTML TEMPLATES IN RAW BYTE ARRAY.
 * 
 */


/**
 * TODO: TEST THIS CLASS 
 */
public abstract class ReportGenerator {
   
    /**
     * Generates an invoice pdf from the given data context of the given invoice.
     */
    public byte[] generate(DataAccessContext context, int invoiceId){
        Html html = getHtmlTemplate(context, invoiceId);
        if(Play.isDev()){
            System.err.println(html);

            
        }
        return PdfGenerator.toBytes(html, null);
    }


    public abstract Html getHtmlTemplate (DataAccessContext context, int invoiceId)throws IllegalArgumentException; 


}