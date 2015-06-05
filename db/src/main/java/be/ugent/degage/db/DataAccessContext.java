/* DataAccessContext.java
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

package be.ugent.degage.db;

import be.ugent.degage.db.dao.*;

/**
 * Created by Cedric on 2/16/14.
 */
public interface DataAccessContext extends AutoCloseable {
    public UserDAO getUserDAO();

    public InfoSessionDAO getInfoSessionDAO();

    public NotificationDAO getNotificationDAO();

    public MessageDAO getMessageDAO();

    public AddressDAO getAddressDAO();
    
    public CarDAO getCarDAO();

    public PrivilegedDAO getPrivilegedDAO();

    public CarCostDAO getCarCostDAO();

    public ReceiptDAO getReceiptDAO();

    public RefuelDAO getRefuelDAO();

    public SchedulerDAO getSchedulerDAO();
    
    public ReservationDAO getReservationDAO();

    public DamageDAO getDamageDAO();

    public DamageLogDAO getDamageLogDAO();
    
    public UserRoleDAO getUserRoleDAO();

    public CarRideDAO getCarRideDAO();

    public CheckDAO getCheckDAO();

    // (Temporarily) disabled
    // public AvailabilityDAO getAvailabilityDAO();

    public ApprovalDAO getApprovalDAO();

    public JobDAO getJobDAO();

    public FileDAO getFileDAO();

    public SettingDAO getSettingDAO();

    public VerificationDAO getVerificationDAO();

    public TripDAO getTripDAO();

    public BillingDAO getBillingDAO();

    public void begin();

    public void commit();

    public void rollback();

    public void close();
}
