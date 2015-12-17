package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Membership;

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
    public void updateUserMembership(int userId, Integer deposit, Integer fee);

    /**
     * Update the contract information of the user
     */
    public void updateUserContract(int userId, LocalDate contract);

}
