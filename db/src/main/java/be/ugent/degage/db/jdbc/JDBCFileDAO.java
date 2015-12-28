/* JDBCFileDAO.java
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
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.models.File;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

class JDBCFileDAO extends AbstractDAO implements FileDAO {

    public JDBCFileDAO(JDBCDataAccessContext context) {
        super(context);
    }


    public static File populateFile(ResultSet rs) throws SQLException {
        return new File(rs.getInt("file_id"), rs.getString("file_path"), rs.getString("file_name"), rs.getString("file_content_type"));
    }


    @Override
    public File getFile(int id) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT file_id, file_path, file_name, file_content_type FROM files WHERE file_id = ?"
        )) {
            ps.setInt(1, id);
            return toSingleObject(ps, JDBCFileDAO::populateFile);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get file from db.", ex);
        }
    }

    @Override
    public File createFile(String path, String fileName, String contentType) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO files(file_path, file_name, file_content_type) VALUES(?,?,?)",
                "file_id"
        )) {
            ps.setString(1, path);
            ps.setString(2, fileName);
            ps.setString(3, contentType);

            if (ps.executeUpdate() != 1)
                throw new DataAccessException("New file record failed. No rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next())
                    throw new DataAccessException("Failed to read keys for new file record.");
                return new File(keys.getInt(1), path, fileName, contentType);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create file in be.ugent.degage.database.", ex);
        }
    }

    @Override
    public void deleteFile(int fileId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement("DELETE FROM files WHERE file_id = ?")) {
            ps.setInt(1, fileId);
            if (ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to delete file in database. 0 rows affected.");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete file.", ex);
        }
    }

    private static final Map<UserFileType, String> TABLE_NAMES = new EnumMap<>(UserFileType.class);

    static {
        TABLE_NAMES.put(UserFileType.ID, "idfiles");
        TABLE_NAMES.put(UserFileType.LICENSE, "licensefiles");
    }

    private String getTableName(UserFileType uft) {
        return TABLE_NAMES.get(uft);
    }

    @Override
    public void deleteUserFile(int userId, int fileId, UserFileType uft) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "DELETE FROM " + getTableName(uft) + "  WHERE user_id = ? AND file_id = ?"
        )) {
            ps.setInt(1, userId);
            ps.setInt(2, fileId);
            if (ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to delete file in database. 0 rows affected.");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete file.", ex);
        }
    }

    @Override
    public Iterable<File> getUserFiles(int userId, UserFileType uft) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT file_id, file_path, file_name, file_content_type " +
                        "FROM files JOIN " + getTableName(uft) + " USING (file_id) " +
                        "WHERE user_id = ?"

        )) {
            ps.setInt(1, userId);
            return toList(ps, JDBCFileDAO::populateFile);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get id files", ex);
        }
    }

    @Override
    public File getUserFile(int userId, int fileId, UserFileType uft) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT file_id, file_path, file_name, file_content_type " +
                        "FROM files JOIN " + getTableName(uft) + " USING (file_id) " +
                        "WHERE user_id = ? AND file_id = ?"

        )) {
            ps.setInt(1, userId);
            ps.setInt(2, fileId);
            return toSingleObject(ps, JDBCFileDAO::populateFile);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get id files", ex);
        }
    }

    @Override
    public void addUserFile(int userId, int fileId, UserFileType uft) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "INSERT INTO " + getTableName(uft) + "(user_id,file_id) VALUES (?,?)"

        )) {
            ps.setInt(1, userId);
            ps.setInt(2, fileId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to add id file", ex);
        }
    }

    @Override
    public boolean hasUserFile(int userId, UserFileType uft) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT 1 FROM " + getTableName(uft) + " WHERE user_id = ? LIMIT 1"
        )) {
            ps.setInt(1, userId);
            return isNonEmpty(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find license files", ex);
        }
    }

    private LazyStatement getDamageFilesStatement = new LazyStatement(
            "SELECT file_id, file_path, file_name, file_content_type " +
                    "FROM files JOIN damagefiles USING (file_id) " +
                    "WHERE damagefiles.damage_id = ?"
    );

    @Override
    public Iterable<File> getDamageFiles(int damageId) throws DataAccessException {
        try {
            PreparedStatement ps = getDamageFilesStatement.value();
            ps.setInt(1, damageId);
            return toList(ps, JDBCFileDAO::populateFile);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get damage files", ex);
        }
    }

    private LazyStatement addDamageFileStatement = new LazyStatement(
            "INSERT INTO damagefiles(damage_id,file_id) VALUES (?,?)"
    );

    @Override
    public void addDamageFile(int damageId, int fileId) throws DataAccessException {
        try {
            PreparedStatement ps = addDamageFileStatement.value();
            ps.setInt(1, damageId);
            ps.setInt(2, fileId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to add damage file", ex);
        }
    }


}
