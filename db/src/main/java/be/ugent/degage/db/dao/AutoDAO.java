package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

import java.time.Instant;
import java.time.LocalDate;

public interface AutoDAO {
  public int createAuto(Auto auto) throws DataAccessException;

  public void updateAuto(Auto auto) throws DataAccessException;

  public Auto getAuto(int autoId) throws DataAccessException;

  public Auto getAutoByUserId(int userId) throws DataAccessException;

  public Page<AutoAndUser> listAutosAndOwners(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException;

  public Page<AutoAndUserAndEnrollee> listAutosAndOwnersAndEnrollees(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException;

  public void updateAutoDocument(int autoId, int fileId) throws DataAccessException;
}
