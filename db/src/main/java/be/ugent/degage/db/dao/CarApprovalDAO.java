package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.CarApproval;
import be.ugent.degage.db.models.Page;

/**
 * Data access object for approvals
 */
public interface CarApprovalDAO {

  public CarApproval getCarApproval(int carApprovalId) throws DataAccessException;

  public Page<CarApproval> listCarApprovals(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException;

  public void updateCarApproval(CarApproval carApproval) throws DataAccessException;

  public int getNrOfPendingCarApprovals() throws DataAccessException;

}
