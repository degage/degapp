package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Approval;

/**
 * Created by Cedric on 3/30/2014.
 */
public interface ApprovalDAO {

    public boolean hasApprovalPending (int userId) throws DataAccessException;
    public Iterable<Approval> getApprovals(int page, int pageSize) throws DataAccessException;
    public int getApprovalCount() throws DataAccessException;
    public void setApprovalAdmin(int approvalId, int adminId) throws DataAccessException;
    public Approval getApproval(int approvalId) throws DataAccessException;

    /**
     * Create approval with submit time set to now
     */
    public void createApproval(int userId, int sessionId, String userMessage) throws DataAccessException;

    /**
     Update the approval record and set the review date to now
     */
    public void updateApproval(Approval approval) throws DataAccessException;
}
