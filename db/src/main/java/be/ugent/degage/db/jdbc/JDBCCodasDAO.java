package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.CodasDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * JDBC implementation of {@link CodasDAO}
 */
public class JDBCCodasDAO extends AbstractDAO implements CodasDAO {

    private static final String CODA_FIELDS =
            "coda_id, coda_date, coda_user_id, coda_filename";


    public JDBCCodasDAO (JDBCDataAccessContext context) {
        super(context);
    }

    private static Coda populateCoda(ResultSet rs) throws SQLException {
        Coda coda = new Coda(rs.getInt("coda_id"),
                rs.getDate("coda_date").toLocalDate(),
                rs.getString("coda_filename"),
                rs.getObject("user_id") == null ? null : new User(
                        rs.getInt("user_id"),
                        rs.getString("user_email"),
                        rs.getString("user_firstname"),
                        rs.getString("user_lastname"),
                        UserStatus.valueOf(rs.getString("user_status")),
                        rs.getString("user_phone"),
                        rs.getString("user_cellphone"),
                        (Integer) rs.getObject("user_degage_id"))
                );

        return coda;
    }

    @Override
    public Iterable<Coda> listAllCodas() {
        try (PreparedStatement ps = prepareStatement(
                "SELECT * FROM codas " +
                " LEFT JOIN users ON coda_user_id = user_id ORDER BY coda_id DESC" //newest upload first
        )) {
            return toList(ps, JDBCCodasDAO::populateCoda);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot list all codas", ex);
        }
    }

    @Override
    public int createCoda(Coda coda) throws DataAccessException {

        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO codas(coda_date, coda_filename, coda_user_id)" +
                        "VALUES (?,?,?)",
                "coda_id"
        )) {

            ps.setDate(1, coda.getDate() == null ? null : Date.valueOf(coda.getDate()));
            ps.setString(2, coda.getFilename());
            ps.setInt(3, coda.getUser().getId());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                return id;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create coda.", ex);
        }
    }

    @Override
    public Page<Coda> listCodasPage(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {

        StringBuilder builder = new StringBuilder(
                "SELECT SQL_CALC_FOUND_ROWS * FROM codas " +
                        "LEFT JOIN users ON coda_user_id = user_id ORDER BY coda_id DESC "
        );

        builder.append (" LIMIT ").append(pageSize).append(" OFFSET ").append((page-1)*pageSize);

        //TODO verwerk filters

        try (PreparedStatement ps = prepareStatement(builder.toString())) {

            return toPage(ps, pageSize, rs -> new Coda(rs.getInt("coda_id"),
                    rs.getDate("coda_date").toLocalDate(),
                    rs.getString("coda_filename"),
                    rs.getObject("user_id") == null ? null : new User(
                            rs.getInt("user_id"),
                            rs.getString("user_email"),
                            rs.getString("user_firstname"),
                            rs.getString("user_lastname"),
                            UserStatus.valueOf(rs.getString("user_status")),
                            rs.getString("user_phone"),
                            rs.getString("user_cellphone"),
                            (Integer) rs.getObject("user_degage_id"))
                    ));

        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public Page<Coda> listCodasPage(FilterField orderBy, boolean asc, int page, int pageSize, String filter) throws DataAccessException {

        StringBuilder builder = new StringBuilder(
                "SELECT SQL_CALC_FOUND_ROWS * FROM codas " +
                        "LEFT JOIN users ON coda_user_id = user_id "
        );

        if (filter != null && filter.length() > 0) {
          builder.append(" WHERE ");
          String[] searchStrings = filter.trim().split(" ");
          for (int i = 0; i < searchStrings.length; i++) {
            if (i > 0) {
              builder.append(" AND ");
            }
            builder.append("(");
            StringBuilder filterBuilder = new StringBuilder();
            FilterUtils.appendOrContainsFilter(filterBuilder, "user_lastname", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "user_firstname", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "coda_filename", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "coda_id", searchStrings[i]);
            FilterUtils.appendOrContainsFilter(filterBuilder, "coda_date", searchStrings[i]);
            builder.append(filterBuilder).append(")");
          }
        }

        builder.append(" ORDER BY coda_id DESC ");
        builder.append (" LIMIT ").append(pageSize).append(" OFFSET ").append((page-1)*pageSize);

        //TODO verwerk filters

        try (PreparedStatement ps = prepareStatement(builder.toString())) {

            return toPage(ps, pageSize, rs -> new Coda(rs.getInt("coda_id"),
                    rs.getDate("coda_date").toLocalDate(),
                    rs.getString("coda_filename"),
                    rs.getObject("user_id") == null ? null : new User(
                            rs.getInt("user_id"),
                            rs.getString("user_email"),
                            rs.getString("user_firstname"),
                            rs.getString("user_lastname"),
                            UserStatus.valueOf(rs.getString("user_status")),
                            rs.getString("user_phone"),
                            rs.getString("user_cellphone"),
                            (Integer) rs.getObject("user_degage_id"))
                    ));

        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}
