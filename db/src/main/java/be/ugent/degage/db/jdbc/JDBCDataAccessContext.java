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
        return new JDBCUserDAO(this);
    }

    @Override
    public InfoSessionDAO getInfoSessionDAO() {
        return  new JDBCInfoSessionDAO(this);
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        return new JDBCNotificationDAO(this);
    }

    @Override
    public MessageDAO getMessageDAO() {
        return new JDBCMessageDAO(this);
    }

    @Override
    public AddressDAO getAddressDAO() {
        return new JDBCAddressDAO(this);
    }

	@Override
	public CarDAO getCarDAO() {
		return new JDBCCarDAO(this);
	}

    @Override
    public PrivilegedDAO getPrivilegedDAO() {
        return new JDBCPrivilegedDAO(this);
    }

    @Override
    public CarCostDAO getCarCostDAO() {
        return new JDBCCarCostDAO(this);
    }

	@Override
	public ReservationDAO getReservationDAO() {
		return new JDBCReservationDAO(this);
	}

    @Override
    public DamageDAO getDamageDAO() {
        return new JDBCDamageDAO(this);
    }

    @Override
    public DamageLogDAO getDamageLogDAO() {
        return new JDBCDamageLogDAO(this);
    }

    @Override
	public UserRoleDAO getUserRoleDAO() {
		return new JDBCUserRoleDAO(this);
	}

    @Override
    public CarRideDAO getCarRideDAO() {
        return new JDBCCarRideDAO(this);
    }

    @Override
    public RefuelDAO getRefuelDAO() {
        return new JDBCRefuelDAO(this);
    }

    @Override
    public SchedulerDAO getSchedulerDAO() {
        return new JDBCSchedulerDAO(this);
    }

    @Override
    public ApprovalDAO getApprovalDAO() {
        return new JDBCApprovalDAO(this);
    }

    @Override
    public JobDAO getJobDAO() {
        return new JDBCJobDAO(this);
    }

    @Override
    public FileDAO getFileDAO() {
        return new JDBCFileDAO(this);
    }

    @Override
    public SettingDAO getSettingDAO() {
        return new JDBCSettingDAO(this);
    }

    @Override
    public VerificationDAO getVerificationDAO() {
        return new JDBCVerificationDAO(this);
    }

    @Override
    public TripDAO getTripDAO() {
        return new JDBCTripDAO(this);
    }

    @Override
    public CheckDAO getCheckDAO() {
        return new JDBCCheckDAO(this);
    }

    @Override
    public BillingDAO getBillingDAO() {
        return new JDBCBillingDAO(this);
    }

    @Override
    public BillingAdmDAO getBillingAdmDAO() {
        return new JDBCBillingAdmDAO(this);
    }

    @Override
    public MembershipDAO getMembershipDAO() {
        return new JDBCMembershipDAO(this);
    }

    @Override
    public AnnouncementDAO getAnnouncementDAO() {
        return new JDBCAnnouncementDAO(this);
    }

    @Override
    public CarPreferencesDAO getCarPreferencesDao() {
        return new JDBCCarPreferencesDAO(this);
    }
}
