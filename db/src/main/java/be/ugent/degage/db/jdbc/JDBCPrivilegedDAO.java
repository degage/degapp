package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.PrivilegedDAO;
import be.ugent.degage.db.models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * JDBC implementation of {@link be.ugent.degage.db.dao.PrivilegedDAO}
 */
public class JDBCPrivilegedDAO extends AbstractDAO implements PrivilegedDAO {

    public JDBCPrivilegedDAO(JDBCDataAccessContext context) {
        super(context);
    }

    // TODO: replace * by actual fields
    private LazyStatement getPrivilegedStatement = new LazyStatement (
            "SELECT * FROM carprivileges " +
                "INNER JOIN users ON users.user_id = carprivileges.car_privilege_user_id WHERE car_privilege_car_id=?"
    );

    @Override
    public Iterable<User> getPrivileged(int carId) throws DataAccessException {
        try {
            PreparedStatement ps = getPrivilegedStatement.value();
            ps.setInt(1, carId);
            Collection<User> users = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(JDBCUserDAO.populateUserPartial(rs));
                }
                return users;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading privileged resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of privileged", ex);
        }
    }

    private LazyStatement createPrivilegedStatement = new LazyStatement (
            "INSERT INTO carprivileges(car_privilege_user_id, car_privilege_car_id) VALUES (?,?)"
    );

    @Override
    public void addPrivileged(int carId, Iterable<User> users) throws DataAccessException {
        try {
            for(User user : users) {
                PreparedStatement ps = createPrivilegedStatement.value();
                ps.setInt(1, user.getId());
                ps.setInt(2, carId);

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when creating privileged.");
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create new privileged");
        }
    }

    private LazyStatement deletePrivilegedStatement = new LazyStatement (
        "DELETE FROM carprivileges WHERE car_privilege_user_id = ? AND car_privilege_car_id=?"
    );

    @Override
    public void deletePrivileged(int carId, Iterable<User> users) throws DataAccessException {
        try {
            for(User user : users) {
                PreparedStatement ps = deletePrivilegedStatement.value();

                ps.setInt(1, user.getId());
                ps.setInt(2, carId);

                if(ps.executeUpdate() == 0)
                    throw new DataAccessException("No rows were affected when deleting privileged.");

            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to delete privileged");
        }
    }

}
