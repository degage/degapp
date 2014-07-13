package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.File;

/**
 * DAO for handling file(path)s
 */
public interface FileDAO {

    /**
     * Return the file with given id, or null if it does not exist
     */
    public File getFile(int id) throws DataAccessException;

    /**
     * Create a new file group number.
     */
    public int createFileGroupNumber() throws DataAccessException;

    /**
     * Return all files in a given file group.
     */
    public Iterable<File> getFiles(int fileGroupNumber) throws DataAccessException;

    /**
     * Create a file in the given file group (which can be null)
     */
    public File createFile(String path, String fileName, String contentType, Integer fileGroupNumber) throws DataAccessException;

    /**
     * Delete a file
     */
    public void deleteFile(int fileId) throws DataAccessException;

    /**
     * Return the image files for a damage case
     */
    public Iterable<File> getDamageFiles(int damageId) throws DataAccessException;

    /**
     * Add an image file to a damage case
     */
    public void addDamageFile(int damageId, int fileId) throws DataAccessException;

    /**
     * Return the image files for an identity card
     */
    public Iterable<File> getIdFiles(int userId) throws DataAccessException;

    /**
     * Add an image file for an identity card
     */
    public void addIdFile(int userId, int fileId) throws DataAccessException;

    /**
     * Return the image files for a drivers license
     */
    public Iterable<File> getLicenseFiles(int userId) throws DataAccessException;

    /**
     * Add an image file for a drivers license
     */
    public void addLicenseFile(int userId, int fileId) throws DataAccessException;
}
