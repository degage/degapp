package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.dao.DamageLogDAO;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.Damage;
import be.ugent.degage.db.models.DamageLog;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 04/05/14.
 */
class JDBCDamageLogDAO extends AbstractDAO implements DamageLogDAO {

    public JDBCDamageLogDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private LazyStatement createDamageLogStatement = new LazyStatement(
            "INSERT INTO damagelogs " +
                    "(damage_log_damage_id, damage_log_description, damage_log_created_at) " +
                    "VALUES(?, ?, ?)",
            "damage_log_id"
    );

    @Override
    public DamageLog createDamageLog(Damage damage, String description) throws DataAccessException {
        DateTime created = new DateTime();
        try {
            PreparedStatement ps = createDamageLogStatement.value();
            ps.setInt(1, damage.getId());
            ps.setString(2, description);
            ps.setTimestamp(3, new Timestamp(created.getMillis()));
            if (ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when creating damagelog.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next(); //if this fails we want an exception anyway
                return new DamageLog(keys.getInt(1), damage, description, created);
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to get primary key for damagelog.", ex);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create damagelog", e);
        }
    }

    private LazyStatement getDamageLogsStatement = new LazyStatement(
            "SELECT * FROM damagelogs " +
                "JOIN damages ON damage_log_damage_id = damage_id " +
                "JOIN carrides ON damage_car_ride_id = car_ride_car_reservation_id " +
                "JOIN carreservations ON damage_car_ride_id = reservation_id " +
                "LEFT JOIN filegroups ON damage_filegroup_id = file_group_id " +
                "JOIN cars ON reservation_car_id = car_id " +
                "JOIN users ON reservation_user_id = user_id " +
                "WHERE damage_log_damage_id = ? ORDER BY damage_log_created_at DESC"
            );


    @Override
    public List<DamageLog> getDamageLogsForDamage(int damageId) throws DataAccessException {
        try {
            PreparedStatement ps = getDamageLogsStatement.value();
            ps.setInt(1, damageId);
            return getDamageLogList(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve the list of damagelogs.", e);
        }
    }

    private List<DamageLog> getDamageLogList(PreparedStatement ps) throws DataAccessException {
        List<DamageLog> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(
                        new DamageLog(rs.getInt("damage_log_id"),
                                JDBCDamageDAO.populateDamage(rs),
                                rs.getString("damage_log_description"),
                                new DateTime(rs.getTimestamp("damage_log_created_at")))
                );
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error while reading damage resultset", e);

        }
    }

}
