/* FileHelper.java
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
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package controllers.util;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.FileDAO;
import com.google.common.collect.Sets;
import db.DataAccess;
import play.Logger;
import play.api.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by Cedric on 4/11/2014.
 */
public class FileHelper {

    //Source: http://www.cs.helsinki.fi/u/hahonen/uusmedia/sisalto/cgi_perl_ssi/mime.html

    public static final Collection<String> IMAGE_CONTENT_TYPES
            = Sets.newHashSet("image/gif", "image/jpeg", "image/png", "image/tiff");
    public static final Collection<String> DOCUMENT_CONTENT_TYPES
            = Sets.newHashSet("text/plain", "application/pdf", "application/x-zip-compressed", "application/x-rar-compressed", "application/octet-stream");

    static {
        DOCUMENT_CONTENT_TYPES.addAll(IMAGE_CONTENT_TYPES);
    }

    private static String UPLOAD_FOLDER;

    // Initialize path to save uploads  to
    static {
        String property = ConfigurationHelper.getConfigurationString("uploads.path");
        if (property.startsWith("./")) {
            UPLOAD_FOLDER = Paths.get(Play.current().path().getAbsolutePath(), property.substring(2)).toString(); // Get relative path to Play
        } else {
            UPLOAD_FOLDER = property;
        }
    }

    private static Path createPath(String fileName, String subfolder) {
        return Paths.get(subfolder, UUID.randomUUID() + "-" + fileName.replace("/", "-").replace("\\", "-"));
    }

    public static Path saveFile(Http.MultipartFormData.FilePart filePart, String subfolder) throws IOException {
        Path path = createPath(filePart.getFilename(), subfolder);
        Path absolutePath = Paths.get(UPLOAD_FOLDER).resolve(path.toString());

        // Create subdirectories if not exist
        Files.createDirectories(absolutePath.getParent());

        // Move upload data to our upload folder
        Files.move(filePart.getFile().toPath(), absolutePath);

        Logger.debug("File (" + filePart.getContentType() + ") upload to " + path);
        return path;
    }

    public static Path saveResizedImage(Http.MultipartFormData.FilePart filePart, String subfolder, int maxWidth) throws IOException {
        Path path = createPath(filePart.getFilename(), subfolder);
        Path absolutePath = Paths.get(UPLOAD_FOLDER).resolve(path.toString());

        // Create subdirectories if not exist
        Files.createDirectories(absolutePath.getParent());

        // Copy or move upload data to our upload folder
        createResizedJpeg(filePart.getFile(), absolutePath, maxWidth);

        Logger.debug("File (%s - resized maxw=%d) uploaded to %s%n", filePart.getContentType(), maxWidth, path);
        return path;
    }

    public static Result getFileStreamResult(FileDAO dao, int fileId) {
        be.ugent.degage.db.models.File file = dao.getFile(fileId);
        if (file != null) {
            try {
                FileInputStream is = new FileInputStream(Paths.get(UPLOAD_FOLDER, file.getPath()).toFile()); //TODO: this cannot be sent with a Try-with-resources (stream already closed), check if Play disposes properly
                return file.getContentType() != null && !file.getContentType().isEmpty() ? Controller.ok(is).as(file.getContentType()) : Controller.ok(is);
            } catch (FileNotFoundException e) {
                Logger.error("Missing file: " + file.getPath());
                return Controller.notFound();
            }
        } else {
            return Controller.notFound();
        }
    }

    public static boolean isDocumentContentType(String contentType) {
        return DOCUMENT_CONTENT_TYPES.contains(contentType);
    }

    /**
     * Returns a file in the public directory
     *
     * @param path
     * @return
     */
    public static Result getPublicFile(String path, String contentType) {
        return (Controller.ok(FileHelper.class.getResourceAsStream("/public/" + path)).as(contentType));
    }

    /**
     * Deletes a file relative to the upload path
     *
     * @param path The file path to delete
     * @returns Whether the delete operation was successfull
     */
    public static void deleteFile(Path path) {
        try {
            Files.delete(Paths.get(UPLOAD_FOLDER).resolve(path));
        } catch (IOException ex) {
            // ignore
        }
    }

    private static void createResizedJpeg(File sourceFile, Path dest, int maxWidth) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(sourceFile)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            while (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis, false); // We want to enforce a specific reader to keep the format
                    if (reader.getNumImages(true) > 1) {
                        throw new RuntimeException("Multi-image containers are disabled.");
                    }

                    BufferedImage sourceImage = reader.read(0); //no support for multi-image files??
                    Image thumbnail = sourceImage.getScaledInstance(maxWidth, -1, Image.SCALE_SMOOTH);
                    BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null), thumbnail.getHeight(null), BufferedImage.TYPE_INT_RGB);
                    bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
                    ImageIO.write(bufferedThumbnail, reader.getFormatName(), Files.newOutputStream(dest));
                    return;
                } catch (IIOException ex) {
                    // do nothing ???
                }
            }
            throw new RuntimeException("No image reader installed for given format.");
        }
    }

    /**
     * Returns a handle to the named file in the current http request. (Should only be used with an injected context.)
     *
     * @return null if no file was present. If the contenttype was invalid, returns a File-object
     * with null content type and negative id.
     */
    public static be.ugent.degage.db.models.File getFileFromRequest(String fieldName, Collection<String> contentTypes, String fileType, int width) {
        return getFileFromFilePart(
                Controller.request().body().asMultipartFormData().getFile(fieldName),
                contentTypes, fileType, width);
    }

    public static be.ugent.degage.db.models.File getFileFromFilePart(
            Http.MultipartFormData.FilePart f, Collection<String> contentTypes, String fileType, int width) {
        if (f == null) {
            return null;
        } else if (!contentTypes.contains(f.getContentType())) {
            return new be.ugent.degage.db.models.File(-1, null, null, null);
        } else {
            // TODO: make general method for file handling. There is too much cut and paste
            try {
                Path relativePath =
                        width <= 0 ? saveFile(f, ConfigurationHelper.getConfigurationString(fileType))
                                : saveResizedImage(f, ConfigurationHelper.getConfigurationString(fileType), width);
                DataAccessContext context = DataAccess.getInjectedContext();
                FileDAO fdao = context.getFileDAO();
                try {
                    return fdao.createFile(relativePath.toString(), f.getFilename(), f.getContentType());
                } catch (DataAccessException ex) {
                    FileHelper.deleteFile(relativePath);
                    throw ex;
                }
            } catch (IOException ex) {
                throw new DataAccessException("File system I/O error", ex);
            }
        }
    }

    /**
     * Delete a file from the database and from the upload area. (Should only be used with an injected context.)
     * @param id Id of the file to be deleted. If negative or zero, nothingis deleted.
     */
    public static void deleteOldFile (int id) {
        if (id > 0) {
            FileDAO fileDAO = DataAccess.getInjectedContext().getFileDAO();
            FileHelper.deleteFile(
                    Paths.get(fileDAO.getFile(id).getPath())
            );
            fileDAO.deleteFile(id);
        }
    }

}

