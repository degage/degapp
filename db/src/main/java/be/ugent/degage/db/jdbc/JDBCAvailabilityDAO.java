package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.AvailabilityDAO;
import be.ugent.degage.db.models.CarAvailabilityInterval;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kc on 10/25/14.
 */
class JDBCAvailabilityDAO extends AbstractDAO implements AvailabilityDAO {

    public JDBCAvailabilityDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private LazyStatement getAvailabilitiesStatement = new LazyStatement (
            "SELECT car_availability_id, car_availability_start, car_availability_end " +
            "FROM caravailabilities WHERE car_availability_car_id=?"
    );

    @Override
    public Iterable<CarAvailabilityInterval> getAvailabilities(int carId) throws DataAccessException {
        try {
            PreparedStatement ps = getAvailabilitiesStatement.value();
            ps.setInt(1, carId);
            List<CarAvailabilityInterval> availabilities = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    availabilities.add(
                            new CarAvailabilityInterval(
                                    rs.getInt("car_availability_id"),
                                    rs.getInt("car_availability_start"),
                                    rs.getInt("car_availability_end")
                            )
                    );
                }
                return availabilities;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of availabilities", ex);
        }
    }

    private LazyStatement updateAvailabilityStatement = new LazyStatement (
            "UPDATE caravailabilities " +
            "SET  car_availability_start=?, car_availability_end=? " +
            "WHERE car_availability_car_id=? AND car_availability_id = ?"
    );

    @Override
    public void updateAvailabilities(int carId, Iterable<CarAvailabilityInterval> availabilities) throws DataAccessException {
        try {
            for (CarAvailabilityInterval availability : availabilities) {

                PreparedStatement ps = updateAvailabilityStatement.value();
                ps.setInt(1, availability.getStart());
                ps.setInt(2, availability.getEnd());
                ps.setInt(3, carId);
                ps.setInt(4, availability.getId());

                if (ps.executeUpdate() == 0) {
                    throw new DataAccessException("No rows were affected when updating availability.");
                }

            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update availability", ex);
        }
    }

    private LazyStatement createAvailabilityStatement = new LazyStatement (
            "INSERT INTO caravailabilities(car_availability_car_id, " +
                    "car_availability_start, car_availability_end) " +
                    "VALUES (?,?,?)",
            "car_availability_id"
    );


    @Override
    public void createAvailabilities(int carId, Iterable<CarAvailabilityInterval> availabilities) throws DataAccessException {
        try {
            for (CarAvailabilityInterval availability : availabilities) {
                PreparedStatement ps = createAvailabilityStatement.value();
                ps.setInt(1, carId);
                ps.setInt(2, availability.getStart());
                ps.setInt(3, availability.getEnd());

                if (ps.executeUpdate() == 0) {
                    throw new DataAccessException("No rows were affected when creating availability.");
                }
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    availability.setId(keys.getInt(1));
                } catch (SQLException ex) {
                    throw new DataAccessException("Failed to get primary key for new availability.", ex);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create new availabilitiy", ex);
        }
    }

    private LazyStatement deleteAvailabilityStatement = new LazyStatement (
            "DELETE FROM caravailabilities WHERE car_availability_id = ?"
    );

    @Override
    public void deleteAvailabilties(Iterable<Integer> ids) throws DataAccessException {
        try {
            for (Integer id : ids) {
                PreparedStatement ps = deleteAvailabilityStatement.value();
                ps.setInt(1, id);

                if (ps.executeUpdate() == 0) {
                    throw new DataAccessException("No rows were affected when deleting availability for " + id);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete availabilitiy");
        }
    }

}
