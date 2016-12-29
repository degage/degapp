/* CarParkingcardDAO.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Emmanuel Isebaert
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
import be.ugent.degage.db.dao.CarParkingcardDAO;
import be.ugent.degage.db.models.Car;
import be.ugent.degage.db.models.CarParkingcardExtended;
import be.ugent.degage.db.models.Page;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

/**
 * JDBC implementation of {@link be.ugent.degage.db.dao.CarParkingcardDAO}
 */
class JDBCCarParkingcardDAO extends AbstractDAO implements CarParkingcardDAO {

	private static final String ASSISTANCE_QUERY =
		"SELECT SQL_CALC_FOUND_ROWS parkingcard_id, parkingcard_city, parkingcard_expiration, parkingcard_contract_id, parkingcard_zones, " +
		"parkingcard_updated_at, car_name, details_car_license_plate " +
        "FROM carparkingcards " +
		"LEFT JOIN cars ON parkingcard_id = car_id " +
        "LEFT JOIN technicalcardetails on details_id = car_id ";

    public JDBCCarParkingcardDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static CarParkingcardExtended populateCarParkingcard(ResultSet rs) throws SQLException {
        Date parkingcardExpiration = rs.getDate("parkingcard_expiration");
        return new CarParkingcardExtended(
            rs.getString("parkingcard_city"),
            parkingcardExpiration == null ? null : parkingcardExpiration.toLocalDate(),
            rs.getString("parkingcard_zones"),
            rs.getString("parkingcard_contract_id"),
            rs.getString("car_name"),
            rs.getInt("parkingcard_id"),
            rs.getString("details_car_license_plate")
        );
    }

	// public CarParkingcardExtended createCarParkingcard(String name, Date expiration, CarParkingcardType type, String contractNr, Car car) throws DataAccessException;
	// public void updateCarParkingcard(CarParkingcardExtended parkingcard) throws DataAccessException;
	// public void deleteCarParkingcard(CarParkingcardExtended parkingcard) throws DataAccessException;
	
	@Override
    public Page<CarParkingcardExtended> getAllCarParkingcards(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
		StringBuilder builder = new StringBuilder(ASSISTANCE_QUERY);
        if (Integer.parseInt(filter.getValue(FilterField.CAR_ID)) >= 0) {
        	builder.append("WHERE parkingcard_id = " + filter.getValue(FilterField.CAR_ID));
        }
        // add order
        switch (orderBy) {
            case CAR_NAME:
                builder.append(" ORDER BY car_name ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case CITY:
                builder.append(" ORDER BY parkingcard_city ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case DATE:
                builder.append(" ORDER BY parkingcard_expiration ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case CONTRACT_ID:
                builder.append(" ORDER BY parkingcard_contract_id ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case ZONES:
                builder.append(" ORDER BY parkingcard_zones ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            case LICENSE_PLATE:
                builder.append(" ORDER BY details_car_license_plate ");
                builder.append(asc ? "ASC" : "DESC");
                break;
            default:
                builder.append(" ORDER BY car_id ");
                builder.append("DESC");
                break;                
        }
        builder.append(" LIMIT ?,?");

        try (PreparedStatement ps = prepareStatement(builder.toString())) {
            ps.setInt(1, (page - 1) * pageSize);
            ps.setInt(2, pageSize);
            return toPage(ps, pageSize, JDBCCarParkingcardDAO::populateCarParkingcard);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get carparkingcard", ex);
        }
    }

	// public void deleteAllCarParkingcards(Car car) throws DataAccessException;
}
