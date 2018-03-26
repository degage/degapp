package schedulers.joblessprocesses;
import java.nio.channels.AcceptPendingException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.dao.MembershipDAO;

import be.ugent.degage.db.models.InvoiceType;
import be.ugent.degage.db.models.Invoice;
import be.ugent.degage.db.models.InvoiceAndUser;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserStatus;
import db.RunnableInContext;
import be.ugent.degage.db.models.Job;


import java.time.LocalDateTime;
import notifiers.Notifier;
import play.Logger;

/**
 * Updates the membership status of all users who have paid both deposit and fee.  
 */
public class MembershipUpdateJob extends JoblessProcess {

    private static final int deposit; 
    private static final int fee; 
    private static final Set<UserStatus> acceptSet = new HashSet<>();

    static{
        deposit = 75;
        fee = 35;
        acceptSet.add(UserStatus.FULL);
        acceptSet.add(UserStatus.FULL_VALIDATING);
        acceptSet.add(UserStatus.REGISTERED);
    }

    public MembershipUpdateJob(){
        super("Membership update for deposit and fee" , JoblessProcessType.MEMBERSHIP_UPDATE.toString()); 
    }
    @Override
    public void runInContext(DataAccessContext context){
        updateMemberships(context);
    }

    /**
     * This method will find all the invoices with the type  CAR_MEMBERSHIP and 
     * checks if it has been paid.
     * 
     * The users will be updated accordingly if they have paid for the invoices.
     */
    private static void updateMemberships (DataAccessContext context){
        InvoiceDAO invoiceDAO =  context.getInvoiceDAO();
        MembershipDAO memDAO =  context.getMembershipDAO(); 

        Iterable<InvoiceAndUser> membershipInvoices =  invoiceDAO.listInvoicesAndUsersOnType(InvoiceType.CAR_MEMBERSHIP, MembershipUpdateJob.acceptSet);
        System.out.println(LocalDateTime.now() + ": updateMemberships");

        for(InvoiceAndUser inUser : membershipInvoices){
            Invoice invoice = inUser.getInvoice(); 
            User user = inUser.getUser();
            LocalDate paymentDate =  invoice.getPaymentDate();

            if (paymentDate != null && !acceptSet.contains(user.getStatus())) {
                memDAO.updateUserMembership(user.getId(), deposit, fee);
            } else {
                memDAO.updateUserMembership(user.getId(), 0, 0);
            }
        }
    }

    
}