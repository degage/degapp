/* CarAssistanceDAO.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarAssistanceDAO;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.CarAssistanceExtended;
import be.ugent.degage.db.models.CarAssistanceType;
import be.ugent.degage.db.models.Page;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

/**
 * JDBC implementation of {@link be.ugent.degage.db.dao.CarAssistanceDAO}
 */
class JDBCCarAssistanceDAO extends AbstractDAO implements CarAssistanceDAO {

	private static final String ASSISTANCE_QUERY =
		"SELECT SQL_CALC_FOUND_ROWS assistance_id, assistance_name, assistance_expiration, assistance_contract_id, assistance_type, " +
		"assistance_updated_at, car_name FROM carassistances " +
		"LEFT JOIN cars ON assistance_id = car_id ";

    public JDBCCarAssistanceDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static CarAssistanceExtended populateCarAssistance(ResultSet rs) throws SQLException {
        Date assistanceExpiration = rs.getDate("assistance_expiration");
        return new CarAssistanceExtended(
            rs.getString("assistance_name"),
            assistanceExpiration == null ? null : assistanceExpiration.toLocalDate(),
            CarAssistanceType.valueOf(rs.getString("assistance_type")),
            rs.getString("assistance_contract_id"),
            rs.getString("car_name"),
            rs.getInt("assistance_id")
        );
    }

	// public CarAssistanceExtended createCarAssistance(String name, Date expiration, CarAssistanceType type, String contractNr, Car car) throws DataAccessException;
	// public void updateCarAssistance(CarAssistanceExtended assistance) throws DataAccessException;
	// public void deleteCarAssistance(CarAssistanceExtended assistance) throws DataAccessException;
	
	@Override
    public Page<CarAssistanceExtended> getAllCarAssistances(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
		StringBuilder builder = new StringBuilder(ASSISTANCE_QUERY);
        if (Integer.parseInt(filter.getValue(FilterField.CAR_ID)) >= 0) {
        	builder.append("WHERE assistance_id = " + filter.getValue(FilterField.CAR_ID));
        }
        // add order
        switch (orderBy) {
            case CAR_NAME:
                builder.append(" ORDER BY car_name ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case NAME:
                builder.append(" ORDER BY assistance_name ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case DATE:
                builder.append(" ORDER BY assistance_expiration ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case CONTRACT_ID:
                builder.append(" ORDER BY assistance_contract_id ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case TYPE:
                builder.append(" ORDER BY assistance_type ");
                builder.append(asc ? "ASC" : "DESC");
                break;
        }
        builder.append(" LIMIT ?,?");

        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            ps.setInt(1, (page - 1) * pageSize);
            ps.setInt(2, pageSize);
            return toPage(ps, pageSize, JDBCCarAssistanceDAO::populateCarAssistance);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get carassistance", ex);
        }
    }

	// public void deleteAllCarAssistances(Car car) throws DataAccessException;
}
