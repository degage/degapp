/* TestDataAccessContext.java
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
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package database.mocking;

import be.ugent.degage.db.*;
import be.ugent.degage.db.jdbc.JDBCJobDAO;

/**
 * Created by Cedric on 3/7/14.
 */
public class TestDataAccessContext implements DataAccessContext {

    private UserDAO userDao;
    private InfoSessionDAO infoSessionDAO;
    private UserRoleDAO userRoleDao;
    private ReservationDAO reservationDAO;
    private CarDAO carDAO;
    private TemplateDAO templateDAO;
    private AddressDAO addressDAO;
    private NotificationDAO notificationDAO;
    private MessageDAO messageDAO;
    private CarRideDAO carRidesDAO;
    private JobDAO jobDAO;

    public TestDataAccessContext(){
        userDao = new TestUserDAO();
        infoSessionDAO = new TestInfoSessionDAO();
        userRoleDao = new TestUserRoleDAO(userDao);
        reservationDAO = new TestReservationDAO();
        carDAO = new TestCarDAO();
        templateDAO = new TestTemplateDAO();
        addressDAO = new TestAddressDAO();
        notificationDAO = new TestNotificationDAO();
        messageDAO = new TestMessageDAO();
        carRidesDAO = new TestCarRidesDAO();
        jobDAO = new TestJobDAO();
    }

    @Override
    public UserDAO getUserDAO() {
        return userDao;
    }

    @Override
    public InfoSessionDAO getInfoSessionDAO() {
        return infoSessionDAO;
    }

    @Override
    public TemplateDAO getTemplateDAO() {
        return templateDAO;
    }

    @Override
    public AddressDAO getAddressDAO() {
        return addressDAO;
    }

    @Override
    public CarDAO getCarDAO() {
        return carDAO;
    }

    @Override
    public CarCostDAO getCarCostDAO() {
        return null; // TODO
    }

    @Override
    public RefuelDAO getRefuelDAO() {
        return null;
    }

    @Override
    public SchedulerDAO getSchedulerDAO() {
        return null;
    }

    @Override
    public ReservationDAO getReservationDAO() {
        return reservationDAO;
    }

    @Override
    public DamageDAO getDamageDAO() {
        return null;
    }

    @Override
    public DamageLogDAO getDamageLogDAO() {
        return null;
    }

    @Override
    public UserRoleDAO getUserRoleDAO() {
        return userRoleDao;
    }

    @Override
    public CarRideDAO getCarRideDAO() {
        return carRidesDAO;
    }

    @Override
    public ApprovalDAO getApprovalDAO() {
        return null;
    }

    @Override
    public JobDAO getJobDAO() {
        return jobDAO;
    }

    @Override
    public FileDAO getFileDAO() {
        return null;
    }

    @Override
    public SettingDAO getSettingDAO() {
        return null;
    }

    @Override
    public void begin() {

    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {
        throw new RuntimeException("Rollback is not supported");
    }

    @Override
    public void close() {

    }

	@Override
	public NotificationDAO getNotificationDAO() {
		return notificationDAO;
	}

	@Override
	public MessageDAO getMessageDAO() {
		return messageDAO;
	}
}
