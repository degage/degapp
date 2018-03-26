package be.ugent.degage.db.jdbc.api;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.jdbc.JDBCDataAccessContext;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.api.CarApiDAO;
import be.ugent.degage.db.dao.api.CarApprovalApiDAO;
import be.ugent.degage.db.models.api.*;
import be.ugent.degage.db.jdbc.AbstractDAO;

import java.sql.*;

/**
 * @author Dries
 */
public class JDBCCarApprovalApiDAO extends AbstractDAO implements CarApprovalApiDAO {

    public JDBCCarApprovalApiDAO(JDBCDataAccessContext context) {
        super(context);
    }

}
