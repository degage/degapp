package be.ugent.degage.db.jdbc;


import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.*;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Cedric on 2/16/14.
 */
public class JDBCDataAccessContext implements DataAccessContext {

    private Connection connection;
    private UserDAO userDAO;
    private InfoSessionDAO infoSessionDAO;
    private AddressDAO addressDAO;
    private ReservationDAO reservationDAO;
    private CarDAO carDAO;
    private CarCostDAO carCostDAO;
    private UserRoleDAO userRoleDAO;
    private TemplateDAO templateDAO;
    private CarRideDAO carRideDAO;
    private DamageDAO damageDAO;
    private DamageLogDAO damageLogDAO;
    private ReceiptDAO receiptDAO;
    private RefuelDAO refuelDAO;
    private SchedulerDAO schedulerDAO;
    private NotificationDAO notificationDAO;
    private MessageDAO messageDAO;
    private ApprovalDAO approvalDAO;
    private FileDAO fileDAO;
    private SettingDAO settingDAO;
    private JobDAO jobDAO;
    
    public JDBCDataAccessContext(Connection connection) {
        this.connection = connection;
        try {
            this.connection.setAutoCommit(false); //we don't want to commit ourselves
        }catch(SQLException ex){
            //TODO: log?
        }
    }

    @Override
    public UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new JDBCUserDAO(connection);
        }
        return userDAO;
    }

    @Override
    public InfoSessionDAO getInfoSessionDAO() {
        if(infoSessionDAO == null){
            infoSessionDAO = new JDBCInfoSessionDAO(connection);
        }
        return infoSessionDAO;
    }

    @Override
    public TemplateDAO getTemplateDAO() {
        if(templateDAO == null){
            templateDAO = new JDBCTemplateDAO(connection);
        }
        return templateDAO;
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        if(notificationDAO == null){
            notificationDAO = new JDBCNotificationDAO(connection);
        }
        return notificationDAO;
    }

    @Override
    public MessageDAO getMessageDAO() {
        if(messageDAO == null){
            messageDAO = new JDBCMessageDAO(connection);
        }
        return messageDAO;
    }

    @Override
    public AddressDAO getAddressDAO() {
        if(addressDAO == null){
            addressDAO = new JDBCAddressDAO(connection);
        }
        return addressDAO;
    }

    @Override
    public void begin() {
        //TODO What happens here?
    }

    @Override
    public void commit() {
        try {
            connection.commit();
        } catch (SQLException ex) {
            //TODO ??
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
        } catch(SQLException ex){
            //TODO ??
        }
    }

    @Override
    public void close() {
        try {
            connection.commit(); //finalize commit if necessary
            connection.close();
        } catch(SQLException ex){
            //TODO ??
        }
    }

	@Override
	public CarDAO getCarDAO() {
		if(carDAO == null){
            carDAO = new JDBCCarDAO(connection);
        }
        return carDAO;
	}

    @Override
    public CarCostDAO getCarCostDAO() {
        if(carCostDAO == null){
            carCostDAO = new JDBCCarCostDAO(connection);
        }
        return carCostDAO;
    }

	@Override
	public ReservationDAO getReservationDAO() {
		if(reservationDAO == null){
            reservationDAO = new JDBCReservationDAO(connection);
        }
        return reservationDAO;
	}

    @Override
    public DamageDAO getDamageDAO() {
        if(damageDAO == null){
            damageDAO = new JDBCDamageDAO(connection);
        }
        return damageDAO;
    }

    @Override
    public DamageLogDAO getDamageLogDAO() {
        if(damageLogDAO == null){
            damageLogDAO = new JDBCDamageLogDAO(connection);
        }
        return damageLogDAO;
    }

    @Override
	public UserRoleDAO getUserRoleDAO() {
		if(userRoleDAO == null){
			userRoleDAO = new JDBCUserRoleDAO(connection);
        }
        return userRoleDAO;
	}

    @Override
    public CarRideDAO getCarRideDAO() {
        if(carRideDAO == null){
            carRideDAO = new JDBCCarRideDAO(connection);
        }
        return carRideDAO;
    }

    @Override
    public RefuelDAO getRefuelDAO() {
        if(refuelDAO == null){
            refuelDAO = new JDBCRefuelDAO(connection);
        }
        return refuelDAO;
    }

    @Override
    public SchedulerDAO getSchedulerDAO() {
        if(schedulerDAO == null){
            schedulerDAO = new JDBCSchedulerDAO(connection);
        }
        return schedulerDAO;
    }

    @Override
    public ApprovalDAO getApprovalDAO() {
        if(approvalDAO == null){
            approvalDAO = new JDBCApprovalDAO(connection);
        }
        return approvalDAO;
    }

    @Override
    public JobDAO getJobDAO() {
        if(jobDAO == null){
            jobDAO = new JDBCJobDAO(connection);
        }
        return jobDAO;
    }

    @Override
    public FileDAO getFileDAO() {
        if(fileDAO == null){
            fileDAO = new JDBCFileDAO(connection);
        }
        return fileDAO;
    }

    @Override
    public SettingDAO getSettingDAO() {
        if(settingDAO == null){
            settingDAO = new JDBCSettingDAO(connection);
        }
        return settingDAO;
    }

    @Override
    public ReceiptDAO getReceiptDAO() {
        if(receiptDAO == null){
            receiptDAO = new JDBCReceiptDAO(connection);
        }
        return receiptDAO;
    }
}
