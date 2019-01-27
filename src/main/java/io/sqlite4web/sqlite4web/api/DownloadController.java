package io.sqlite4web.sqlite4web.api;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@CrossOrigin(origins = "*")
@RestController
public class DownloadController {

    /**
     * Returns a ResponseEntity containing the appropriate database file as attachment
     *
     * @param dbToken The database file's token
     * @return A 200 (OK) status code with the database file as attachment
     */
    @RequestMapping(value = "/api/download")
    public ResponseEntity<byte[]> returnDbFile(@RequestParam String dbToken) {
        File f;
        FileInputStream fis;
        byte[] fileBytes;

        try {
            dbToken = dbToken.replace("dbToken=", "");

            f = new File(Constants.BASE_DIR + File.separator + dbToken);
            fis = new FileInputStream(f);
            fileBytes = new byte[(int) f.length()];

            IOUtils.readFully(fis, fileBytes);
        } catch (IOException ioException) {
            return ResponseEntity.badRequest().body("No database with this token found".getBytes());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbToken + "\"")
                .body(fileBytes);
    }
}
