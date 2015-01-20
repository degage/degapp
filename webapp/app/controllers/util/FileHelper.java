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
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
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
import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Cedric on 4/11/2014.
 */
public class FileHelper {

    private static final boolean MOVE_INSTEAD_OF_COPY = true;

    //Source: http://www.cs.helsinki.fi/u/hahonen/uusmedia/sisalto/cgi_perl_ssi/mime.html

    public static final Collection<String> IMAGE_CONTENT_TYPES
            = Sets.newHashSet("image/gif", "image/jpeg", "image/png", "image/tiff");
    public static final Collection<String> DOCUMENT_CONTENT_TYPES
            = Sets.newHashSet("text/plain", "application/pdf", "application/x-zip-compressed", "application/x-rar-compressed", "application/octet-stream");
    static {
        DOCUMENT_CONTENT_TYPES.addAll(IMAGE_CONTENT_TYPES);
    }

    private static String uploadFolder;
    private static String generatedFolder;

    // Initialize path to save uploads / generated files to
    static {
        String property = ConfigurationHelper.getConfigurationString("uploads.path");
        if (property.startsWith("./")) {
            uploadFolder = Paths.get(Play.current().path().getAbsolutePath(), property.substring(2)).toString(); // Get relative path to Play
        } else uploadFolder = property;

        property = ConfigurationHelper.getConfigurationString("generated.path");
        if (property.startsWith("./")) {
            generatedFolder = Paths.get(Play.current().path().getAbsolutePath(), property.substring(2)).toString(); // Get relative path to Play
        } else generatedFolder = property;
    }

    private static Path createPath(String fileName, String subfolder) {
        if (fileName.contains("/") || fileName.contains("\\"))
            throw new RuntimeException("Filename contains slashes.");

        String uuid = UUID.randomUUID().toString();
        String newFileName = uuid + "-" + fileName;

        return Paths.get(subfolder, newFileName);
    }

    public static String getGeneratedFilesPath(String filename, String subfolder) throws IOException {
        Path path = createPath(filename, subfolder);
        Path absolutePath = Paths.get(generatedFolder).resolve(path.toString());

        Files.createDirectories(absolutePath.getParent());

        return absolutePath.toString();
    }

    public static Path saveFile(Http.MultipartFormData.FilePart filePart, String subfolder) throws IOException {
        Path path = createPath(filePart.getFilename(), subfolder);
        Path absolutePath = Paths.get(uploadFolder).resolve(path.toString());

        // Create subdirectories if not exist
        Files.createDirectories(absolutePath.getParent());

        // Copy or move upload data to our upload folder
        File file = filePart.getFile();
        File toFile = absolutePath.toFile();
        if (MOVE_INSTEAD_OF_COPY)
            moveFile(file, toFile);
        else
            copyFile(file, toFile);

        Logger.debug("File (" + filePart.getContentType() + ") upload to " + path);
        return path;
    }

    public static Path saveResizedImage(Http.MultipartFormData.FilePart filePart, String subfolder, int maxWidth) throws IOException {
        Path path = createPath(filePart.getFilename(), subfolder);
        Path absolutePath = Paths.get(uploadFolder).resolve(path.toString());

        // Create subdirectories if not exist
        Files.createDirectories(absolutePath.getParent());

        // Copy or move upload data to our upload folder
        File file = filePart.getFile();
        File toFile = absolutePath.toFile();
        createResizedJpeg(file, toFile, maxWidth);

        Logger.debug("File (%s - resized maxw=%d) uploaded to %s%n", filePart.getContentType(), maxWidth, path);
        return path;
    }

    public static Result getFileStreamResult(FileDAO dao, int fileId) {
        be.ugent.degage.db.models.File file = dao.getFile(fileId);
        if (file != null) {
            try {
                FileInputStream is = new FileInputStream(Paths.get(uploadFolder, file.getPath()).toFile()); //TODO: this cannot be sent with a Try-with-resources (stream already closed), check if Play disposes properly
                return file.getContentType() != null && !file.getContentType().isEmpty() ? Controller.ok(is).as(file.getContentType()) : Controller.ok(is);
            } catch (FileNotFoundException e) {
                Logger.error("Missing file: " + file.getPath());
                return Controller.notFound();
            }
        } else return Controller.notFound();
    }

    public static boolean isImageContentType(String contentType) {
        return IMAGE_CONTENT_TYPES.contains(contentType);
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
        String playPath = Play.current().path().getAbsolutePath();
        try {
            FileInputStream is;
            if (Play.isProd(Play.current())) {
                playPath = Paths.get(playPath, ConfigurationHelper.getConfigurationString("application.classpath")).toString();
                is = new FileInputStream(Paths.get(playPath, "public", path).toFile());
            } else {
                is = new FileInputStream(Paths.get(playPath, "public", path).toFile());
            }
            return Controller.ok(is).as(contentType);
        } catch (FileNotFoundException e) {
            return Controller.notFound();
        }
    }

    /**
     * Deletes a file relative to the upload path
     *
     * @param path The file path to delete
     * @returns Whether the delete operation was successfull
     */
    public static boolean deleteFile(Path path) {
        try {
            Path absPath = Paths.get(uploadFolder).resolve(path);
            Files.delete(absPath);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static void moveFile(File sourceFile, File destFile) throws IOException {
        Files.move(java.nio.file.Paths.get(sourceFile.getAbsolutePath()), java.nio.file.Paths.get(destFile.getAbsolutePath()));
    }

    private static void createResizedJpeg(File sourceFile, File destFile, int maxWidth) throws IOException {
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
                    ImageIO.write(bufferedThumbnail, reader.getFormatName(), new FileOutputStream(destFile, false));
                    return;
                } catch (IIOException ex) {
                    // do nothing ???
                }
            }
            throw new RuntimeException("No image reader installed for given format.");
        }
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile(); // TODO: check return value?
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    /**
     * Returns a handle to the named file in the current http request. (Should only be used with an injected context.)
     * @return null if no file was present. If the contenttype was invalid, returns a File-object
     * with null content type and negative id.
     */
    public static be.ugent.degage.db.models.File getFileFromRequest (String fieldName, Collection<String> contentTypes, String fileType) {
        return getFileFromFilePart(
                Controller.request().body().asMultipartFormData().getFile(fieldName),
                contentTypes, fileType);
    }

    public static be.ugent.degage.db.models.File getFileFromFilePart(Http.MultipartFormData.FilePart f, Collection<String> contentTypes, String fileType) {
        if (f == null) {
            return null;
        } else if (!contentTypes.contains(f.getContentType())) {
            return new be.ugent.degage.db.models.File (-1, null, null, null);
        } else {
            // TODO: make general method for file handling. There is too much cut and paste
            try {
                Path relativePath = saveFile(f, ConfigurationHelper.getConfigurationString(fileType));
                DataAccessContext context = DataAccess.getInjectedContext();
                FileDAO fdao = context.getFileDAO();
                try {
                    be.ugent.degage.db.models.File file = fdao.createFile(relativePath.toString(), f.getFilename(), f.getContentType());
                    return file;
                } catch (DataAccessException ex) {
                    FileHelper.deleteFile(relativePath);
                    throw ex;
                }
            }  catch (IOException ex) {
                throw new DataAccessException("File system I/O error", ex);
            }
        }
    }

}

