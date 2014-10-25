package be.ugent.degage.db;

import be.ugent.degage.db.dao.*;

/**
 * Created by Cedric on 2/16/14.
 */
public interface DataAccessContext extends AutoCloseable {
    public UserDAO getUserDAO();

    public InfoSessionDAO getInfoSessionDAO();

    public TemplateDAO getTemplateDAO();

    public NotificationDAO getNotificationDAO();

    public MessageDAO getMessageDAO();

    public AddressDAO getAddressDAO();
    
    public CarDAO getCarDAO();

    public CarCostDAO getCarCostDAO();

    public ReceiptDAO getReceiptDAO();

    public RefuelDAO getRefuelDAO();

    public SchedulerDAO getSchedulerDAO();
    
    public ReservationDAO getReservationDAO();

    public DamageDAO getDamageDAO();

    public DamageLogDAO getDamageLogDAO();
    
    public UserRoleDAO getUserRoleDAO();

    public CarRideDAO getCarRideDAO();

    public AvailabilityDAO getAvailabilityDAO();

    public ApprovalDAO getApprovalDAO();

    public JobDAO getJobDAO();

    public FileDAO getFileDAO();

    public SettingDAO getSettingDAO();

    public void begin();

    public void commit();

    public void rollback();

    public void close();
}
