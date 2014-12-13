/* JDBCDataAccessContext.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.jdbc;


import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.*;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC implementation of {@link be.ugent.degage.db.DataAccessContext}
 */
class JDBCDataAccessContext implements DataAccessContext {

    private Connection connection;
    private UserDAO userDAO;
    private InfoSessionDAO infoSessionDAO;
    private AddressDAO addressDAO;
    private ReservationDAO reservationDAO;
    private CarDAO carDAO;
    private PrivilegedDAO privilegedDAO;
    private AvailabilityDAO availabilityDAO;
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
    }

    @Override
    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new DataAccessException("Auto commit could not be disabled", ex);
        }
    }

    @Override
    public void commit() {
        try {
            connection.commit();
        } catch (SQLException ex) {
            throw new DataAccessException("Commit error", ex);
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException("Rollback error", ex);
        }
    }

    @Override
    public void close() {
        try {
            connection.setAutoCommit(true);
               // needed in case of connection pooling,
               // also commits current transaction as a bonus, but we do not want to rely on this

            connection.close();
        } catch(SQLException ex){
            throw new DataAccessException("Close error", ex);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new JDBCUserDAO(this);
        }
        return userDAO;
    }

    @Override
    public InfoSessionDAO getInfoSessionDAO() {
        if(infoSessionDAO == null){
            infoSessionDAO = new JDBCInfoSessionDAO(this);
        }
        return infoSessionDAO;
    }

    @Override
    public TemplateDAO getTemplateDAO() {
        if(templateDAO == null){
            templateDAO = new JDBCTemplateDAO(this);
        }
        return templateDAO;
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        if(notificationDAO == null){
            notificationDAO = new JDBCNotificationDAO(this);
        }
        return notificationDAO;
    }

    @Override
    public MessageDAO getMessageDAO() {
        if(messageDAO == null){
            messageDAO = new JDBCMessageDAO(this);
        }
        return messageDAO;
    }

    @Override
    public AddressDAO getAddressDAO() {
        if(addressDAO == null){
            addressDAO = new JDBCAddressDAO(this);
        }
        return addressDAO;
    }

	@Override
	public CarDAO getCarDAO() {
		if(carDAO == null){
            carDAO = new JDBCCarDAO(this);
        }
        return carDAO;
	}

    @Override
    public PrivilegedDAO getPrivilegedDAO() {
        if (privilegedDAO == null) {
            privilegedDAO = new JDBCPrivilegedDAO(this);
        }
        return privilegedDAO;
    }

    /* (Temporarily) disabled
    @Override
	public AvailabilityDAO getAvailabilityDAO() {
		if(availabilityDAO == null){
            availabilityDAO = new JDBCAvailabilityDAO(this);
        }
        return availabilityDAO;
	}
	*/

    @Override
    public CarCostDAO getCarCostDAO() {
        if(carCostDAO == null){
            carCostDAO = new JDBCCarCostDAO(this);
        }
        return carCostDAO;
    }

	@Override
	public ReservationDAO getReservationDAO() {
		if(reservationDAO == null){
            reservationDAO = new JDBCReservationDAO(this);
        }
        return reservationDAO;
	}

    @Override
    public DamageDAO getDamageDAO() {
        if(damageDAO == null){
            damageDAO = new JDBCDamageDAO(this);
        }
        return damageDAO;
    }

    @Override
    public DamageLogDAO getDamageLogDAO() {
        if(damageLogDAO == null){
            damageLogDAO = new JDBCDamageLogDAO(this);
        }
        return damageLogDAO;
    }

    @Override
	public UserRoleDAO getUserRoleDAO() {
		if(userRoleDAO == null){
			userRoleDAO = new JDBCUserRoleDAO(this);
        }
        return userRoleDAO;
	}

    @Override
    public CarRideDAO getCarRideDAO() {
        if(carRideDAO == null){
            carRideDAO = new JDBCCarRideDAO(this);
        }
        return carRideDAO;
    }

    @Override
    public RefuelDAO getRefuelDAO() {
        if(refuelDAO == null){
            refuelDAO = new JDBCRefuelDAO(this);
        }
        return refuelDAO;
    }

    @Override
    public SchedulerDAO getSchedulerDAO() {
        if(schedulerDAO == null){
            schedulerDAO = new JDBCSchedulerDAO(this);
        }
        return schedulerDAO;
    }

    @Override
    public ApprovalDAO getApprovalDAO() {
        if(approvalDAO == null){
            approvalDAO = new JDBCApprovalDAO(this);
        }
        return approvalDAO;
    }

    @Override
    public JobDAO getJobDAO() {
        if(jobDAO == null){
            jobDAO = new JDBCJobDAO(this);
        }
        return jobDAO;
    }

    @Override
    public FileDAO getFileDAO() {
        if(fileDAO == null){
            fileDAO = new JDBCFileDAO(this);
        }
        return fileDAO;
    }

    @Override
    public SettingDAO getSettingDAO() {
        if(settingDAO == null){
            settingDAO = new JDBCSettingDAO(this);
        }
        return settingDAO;
    }

    @Override
    public ReceiptDAO getReceiptDAO() {
        if(receiptDAO == null){
            receiptDAO = new JDBCReceiptDAO(this);
        }
        return receiptDAO;
    }
}
