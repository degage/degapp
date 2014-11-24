package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Approval;
import be.ugent.degage.db.models.InfoSession;
import be.ugent.degage.db.models.UserHeader;

/**
 * Created by Cedric on 3/30/2014.
 */
public interface ApprovalDAO {

    public boolean hasApprovalPending (int userId) throws DataAccessException;
    public Iterable<Approval> getApprovals(int page, int pageSize) throws DataAccessException;
    public int getApprovalCount() throws DataAccessException;
    public void setApprovalAdmin(int approvalId, int adminId) throws DataAccessException;
    public Approval getApproval(int approvalId) throws DataAccessException;
    public Approval createApproval(UserHeader user, InfoSession session, String userMessage) throws DataAccessException;
    public void updateApproval(Approval approval) throws DataAccessException;
}
