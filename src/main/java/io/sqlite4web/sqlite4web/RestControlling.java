package io.sqlite4web.sqlite4web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.*;
import java.net.ConnectException;
import java.nio.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Random;

@CrossOrigin(origins = "*")
@RestController
public class RestControlling {
    final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890".toCharArray();
    private Random random = new Random();

    private Connection mConnection;
    private String mTableName;



    /**
     * POST /uploadFile -> receive and locally save a file, return JSON
     *
     * @param file The uploaded file as Multipart file parameter in the
     * HTTP request. The RequestParam name must be the same of the attribute
     * "name" in the input tag with type file.
     *
     * @return The JSON parsed from the database file
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadFile (
            @RequestParam("file") MultipartFile file) {
        System.out.println(file.getName());
        System.out.println(file.getOriginalFilename());
        try {
            System.out.println(file.getBytes().length);

            byte[] tokenBytes = new byte[8];
            String dbToken = DatatypeConverter.printHexBinary(tokenBytes);
            System.out.println("New Token: '" + dbToken + "'");

            String path = System.getProperty("user.home") + File.separator + "SpringProjects" + File.separator + "databases" + File.separator + dbToken + file.getOriginalFilename();

            System.out.println(path);
            File f = new File("New path: '" + path + "'");
            f.createNewFile();

            BufferedOutputStream bufferedOut = new BufferedOutputStream(new FileOutputStream(f));
            bufferedOut.write(file.getBytes());
            bufferedOut.flush();
            bufferedOut.close();
            System.out.println("Finished saving the file");

            System.out.println(constructJSON(path).toString(4));
            return constructJSON(path).toString();

        } catch (IOException e) {
            e.printStackTrace();
        }


        JSONObject object = new JSONObject();
        object.put("key1", "Schlüssel 1");
        object.put("Greeting", "Formal Greeting with friendly handshake");
        object.put("Fehler", "Oh je; da ist wohl was schief gelaufen. Dieses JSON schickt die API immer wenn was nich so lief :(");

        return object.toString();
    }

    @RequestMapping(value = "/update/cell", method = RequestMethod.PATCH)
    public ResponseEntity updateCell() {
        return new ResponseEntity(HttpStatus.BAD_GATEWAY);
    }


    /**
     * {
     *     "metadata": {
     *         "columnNames": [
     *              "cname1",
     *              "cname2"
     *         ],
     *         "primaryKey" : "key_value"
     *     },
     *     "data": [
     *         [
     *              "column1value",
     *              "column2value"
     *         ],
     *         [
     *              "column1value2",
     *              "column2value2"
     *         ]
     *     ]
     * }
     *
     *
     * @param path The DB file's path
     * @return The JSON containing metadata about and all data of the database file.
     */
    private JSONObject constructJSON(String path) {
        JSONObject container = new JSONObject();
        JSONObject metaData = new JSONObject();
        JSONArray columnNames = new JSONArray();
        JSONArray table = new JSONArray();

        try {
            mConnection = DriverManager.getConnection("jdbc:sqlite:" + path);

            ResultSet rs = mConnection.createStatement().executeQuery("SELECT * FROM " + getDatabaseTableName());

            DatabaseMetaData dbmd = mConnection.getMetaData();
            ResultSetMetaData md = rs.getMetaData();

            // Construct meta data JSONArray
            System.out.println("Listing meta data - column names...");
            for (int i = 1; i <= md.getColumnCount(); i++) {
                System.out.println("(" + i + ") " + md.getColumnName(i));
                //metaData.put(md.getColumnName(i));
                columnNames.put(md.getColumnName(i));
            }
            System.out.println("Done listing meta data - column names.");

            JSONArray row;
            while (rs.next()) {
                row = new JSONArray();
                System.out.println(rs.getString(1));

                for (int i = 1; i <= md.getColumnCount(); i++) {

                    if (md.getColumnType(i) == java.sql.Types.ARRAY) {
                        row.put(rs.getArray(i));
                    } else if (md.getColumnType(i) == java.sql.Types.BIGINT) {
                        row.put(rs.getInt(i));
                    } else if (md.getColumnType(i) == java.sql.Types.BOOLEAN) {
                        row.put(rs.getBoolean(i));
                    } else if (md.getColumnType(i) == java.sql.Types.BLOB) {
                        row.put(rs.getBlob(i));
                    } else if (md.getColumnType(i) == java.sql.Types.DOUBLE) {
                        row.put(rs.getDouble(i));
                    } else if (md.getColumnType(i) == java.sql.Types.FLOAT) {
                        row.put(rs.getFloat(i));
                    } else if (md.getColumnType(i) == java.sql.Types.INTEGER) {
                        row.put(rs.getInt(i));
                    } else if (md.getColumnType(i) == java.sql.Types.NVARCHAR) {
                        row.put(rs.getNString(i));
                    } else if (md.getColumnType(i) == java.sql.Types.VARCHAR) {
                        row.put(rs.getString(i));
                    } else if (md.getColumnType(i) == java.sql.Types.TINYINT) {
                        row.put(rs.getInt(i));
                    } else if (md.getColumnType(i) == java.sql.Types.SMALLINT) {
                        row.put(rs.getInt(i));
                    } else if (md.getColumnType(i) == java.sql.Types.DATE) {
                        row.put(rs.getDate(i));
                    } else if (md.getColumnType(i) == java.sql.Types.TIMESTAMP) {
                        row.put(rs.getTimestamp(i));
                    } else {
                        row.put(rs.getObject(i));
                    }
                }
                table.put(row);
            }

            ResultSet primaryKeys = dbmd.getPrimaryKeys(null, null, getDatabaseTableName());
            String primaryKey = "null";
            if (primaryKeys != null && primaryKeys.next()) primaryKey = primaryKeys.getString("COLUMN_NAME");

            metaData.put("columnNames", columnNames);
            metaData.put("primaryKey", primaryKey);

            container.put("metadata", metaData);
            container.put("data", table);

            return container;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    // Order:
    // 1. Primary key
    // 2. Complete row
    // 3. Take the n'th row the client chose (later version, not yet)
    @RequestMapping(value = "/update/cell", method = RequestMethod.POST)
    public ResponseEntity updateCell(@RequestParam String primaryKey, @RequestBody JSONObject jsonBody) {
        System.out.println("Primary key: '" + primaryKey + "'");
        if (primaryKey != null) {
            //mConnection.createStatement().execute("INSERT INTO " + getDatabaseTableName())
        }

        return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
    }



    private static JSONArray convert(ResultSet rs) throws SQLException, JSONException {
        JSONArray array = new JSONArray();
        JSONArray columnNames = new JSONArray();
        ResultSetMetaData md = rs.getMetaData();

        for (int i = 0; i < md.getColumnCount(); i++) {
            columnNames.put(md.getColumnName(i));
        }
        return array;
    }

    public String getDatabaseTableName() {
        try {
            DatabaseMetaData databaseMetaData = mConnection.getMetaData();

            DatabaseMetaData md = mConnection.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            System.out.println("Tablename: '" + rs.getString(3) + "'");
            return rs.getString(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String generateRandomString() {
        String randomString = "";

        for (int i = 0; i < 16; i++) {
            randomString = randomString + chars[random.nextInt(chars.length)];
        }

        return randomString;
    }
}
