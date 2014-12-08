package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.models.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Cedric on 4/11/2014.
 */
public interface ReceiptDAO {
     public Receipt createReceipt(String name, LocalDate date, File file, User user, BigDecimal price) throws DataAccessException;

     public List<Receipt> getReceiptsList(FilterField orderBy, boolean asc, int page, int PAGE_SIZE,
                                          LocalDate filterDate, User user) throws DataAccessException;

     public int getAmountOfReceipts(LocalDate filterDate, User user);
}
