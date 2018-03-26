package schedulers.utils;

import java.util.HashMap;
import java.util.Map;

import be.ugent.degage.db.models.InvoiceType;
/**
 * TODO: TEST THIS CLASS 
 */
public class ReportGeneratorFactory{
    

    private static final  Map<InvoiceType,ReportGenerator> generatorTypeMap = createMap(); 

    private static Map<InvoiceType,ReportGenerator> createMap () { 

        Map<InvoiceType, ReportGenerator> map = new HashMap<>(); 
        map.put(InvoiceType.CAR_MEMBERSHIP, new MembershipInvoiceGenerator());
        map.put(InvoiceType.CAR_OWNER, new CarInvoiceGenerator());
        map.put(InvoiceType.CAR_USER, new UserInvoiceGenerator());
        return map; 
    }
    public static ReportGenerator getGenerator(InvoiceType type){
        if(type == null){
            throw new IllegalArgumentException("Given invoice type for report generator is null.");
        }
        
        return generatorTypeMap.get(type);
    }
}