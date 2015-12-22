/* MembershipDAO.java
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

package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Membership;
import be.ugent.degage.db.models.Page;

import java.time.LocalDate;

/**
 * Data access object for actions related to {@link Membership}, i.e., deposit, membership fee and contracts
 */
public interface MembershipDAO {

    /**
     * Return membership information of the user with the given id
     */
    public Membership getMembership(int userId) throws DataAccessException;

    /**
     * Update the deposit/fee of the user
     */
    public void updateUserMembership(int userId, Integer deposit, Integer fee) throws DataAccessException;

    /**
     * Update the contract information of the user
     */
    public void updateUserContract(int userId, LocalDate contract) throws DataAccessException;

    /**
     * Is the first user the contract administrator of the second? Used in privacy checks.
     */
    public boolean isContractAdminOf(int adminId, int userId) throws DataAccessException;

    /**
     * Give a paged list of all contractees of a certain administrator.
     * @param type: 0 contract not signed, 1 signed but not member, 2 member
     */
    public Page<Membership> getContractees (int adminId, int type, int page, int pageSize) throws DataAccessException;

    /**
     * The number of unsigned contracts for the given contract admin
     */
    public int getNrOfUnsignedContracts (int adminId) throws DataAccessException;

}
