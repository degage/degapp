package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.CarInsuranceDAO;
import be.ugent.degage.db.models.CarInsurance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

/**
 * JDBC implementation of {@link be.ugent.degage.db.dao.CarInsuranceDAO}
 */
class JDBCCarInsuranceDAO extends AbstractDAO implements CarInsuranceDAO {

    public JDBCCarInsuranceDAO(JDBCDataAccessContext context) {
        super(context);
    }

    public static CarInsurance populateCarInsurance(ResultSet rs) throws SQLException {
        return new CarInsurance(
            rs.getString("insurance_name"),
            rs.getDate("insurance_expiration") == null ? null : rs.getDate("insurance_expiration").toLocalDate(),
            rs.getString("insurance_bonus_malus"),
            rs.getString("insurance_contract_id"),
            rs.getDate("start_insurance_policy") == null ? null : rs.getDate("start_insurance_policy").toLocalDate(),
            rs.getInt("start_bonus_malus"),
            rs.getBoolean("civil_liability"),
            rs.getBoolean("legal_counsel"),
            rs.getBoolean("driver_insurance"),
            rs.getBoolean("material_damage"),
            rs.getInt("value_exclusive_VAT"),
            rs.getInt("exemption"),
            rs.getBoolean("glass_breakage"),
            rs.getBoolean("theft"),
            rs.getString("insurance_name_before"),
            rs.getInt("insurance_file_id"),
            rs.getInt("greencard_file_id")
        );
    }

    @Override
    public void updateCarInsuranceDocument(int autoId, int fileId) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE carinsurances SET insurance_file_id = ? WHERE insurance_id = ?")) {
            ps.setInt(1, fileId);
            ps.setInt(2, autoId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update CarInsurance document", ex);
        }
    }

    @Override
    public void updateCarGreenCardDocument(int autoId, int fileId) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE carinsurances SET greencard_file_id = ? WHERE insurance_id = ?")) {
            ps.setInt(1, fileId);
            ps.setInt(2, autoId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update GreenCard document", ex);
        }
    }
}
