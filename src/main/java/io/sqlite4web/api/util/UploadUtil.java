package io.sqlite4web.api.util;

import io.sqlite4web.api.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UploadUtil {

    /**
     * Adds the original file's file extension to the token and returns it.
     * The dbToken is the name of the file in the server's file system and
     * is also the id to access it via the Web and API.
     *
     * @param filename The original file's extension
     * @param dbToken The new file's token
     * @return The dbToken, extended by the original file's extension
     */
    public static String addFileExtension(String filename, String dbToken) {
        // Add the database's file extension to the token
        String fileExtension = filename.substring(filename.lastIndexOf("."));
        dbToken += fileExtension;
        return dbToken;
    }


    /**
     * Saves the newly uploaded database file to the file system.
     *
     * @param filename The original filename
     * @param fileBytes The database file's bytes
     */
    public static boolean saveDbFile(String filename, byte[] fileBytes) {
        String path;
        File dbFile;

        // Create new file inside the database directory with the token as its name
        path = Constants.DATABASE_DIR + File.separator + filename;
        dbFile = new File(path);

        try {
            if (!dbFile.createNewFile()) return false;

            // Save the contents to the file
            BufferedOutputStream bufferedOut = new BufferedOutputStream(new FileOutputStream(dbFile));
            bufferedOut.write(fileBytes);
            bufferedOut.flush();
            bufferedOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Finished saving the file");
        return true;
    }
}
