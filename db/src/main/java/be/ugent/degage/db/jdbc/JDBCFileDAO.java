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
import java.util.ArrayList;
import java.util.Collection;

class JDBCFileDAO extends AbstractDAO implements FileDAO {

    public JDBCFileDAO(JDBCDataAccessContext context) {
        super(context);
    }


    public static File populateFile(ResultSet rs) throws SQLException {
        return new File(rs.getInt("file_id"), rs.getString("file_path"), rs.getString("file_name"), rs.getString("file_content_type"));
    }

    public static File populateFile(ResultSet rs, String tableName) throws SQLException {
        return new File(rs.getInt(tableName + ".file_id"), rs.getString(tableName + ".file_path"), rs.getString(tableName + ".file_name"), rs.getString(tableName + ".file_content_type"));
    }

    private LazyStatement getFileStatement = new LazyStatement(
            "SELECT file_id, file_path, file_name, file_content_type FROM files WHERE file_id = ?"
    );

    @Override
    public File getFile(int id) throws DataAccessException {
        try {
            PreparedStatement ps = getFileStatement.value();
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                else
                    return populateFile(rs);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get file from db.", ex);
        }
    }

    // used with queries that return a list of files
    private Iterable<File> getFiles(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            Collection<File> files = new ArrayList<>();
            while (rs.next()) {
                files.add(populateFile(rs));
            }
            return files;
        }
    }

    private LazyStatement createFileStatement = new LazyStatement(
            "INSERT INTO files(file_path, file_name, file_content_type) VALUES(?,?,?)",
            "file_id"
    );

    @Override
    public File createFile(String path, String fileName, String contentType) throws DataAccessException {
        try {
            PreparedStatement ps = createFileStatement.value();
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

    private LazyStatement deleteFileStatement = new LazyStatement(
             "DELETE FROM files WHERE file_id = ?"
        );

        @Override
        public void deleteFile(int fileId) throws DataAccessException {
            try {
                PreparedStatement ps = deleteFileStatement.value();
                ps.setInt(1, fileId);
                if (ps.executeUpdate() != 1)
                    throw new DataAccessException("Failed to delete file in database. 0 rows affected.");
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to delete file.", ex);
            }
        }    private LazyStatement deleteIdFileStatement = new LazyStatement(
         "DELETE FROM idfiles WHERE user_id = ? AND file_id = ?"
    );

    @Override
    public void deleteIdFile(int userId, int fileId) throws DataAccessException {
        try {
            PreparedStatement ps = deleteIdFileStatement.value();
            ps.setInt(1, userId);
            ps.setInt(2, fileId);
            if (ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to delete file in database. 0 rows affected.");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete file.", ex);
        }
    }

    private LazyStatement deleteLicenseFileStatement = new LazyStatement(
         "DELETE FROM licensefiles WHERE user_id = ? AND  file_id = ?"
    );

    @Override
    public void deleteLicenseFile(int userId, int fileId) throws DataAccessException {
        try {
            PreparedStatement ps = deleteLicenseFileStatement.value();
            ps.setInt(1, userId);
            ps.setInt(2, fileId);
            if (ps.executeUpdate() != 1)
                throw new DataAccessException("Failed to delete file in database. 0 rows affected.");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete file.", ex);
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
            return getFiles(ps);
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
            ps.setInt (1, damageId);
            ps.setInt (2, fileId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to add damage file", ex);
        }
    }

    private LazyStatement getIdFilesStatement = new LazyStatement(
            "SELECT file_id, file_path, file_name, file_content_type " +
                    "FROM files JOIN idfiles USING (file_id) " +
                    "WHERE idfiles.user_id = ?"
    );

    @Override
    public Iterable<File> getIdFiles(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getIdFilesStatement.value();
            ps.setInt(1, userId);
            return getFiles(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get id files", ex);
        }
    }

    private LazyStatement addIdFileStatement = new LazyStatement(
            "INSERT INTO idfiles(user_id,file_id) VALUES (?,?)"
    );

    @Override
    public void addIdFile(int userId, int fileId) throws DataAccessException {
        try {
            PreparedStatement ps = addIdFileStatement.value();
            ps.setInt (1, userId);
            ps.setInt (2, fileId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to add id file", ex);
        }
    }

    private LazyStatement getLicenseFilesStatement = new LazyStatement(
            "SELECT file_id, file_path, file_name, file_content_type " +
                    "FROM files JOIN licensefiles USING(file_id) " +
                    "WHERE licensefiles.user_id = ?"
    );

    @Override
    public Iterable<File> getLicenseFiles(int userId) throws DataAccessException {
        try {
            PreparedStatement ps = getLicenseFilesStatement.value();
            ps.setInt(1, userId);
            return getFiles(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get license files", ex);
        }
    }

    @Override
    public boolean hasLicenseFile(int userId) throws DataAccessException {
        try (PreparedStatement ps = prepareStatement(
                "SELECT 1 FROM licensefiles WHERE user_id = ? LIMIT 1"
        )) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find license files", ex);
        }
    }

    private LazyStatement addLicenseFileStatement = new LazyStatement(
            "INSERT INTO licensefiles(user_id,file_id) VALUES (?,?)"
    );

    @Override
    public void addLicenseFile(int userId, int fileId) throws DataAccessException {
        try {
            PreparedStatement ps = addLicenseFileStatement.value();
            ps.setInt (1, userId);
            ps.setInt (2, fileId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to add license file", ex);
        }
    }
}
