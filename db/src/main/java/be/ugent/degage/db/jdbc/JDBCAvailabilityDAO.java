package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.AvailabilityDAO;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.CarAvailabilityInterval;
import be.ugent.degage.db.models.DayOfWeek;
import org.joda.time.LocalTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
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
            "SELECT * FROM caravailabilities WHERE car_availability_car_id=?"
    );

    @Override
    public List<CarAvailabilityInterval> getAvailabilities(int carId) throws DataAccessException {
        try {
            PreparedStatement ps = getAvailabilitiesStatement.value();
            ps.setInt(1, carId);
            List<CarAvailabilityInterval> availabilities = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time beginTime = rs.getTime("car_availability_begin_time");
                    LocalTime beginLocalTime = LocalTime.fromDateFields(beginTime);

                    Time endTime = rs.getTime("car_availability_end_time");
                    LocalTime endLocalTime = LocalTime.fromDateFields(endTime);

                    availabilities.add(new CarAvailabilityInterval(rs.getInt("car_availability_id"), DayOfWeek.getDayFromInt(rs.getInt("car_availability_begin_day_of_week")),
                            beginLocalTime, DayOfWeek.getDayFromInt(rs.getInt("car_availability_end_day_of_week")), endLocalTime));
                }
                return availabilities;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading availabilities resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of availabilities", ex);
        }
    }

    private void setAvailabilityVariables(PreparedStatement ps, int carId, CarAvailabilityInterval availability) throws SQLException {
        ps.setInt(1, carId);
        ps.setInt(2, availability.getBeginDayOfWeek().getI());
        ps.setTime(3, new Time(availability.getBeginTime().toDateTimeToday().getMillis()));
        ps.setInt(4, availability.getEndDayOfWeek().getI());
        ps.setTime(5, new Time(availability.getEndTime().toDateTimeToday().getMillis()));
    }

    private LazyStatement createAvailabilityStatement = new LazyStatement (
            "INSERT INTO caravailabilities(car_availability_car_id, " +
                    "car_availability_begin_day_of_week, car_availability_begin_time, car_availability_end_day_of_week, car_availability_end_time) " +
                    "VALUES (?,?,?,?,?)",
            "car_availability_id"
    );

    private LazyStatement updateAvailabilityStatement = new LazyStatement (
            "UPDATE caravailabilities SET car_availability_car_id=?, " +
                    "car_availability_begin_day_of_week=?, car_availability_begin_time=?, car_availability_end_day_of_week=?, car_availability_end_time=? " +
                    "WHERE car_availability_id = ?"
    );

    @Override
    public void addOrUpdateAvailabilities(Car car, Iterable<CarAvailabilityInterval> availabilities) throws DataAccessException {
        try {
            for(CarAvailabilityInterval availability : availabilities) {
                if(availability.getId() == null) { // create
                    PreparedStatement ps = createAvailabilityStatement.value();
                    setAvailabilityVariables(ps, car.getId(), availability);

                    if(ps.executeUpdate() == 0)
                        throw new DataAccessException("No rows were affected when creating availability.");
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        int id = keys.getInt(1);
                        availability.setId(id);
                    } catch (SQLException ex) {
                        throw new DataAccessException("Failed to get primary key for new availability.", ex);
                    }
                } else { // update
                    PreparedStatement ps = updateAvailabilityStatement.value();
                    setAvailabilityVariables(ps, car.getId(), availability);

                    ps.setInt(6, availability.getId());

                    if(ps.executeUpdate() == 0)
                        throw new DataAccessException("No rows were affected when updating availability.");

                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to create/update new availabilitiy");
        }
    }

    private LazyStatement deleteAvailabilityStatement = new LazyStatement (
            "DELETE FROM caravailabilities WHERE car_availability_id = ?"
    );

    @Override
    public void deleteAvailabilties(Iterable<CarAvailabilityInterval> availabilities) throws DataAccessException {
        try {
            for(CarAvailabilityInterval availability : availabilities) {
                if(availability.getId() == null) {
                    throw new DataAccessException("No id is available to availability.");
                } else {
                    PreparedStatement ps = deleteAvailabilityStatement.value();

                    ps.setInt(1, availability.getId());

                    if(ps.executeUpdate() == 0)
                        throw new DataAccessException("No rows were affected when deleting availability.");
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException("Failed to delete new availabilitiy");
        }
    }

}
