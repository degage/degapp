package be.ugent.degage.db.dao;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.models.File;
import be.ugent.degage.db.models.FileGroup;

/**
 * Created by Cedric on 4/11/2014.
 */
public interface FileDAO {
    public File getFile(int id) throws DataAccessException;
    public FileGroup getFiles(int fileGroup) throws DataAccessException;
    public File createFile(String path, String fileName, String contentType, int fileGroup) throws DataAccessException;
    public File createFile(String path, String fileName, String contentType) throws DataAccessException;
    public FileGroup createFileGroup() throws DataAccessException;
    public void deleteFile(int fileId) throws DataAccessException;
}
