package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

public interface CodasDAO {

    public Iterable<Coda> listAllCodas();

    public Page<Coda> listCodasPage(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;

    public Page<Coda> listCodasPage(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException;

    public int createCoda(Coda coda) throws DataAccessException;



}
